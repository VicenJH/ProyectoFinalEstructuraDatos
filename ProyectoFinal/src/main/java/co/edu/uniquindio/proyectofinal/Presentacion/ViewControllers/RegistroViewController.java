package co.edu.uniquindio.proyectofinal.Presentacion.ViewControllers;

import co.edu.uniquindio.proyectofinal.App;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.Usuario;
import co.edu.uniquindio.proyectofinal.Presentacion.ContextoAplicacion;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

/** Pantalla de auto‑registro: crea un usuario nuevo y lo lleva directo a su billetera. */
public class RegistroViewController {

    @FXML private TextField txtId;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCorreo;
    @FXML private Label lblMensaje;

    private ContextoAplicacion contexto;
    private Stage escenario;

    public void configurar(ContextoAplicacion contexto, Stage escenario) {
        this.contexto = contexto;
        this.escenario = escenario;
    }

    @FXML
    private void registrar() {
        String id = texto(txtId);
        String nombre = texto(txtNombre);
        String correo = texto(txtCorreo);
        if (id.isEmpty() || nombre.isEmpty() || correo.isEmpty()) {
            lblMensaje.setText("Completa la identificación, el nombre y el correo.");
            return;
        }
        if (!correo.contains("@")) {
            lblMensaje.setText("El correo debe contener «@».");
            return;
        }
        boolean creado;
        try {
            creado = contexto.getSistema().registrarUsuario(id, nombre, correo);
        } catch (RuntimeException ex) {
            lblMensaje.setText("No se pudo registrar: " + ex.getMessage());
            return;
        }
        if (!creado) {
            lblMensaje.setText("No se pudo registrar. Es posible que la identificación ya exista.");
            return;
        }
        try {
            contexto.guardar();
        } catch (IOException ex) {
            mostrar(Alert.AlertType.WARNING, "Guardado pendiente",
                    "La cuenta se creó, pero falló el guardado automático: " + ex.getMessage());
        }
        Usuario usuario = contexto.getSistema().buscarUsuario(id);
        mostrar(Alert.AlertType.INFORMATION, "Cuenta creada", "¡Bienvenido/a, " + nombre + "!");
        if (usuario == null) {
            volverInicio();
            return;
        }
        App.mostrarVentana(escenario, contexto, "/fxml/usuario.fxml", "NEXO · Mi billetera", 1320, 860,
                (controller, stage) -> ((UserViewController) controller).configurar(contexto, usuario.getId(), stage));
    }

    @FXML
    private void volverInicio() {
        App.mostrarVentana(escenario, contexto, "/fxml/inicio.fxml", "NEXO · Billetera digital", 1120, 760,
                (controller, stage) -> ((LoginViewController) controller).configurar(contexto, stage));
    }

    private String texto(TextField campo) {
        return campo.getText() == null ? "" : campo.getText().trim();
    }

    private void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
