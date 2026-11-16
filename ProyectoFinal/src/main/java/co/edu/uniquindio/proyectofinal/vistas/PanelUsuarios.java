package co.edu.uniquindio.proyectofinal.vistas;

import co.edu.uniquindio.proyectofinal.Estructuras.Controllers.FintechController;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.Billetera;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.TipoBilletera;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.Usuario;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Panel de gestión de usuarios y billeteras (requisitos 4.1, 6.1 y 6.2).
 *
 * La lista de usuarios se obtiene del árbol binario de búsqueda, de modo que
 * aparece como ranking: el usuario con más puntos queda arriba.
 */
public class PanelUsuarios extends BorderPane implements Refrescable {

    private final FintechController controlador;

    private final TextField txtId = new TextField();
    private final TextField txtNombre = new TextField();
    private final TextField txtCorreo = new TextField();
    private final TableView<Usuario> tablaUsuarios = new TableView<>();

    private final Label lblBilleteras = new Label("Billeteras");
    private final TextField txtCodigo = new TextField();
    private final TextField txtNombreBilletera = new TextField();
    private final ComboBox<TipoBilletera> cmbTipo = new ComboBox<>();
    private final TextField txtSaldo = new TextField("0");
    private final TableView<Billetera> tablaBilleteras = new TableView<>();

    private final Label lblEstado = new Label();

    /** Evita reaccionar a los cambios de selección que provoca refrescar(). */
    private boolean actualizando = false;

    public PanelUsuarios(FintechController controlador) {
        this.controlador = controlador;

        construirTablaUsuarios();
        construirTablaBilleteras();

        SplitPane centro = new SplitPane(panelUsuarios(), panelBilleteras());
        centro.setOrientation(javafx.geometry.Orientation.VERTICAL);
        centro.setDividerPositions(0.5);

        setCenter(centro);
        setBottom(lblEstado);
        BorderPane.setMargin(lblEstado, new Insets(8, 12, 8, 12));

        refrescar();
    }

    // ------------------------------------------------------------------
    // CONSTRUCCIÓN DE LA VISTA
    // ------------------------------------------------------------------

    private VBox panelUsuarios() {
        txtId.setPromptText("Ej. U005");
        txtNombre.setPromptText("Nombre completo");
        txtCorreo.setPromptText("correo@dominio.com");

        GridPane formulario = new GridPane();
        formulario.setHgap(10);
        formulario.setVgap(8);
        formulario.addRow(0, new Label("Identificación"), txtId, new Label("Nombre"), txtNombre);
        formulario.addRow(1, new Label("Correo"), txtCorreo);
        GridPane.setHgrow(txtNombre, Priority.ALWAYS);
        GridPane.setHgrow(txtCorreo, Priority.ALWAYS);

        Button btnRegistrar = new Button("Registrar");
        Button btnModificar = new Button("Modificar");
        Button btnEliminar = new Button("Eliminar");
        Button btnLimpiar = new Button("Limpiar");

        btnRegistrar.setOnAction(e -> registrar());
        btnModificar.setOnAction(e -> modificar());
        btnEliminar.setOnAction(e -> eliminar());
        btnLimpiar.setOnAction(e -> limpiarFormulario());

        HBox botones = new HBox(8, btnRegistrar, btnModificar, btnEliminar, btnLimpiar);

        Label titulo = new Label("Usuarios (ranking por puntos)");
        titulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        VBox caja = new VBox(10, titulo, formulario, botones, tablaUsuarios);
        caja.setPadding(new Insets(12));
        VBox.setVgrow(tablaUsuarios, Priority.ALWAYS);
        return caja;
    }

    private VBox panelBilleteras() {
        txtCodigo.setPromptText("Código numérico");
        txtNombreBilletera.setPromptText("Ej. Vacaciones");
        cmbTipo.getItems().addAll(TipoBilletera.values());
        cmbTipo.getSelectionModel().selectFirst();

        HBox formulario = new HBox(8,
                new Label("Código"), txtCodigo,
                new Label("Nombre"), txtNombreBilletera,
                new Label("Tipo"), cmbTipo,
                new Label("Saldo inicial"), txtSaldo);
        formulario.setStyle("-fx-alignment: center-left;");
        HBox.setHgrow(txtNombreBilletera, Priority.ALWAYS);
        txtCodigo.setPrefWidth(110);
        txtSaldo.setPrefWidth(110);

        Button btnCrear = new Button("Crear billetera");
        btnCrear.setOnAction(e -> crearBilletera());

        lblBilleteras.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        VBox caja = new VBox(10, lblBilleteras, formulario, btnCrear, tablaBilleteras);
        caja.setPadding(new Insets(12));
        VBox.setVgrow(tablaBilleteras, Priority.ALWAYS);
        return caja;
    }

