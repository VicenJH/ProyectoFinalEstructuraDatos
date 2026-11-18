package co.edu.uniquindio.proyectofinal.Presentacion.ViewControllers;

import co.edu.uniquindio.proyectofinal.App;
import co.edu.uniquindio.proyectofinal.Estructuras.Controllers.FintechController;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.*;
import co.edu.uniquindio.proyectofinal.Presentacion.ContextoAplicacion;
import co.edu.uniquindio.proyectofinal.Presentacion.EtiquetasTabla;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class UserViewController {

    @FXML private Label lblNombreUsuario, lblIdentificacion, lblSaldoTotal, lblPuntos, lblNivel;
    @FXML private Label lblPerfilNombre, lblPerfilId, lblPerfilCorreo;
    @FXML private TableView<Billetera> tablaBilleteras;
    @FXML private TableView<Transaccion> tablaTransacciones;
    @FXML private TableView<OperacionProgramada> tablaProgramadas;
    @FXML private TableView<Beneficio> tablaBeneficios;
    @FXML private TableView<CanjeBeneficio> tablaCanjes;
    @FXML private TableView<Alerta> tablaAlertas;
    @FXML private TabPane tabsUsuario;
    @FXML private TableColumn<Billetera, Number> colWalletCodigo, colWalletSaldo;
    @FXML private TableColumn<Billetera, String> colWalletNombre, colWalletTipo, colWalletEstado;
    @FXML private TableColumn<Transaccion, LocalDateTime> colTxFecha;
    @FXML private TableColumn<Transaccion, TipoTransaccion> colTxTipo;
    @FXML private TableColumn<Transaccion, Number> colTxValor, colTxPuntos;
    @FXML private TableColumn<Transaccion, String> colTxOrigen, colTxDestino, colTxEstado;
    @FXML private TableColumn<OperacionProgramada, LocalDateTime> colProgFecha;
    @FXML private TableColumn<OperacionProgramada, String> colProgId, colProgTipo, colProgFrecuencia, colProgEstado;
    @FXML private TableColumn<OperacionProgramada, Number> colProgMonto;
    @FXML private TableColumn<Beneficio, String> colBenNombre;
    @FXML private TableColumn<Beneficio, Number> colBenPuntos, colBenBono;
    @FXML private TableColumn<CanjeBeneficio, String> colCanjeCodigo, colCanjeNombre, colCanjeEstado;
    @FXML private TableColumn<CanjeBeneficio, Number> colCanjeBono;
    @FXML private TableColumn<Alerta, LocalDateTime> colAlertaFecha;
    @FXML private TableColumn<Alerta, String> colAlertaTipo, colAlertaMensaje, colAlertaAtendida;

    @FXML private TextField txtNuevaWalletCodigo, txtNuevaWalletNombre, txtMonto, txtDestino;
    @FXML private ChoiceBox<TipoBilletera> cmbNuevaWalletTipo;
    @FXML private ComboBox<Billetera> cmbOrigen, cmbWalletProgramada, cmbBilleteraCupon;
    @FXML private TextField txtProgramadaId, txtProgramadaDestino, txtProgramadaMonto, txtProgramadaFecha;
    @FXML private ChoiceBox<TipoTransaccion> cmbProgramadaTipo;
    @FXML private ChoiceBox<Frecuencia> cmbProgramadaFrecuencia;
    @FXML private TextField txtCodigoCupon;
    @FXML private Label lblProgramadaInfo;
    @FXML private TextField txtPerfilNombre, txtPerfilCorreo;

    private ContextoAplicacion contexto;
    private FintechController sistema;
    private Stage escenario;
    private String idUsuario;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    /** Refresca las tablas cada pocos segundos para ver cuándo se ejecuta una operación programada. */
    private Timeline autoRefresco;
    /** Evita que el refresco automático dispare la carga del formulario al reponer la selección. */
    private boolean cargandoTabla;

    @FXML
    private void initialize() {
        configurarColumnas();
        cmbNuevaWalletTipo.setItems(FXCollections.observableArrayList(TipoBilletera.values()));
        cmbNuevaWalletTipo.getSelectionModel().select(TipoBilletera.AHORRO);
        var tipos = FXCollections.observableArrayList(TipoTransaccion.RECARGA, TipoTransaccion.RETIRO,
                TipoTransaccion.TRANSFERENCIA_INTERNA, TipoTransaccion.TRANSFERENCIA_EXTERNA);
        cmbProgramadaTipo.setItems(tipos);
        cmbProgramadaTipo.getSelectionModel().select(TipoTransaccion.RECARGA);
        cmbProgramadaFrecuencia.setItems(FXCollections.observableArrayList(Frecuencia.values()));
        cmbProgramadaFrecuencia.getSelectionModel().select(Frecuencia.UNICA);
        StringConverter<Billetera> conversor = new StringConverter<>() {
            @Override public String toString(Billetera wallet) {
                return wallet == null ? "" : wallet.getCodigo() + " · " + wallet.getNombre()
                        + " · $" + wallet.getSaldo();
            }
            @Override public Billetera fromString(String texto) { return null; }
        };
        cmbOrigen.setConverter(conversor);
        cmbWalletProgramada.setConverter(conversor);
        cmbBilleteraCupon.setConverter(conversor);

        // Al elegir una operación pendiente se cargan sus datos en el formulario para poder editarla.
        tablaProgramadas.getSelectionModel().selectedItemProperty().addListener((obs, anterior, nueva) -> {
            if (!cargandoTabla && nueva != null) cargarFormularioProgramada(nueva);
        });
    }

    public void configurar(ContextoAplicacion contexto, String idUsuario, Stage escenario) {
        this.contexto = contexto;
        this.sistema = contexto.getSistema();
        this.idUsuario = idUsuario;
        this.escenario = escenario;
        refrescarTodo();
        if (sistema.buscarUsuario(idUsuario) != null) iniciarAutoRefresco();
    }

    private void iniciarAutoRefresco() {
        autoRefresco = new Timeline(new KeyFrame(Duration.seconds(3), evento -> {
            Usuario usuario = sistema.buscarUsuario(idUsuario);
            if (usuario != null) refrescarDatos(usuario);
        }));
        autoRefresco.setCycleCount(Timeline.INDEFINITE);
        autoRefresco.play();
    }

    private void configurarColumnas() {
        colWalletCodigo.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("codigo"));
        colWalletNombre.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nombre"));
        colWalletTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo().name()));
        colWalletSaldo.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("saldo"));
        colWalletEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActivo() ? "ACTIVA" : "INACTIVA"));

        colTxFecha.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("fechaHora"));
        colTxTipo.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("tipo"));
        colTxValor.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("valor"));
        colTxOrigen.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("billeteraOrigen"));
        colTxDestino.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("billeteraDestino"));
        colTxPuntos.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("puntosGenerados"));
        colTxEstado.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("estado"));

        colProgFecha.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("fechaHora"));
        colProgId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colProgTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo().name()));
        colProgMonto.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("valor"));
        colProgFrecuencia.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFrecuencia().name()));
        colProgEstado.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("estado"));

        colBenNombre.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nombre"));
        colBenPuntos.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("costoPuntos"));
        colBenBono.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("valorBono"));
        colCanjeCodigo.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("codigoRedencion"));
        colCanjeNombre.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nombreBeneficio"));
        colCanjeEstado.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("estado"));
        colCanjeBono.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("valorBono"));

        colAlertaFecha.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("fecha"));
        colAlertaTipo.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("tipoAlerta"));
        colAlertaMensaje.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("mensaje"));
        colAlertaAtendida.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isAtendida() ? "Sí" : "Pendiente"));

        // Etiquetas de color para estados, tipos y niveles
        EtiquetasTabla.aplicar(colWalletEstado);
        EtiquetasTabla.aplicar(colTxTipo);
        EtiquetasTabla.aplicar(colTxEstado);
        EtiquetasTabla.aplicar(colProgTipo);
        EtiquetasTabla.aplicar(colProgEstado);
        EtiquetasTabla.aplicar(colCanjeEstado);
        EtiquetasTabla.aplicar(colAlertaAtendida);
    }

    /** Copia los datos guardados a los campos editables (solo en el refresco completo, no en el automático). */
    private void cargarFormularioPerfil(Usuario usuario) {
        txtPerfilNombre.setText(usuario.getNombre());
        txtPerfilCorreo.setText(usuario.getCorreo());
    }

    @FXML private void guardarPerfil() {
        if (sistema.modificarUsuario(idUsuario, txtPerfilNombre.getText(), txtPerfilCorreo.getText())) {
            mutacion("Tus datos se actualizaron correctamente.");
        } else {
            aviso("No se pudo guardar. El nombre no puede estar vacío y el correo debe tener un formato válido, "
                    + "por ejemplo nombre@correo.com.");
        }
    }

    /** Descarta lo escrito y vuelve a mostrar los datos guardados. */
    @FXML private void restablecerPerfil() {
        Usuario usuario = sistema.buscarUsuario(idUsuario);
        if (usuario != null) cargarFormularioPerfil(usuario);
    }

    /** Refresco completo: datos, listas desplegables y catálogo de recompensas. */
    private void refrescarTodo() {
        Usuario usuario = sistema.buscarUsuario(idUsuario);
        if (usuario == null) {
            mostrar(Alert.AlertType.ERROR, "Cuenta no disponible", "La cuenta fue desactivada o ya no existe.");
            cerrarSesion();
            return;
        }
        refrescarDatos(usuario);
        cargarFormularioPerfil(usuario);
        List<Billetera> activas = lista(usuario.getBilleteras()).stream().filter(Billetera::isActivo).toList();
        actualizarCombo(cmbOrigen, activas);
        actualizarCombo(cmbWalletProgramada, activas);
        actualizarCombo(cmbBilleteraCupon, activas);
        tablaBeneficios.setItems(FXCollections.observableArrayList(lista(sistema.obtenerBeneficiosDisponibles())));
        tablaCanjes.setItems(FXCollections.observableArrayList(lista(sistema.obtenerHistorialCanjesUsuario(idUsuario))));
    }

    /**
     * Refresco liviano (también lo usa el temporizador): etiquetas y tablas de datos, sin tocar
     * formularios, listas desplegables ni la selección del catálogo de recompensas.
     */
    private void refrescarDatos(Usuario usuario) {
        lblNombreUsuario.setText("Hola, " + usuario.getNombre());
        lblIdentificacion.setText("Cédula " + usuario.getId());
        lblPerfilNombre.setText(usuario.getNombre());
        lblPerfilId.setText(usuario.getId());
        lblPerfilCorreo.setText(usuario.getCorreo());
        lblPuntos.setText(String.format("%,d", usuario.getPuntosAcumulados()));
        lblNivel.setText(usuario.getNivel());

        List<Billetera> billeteras = lista(usuario.getBilleteras());
        tablaBilleteras.setItems(FXCollections.observableArrayList(billeteras));
        long saldo = billeteras.stream().filter(Billetera::isActivo).mapToLong(Billetera::getSaldo).sum();
        lblSaldoTotal.setText(moneda(saldo));

        List<Transaccion> transacciones = lista(usuario.getHistorialTransacciones());
        if (tablaTransacciones.getItems().size() != transacciones.size()) {
            tablaTransacciones.setItems(FXCollections.observableArrayList(transacciones));
        }
        List<Alerta> alertas = lista(usuario.getHistorialAlertas());
        if (tablaAlertas.getItems().size() != alertas.size()) {
            tablaAlertas.setItems(FXCollections.observableArrayList(alertas));
        }
        actualizarTablaProgramadas();
    }

    /** Pendientes (por fecha) seguidas de las ya procesadas, la más reciente primero. Las canceladas no se muestran. */
    private List<OperacionProgramada> operacionesProgramadasDelUsuario() {
        List<OperacionProgramada> resultado = new ArrayList<>();
        for (OperacionProgramada op : sistema.obtenerOperacionesPendientes()) {
            if (idUsuario.equals(op.getIdUsuario())) resultado.add(op);
        }
        List<OperacionProgramada> procesadas = new ArrayList<>();
        for (OperacionProgramada op : sistema.obtenerOperacionesProcesadas()) {
            if (idUsuario.equals(op.getIdUsuario())
                    && !OperacionProgramada.CANCELADA_POR_USUARIO.equals(op.getEstado())) {
                procesadas.add(op);
            }
        }
        Collections.reverse(procesadas);
        resultado.addAll(procesadas);
        return resultado;
    }

    private static String clave(OperacionProgramada op) {
        return op.getId() + "|" + op.getEstado() + "|" + op.getFechaHora() + "|" + op.getValor()
                + "|" + op.getTipo() + "|" + op.getFrecuencia() + "|" + op.getBilleteraOrigen()
                + "|" + op.getBilleteraDestino();
    }

    /** Actualiza la tabla solo si algo cambió y conserva la fila seleccionada (por identificador). */
    private void actualizarTablaProgramadas() {
        List<OperacionProgramada> nuevas = operacionesProgramadasDelUsuario();
        List<OperacionProgramada> actuales = tablaProgramadas.getItems();
        boolean igual = actuales.size() == nuevas.size();
        for (int i = 0; igual && i < nuevas.size(); i++) {
            igual = clave(actuales.get(i)).equals(clave(nuevas.get(i)));
        }
        if (igual) return;

        OperacionProgramada seleccionada = tablaProgramadas.getSelectionModel().getSelectedItem();
        String idSeleccionado = seleccionada == null ? null : seleccionada.getId();
        cargandoTabla = true;
        try {
            tablaProgramadas.setItems(FXCollections.observableArrayList(nuevas));
            if (idSeleccionado != null) {
                for (OperacionProgramada op : nuevas) {
                    if (op.getId().equals(idSeleccionado)) {
                        tablaProgramadas.getSelectionModel().select(op);
                        break;
                    }
                }
            }
        } finally {
            cargandoTabla = false;
        }
    }

    private void cargarFormularioProgramada(OperacionProgramada op) {
        if (!OperacionProgramada.PENDIENTE.equals(op.getEstado())) {
            lblProgramadaInfo.setText("La operación " + op.getId() + " ya está " + op.getEstado()
                    + " y no se puede modificar.");
            return;
        }
        txtProgramadaId.setText(op.getId());
        for (Billetera billetera : cmbWalletProgramada.getItems()) {
            if (String.valueOf(billetera.getCodigo()).equals(op.getBilleteraOrigen())) {
                cmbWalletProgramada.setValue(billetera);
                break;
            }
        }
        txtProgramadaDestino.setText(op.getBilleteraDestino() == null ? "" : op.getBilleteraDestino());
        txtProgramadaMonto.setText(String.valueOf(Math.round(op.getValor())));
        txtProgramadaFecha.setText(op.getFechaHora().format(FORMATO_FECHA));
        cmbProgramadaTipo.setValue(op.getTipo());
        cmbProgramadaFrecuencia.setValue(op.getFrecuencia());
        lblProgramadaInfo.setText("Editando " + op.getId()
                + ". Cambia los datos y pulsa Actualizar, o Eliminar para cancelarla.");
    }

    private void limpiarFormularioProgramada() {
        tablaProgramadas.getSelectionModel().clearSelection();
        txtProgramadaId.clear();
        txtProgramadaDestino.clear();
        txtProgramadaMonto.clear();
        txtProgramadaFecha.clear();
        cmbProgramadaTipo.getSelectionModel().select(TipoTransaccion.RECARGA);
        cmbProgramadaFrecuencia.getSelectionModel().select(Frecuencia.UNICA);
        lblProgramadaInfo.setText("Selecciona una fila para editarla o eliminarla.");
    }

    /** Recarga las opciones y conserva la billetera elegida (por código) con su saldo actualizado. */
    private void actualizarCombo(ComboBox<Billetera> combo, List<Billetera> activas) {
        Billetera previa = combo.getValue();
        combo.setItems(FXCollections.observableArrayList(activas));
        Billetera elegida = null;
        if (previa != null) {
            for (Billetera candidata : activas) {
                if (candidata.getCodigo() == previa.getCodigo()) { elegida = candidata; break; }
            }
        }
        if (elegida == null && !activas.isEmpty()) elegida = activas.get(0);
        combo.setValue(elegida);
    }

    @FXML private void crearBilletera() {
        try {
            boolean ok = sistema.crearBilleteraAUsuario(idUsuario, entero(txtNuevaWalletCodigo),
                    txtNuevaWalletNombre.getText(), cmbNuevaWalletTipo.getValue(), 0);
            if (ok) mutacion("Tu nueva billetera quedó creada con saldo inicial de $0.");
            else aviso("No se pudo crear. Verifica que el código sea único y los datos estén completos.");
        } catch (RuntimeException ex) { aviso("Datos inválidos: " + ex.getMessage()); }
    }

    @FXML private void recargar() {
        Billetera wallet = cmbOrigen.getValue();
        if (wallet == null) { aviso("Selecciona tu billetera de origen."); return; }
        ejecutar(() -> sistema.realizarRecarga(wallet.getCodigo(), entero(txtMonto), idUsuario),
                "Recarga registrada correctamente.");
    }

    @FXML private void retirar() {
        Billetera wallet = cmbOrigen.getValue();
        if (wallet == null) { aviso("Selecciona tu billetera de origen."); return; }
        ejecutar(() -> sistema.realizarRetiro(wallet.getCodigo(), entero(txtMonto), idUsuario),
                "Retiro realizado correctamente.");
    }

    @FXML private void transferir() {
        Billetera wallet = cmbOrigen.getValue();
        if (wallet == null) { aviso("Selecciona tu billetera de origen."); return; }
        try {
            int destino = entero(txtDestino);
            ejecutar(() -> sistema.realizarTransferencia(wallet.getCodigo(), destino, entero(txtMonto), idUsuario),
                    "Transferencia enviada correctamente.");
        } catch (RuntimeException ex) { aviso("Indica un código de billetera destino y un monto válido."); }
    }

    @FXML private void programarOperacion() {
        try {
            Billetera origen = cmbWalletProgramada.getValue();
            if (origen == null) { aviso("Selecciona una billetera de origen."); return; }
            String id = txtProgramadaId.getText().isBlank()
                    ? "USR-" + idUsuario + "-" + System.currentTimeMillis() : txtProgramadaId.getText().trim();
            int destino = txtProgramadaDestino.getText().isBlank() ? 0 : entero(txtProgramadaDestino);
            LocalDateTime fecha = leerFechaProgramada();
            boolean ok = sistema.programarOperacion(id, entero(txtProgramadaMonto), origen.getCodigo(), destino,
                    fecha, cmbProgramadaTipo.getValue(), cmbProgramadaFrecuencia.getValue(), "PENDIENTE");
            if (ok) {
                mutacion("La operación quedó en cola y se ejecutará automáticamente cuando llegue su fecha.");
                limpiarFormularioProgramada();
            } else {
                aviso("No se pudo programar. Verifica el destino, que el identificador no esté repetido "
                        + "(si cargaste una fila, cámbialo) y que los datos sean válidos.");
            }
        } catch (NumberFormatException ex) {
            aviso("El monto y el destino deben ser números enteros.");
        } catch (RuntimeException ex) {
            aviso(ex.getMessage());
        }
    }

    /** Modifica la operación pendiente seleccionada; sin selección solo refresca la lista. */
    @FXML private void actualizarProgramada() {
        OperacionProgramada seleccionada = tablaProgramadas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            refrescarTodo();
            lblProgramadaInfo.setText("Lista actualizada a las " + LocalTime.now().format(FORMATO_HORA) + ".");
            return;
        }
        if (!OperacionProgramada.PENDIENTE.equals(seleccionada.getEstado())) {
            refrescarTodo();
            lblProgramadaInfo.setText("La operación " + seleccionada.getId() + " ya está "
                    + seleccionada.getEstado() + " y no se puede modificar.");
            return;
        }
        try {
            Billetera origen = cmbWalletProgramada.getValue();
            if (origen == null) { aviso("Selecciona una billetera de origen."); return; }
            int destino = txtProgramadaDestino.getText().isBlank() ? 0 : entero(txtProgramadaDestino);
            LocalDateTime fecha = leerFechaProgramada();
            boolean ok = sistema.modificarOperacionProgramada(seleccionada.getId(), idUsuario,
                    entero(txtProgramadaMonto), origen.getCodigo(), destino, fecha,
                    cmbProgramadaTipo.getValue(), cmbProgramadaFrecuencia.getValue());
            if (ok) mutacion("La operación " + seleccionada.getId() + " se actualizó correctamente.");
            else aviso("No se pudo actualizar. Verifica el destino, el monto y que la operación siga pendiente.");
        } catch (NumberFormatException ex) {
            aviso("El monto y el destino deben ser números enteros.");
        } catch (RuntimeException ex) {
            aviso(ex.getMessage());
        }
    }

    /** Cancela la operación pendiente seleccionada: sale de la cola y ya no se ejecuta. */
    @FXML private void eliminarProgramada() {
        OperacionProgramada seleccionada = tablaProgramadas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) { aviso("Selecciona una operación de la tabla."); return; }
        if (!OperacionProgramada.PENDIENTE.equals(seleccionada.getEstado())) {
            aviso("Solo se pueden eliminar operaciones pendientes. Esta ya está " + seleccionada.getEstado() + ".");
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar la operación " + seleccionada.getId() + "? Dejará de ejecutarse.",
                ButtonType.OK, ButtonType.CANCEL);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText(null);
        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) return;

        if (sistema.cancelarOperacionProgramada(seleccionada.getId(), idUsuario)) {
            mutacion("La operación se eliminó y ya no se ejecutará.");
            limpiarFormularioProgramada();
        } else {
            aviso("No se pudo eliminar: la operación ya no está pendiente.");
        }
    }

    private LocalDateTime leerFechaProgramada() {
        try {
            return LocalDateTime.parse(txtProgramadaFecha.getText().trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "La fecha debe tener el formato AAAA-MM-DDTHH:MM, por ejemplo 2026-12-01T09:30.");
        }
    }

    @FXML private void canjearBeneficio() {
        Beneficio beneficio = tablaBeneficios.getSelectionModel().getSelectedItem();
        if (beneficio == null) { aviso("Selecciona una recompensa del catálogo."); return; }
        CanjeBeneficio canje = sistema.canjearBeneficio(idUsuario, beneficio.getId());
        if (canje == null) aviso("No tienes puntos suficientes o el beneficio dejó de estar disponible.");
        else {
            txtCodigoCupon.setText(canje.getCodigoRedencion());
            mutacion("Canje realizado. Tu código es " + canje.getCodigoRedencion()
                    + ". El bono de " + moneda(canje.getValorBono()) + " se acredita al aplicar el cupón.");
        }
    }

    @FXML private void aplicarCupon() {
        Billetera wallet = cmbBilleteraCupon.getValue();
        if (wallet == null) { aviso("Selecciona la billetera que recibirá el bono."); return; }
        Transaccion tx = sistema.aplicarCupon(idUsuario, wallet.getCodigo(), txtCodigoCupon.getText());
        if (tx == null) aviso("Cupón no válido, ya utilizado, de otro usuario o la billetera no puede recibir el abono.");
        else mutacion("Cupón aplicado. Se abonaron " + moneda((long) tx.getValor()) + " a tu saldo.");
    }

    private void ejecutar(java.util.function.BooleanSupplier accion, String mensaje) {
        try {
            if (accion.getAsBoolean()) mutacion(mensaje);
            else aviso("La operación no se pudo realizar. Comprueba el saldo, el monto y que las billeteras estén activas.");
        } catch (RuntimeException ex) { aviso("No se pudo completar la operación: " + ex.getMessage()); }
    }

    private void mutacion(String mensaje) {
        try { contexto.guardar(); } catch (IOException ex) { aviso("La operación se realizó, pero no se pudo guardar: " + ex.getMessage()); }
        refrescarTodo();
        mostrar(Alert.AlertType.INFORMATION, "Listo", mensaje);
    }

    @FXML private void cerrarSesion() {
        if (autoRefresco != null) autoRefresco.stop();
        App.mostrarVentana(escenario, contexto, "/fxml/inicio.fxml", "NEXO · Billetera digital", 1120, 760,
                (controller, stage) -> ((LoginViewController) controller).configurar(contexto, stage));
    }

    private int entero(TextField campo) { return Integer.parseInt(campo.getText().trim()); }
    private String moneda(long valor) { return "$ " + String.format(java.util.Locale.forLanguageTag("es-CO"), "%,d", valor); }
    private void aviso(String mensaje) { mostrar(Alert.AlertType.WARNING, "Atención", mensaje); }
    private void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo); alerta.setTitle(titulo); alerta.setHeaderText(null); alerta.setContentText(mensaje); alerta.showAndWait();
    }
    private static <T> List<T> lista(Iterable<T> iterable) {
        ArrayList<T> resultado = new ArrayList<>();
        for (T elemento : iterable) resultado.add(elemento);
        return resultado;
    }
}