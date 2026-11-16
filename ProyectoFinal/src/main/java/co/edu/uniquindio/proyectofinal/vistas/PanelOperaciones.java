package co.edu.uniquindio.proyectofinal.vistas;

import co.edu.uniquindio.proyectofinal.Estructuras.Controllers.FintechController;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.Alerta;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.Billetera;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.NivelRiesgo;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.TipoTransaccion;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.Transaccion;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Panel de operaciones financieras: recargar, retirar, transferir, revertir
 * y consultar el historial y las alertas del usuario (requisitos 4.2, 4.6 y 4.7).
 */
public class PanelOperaciones extends BorderPane implements Refrescable {

    private final FintechController controlador;

    private final ComboBox<Usuario> cmbUsuario = new ComboBox<>();
    private final Label lblResumen = new Label("Seleccione un usuario.");

    private final ComboBox<Billetera> cmbOrigen = new ComboBox<>();
    private final TextField txtDestino = new TextField();
    private final TextField txtMonto = new TextField();

    private final Label lblResultado = new Label();
    private final TableView<Transaccion> tablaHistorial = new TableView<>();
    private final ListView<String> listaAlertas = new ListView<>();

    /** Evita reaccionar a los cambios que provoca refrescar(). */
    private boolean actualizando = false;

    public PanelOperaciones(FintechController controlador) {
        this.controlador = controlador;

        configurarCombos();
        construirTablaHistorial();

        setTop(panelSuperior());

        SplitPane centro = new SplitPane(panelHistorial(), panelAlertas());
        centro.setDividerPositions(0.72);
        setCenter(centro);

        refrescar();
    }

    // ------------------------------------------------------------------
    // CONSTRUCCIÓN DE LA VISTA
    // ------------------------------------------------------------------

    private void configurarCombos() {
        cmbUsuario.setConverter(new StringConverter<Usuario>() {
            @Override
            public String toString(Usuario usuario) {
                return usuario == null ? "" : usuario.getId() + " - " + usuario.getNombre();
            }

            @Override
            public Usuario fromString(String texto) {
                return null;
            }
        });

        cmbOrigen.setConverter(new StringConverter<Billetera>() {
            @Override
            public String toString(Billetera b) {
                return b == null ? "" : b.getCodigo() + " - " + b.getNombre() + " ($" + b.getSaldo() + ")";
            }

            @Override
            public Billetera fromString(String texto) {
                return null;
            }
        });

        cmbUsuario.setPrefWidth(260);
        cmbOrigen.setPrefWidth(320);
        cmbUsuario.valueProperty().addListener((obs, anterior, nuevo) -> {
            if (!actualizando) {
                cargarDatosUsuario();
            }
        });
    }

    private VBox panelSuperior() {
        Label titulo = new Label("Operaciones financieras");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        HBox filaUsuario = new HBox(10, new Label("Usuario"), cmbUsuario, lblResumen);
        filaUsuario.setStyle("-fx-alignment: center-left;");

        txtDestino.setPromptText("Solo para transferir");
        txtMonto.setPromptText("Monto entero");

        GridPane formulario = new GridPane();
        formulario.setHgap(10);
        formulario.setVgap(8);
        formulario.addRow(0, new Label("Billetera de origen"), cmbOrigen,
                new Label("Código billetera destino"), txtDestino);
        formulario.addRow(1, new Label("Monto"), txtMonto);

        Button btnRecargar = new Button("Recargar");
        Button btnRetirar = new Button("Retirar");
        Button btnTransferir = new Button("Transferir");
        Button btnRevertir = new Button("Revertir última operación");

        btnRecargar.setOnAction(e -> recargar());
        btnRetirar.setOnAction(e -> retirar());
        btnTransferir.setOnAction(e -> transferir());
        btnRevertir.setOnAction(e -> revertir());

        HBox botones = new HBox(8, btnRecargar, btnRetirar, btnTransferir, btnRevertir);

        VBox caja = new VBox(10, titulo, filaUsuario, formulario, botones, lblResultado);
        caja.setPadding(new Insets(12));
        return caja;
    }

