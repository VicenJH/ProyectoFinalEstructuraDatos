package co.edu.uniquindio.proyectofinal.Presentacion.ViewControllers;

import co.edu.uniquindio.proyectofinal.App;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.Usuario;
import co.edu.uniquindio.proyectofinal.Estructuras.Servicios.AutenticacionService;
import co.edu.uniquindio.proyectofinal.Presentacion.ContextoAplicacion;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginViewController {

    @FXML private TextField txtIdentificacion;
    @FXML private PasswordField txtClaveAdmin;
    @FXML private Label lblEstadoDatos;

    private ContextoAplicacion contexto;
    private Stage escenario;
    private final AutenticacionService autenticacion = new AutenticacionService();

    public void configurar(ContextoAplicacion contexto, Stage escenario) {
        this.contexto = contexto;
        this.escenario = escenario;
        int usuarios = contexto.getSistema().obtenerUsuarios().getSize();
        int billeteras = contexto.getSistema().obtenerBilleteras().getSize();
        lblEstadoDatos.setText(usuarios + " usuarios · " + billeteras + " billeteras · datos guardados automáticamente");
    }

    @FXML
    private void ingresarUsuario() {
        String identificacion = txtIdentificacion.getText() == null ? "" : txtIdentificacion.getText().trim();
        if (identificacion.isEmpty()) {
            mostrar(Alert.AlertType.WARNING, "Identificación requerida", "Escribe tu cédula o identificación.");
            return;
        }
        Usuario usuario = contexto.getSistema().buscarUsuario(identificacion);
        if (usuario == null) {
            mostrar(Alert.AlertType.ERROR, "Cuenta no encontrada",
                    "No existe un usuario activo con esa identificación. Contacta al administrador para crear la cuenta.");
            return;
        }
        App.mostrarVentana(escenario, contexto, "/fxml/usuario.fxml", "NEXO · Mi billetera", 1320, 860,
                (controller, stage) -> ((UserViewController) controller).configurar(contexto, usuario.getId(), stage));
    }

    @FXML
    private void ingresarAdministrador() {
        if (!autenticacion.autenticarAdministrador(txtClaveAdmin.getText())) {
            mostrar(Alert.AlertType.ERROR, "Acceso denegado", "La clave de administrador no es correcta.");
            return;
        }
        App.mostrarVentana(escenario, contexto, "/fxml/admin.fxml", "NEXO · Administración", 1480, 940,
                (controller, stage) -> ((AdminViewController) controller).configurar(contexto, stage));
    }

    @FXML
    private void abrirRegistro() {
        App.mostrarVentana(escenario, contexto, "/fxml/registro.fxml", "NEXO · Crear cuenta", 900, 760,
                (controller, stage) -> ((RegistroViewController) controller).configurar(contexto, stage));
    }

    @FXML
    private void abrirMonitor() {
        App.mostrarVentana(escenario, contexto, "/fxml/monitor-pruebas.fxml", "NEXO · Monitor de pruebas", 1260, 850,
                (controller, stage) -> ((TestMonitorViewController) controller).configurar(contexto, stage));
    }

    private void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
