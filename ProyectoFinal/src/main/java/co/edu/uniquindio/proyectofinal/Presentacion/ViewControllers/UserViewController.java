package co.edu.uniquindio.proyectofinal.Presentacion.ViewControllers;

import co.edu.uniquindio.proyectofinal.App;
import co.edu.uniquindio.proyectofinal.Estructuras.Controllers.FintechController;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.*;
import co.edu.uniquindio.proyectofinal.Presentacion.ContextoAplicacion;
import co.edu.uniquindio.proyectofinal.Presentacion.EtiquetasTabla;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private ContextoAplicacion contexto;
    private FintechController sistema;
    private Stage escenario;
    private String idUsuario;

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
    }

    public void configurar(ContextoAplicacion contexto, String idUsuario, Stage escenario) {
        this.contexto = contexto;
        this.sistema = contexto.getSistema();
        this.idUsuario = idUsuario;
        this.escenario = escenario;
        refrescarTodo();
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

    private void refrescarTodo() {
        Usuario usuario = sistema.buscarUsuario(idUsuario);
        if (usuario == null) {
            mostrar(Alert.AlertType.ERROR, "Cuenta no disponible", "La cuenta fue desactivada o ya no existe.");
            cerrarSesion();
            return;
        }
        lblNombreUsuario.setText("Hola, " + usuario.getNombre());
        lblIdentificacion.setText("Cédula " + usuario.getId());
        lblPerfilNombre.setText(usuario.getNombre());
        lblPerfilId.setText(usuario.getId());
        lblPerfilCorreo.setText(usuario.getCorreo());
        lblPuntos.setText(String.format("%,d", usuario.getPuntosAcumulados()));
        lblNivel.setText(usuario.getNivel());

        List<Billetera> billeteras = lista(usuario.getBilleteras());
        List<Billetera> activas = billeteras.stream().filter(Billetera::isActivo).toList();
        tablaBilleteras.setItems(FXCollections.observableArrayList(billeteras));
        actualizarCombo(cmbOrigen, activas);
        actualizarCombo(cmbWalletProgramada, activas);
        actualizarCombo(cmbBilleteraCupon, activas);
        long saldo = billeteras.stream().filter(Billetera::isActivo).mapToLong(Billetera::getSaldo).sum();
        lblSaldoTotal.setText(moneda(saldo));

        tablaTransacciones.setItems(FXCollections.observableArrayList(lista(usuario.getHistorialTransacciones())));
        List<OperacionProgramada> propias = new ArrayList<>();
        for (OperacionProgramada op : sistema.obtenerOperacionesPendientes()) {
            if (idUsuario.equals(op.getIdUsuario())) propias.add(op);
        }
        tablaProgramadas.setItems(FXCollections.observableArrayList(propias));
        tablaBeneficios.setItems(FXCollections.observableArrayList(lista(sistema.obtenerBeneficiosDisponibles())));
        tablaCanjes.setItems(FXCollections.observableArrayList(lista(sistema.obtenerHistorialCanjesUsuario(idUsuario))));
        tablaAlertas.setItems(FXCollections.observableArrayList(lista(usuario.getHistorialAlertas())));
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
            LocalDateTime fecha = LocalDateTime.parse(txtProgramadaFecha.getText().trim());
            boolean ok = sistema.programarOperacion(id, entero(txtProgramadaMonto), origen.getCodigo(), destino,
                    fecha, cmbProgramadaTipo.getValue(), cmbProgramadaFrecuencia.getValue(), "PENDIENTE");
            if (ok) mutacion("La operación quedó en cola y se ejecutará automáticamente cuando llegue su fecha.");
            else aviso("No se pudo programar. Verifica el destino y que la fecha sea válida.");
        } catch (RuntimeException ex) { aviso("Usa la fecha AAAA-MM-DDTHH:MM y valores válidos. " + ex.getMessage()); }
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