    private void construirTablaUsuarios() {
        tablaUsuarios.getColumns().add(Util.<Usuario, String>columna("ID", Usuario::getId));
        tablaUsuarios.getColumns().add(Util.<Usuario, String>columna("Nombre", Usuario::getNombre));
        tablaUsuarios.getColumns().add(Util.<Usuario, String>columna("Correo", Usuario::getCorreo));
        tablaUsuarios.getColumns().add(Util.<Usuario, Integer>columna("Puntos", Usuario::getPuntosAcumulados));
        tablaUsuarios.getColumns().add(Util.<Usuario, String>columna("Nivel", Usuario::getNivel));
        tablaUsuarios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaUsuarios.setPlaceholder(new Label("No hay usuarios registrados."));

        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, anterior, usuario) -> {
            if (!actualizando) {
                mostrarUsuario(usuario);
            }
        });
    }

    private void construirTablaBilleteras() {
        tablaBilleteras.getColumns().add(Util.<Billetera, Integer>columna("Código", Billetera::getCodigo));
        tablaBilleteras.getColumns().add(Util.<Billetera, String>columna("Nombre", Billetera::getNombre));
        tablaBilleteras.getColumns().add(Util.<Billetera, TipoBilletera>columna("Tipo", Billetera::getTipo));
        tablaBilleteras.getColumns().add(Util.<Billetera, String>columna("Saldo", b -> "$" + b.getSaldo()));
        tablaBilleteras.getColumns().add(Util.<Billetera, String>columna("Estado", b -> b.isActivo() ? "Activa" : "Inactiva"));
        tablaBilleteras.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaBilleteras.setPlaceholder(new Label("Seleccione un usuario para ver sus billeteras."));
    }

    // ------------------------------------------------------------------
    // ACCIONES
    // ------------------------------------------------------------------

    private void registrar() {
        boolean ok = controlador.registrarUsuario(txtId.getText().trim(), txtNombre.getText(), txtCorreo.getText());

        if (ok) {
            informar("Usuario " + txtId.getText().trim() + " registrado.", true);
            String id = txtId.getText().trim();
            refrescar();
            seleccionar(id);
        } else {
            informar("No se pudo registrar: la identificación ya existe o hay campos vacíos.", false);
        }
    }

    private void modificar() {
        Usuario usuario = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (usuario == null) {
            informar("Seleccione primero el usuario que desea modificar.", false);
            return;
        }

        if (controlador.modificarUsuario(usuario.getId(), txtNombre.getText(), txtCorreo.getText())) {
            informar("Usuario " + usuario.getId() + " actualizado.", true);
            refrescar();
        } else {
            informar("No se pudo modificar: el nombre y el correo no pueden estar vacíos.", false);
        }
    }

    private void eliminar() {
        Usuario usuario = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (usuario == null) {
            informar("Seleccione primero el usuario que desea eliminar.", false);
            return;
        }

        if (!Util.confirmar("¿Eliminar a " + usuario.getNombre() + " y todas sus billeteras?")) {
            return;
        }

        if (controlador.eliminarUsuario(usuario.getId())) {
            informar("Usuario " + usuario.getId() + " eliminado.", true);
            limpiarFormulario();
            refrescar();
        } else {
            informar("No se pudo eliminar el usuario.", false);
        }
    }

    private void crearBilletera() {
        Usuario usuario = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (usuario == null) {
            informar("Seleccione el usuario dueño de la nueva billetera.", false);
            return;
        }

        Integer codigo = Util.entero(txtCodigo.getText());
        Integer saldo = Util.entero(txtSaldo.getText());

        if (codigo == null || saldo == null) {
            informar("El código y el saldo inicial deben ser números enteros.", false);
            return;
        }

        boolean ok = controlador.crearBilleteraAUsuario(
                usuario.getId(), codigo, txtNombreBilletera.getText(), cmbTipo.getValue(), saldo);

        if (ok) {
            informar("Billetera " + codigo + " creada para " + usuario.getNombre() + ".", true);
            txtCodigo.clear();
            txtNombreBilletera.clear();
            txtSaldo.setText("0");
            cargarBilleteras(usuario);
        } else {
            informar("No se pudo crear: el código ya existe, es menor o igual a cero o el saldo es negativo.", false);
        }
    }

    // ------------------------------------------------------------------
    // ACTUALIZACIÓN DE LA VISTA
    // ------------------------------------------------------------------

    @Override
    public void refrescar() {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        String id = seleccionado == null ? null : seleccionado.getId();

        actualizando = true;
        // Inorden del árbol = ascendente; se invierte para mostrar el mayor puntaje primero.
        tablaUsuarios.setItems(Util.aListaInversa(controlador.getAnalitica().obtenerUsuariosOrdenadosPorPuntos()));
        actualizando = false;

        if (id != null) {
            seleccionar(id);
        } else {
            cargarBilleteras(null);
        }
    }

    private void seleccionar(String id) {
        Usuario usuario = controlador.buscarUsuario(id);

        if (usuario == null) {
            cargarBilleteras(null);
            return;
        }

        tablaUsuarios.getSelectionModel().select(usuario);
        mostrarUsuario(usuario);
    }

    private void mostrarUsuario(Usuario usuario) {
        if (usuario == null) {
            limpiarCampos();
            cargarBilleteras(null);
            return;
        }

        txtId.setText(usuario.getId());
        txtId.setDisable(true);
        txtNombre.setText(usuario.getNombre());
        txtCorreo.setText(usuario.getCorreo());
        cargarBilleteras(usuario);
    }

    private void cargarBilleteras(Usuario usuario) {
        if (usuario == null) {
            lblBilleteras.setText("Billeteras");
            tablaBilleteras.getItems().clear();
            return;
        }

        lblBilleteras.setText("Billeteras de " + usuario.getNombre());
        tablaBilleteras.setItems(Util.aLista(usuario.getBilleteras()));
    }

    private void limpiarFormulario() {
        actualizando = true;
        tablaUsuarios.getSelectionModel().clearSelection();
        actualizando = false;

        limpiarCampos();
        cargarBilleteras(null);
    }

    private void limpiarCampos() {
        txtId.clear();
        txtId.setDisable(false);
        txtNombre.clear();
        txtCorreo.clear();
    }

    private void informar(String mensaje, boolean exito) {
        lblEstado.setText(mensaje);
        lblEstado.setStyle(exito ? "-fx-text-fill: #1b7f3b;" : "-fx-text-fill: #b3261e;");
    }
}