    private VBox panelHistorial() {
        Label titulo = new Label("Historial de movimientos (más reciente primero)");
        titulo.setStyle("-fx-font-weight: bold;");

        VBox caja = new VBox(8, titulo, tablaHistorial);
        caja.setPadding(new Insets(0, 8, 8, 12));
        VBox.setVgrow(tablaHistorial, Priority.ALWAYS);
        return caja;
    }

    private VBox panelAlertas() {
        Label titulo = new Label("Alertas y notificaciones");
        titulo.setStyle("-fx-font-weight: bold;");
        listaAlertas.setPlaceholder(new Label("Sin alertas."));

        VBox caja = new VBox(8, titulo, listaAlertas);
        caja.setPadding(new Insets(0, 12, 8, 8));
        VBox.setVgrow(listaAlertas, Priority.ALWAYS);
        return caja;
    }

    private void construirTablaHistorial() {
        tablaHistorial.getColumns().add(Util.<Transaccion, String>columna("ID", Transaccion::getId));
        tablaHistorial.getColumns().add(Util.<Transaccion, String>columna("Fecha",
                t -> t.getFechaHora().format(Util.FORMATO_FECHA)));
        tablaHistorial.getColumns().add(Util.<Transaccion, TipoTransaccion>columna("Tipo", Transaccion::getTipo));
        tablaHistorial.getColumns().add(Util.<Transaccion, String>columna("Valor", t -> "$" + (long) t.getValor()));
        tablaHistorial.getColumns().add(Util.<Transaccion, String>columna("Origen", Transaccion::getBilleteraOrigen));
        tablaHistorial.getColumns().add(Util.<Transaccion, String>columna("Destino",
                t -> t.getBilleteraDestino() == null ? "—" : t.getBilleteraDestino()));
        tablaHistorial.getColumns().add(Util.<Transaccion, String>columna("Estado", Transaccion::getEstado));
        tablaHistorial.getColumns().add(Util.<Transaccion, Integer>columna("Puntos", Transaccion::getPuntosGenerados));
        tablaHistorial.getColumns().add(Util.<Transaccion, NivelRiesgo>columna("Riesgo", Transaccion::getNivelRiesgo));
        tablaHistorial.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaHistorial.setPlaceholder(new Label("Sin movimientos."));
    }

    // ------------------------------------------------------------------
    // ACCIONES
    // ------------------------------------------------------------------

    /** Lógica que ejecuta una operación sobre el controlador. */
    @FunctionalInterface
    private interface Accion {
        boolean ejecutar(Usuario usuario);
    }

    private void recargar() {
        Billetera origen = origenValido();
        Integer monto = montoValido();

        if (origen == null || monto == null) {
            return;
        }

        ejecutar("Recarga realizada.", u -> controlador.realizarRecarga(origen.getCodigo(), monto, u.getId()));
    }

    private void retirar() {
        Billetera origen = origenValido();
        Integer monto = montoValido();

        if (origen == null || monto == null) {
            return;
        }

        ejecutar("Retiro realizado.", u -> controlador.realizarRetiro(origen.getCodigo(), monto, u.getId()));
    }

    private void transferir() {
        Billetera origen = origenValido();
        Integer monto = montoValido();

        if (origen == null || monto == null) {
            return;
        }

        Integer destino = Util.entero(txtDestino.getText());

        if (destino == null) {
            resultado("Ingrese el código de la billetera destino (número entero).", false);
            return;
        }

        ejecutar("Transferencia realizada.",
                u -> controlador.realizarTransferencia(origen.getCodigo(), destino, monto, u.getId()));
    }

    private void revertir() {
        boolean ok = controlador.revertirUltimaOperacion();

        if (ok) {
            resultado("Última operación revertida: saldos, historial y puntos actualizados.", true);
        } else {
            resultado("No hay una operación que se pueda revertir (pila vacía o el destino ya no tiene fondos).", false);
        }

        recargarVista();
    }

