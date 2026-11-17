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

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AdminViewController {

    @FXML private Label lblTotalUsuarios, lblTotalBilleteras, lblTotalTransacciones, lblSaldoGlobal;
    @FXML private Label lblRutaRespaldo, lblArchivoDatos;
    @FXML private TableView<Transaccion> tablaUltimasTransacciones, tablaTransacciones;
    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TextField txtBuscarUsuario;
    @FXML private TableView<Billetera> tablaBilleteras;
    @FXML private TableView<OperacionProgramada> tablaProgramadas;
    @FXML private TableView<Beneficio> tablaBeneficios;
    @FXML private TableView<CanjeBeneficio> tablaCanjes;
    @FXML private TableView<EventoAuditoria> tablaAuditoria;
    @FXML private TableView<Alerta> tablaAlertas;
    @FXML private TabPane tabsAdmin;
    @FXML private TextArea txtAnalitica;

    @FXML private TextField txtUsuarioId, txtUsuarioNombre, txtUsuarioCorreo;
    @FXML private TextField txtBilleteraUsuario, txtBilleteraCodigo, txtBilleteraNombre, txtBilleteraSaldo;
    @FXML private ChoiceBox<TipoBilletera> cmbBilleteraTipo;
    @FXML private TextField txtOperacionUsuario, txtOperacionOrigen, txtOperacionDestino, txtOperacionMonto;
    @FXML private TextField txtProgramadaId, txtProgramadaUsuario, txtProgramadaOrigen, txtProgramadaDestino;
    @FXML private TextField txtProgramadaMonto, txtProgramadaFecha;
    @FXML private ChoiceBox<TipoTransaccion> cmbProgramadaTipo;
    @FXML private ChoiceBox<Frecuencia> cmbFrecuencia;
    @FXML private TextField txtBeneficioId, txtBeneficioNombre, txtBeneficioDescripcion, txtBeneficioPuntos, txtBeneficioBono;

    @FXML private TableColumn<Transaccion, LocalDateTime> colUltimaFecha, colTxFecha;
    @FXML private TableColumn<Transaccion, TipoTransaccion> colUltimoTipo, colTxTipo;
    @FXML private TableColumn<Transaccion, Number> colUltimoMonto, colTxMonto;
    @FXML private TableColumn<Transaccion, String> colUltimoEstado, colTxId, colTxUsuario, colTxOrigen, colTxDestino, colTxRiesgo, colTxEstado;
    @FXML private TableColumn<Usuario, String> colUsuarioId, colUsuarioNombre, colUsuarioCorreo, colUsuarioNivel, colUsuarioActivo;
    @FXML private TableColumn<Usuario, Number> colUsuarioPuntos;
    @FXML private TableColumn<Billetera, Number> colBilleteraCodigo, colBilleteraSaldo;
    @FXML private TableColumn<Billetera, String> colBilleteraUsuario, colBilleteraNombre, colBilleteraTipo, colBilleteraEstado;
    @FXML private TableColumn<OperacionProgramada, String> colProgId, colProgUsuario, colProgTipo, colProgFrecuencia, colProgEstado;
    @FXML private TableColumn<OperacionProgramada, LocalDateTime> colProgFecha;
    @FXML private TableColumn<OperacionProgramada, Number> colProgValor;
    @FXML private TableColumn<Beneficio, String> colBenId, colBenNombre, colBenActivo;
    @FXML private TableColumn<Beneficio, Number> colBenPuntos, colBenBono;
    @FXML private TableColumn<CanjeBeneficio, String> colCanjeCodigo, colCanjeUsuario, colCanjeNombre, colCanjeEstado;
    @FXML private TableColumn<CanjeBeneficio, Number> colCanjeBono;
    @FXML private TableColumn<EventoAuditoria, LocalDateTime> colAudFecha;
    @FXML private TableColumn<EventoAuditoria, String> colAudUsuario, colAudTx, colAudMotivo;
    @FXML private TableColumn<EventoAuditoria, NivelRiesgo> colAudNivel;
    @FXML private TableColumn<Alerta, LocalDateTime> colAlertaFecha;
    @FXML private TableColumn<Alerta, String> colAlertaUsuario, colAlertaTipo, colAlertaMensaje, colAlertaAtendida;

    private ContextoAplicacion contexto;
    private FintechController sistema;
    private Stage escenario;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private void initialize() {
        configurarColumnas();
        cmbBilleteraTipo.setItems(FXCollections.observableArrayList(TipoBilletera.values()));
        cmbBilleteraTipo.getSelectionModel().select(TipoBilletera.AHORRO);
        cmbProgramadaTipo.setItems(FXCollections.observableArrayList(
                TipoTransaccion.RECARGA, TipoTransaccion.RETIRO,
                TipoTransaccion.TRANSFERENCIA_INTERNA, TipoTransaccion.TRANSFERENCIA_EXTERNA));
        cmbProgramadaTipo.getSelectionModel().select(TipoTransaccion.RECARGA);
        cmbFrecuencia.setItems(FXCollections.observableArrayList(Frecuencia.values()));
        cmbFrecuencia.getSelectionModel().select(Frecuencia.UNICA);
        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, old, user) -> {
            if (user != null) {
                txtUsuarioId.setText(user.getId());
                txtUsuarioNombre.setText(user.getNombre());
                txtUsuarioCorreo.setText(user.getCorreo());
            }
        });
        tablaBilleteras.getSelectionModel().selectedItemProperty().addListener((obs, old, wallet) -> {
            if (wallet != null) {
                txtBilleteraUsuario.setText(wallet.getIdUsuario());
                txtBilleteraCodigo.setText(String.valueOf(wallet.getCodigo()));
                txtBilleteraNombre.setText(wallet.getNombre());
                txtBilleteraSaldo.setText(String.valueOf(wallet.getSaldo()));
                cmbBilleteraTipo.setValue(wallet.getTipo());
            }
        });
        tablaBeneficios.getSelectionModel().selectedItemProperty().addListener((obs, old, benefit) -> {
            if (benefit != null) {
                txtBeneficioId.setText(benefit.getId());
                txtBeneficioNombre.setText(benefit.getNombre());
                txtBeneficioDescripcion.setText(benefit.getDescripcion());
                txtBeneficioPuntos.setText(String.valueOf(benefit.getCostoPuntos()));
                txtBeneficioBono.setText(String.valueOf(benefit.getValorBono()));
            }
        });
    }

    public void configurar(ContextoAplicacion contexto, Stage escenario) {
        this.contexto = contexto;
        this.sistema = contexto.getSistema();
        this.escenario = escenario;
        lblArchivoDatos.setText(contexto.getPersistencia().getArchivo().toString());
        refrescarTodo();
    }

    private void configurarColumnas() {
        colUsuarioId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colUsuarioNombre.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nombre"));
        colUsuarioCorreo.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("correo"));
        colUsuarioPuntos.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("puntosAcumulados"));
        colUsuarioNivel.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nivel"));
        colUsuarioActivo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActivo() ? "ACTIVO" : "BAJA"));

        colBilleteraCodigo.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("codigo"));
        colBilleteraUsuario.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("idUsuario"));
        colBilleteraNombre.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nombre"));
        colBilleteraTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo().name()));
        colBilleteraSaldo.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("saldo"));
        colBilleteraEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActivo() ? "ACTIVA" : "INACTIVA"));

        colTxId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colTxUsuario.setCellValueFactory(c -> new SimpleStringProperty(obtenerUsuarioOrigen(c.getValue())));
        colTxFecha.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("fechaHora"));
        colTxTipo.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("tipo"));
        colTxMonto.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("valor"));
        colTxOrigen.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("billeteraOrigen"));
        colTxDestino.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("billeteraDestino"));
        colTxRiesgo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNivelRiesgo().name()));
        colTxEstado.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("estado"));
        colUltimaFecha.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("fechaHora"));
        colUltimoTipo.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("tipo"));
        colUltimoMonto.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("valor"));
        colUltimoEstado.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("estado"));

        colProgId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colProgUsuario.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("idUsuario"));
        colProgFecha.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("fechaHora"));
        colProgTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo().name()));
        colProgValor.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("valor"));
        colProgFrecuencia.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFrecuencia().name()));
        colProgEstado.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("estado"));

        colBenId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        colBenNombre.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nombre"));
        colBenPuntos.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("costoPuntos"));
        colBenBono.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("valorBono"));
        colBenActivo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActivo() ? "ACTIVO" : "INACTIVO"));

        colCanjeCodigo.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("codigoRedencion"));
        colCanjeUsuario.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("idUsuario"));
        colCanjeNombre.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nombreBeneficio"));
        colCanjeEstado.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("estado"));
        colCanjeBono.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("valorBono"));

        colAudFecha.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("fecha"));
        colAudUsuario.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("idUsuario"));
        colAudTx.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("idTransaccion"));
        colAudNivel.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nivel"));
        colAudMotivo.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("motivo"));

        colAlertaFecha.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("fecha"));
        colAlertaTipo.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("tipoAlerta"));
        colAlertaMensaje.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("mensaje"));
        colAlertaAtendida.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isAtendida() ? "Atendida" : "Pendiente"));
        colAlertaUsuario.setCellValueFactory(c -> new SimpleStringProperty(buscarUsuarioDeAlerta(c.getValue())));

        // Etiquetas de color para estados, tipos y niveles
        EtiquetasTabla.aplicar(colUsuarioNivel);
        EtiquetasTabla.aplicar(colUsuarioActivo);
        EtiquetasTabla.aplicar(colBilleteraEstado);
        EtiquetasTabla.aplicar(colTxTipo);
        EtiquetasTabla.aplicar(colTxRiesgo);
        EtiquetasTabla.aplicar(colTxEstado);
        EtiquetasTabla.aplicar(colUltimoTipo);
        EtiquetasTabla.aplicar(colUltimoEstado);
        EtiquetasTabla.aplicar(colProgTipo);
        EtiquetasTabla.aplicar(colProgEstado);
        EtiquetasTabla.aplicar(colCanjeEstado);
        EtiquetasTabla.aplicar(colAudNivel);
        EtiquetasTabla.aplicar(colAlertaAtendida);
    }

    private String obtenerUsuarioOrigen(Transaccion tx) {
        try {
            Billetera origen = sistema.buscarBilletera(Integer.parseInt(tx.getBilleteraOrigen()));
            return origen == null ? "" : origen.getIdUsuario();
        } catch (RuntimeException ex) { return ""; }
    }

    private String buscarUsuarioDeAlerta(Alerta alerta) {
        if (sistema == null) return "";
        for (Usuario usuario : sistema.obtenerUsuarios()) {
            for (Alerta propia : usuario.getHistorialAlertas()) {
                if (propia.getId() == alerta.getId() && propia.getFecha().equals(alerta.getFecha())) return usuario.getId();
            }
        }
        return "";
    }

    private void refrescarTodo() {
        if (sistema == null) return;
        List<Transaccion> transacciones = lista(sistema.obtenerHistorialGlobal());
        List<Usuario> usuarios = lista(sistema.obtenerUsuarios());
        List<Billetera> billeteras = lista(sistema.obtenerBilleteras());
        tablaUsuarios.setItems(FXCollections.observableArrayList(usuarios));
        tablaBilleteras.setItems(FXCollections.observableArrayList(billeteras));
        tablaTransacciones.setItems(FXCollections.observableArrayList(transacciones));
        tablaProgramadas.setItems(FXCollections.observableArrayList(lista(sistema.obtenerOperacionesPendientes())));
        tablaBeneficios.setItems(FXCollections.observableArrayList(lista(sistema.obtenerCatalogoBeneficios())));
        tablaCanjes.setItems(FXCollections.observableArrayList(lista(sistema.obtenerHistorialCanjes())));
        tablaAuditoria.setItems(FXCollections.observableArrayList(lista(sistema.obtenerHistorialAuditoria())));
        tablaAlertas.setItems(FXCollections.observableArrayList(lista(sistema.obtenerHistorialAlertasGlobal())));

        ArrayList<Transaccion> recientes = new ArrayList<>();
        for (int i = Math.max(0, transacciones.size() - 8); i < transacciones.size(); i++) recientes.add(transacciones.get(i));
        java.util.Collections.reverse(recientes);
        tablaUltimasTransacciones.setItems(FXCollections.observableArrayList(recientes));

        long saldo = 0;
        for (Billetera billetera : billeteras) saldo += billetera.getSaldo();
        lblTotalUsuarios.setText(String.valueOf(usuarios.stream().filter(Usuario::isActivo).count()));
        lblTotalBilleteras.setText(String.valueOf(billeteras.stream().filter(Billetera::isActivo).count()));
        lblTotalTransacciones.setText(String.valueOf(transacciones.size()));
        lblSaldoGlobal.setText(moneda(saldo));
        if (lblRutaRespaldo != null) lblRutaRespaldo.setText("Autoguardado local: " + contexto.getPersistencia().getArchivo());
    }

    @FXML private void crearUsuario() {
        if (sistema.registrarUsuario(txtUsuarioId.getText(), txtUsuarioNombre.getText(), txtUsuarioCorreo.getText()))
            mutacion("Usuario registrado correctamente.");
        else aviso("No se pudo registrar el usuario. Revisa la identificación y los datos obligatorios.");
    }

    @FXML private void buscarUsuario() {
        String consulta = txtBuscarUsuario.getText() == null ? "" : txtBuscarUsuario.getText().trim();
        if (consulta.isEmpty()) { verTodosUsuarios(); return; }
        List<Usuario> resultado = new ArrayList<>();
        Usuario exacto = sistema.buscarUsuario(consulta);          // búsqueda O(1) en la tabla hash
        if (exacto != null) {
            resultado.add(exacto);
        } else {
            String patron = consulta.toLowerCase();
            for (Usuario usuario : lista(sistema.obtenerUsuarios())) {
                if (usuario.getNombre().toLowerCase().contains(patron)
                        || usuario.getCorreo().toLowerCase().contains(patron)
                        || usuario.getId().toLowerCase().contains(patron)) {
                    resultado.add(usuario);
                }
            }
        }
        tablaUsuarios.setItems(FXCollections.observableArrayList(resultado));
        if (resultado.isEmpty()) aviso("No se encontraron usuarios para «" + consulta + "».");
    }

    @FXML private void verTodosUsuarios() {
        txtBuscarUsuario.clear();
        tablaUsuarios.setItems(FXCollections.observableArrayList(lista(sistema.obtenerUsuarios())));
    }

    @FXML private void abrirRendimiento() {
        App.mostrarVentana(escenario, contexto, "/fxml/rendimiento.fxml", "NEXO · Comparación de estructuras", 1320, 900,
                (controller, stage) -> ((RendimientoViewController) controller).configurar(contexto, stage));
    }

    @FXML private void modificarUsuario() {
        if (sistema.modificarUsuario(txtUsuarioId.getText(), txtUsuarioNombre.getText(), txtUsuarioCorreo.getText()))
            mutacion("Datos del usuario actualizados.");
        else aviso("No se pudo modificar. Comprueba que el usuario esté activo.");
    }

    @FXML private void eliminarUsuario() {
        if (confirmar("Dar de baja al usuario", "Se desactivarán sus billeteras y futuras operaciones. El historial se conservará.")) {
            if (sistema.eliminarUsuario(txtUsuarioId.getText())) mutacion("Usuario dado de baja; el historial se conservó.");
            else aviso("No existe un usuario activo con esa identificación.");
        }
    }

    @FXML private void crearBilletera() {
        try {
            boolean creada = sistema.crearBilleteraAUsuario(txtBilleteraUsuario.getText(), entero(txtBilleteraCodigo),
                    txtBilleteraNombre.getText(), cmbBilleteraTipo.getValue(), entero(txtBilleteraSaldo));
            if (creada) mutacion("Billetera creada."); else aviso("No se pudo crear. Verifica el usuario, el código único y el saldo.");
        } catch (RuntimeException ex) { aviso("Datos inválidos: " + ex.getMessage()); }
    }

    @FXML private void cambiarEstadoBilletera() {
        Billetera billetera = tablaBilleteras.getSelectionModel().getSelectedItem();
        if (billetera == null) { aviso("Selecciona una billetera de la tabla."); return; }
        boolean nueva = !billetera.isActivo();
        if (sistema.cambiarEstadoBilletera(billetera.getIdUsuario(), billetera.getCodigo(), nueva))
            mutacion(nueva ? "Billetera activada." : "Billetera suspendida.");
        else aviso("No se pudo cambiar el estado de la billetera.");
    }

    @FXML private void recargarBilletera() {
        ejecutarOperacion(() -> sistema.realizarRecarga(entero(txtOperacionOrigen), entero(txtOperacionMonto), txtOperacionUsuario.getText()), "Recarga realizada.");
    }

    @FXML private void retirarBilletera() {
        ejecutarOperacion(() -> sistema.realizarRetiro(entero(txtOperacionOrigen), entero(txtOperacionMonto), txtOperacionUsuario.getText()), "Retiro realizado.");
    }

    @FXML private void transferir() {
        ejecutarOperacion(() -> sistema.realizarTransferencia(entero(txtOperacionOrigen), entero(txtOperacionDestino),
                entero(txtOperacionMonto), txtOperacionUsuario.getText()), "Transferencia realizada.");
    }

    private void ejecutarOperacion(java.util.function.BooleanSupplier operacion, String exito) {
        try {
            if (operacion.getAsBoolean()) mutacion(exito);
            else aviso("La operación fue rechazada. Revisa la propiedad de la billetera, el saldo y el monto.");
        } catch (RuntimeException ex) { aviso("No se pudo ejecutar: " + ex.getMessage()); }
    }

    @FXML private void revertir() {
        if (!confirmar("Revertir última operación", "Se intentará revertir la operación exitosa más reciente (pila LIFO).")) return;
        if (sistema.revertirUltimaOperacion()) mutacion("Última operación revertida.");
        else aviso("No fue posible revertir. Puede no haber operaciones o los fondos/puntos ya no estar disponibles.");
    }

    @FXML private void programarOperacion() {
        try {
            LocalDateTime fecha = LocalDateTime.parse(txtProgramadaFecha.getText().trim());
            int destino = txtProgramadaDestino.getText() == null || txtProgramadaDestino.getText().isBlank()
                    ? 0 : entero(txtProgramadaDestino);
            String idUsuario = txtProgramadaUsuario.getText() == null ? "" : txtProgramadaUsuario.getText().trim();
            Billetera origen = sistema.buscarBilletera(entero(txtProgramadaOrigen));
            if (origen == null || idUsuario.isEmpty() || !idUsuario.equals(origen.getIdUsuario())) {
                aviso("El usuario indicado debe ser el propietario de la billetera de origen.");
                return;
            }
            boolean creada = sistema.programarOperacion(txtProgramadaId.getText(), entero(txtProgramadaMonto),
                    origen.getCodigo(), destino, fecha, cmbProgramadaTipo.getValue(),
                    cmbFrecuencia.getValue(), "PENDIENTE");
            if (creada) mutacion("Operación programada en la cola de prioridad.");
            else aviso("No se pudo programar. Verifica las billeteras, el usuario y la fecha.");
        } catch (RuntimeException ex) { aviso("Fecha o datos inválidos. Usa AAAA-MM-DDTHH:MM. " + ex.getMessage()); }
    }

    @FXML private void crearBeneficio() {
        try {
            boolean creado = sistema.registrarBeneficio(txtBeneficioId.getText(), txtBeneficioNombre.getText(),
                    txtBeneficioDescripcion.getText(), entero(txtBeneficioPuntos), entero(txtBeneficioBono));
            if (creado) mutacion("Cupón registrado en el catálogo.");
            else aviso("No se pudo crear el cupón. El ID debe ser único y los valores válidos.");
        } catch (RuntimeException ex) { aviso("Datos del cupón inválidos: " + ex.getMessage()); }
    }

    @FXML private void modificarBeneficio() {
        try {
            boolean ok = sistema.modificarBeneficio(txtBeneficioId.getText(), txtBeneficioNombre.getText(),
                    txtBeneficioDescripcion.getText(), entero(txtBeneficioPuntos), entero(txtBeneficioBono));
            if (ok) mutacion("Cupón actualizado. Los canjes anteriores conservan sus condiciones originales.");
            else aviso("No se pudo actualizar. Selecciona un cupón e ingresa un abono positivo.");
        } catch (RuntimeException ex) { aviso("Datos del cupón inválidos: " + ex.getMessage()); }
    }

    @FXML private void cambiarEstadoBeneficio() {
        Beneficio beneficio = tablaBeneficios.getSelectionModel().getSelectedItem();
        if (beneficio == null) { aviso("Selecciona un cupón."); return; }
        boolean activar = !beneficio.isActivo();
        boolean ok = activar ? sistema.activarBeneficio(beneficio.getId()) : sistema.desactivarBeneficio(beneficio.getId());
        if (ok) mutacion(activar ? "Cupón activado." : "Cupón desactivado.");
        else aviso("No se pudo actualizar el cupón.");
    }

    @FXML private void despacharNotificacion() {
        Alerta alerta = sistema.despacharSiguienteNotificacion();
        if (alerta == null) aviso("No hay notificaciones pendientes.");
        else mutacion("Notificación atendida: " + alerta.getMensaje());
    }

    @FXML private void actualizarAnalitica() {
        LocalDateTime desde = LocalDateTime.now().minusDays(30);
        LocalDateTime hasta = LocalDateTime.now().plusSeconds(1);
        var analitica = sistema.getAnalitica();
        StringBuilder texto = new StringBuilder();
        texto.append("RESUMEN DE LOS ÚLTIMOS 30 DÍAS\n\n");
        texto.append("Monto movilizado: ").append(moneda(analitica.obtenerMontoTotalMovilizado(desde, hasta))).append('\n');
        texto.append("Billetera más usada: ").append(analitica.obtenerBilleteraMasUsada() == null
                ? "Sin actividad" : analitica.obtenerBilleteraMasUsada().getCodigo()).append('\n');
        texto.append("Categoría más activa: ").append(analitica.obtenerCategoriaMasActiva()).append('\n');
        texto.append("Usuario más activo: ").append(analitica.obtenerUsuarioMasActivo(desde, hasta) == null
                ? "Sin actividad" : analitica.obtenerUsuarioMasActivo(desde, hasta).getNombre()).append("\n\n");
        texto.append("FRECUENCIA POR TIPO\n");
        int[] frecuencias = analitica.obtenerFrecuenciaPorTipo();
        TipoTransaccion[] tipos = TipoTransaccion.values();
        for (int i = 0; i < Math.min(frecuencias.length, tipos.length); i++)
            texto.append(" • ").append(tipos[i]).append(": ").append(frecuencias[i]).append('\n');
        texto.append("\nUSUARIOS CON MAYOR ACTIVIDAD\n");
        for (Usuario usuario : analitica.obtenerUsuariosMasActivos(5, desde, hasta))
            texto.append(" • ").append(usuario.getNombre()).append(" ( ").append(usuario.getId()).append(" )\n");
        texto.append("\nTRANSACCIONES DE MAYOR VALOR\n");
        for (Transaccion tx : analitica.obtenerTransaccionesMayorValor(5))
            texto.append(" • ").append(tx.getId()).append(" · ").append(tx.getTipo()).append(" · ")
                    .append(moneda(Math.round(tx.getValor()))).append('\n');
        texto.append("\nRUTAS FRECUENTES\n");
        for (var ruta : analitica.obtenerRutasFrecuentes(5)) texto.append(" • ").append(ruta).append('\n');
        txtAnalitica.setText(texto.toString());
    }

    @FXML private void detectarCiclos() {
        var ciclos = sistema.getAnalitica().detectarCiclos();
        txtAnalitica.setText(ciclos.isEmpty() ? "No se detectaron ciclos en la red de transferencias."
                : "CICLOS DETECTADOS EN EL GRAFO\n\n" + String.join("\n", lista(ciclos)));
    }

    @FXML private void exportarRespaldo() {
        try {
            Path carpeta = Path.of(System.getProperty("user.home"), ".fintech-uniquindio", "respaldos");
            java.nio.file.Files.createDirectories(carpeta);
            Path archivo = carpeta.resolve("fintech-backup-" + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".tsv");
            sistema.guardarRespaldoSistema(archivo.toString());
            lblRutaRespaldo.setText("Respaldo TSV exportado: " + archivo);
            mostrar(Alert.AlertType.INFORMATION, "Respaldo exportado", archivo.toString());
        } catch (IOException | RuntimeException ex) { aviso("No se pudo exportar el respaldo: " + ex.getMessage()); }
    }

    @FXML private void guardarAhora() {
        try { contexto.guardar(); mostrar(Alert.AlertType.INFORMATION, "Datos guardados", "El estado completo quedó guardado en:\n" + contexto.getPersistencia().getArchivo()); }
        catch (IOException ex) { aviso("No se pudo guardar: " + ex.getMessage()); }
    }

    @FXML private void recargarRespaldo() {
        if (!confirmar("Recargar datos", "Se reemplazará la sesión actual con el último respaldo local.")) return;
        try { contexto.recargar(); sistema = contexto.getSistema(); refrescarTodo(); aviso("Respaldo restaurado correctamente."); }
        catch (IOException | RuntimeException ex) { aviso("No se pudo restaurar el respaldo: " + ex.getMessage()); }
    }

    @FXML private void cerrarSesion() {
        App.mostrarVentana(escenario, contexto, "/fxml/inicio.fxml", "NEXO · Billetera digital", 1120, 760,
                (controller, stage) -> ((LoginViewController) controller).configurar(contexto, stage));
    }

    private void mutacion(String mensaje) {
        try { contexto.guardar(); } catch (IOException ex) { aviso("La operación se realizó, pero falló el guardado automático: " + ex.getMessage()); }
        refrescarTodo();
        mostrar(Alert.AlertType.INFORMATION, "Operación completada", mensaje);
    }

    private int entero(TextField campo) { return Integer.parseInt(campo.getText().trim()); }
    private String moneda(long valor) { return "$ " + String.format(java.util.Locale.forLanguageTag("es-CO"), "%,d", valor); }
    private boolean confirmar(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.CANCEL, ButtonType.OK);
        alerta.setTitle(titulo); alerta.setHeaderText(null);
        return alerta.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
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