    private void ejecutar(String mensajeExito, Accion accion) {
        Usuario usuario = cmbUsuario.getValue();

        if (usuario == null) {
            resultado("Seleccione un usuario.", false);
            return;
        }

        int alertasAntes = usuario.getHistorialAlertas().getSize();
        boolean ok = accion.ejecutar(usuario);
        String avisos = Util.alertasNuevas(usuario, alertasAntes);

        if (ok) {
            resultado(avisos.isEmpty() ? mensajeExito : mensajeExito + " Avisos: " + avisos, true);
            txtMonto.clear();
        } else {
            resultado(avisos.isEmpty()
                    ? "No se pudo realizar la operación. Revise los datos ingresados."
                    : "Operación rechazada: " + avisos, false);
        }

        recargarVista();
    }

    private Billetera origenValido() {
        Billetera origen = cmbOrigen.getValue();

        if (cmbUsuario.getValue() == null) {
            resultado("Seleccione un usuario.", false);
            return null;
        }
        if (origen == null) {
            resultado("Seleccione la billetera de origen (el usuario debe tener al menos una).", false);
            return null;
        }

        return origen;
    }

    private Integer montoValido() {
        Integer monto = Util.entero(txtMonto.getText());

        if (monto == null || monto <= 0) {
            resultado("El monto debe ser un número entero positivo.", false);
            return null;
        }

        return monto;
    }

    // ------------------------------------------------------------------
    // ACTUALIZACIÓN DE LA VISTA
    // ------------------------------------------------------------------

    @Override
    public void refrescar() {
        recargarVista();
    }

    /** Recarga el combo de usuarios conservando la selección y actualiza el detalle. */
    private void recargarVista() {
        Usuario seleccionado = cmbUsuario.getValue();
        String id = seleccionado == null ? null : seleccionado.getId();

        actualizando = true;
        cmbUsuario.setItems(Util.aLista(controlador.getAnalitica().obtenerUsuariosOrdenadosPorPuntos()));
        cmbUsuario.setValue(id == null ? null : controlador.buscarUsuario(id));
        actualizando = false;

        cargarDatosUsuario();
    }

    private void cargarDatosUsuario() {
        Usuario usuario = cmbUsuario.getValue();

        if (usuario == null) {
            lblResumen.setText("Seleccione un usuario.");
            cmbOrigen.setItems(FXCollections.<Billetera>observableArrayList());
            tablaHistorial.getItems().clear();
            listaAlertas.getItems().clear();
            return;
        }

        lblResumen.setText("Puntos: " + usuario.getPuntosAcumulados()
                + "  ·  Nivel: " + usuario.getNivel()
                + "  ·  Billeteras: " + usuario.getBilleteras().getSize());

        Billetera previa = cmbOrigen.getValue();
        int codigoPrevio = previa == null ? -1 : previa.getCodigo();

        Billetera elegida = null;
        for (Billetera b : usuario.getBilleteras()) {
            if (b.getCodigo() == codigoPrevio) {
                elegida = b;
            }
        }
        if (elegida == null && !usuario.getBilleteras().isEmpty()) {
            elegida = usuario.getBilleteras().getNodeValue(0);
        }

        cmbOrigen.setItems(Util.aLista(usuario.getBilleteras()));
        // Se limpia y se vuelve a asignar para que el texto muestre el saldo actualizado.
        cmbOrigen.setValue(null);
        cmbOrigen.setValue(elegida);

        tablaHistorial.setItems(Util.aListaInversa(usuario.getHistorialTransacciones()));

        ObservableList<String> alertas = FXCollections.observableArrayList();
        for (Alerta alerta : usuario.getHistorialAlertas()) {
            alertas.add(0, "[" + alerta.getFecha().format(Util.FORMATO_FECHA) + "] "
                    + alerta.getTipoAlerta() + ": " + alerta.getMensaje());
        }
        listaAlertas.setItems(alertas);
    }

    private void resultado(String mensaje, boolean exito) {
        lblResultado.setText(mensaje);
        lblResultado.setWrapText(true);
        lblResultado.setStyle(exito ? "-fx-text-fill: #1b7f3b;" : "-fx-text-fill: #b3261e;");
    }
}