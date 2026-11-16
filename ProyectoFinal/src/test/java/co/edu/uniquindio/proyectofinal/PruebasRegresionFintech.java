package co.edu.uniquindio.proyectofinal;

import co.edu.uniquindio.proyectofinal.Estructuras.Arbol.ArbolBinario;
import co.edu.uniquindio.proyectofinal.Estructuras.Controllers.FintechController;
import co.edu.uniquindio.proyectofinal.Estructuras.Colas.Cola;
import co.edu.uniquindio.proyectofinal.Estructuras.Colas.ColaPrioridad;
import co.edu.uniquindio.proyectofinal.Estructuras.Hash.TablaHash;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.*;
import co.edu.uniquindio.proyectofinal.Estructuras.Servicios.ComparadorRendimiento;
import co.edu.uniquindio.proyectofinal.Estructuras.Servicios.DetectorRiesgo;
import co.edu.uniquindio.proyectofinal.Estructuras.Pilas.Stack;
import co.edu.uniquindio.proyectofinal.Estructuras.SimpleList.SimpleLinkedList;

import java.time.LocalDateTime;

/**
 * Suite de regresión ejecutable sin bibliotecas externas.
 * Ejecutar con java -ea co.edu.uniquindio.proyectofinal.PruebasRegresionFintech.
 */
public final class PruebasRegresionFintech {

    private static int pruebasEjecutadas;

    private PruebasRegresionFintech() { }

    public static void main(String[] args) {
        probarEstructurasLineales();
        probarTablaHashYArbolAvl();
        probarRegistroTransferenciasYReversion();
        probarProgramacionYNotificaciones();
        probarRecompensasYReversionConPuntosGastados();
        probarAnaliticaYGrafo();
        probarDetectorDeRiesgo();
        probarComparacionDeRendimiento();
        probarRespaldoSistema();
        probarEncapsulacionDeConsultas();
        probarRechazosYCancelacionProgramada();
        probarRecurrenciaMensual();
        probarActualizacionesAtomicas();
        probarCatalogoYFlujoCompletoDeCanjes();
        probarUmbralesDeNivelesYPuntosPorTipo();
        probarFechaRealDeEjecucionProgramada();
        probarConsultasDeLaFachada();
        probarDatosDeDemostracionIntegrados();
        System.out.println("\nPRUEBAS DE REGRESIÓN SUPERADAS: " + pruebasEjecutadas);
    }

    private static void probarEstructurasLineales() {
        Cola<String> cola = new Cola<>();
        cola.encolar("primero");
        cola.encolar("segundo");
        comprobar("primero".equals(cola.desencolar()), "cola conserva FIFO");
        comprobar("segundo".equals(cola.desencolar()), "cola retira el siguiente FIFO");

        Stack<Integer> pila = new Stack<>();
        pila.push(10);
        pila.push(20);
        comprobar(Integer.valueOf(20).equals(pila.pop()), "pila conserva LIFO");
        comprobar(Integer.valueOf(10).equals(pila.pop()), "pila retira el elemento anterior");

        ColaPrioridad<String> prioridad = new ColaPrioridad<>((a, b) -> Integer.compare(
                Integer.parseInt(a.substring(0, 1)), Integer.parseInt(b.substring(0, 1))));
        prioridad.encolar("2-A");
        prioridad.encolar("1-A");
        prioridad.encolar("1-B");
        comprobar("1-A".equals(prioridad.desencolar()), "cola de prioridad ordena por prioridad");
        comprobar("1-B".equals(prioridad.desencolar()), "cola de prioridad mantiene orden estable en empate");
        comprobar("2-A".equals(prioridad.desencolar()), "cola de prioridad retira el último nivel");
    }

    private static void probarTablaHashYArbolAvl() {
        TablaHash<Integer, Integer> hash = new TablaHash<>(2);
        for (int i = 0; i < 2_000; i++) hash.insertar(i, i * 3);
        for (int i = 0; i < 2_000; i++) comprobar(Integer.valueOf(i * 3).equals(hash.buscar(i)), "búsqueda hash " + i);
        comprobar(hash.getCapacidad() > 2, "redimensionamiento hash");
        comprobar(hash.eliminar(17), "eliminación hash");
        comprobar(!hash.contieneClave(17), "clave eliminada hash");

        ArbolBinario<Usuario> avl = new ArbolBinario<>();
        for (int i = 0; i < 10_000; i++) {
            String id = String.format("USR-%05d", i);
            avl.insertar(new Usuario(id, "Usuario", "u@demo.co"));
        }
        comprobar(avl.getPeso() == 10_000, "peso AVL después de insertar");
        comprobar(avl.obtenerAltura() <= 20, "altura AVL balanceado");
        for (int i = 0; i < 5_000; i++) {
            String id = String.format("USR-%05d", i);
            avl.eliminar(new Usuario(id, "Usuario", "u@demo.co"));
        }
        comprobar(avl.getPeso() == 5_000 && avl.obtenerAltura() <= 20, "AVL después de eliminar");
    }

    private static void probarRegistroTransferenciasYReversion() {
        FintechController sistema = new FintechController();
        comprobar(sistema.registrarUsuario("  A  ", "Ana", "ana@demo.co"), "registro con espacios");
        comprobar(sistema.registrarUsuario("B", "Beto", "beto@demo.co"), "registro B");
        comprobar(sistema.crearBilleteraAUsuario(" A ", 101, "Principal", TipoBilletera.GASTO_DIARIO, 50_000),
                "billetera con id normalizado");
        comprobar(sistema.buscarBilletera(101).getIdUsuario().equals("A"), "dueño de billetera canónico");
        comprobar(sistema.crearBilleteraAUsuario("B", 201, "Ahorro", TipoBilletera.AHORRO, 0), "billetera B");
        comprobar(sistema.crearBilleteraAUsuario("A", 102, "Ahorro propio", TipoBilletera.AHORRO, 0),
                "segunda billetera del mismo usuario");
        comprobar(sistema.realizarTransferencia(101, 102, 500, "A"), "transferencia interna válida");
        comprobar(sistema.obtenerHistorialGlobal().getNodeValue(0).getTipo() == TipoTransaccion.TRANSFERENCIA_INTERNA,
                "tipo de transferencia interna correcto");
        comprobar(sistema.revertirUltimaOperacion(), "reversión de transferencia interna");
        comprobar(sistema.buscarBilletera(101).getSaldo() == 50_000
                        && sistema.buscarBilletera(102).getSaldo() == 0,
                "saldos restaurados en transferencia interna");

        comprobar(!sistema.realizarTransferencia(101, 201, 100_000, "A"), "transferencia con saldo insuficiente rechazada");
        comprobar(sistema.buscarBilletera(101).getSaldo() == 50_000, "saldo intacto tras rechazo");
        comprobar(sistema.realizarTransferencia(101, 201, 10_000, "A"), "transferencia externa válida");
        SimpleLinkedList<Transaccion> historial = sistema.obtenerHistorialGlobal();
        Transaccion transferencia = historial.getNodeValue(historial.getSize() - 1);
        comprobar(transferencia.getTipo() == TipoTransaccion.TRANSFERENCIA_EXTERNA, "tipo externo correcto");
        comprobar(sistema.existeRutaTransferencias("A", "B"), "ruta grafos después de transferir");
        comprobar(sistema.revertirUltimaOperacion(), "reversión transferencia");
        comprobar(sistema.buscarBilletera(101).getSaldo() == 50_000, "saldo origen restaurado");
        comprobar(sistema.buscarBilletera(201).getSaldo() == 0, "saldo destino restaurado");
        Transaccion transferenciaActualizada = sistema.obtenerHistorialGlobal()
                .getNodeValue(sistema.obtenerHistorialGlobal().getSize() - 1);
        comprobar(Transaccion.REVERTIDA.equals(transferenciaActualizada.getEstado()),
                "estado de transacción revertida visible en una nueva consulta");
        comprobar(Transaccion.EXITOSA.equals(transferencia.getEstado()),
                "consulta previa no expone la transacción interna");
        comprobar(!sistema.existeRutaTransferencias("A", "B"), "arista eliminada tras reversión");

        comprobar(sistema.realizarTransferencia(101, 101, 100, "A") == false,
                "no permite transferencia a la misma billetera");
        comprobar(sistema.buscarBilletera(101).getSaldo() == 50_000, "saldo no cambia ante transferencia inválida");
    }

    private static void probarProgramacionYNotificaciones() {
        FintechController sistema = new FintechController();
        comprobar(sistema.registrarUsuario("P", "Pat", "pat@demo.co"), "registro programaciones");
        comprobar(sistema.crearBilleteraAUsuario("P", 500, "Cuenta", TipoBilletera.AHORRO, 10_000), "billetera programaciones");
        LocalDateTime base = LocalDateTime.of(2030, 1, 1, 12, 0);

        comprobar(sistema.programarOperacion("P-2", 200, 500, 0, base.plusMinutes(8),
                TipoTransaccion.RECARGA, Frecuencia.UNICA, "PENDIENTE"), "programación más tardía");
        comprobar(sistema.programarOperacion("P-1", 100, 500, 0, base.plusMinutes(3),
                TipoTransaccion.RECARGA, Frecuencia.UNICA, "PENDIENTE"), "programación más próxima");
        comprobar(!sistema.programarOperacion("P-1", 100, 500, 0, base.plusMinutes(9),
                TipoTransaccion.RECARGA, Frecuencia.UNICA, "PENDIENTE"), "id programado duplicado rechazado");

        comprobar(sistema.notificarOperacionesProximas(base) == 2, "alertas de operaciones próximas");
        comprobar(sistema.notificarOperacionesProximas(base) == 0, "no duplica alertas de proximidad");
        comprobar(sistema.procesarOperacionesProgramadas(base.plusMinutes(9)) == 2, "procesa operaciones vencidas");
        SimpleLinkedList<OperacionProgramada> procesadas = sistema.obtenerOperacionesProcesadas();
        comprobar(procesadas.getNodeValue(0).getId().equals("P-1"), "cola de prioridad ordena por fecha");
        comprobar(procesadas.getNodeValue(1).getId().equals("P-2"), "segunda prioridad por fecha");
        comprobar(!sistema.programarOperacion("P-1", 100, 500, 0, base.plusDays(1),
                TipoTransaccion.RECARGA, Frecuencia.UNICA, "PENDIENTE"), "id usado permanece reservado");

        comprobar(sistema.programarOperacion("P-3", 50, 500, 0, base.minusMinutes(1),
                TipoTransaccion.RECARGA, Frecuencia.SEMANAL, "PENDIENTE"), "programación semanal");
        comprobar(sistema.procesarOperacionesProgramadas(base) == 1, "ejecución semanal inicial");
        comprobar(sistema.getCantidadOperacionesProgramadasPendientes() == 1, "reprogramación semanal pendiente");
        comprobar(sistema.notificarOperacionesProximas(base.plusDays(7).minusMinutes(5)) == 1,
                "alerta de la siguiente ocurrencia semanal");

        Alerta primera = sistema.despacharSiguienteNotificacion();
        comprobar(primera != null && primera.isAtendida(), "despacho FIFO marca atendida");
    }

    private static void probarRecompensasYReversionConPuntosGastados() {
        FintechController sistema = new FintechController();
        comprobar(sistema.registrarUsuario("R", "Rita", "rita@demo.co"), "registro recompensas");
        comprobar(sistema.crearBilleteraAUsuario("R", 700, "Principal", TipoBilletera.GASTO_DIARIO, 0), "billetera recompensas");
        comprobar(sistema.realizarRecarga(700, 100_000, "R"), "recarga puntos");
        Usuario usuario = sistema.buscarUsuario("R");
        comprobar(usuario.getPuntosAcumulados() == 1_000 && "Plata".equals(usuario.getNivel()), "puntos y nivel Plata");
        CanjeBeneficio canje = sistema.canjearBeneficio("R", "BEN-001");
        usuario = sistema.buscarUsuario("R");
        comprobar(canje != null && usuario.getPuntosAcumulados() == 750, "canje descuenta puntos");
        comprobar(usuario.getHistorialCanjes().getSize() == 1, "historial individual de canjes");
        comprobar(!sistema.revertirUltimaOperacion(), "no revierte si puntos ya se gastaron");
        comprobar(sistema.buscarBilletera(700).getSaldo() == 100_000, "reversión bloqueada no altera saldo");
        comprobar(usuario.getPuntosAcumulados() == 750, "reversión bloqueada no altera puntos");
    }

    private static void probarAnaliticaYGrafo() {
        FintechController sistema = new FintechController();
        String[] ids = {"A", "B", "C"};
        int[] codigos = {801, 802, 803};
        for (int i = 0; i < ids.length; i++) {
            comprobar(sistema.registrarUsuario(ids[i], "Usuario " + ids[i], ids[i].toLowerCase() + "@demo.co"), "registro grafo " + ids[i]);
            comprobar(sistema.crearBilleteraAUsuario(ids[i], codigos[i], "Cuenta", TipoBilletera.AHORRO, 10_000), "billetera grafo " + ids[i]);
        }
        comprobar(sistema.realizarTransferencia(801, 802, 100, "A"), "A a B");
        comprobar(sistema.realizarTransferencia(802, 803, 200, "B"), "B a C");
        comprobar(sistema.realizarTransferencia(803, 801, 300, "C"), "C a A");

        LocalDateTime desde = LocalDateTime.now().minusMinutes(1);
        LocalDateTime hasta = LocalDateTime.now().plusMinutes(1);
        comprobar(sistema.getAnalitica().obtenerMontoTotalMovilizado(desde, hasta) == 600, "monto total del periodo");
        comprobar(sistema.getAnalitica().obtenerTransaccionesMayorValor(2).getNodeValue(0).getValor() == 300,
                "top de transacciones ordenado por valor");
        comprobar(!sistema.getAnalitica().detectarCiclos().isEmpty(), "detección de ciclo en grafo");
        comprobar(sistema.getAnalitica().obtenerRutasFrecuentes(5).getSize() == 3, "rutas frecuentes");
        comprobar(sistema.getAnalitica().obtenerUsuariosMasActivos(3, desde, hasta).getSize() == 3,
                "ranking de actividad por periodo");

        comprobar(sistema.eliminarUsuario("B"), "baja lógica de usuario");
        comprobar(sistema.buscarUsuario("B") == null, "usuario dado de baja no opera");
        comprobar(sistema.getAnalitica().obtenerUsuariosMasActivos(3, desde, hasta).getSize() == 3,
                "la analítica conserva actividad histórica tras baja lógica");
    }

    private static void probarDetectorDeRiesgo() {
        DetectorRiesgo detector = new DetectorRiesgo();
        LocalDateTime ahora = LocalDateTime.of(2030, 2, 1, 10, 0);
        DetectorRiesgo.Evaluacion alto = detector.evaluar("R", 6_000_000, true, 10, 20, ahora);
        comprobar(alto.getNivel() == NivelRiesgo.ALTO, "monto atípicamente alto");

        detector.registrarOperacion("R", 1_000, true, 10, 20, ahora);
        DetectorRiesgo.Evaluacion rapido = detector.evaluar("R", 1_000, true, 10, 20, ahora.plusSeconds(30));
        comprobar(rapido.getNivel() == NivelRiesgo.ALTO, "repetición de destino y operaciones consecutivas generan riesgo alto");
        comprobar(rapido.getMotivo().contains("mismo destino"), "destino repetido registrado");

        EventoAuditoria evento = detector.registrarEvento("R", "TX-SEG", rapido, ahora.plusSeconds(30));
        comprobar(evento.getNivel() == rapido.getNivel(), "evento guardado con nivel de riesgo correcto");
        comprobar(detector.obtenerHistorialAuditoria().getSize() == 1, "historial de auditoría consultable");
        comprobar(detector.obtenerEventosPorUsuario("R").getSize() == 1,
                "consulta de auditoría filtrada por usuario");
        comprobar(detector.obtenerEventosPorNivel(NivelRiesgo.ALTO).getSize() == 1,
                "consulta de auditoría filtrada por nivel");

        LocalDateTime base = ahora.plusHours(2);
        DetectorRiesgo promedio = new DetectorRiesgo();
        for (int i = 0; i < 3; i++) {
            promedio.registrarOperacion("AVG", 1_000, true, 10, null, base.plusMinutes(i));
        }
        DetectorRiesgo.Evaluacion montoInusual = promedio.evaluar("AVG", 4_001, true, 10, null,
                base.plusMinutes(4));
        comprobar(montoInusual.getNivel() == NivelRiesgo.MEDIO
                        && montoInusual.getMotivo().contains("promedio"),
                "monto saliente superior a tres veces el promedio genera riesgo");

        DetectorRiesgo fragmentacion = new DetectorRiesgo();
        fragmentacion.registrarOperacion("FRAG", 500, true, 31, null, base);
        fragmentacion.registrarOperacion("FRAG", 500, true, 32, null, base.plusSeconds(30));
        DetectorRiesgo.Evaluacion variasBilleteras = fragmentacion.evaluar(
                "FRAG", 500, true, 33, null, base.plusSeconds(60));
        comprobar(variasBilleteras.getNivel() == NivelRiesgo.MEDIO
                        && variasBilleteras.getMotivo().contains("varias billeteras"),
                "uso de tres billeteras en intervalo corto detecta posible fragmentación");

        DetectorRiesgo frecuencia = new DetectorRiesgo();
        for (int i = 0; i < 5; i++) {
            frecuencia.registrarOperacion("FREQ", 100, true, 44, null, base.plusMinutes(i));
        }
        DetectorRiesgo.Evaluacion frecuenciaAlta = frecuencia.evaluar("FREQ", 100, true, 44, null,
                base.plusMinutes(5));
        comprobar(frecuenciaAlta.getNivel() == NivelRiesgo.MEDIO
                        && frecuenciaAlta.getMotivo().contains("frecuencia"),
                "sexta operación en menos de diez minutos detecta cambio de frecuencia");

        DetectorRiesgo horario = new DetectorRiesgo();
        for (int i = 0; i < 10; i++) {
            horario.registrarOperacion("HORA", 100, false, 50, null,
                    LocalDateTime.of(2030, 3, 1, 10, i));
        }
        DetectorRiesgo.Evaluacion horaInusual = horario.evaluar("HORA", 100, false, 50, null,
                LocalDateTime.of(2030, 3, 2, 3, 0));
        comprobar(horaInusual.getNivel() == NivelRiesgo.MEDIO
                        && horaInusual.getMotivo().contains("horario no habitual"),
                "actividad en una hora no observada antes genera riesgo medio");

        DetectorRiesgo consecutividad = new DetectorRiesgo();
        consecutividad.registrarOperacion("TIPO", 100, true, false, 60, null, base);
        DetectorRiesgo.Evaluacion retiroYTransferencia = consecutividad.evaluar(
                "TIPO", 100, true, true, 60, 61, base.plusSeconds(15));
        comprobar(retiroYTransferencia.getNivel() == NivelRiesgo.BAJO,
                "un retiro seguido de una transferencia no se confunde con dos transferencias consecutivas");

        boolean rechazoRiesgoBajo = false;
        try {
            DetectorRiesgo.Evaluacion bajo = frecuencia.evaluar("OTRO", 100, false, 90, null, base);
            detector.registrarEvento("R", "TX-BAJO", bajo, base);
        } catch (IllegalArgumentException esperado) {
            rechazoRiesgoBajo = true;
        }
        comprobar(rechazoRiesgoBajo, "auditoría rechaza registrar eventos de riesgo bajo");
    }

    private static void probarComparacionDeRendimiento() {
        SimpleLinkedList<ComparadorRendimiento.ResultadoComparacion> resultados =
                new ComparadorRendimiento().compararBusquedaUsuarios(500, 1_000);
        comprobar(resultados.getSize() == 3, "compara tres estructuras");
        for (ComparadorRendimiento.ResultadoComparacion resultado : resultados) {
            comprobar(resultado.getAciertos() == 1_000, "mismas consultas en " + resultado.getEstructura());
        }
    }

    private static void probarRespaldoSistema() {
        FintechController sistema = new FintechController();
        comprobar(sistema.registrarUsuario("BK", "Beatriz Backup", "backup@demo.co"), "registro respaldo");
        comprobar(sistema.crearBilleteraAUsuario("BK", 990, "Cuenta	principal", TipoBilletera.AHORRO, 5_000),
                "billetera respaldo");
        comprobar(sistema.realizarRecarga(990, 20_000, "BK"), "transacción respaldo");

        java.nio.file.Path archivo = null;
        try {
            archivo = java.nio.file.Files.createTempFile("fintech-backup-", ".tsv");
            sistema.guardarRespaldoSistema(archivo.toString());
            String contenido = java.nio.file.Files.readString(archivo, java.nio.charset.StandardCharsets.UTF_8);
            comprobar(contenido.contains("FINTECH_BACKUP\t1"), "respaldo contiene versión y cabecera");
            comprobar(contenido.contains("USUARIO\tBK"), "respaldo contiene usuarios");
            comprobar(contenido.contains("BILLETERA\t990"), "respaldo contiene billeteras");
            comprobar(contenido.contains("TRANSACCION\tTX-1"), "respaldo contiene transacciones");
            comprobar(contenido.contains("BENEFICIO\tBEN-001"), "respaldo contiene catálogo de beneficios");
            comprobar(contenido.contains("\\tprincipal"), "respaldo escapa tabuladores dentro de campos");
        } catch (java.io.IOException e) {
            throw new AssertionError("No se pudo validar el respaldo de prueba.", e);
        } finally {
            if (archivo != null) {
                try { java.nio.file.Files.deleteIfExists(archivo); }
                catch (java.io.IOException ignored) { }
            }
        }
    }


    private static void probarEncapsulacionDeConsultas() {
        FintechController sistema = new FintechController();
        comprobar(sistema.registrarUsuario("ENC", "Encapsulado", "enc@demo.co"), "registro encapsulación");
        comprobar(sistema.crearBilleteraAUsuario("ENC", 1500, "Cuenta", TipoBilletera.AHORRO, 10_000),
                "billetera encapsulación");
        comprobar(sistema.realizarRecarga(1500, 1_000, "ENC"), "recarga encapsulación");

        Usuario usuarioExterno = sistema.buscarUsuario("ENC");
        usuarioExterno.agregarPuntos(50_000);
        usuarioExterno.setActivo(false);
        Billetera billeteraExterna = usuarioExterno.getBilleteras().getNodeValue(0);
        billeteraExterna.recargar(5_000);
        billeteraExterna.setActivo(false);
        Transaccion transaccionExterna = sistema.obtenerHistorialGlobal().getNodeValue(0);
        transaccionExterna.marcarRevertida();

        comprobar(sistema.buscarUsuario("ENC").getPuntosAcumulados() == 10,
                "la copia de usuario no modifica los puntos reales");
        comprobar(sistema.buscarUsuario("ENC").isActivo(), "la copia no puede dar de baja al usuario");
        comprobar(sistema.buscarBilletera(1500).getSaldo() == 11_000,
                "la copia de billetera no modifica el saldo real");
        comprobar(sistema.buscarBilletera(1500).isActivo(), "la copia no puede desactivar la billetera real");
        comprobar(Transaccion.EXITOSA.equals(sistema.obtenerHistorialGlobal().getNodeValue(0).getEstado()),
                "la copia de transacción no modifica el estado real");

        Transaccion transaccionAnalitica = sistema.getAnalitica().obtenerTransaccionesMayorValor(1).getNodeValue(0);
        transaccionAnalitica.marcarRevertida();
        comprobar(Transaccion.EXITOSA.equals(sistema.obtenerHistorialGlobal().getNodeValue(0).getEstado()),
                "analítica entrega transacciones defensivas");

        Usuario usuarioAnalitica = sistema.getAnalitica().obtenerUsuariosOrdenadosPorPuntos().getNodeValue(0);
        usuarioAnalitica.agregarPuntos(100);
        comprobar(sistema.buscarUsuario("ENC").getPuntosAcumulados() == 10,
                "analítica entrega usuarios defensivos");
    }

    private static void probarRechazosYCancelacionProgramada() {
        FintechController sistema = new FintechController();
        comprobar(sistema.registrarUsuario("REJ", "Rechazos", "rechazos@demo.co"), "registro rechazos");
        comprobar(sistema.crearBilleteraAUsuario("REJ", 1600, "Cuenta", TipoBilletera.AHORRO, 1_000),
                "billetera rechazos");
        comprobar(!sistema.realizarRetiro(1600, 2_000, "REJ"), "retiro sin saldo rechazado");
        SimpleLinkedList<Transaccion> historial = sistema.obtenerHistorialGlobal();
        comprobar(historial.getSize() == 1, "rechazo válido queda en el historial global");
        comprobar(Transaccion.RECHAZADA.equals(historial.getNodeValue(0).getEstado()),
                "rechazo conserva estado RECHAZADA");
        comprobar(sistema.buscarBilletera(1600).getSaldo() == 1_000, "rechazo no altera saldo");
        comprobar(sistema.buscarUsuario("REJ").getPuntosAcumulados() == 0, "rechazo no genera puntos");
        comprobar(!sistema.revertirUltimaOperacion(), "una operación rechazada no entra a la pila de reversión");

        LocalDateTime fecha = LocalDateTime.of(2031, 4, 1, 9, 0);
        comprobar(sistema.programarOperacion("CANCEL-1", 100, 1600, 0, fecha,
                TipoTransaccion.RETIRO, Frecuencia.UNICA, "PENDIENTE"), "programa operación cancelable");
        comprobar(!sistema.cancelarOperacionProgramada("OTRO", "CANCEL-1"),
                "otro usuario no puede cancelar la programación");
        comprobar(sistema.cancelarOperacionProgramada("REJ", "CANCEL-1"),
                "el dueño puede cancelar su programación");
        comprobar(sistema.getCantidadOperacionesProgramadasPendientes() == 0,
                "cancelación retira la operación de la cola de prioridad");
        SimpleLinkedList<OperacionProgramada> procesadas = sistema.obtenerOperacionesProcesadas();
        comprobar(OperacionProgramada.CANCELADA_POR_USUARIO.equals(
                        procesadas.getNodeValue(procesadas.getSize() - 1).getEstado()),
                "cancelación queda registrada en el historial");
    }

    private static void probarRecurrenciaMensual() {
        FintechController sistema = new FintechController();
        comprobar(sistema.registrarUsuario("MES", "Mensual", "mensual@demo.co"), "registro mensual");
        comprobar(sistema.crearBilleteraAUsuario("MES", 1700, "Cuenta", TipoBilletera.AHORRO, 0),
                "billetera mensual");
        LocalDateTime enero = LocalDateTime.of(2032, 1, 31, 8, 30);
        comprobar(sistema.programarOperacion("MENSUAL-1", 100, 1700, 0, enero,
                TipoTransaccion.RECARGA, Frecuencia.MENSUAL, "PENDIENTE"), "programación mensual día 31");
        comprobar(sistema.procesarOperacionesProgramadas(enero) == 1, "primera ejecución mensual");
        LocalDateTime febrero = sistema.obtenerOperacionesPendientes().getNodeValue(0).getFechaHora();
        comprobar(febrero.equals(LocalDateTime.of(2032, 2, 29, 8, 30)),
                "febrero bisiesto ajusta al último día sin perder ancla");
        comprobar(sistema.procesarOperacionesProgramadas(febrero) == 1, "segunda ejecución mensual");
        LocalDateTime marzo = sistema.obtenerOperacionesPendientes().getNodeValue(0).getFechaHora();
        comprobar(marzo.equals(LocalDateTime.of(2032, 3, 31, 8, 30)),
                "marzo recupera el día 31 original después de febrero");
        LocalDateTime siguienteDespuesDeAnios = sistema.obtenerOperacionesPendientes().getNodeValue(0)
                .calcularSiguienteFechaPosteriorA(LocalDateTime.of(2040, 6, 1, 0, 0));
        comprobar(siguienteDespuesDeAnios.equals(LocalDateTime.of(2040, 6, 30, 8, 30)),
                "recurrencia mensual salta fechas vencidas sin iterar mes a mes");
    }

    private static void probarActualizacionesAtomicas() {
        FintechController sistema = new FintechController();
        comprobar(sistema.registrarUsuario("UPD", "Nombre Original", "original@demo.co"), "registro actualización");
        comprobar(!sistema.modificarUsuario("UPD", "Nombre Nuevo", "correo-inválido"),
                "modificación rechaza correo inválido");
        Usuario usuario = sistema.buscarUsuario("UPD");
        comprobar("Nombre Original".equals(usuario.getNombre())
                        && "original@demo.co".equals(usuario.getCorreo()),
                "modificación fallida no deja cambios parciales");
        comprobar(sistema.crearBilleteraAUsuario("UPD", 1800, "Cuenta", TipoBilletera.AHORRO, 500),
                "billetera actualización");
        comprobar(sistema.realizarRecarga(1800, 100, "UPD"), "transacción para fijar categoría");
        comprobar(!sistema.modificarBilletera("UPD", 1800, "Cuenta nueva", TipoBilletera.TRANSPORTE),
                "cambio de categoría no reescribe analítica histórica");
        comprobar(sistema.modificarBilletera("UPD", 1800, "Cuenta nueva", TipoBilletera.AHORRO),
                "nombre de billetera sí se puede modificar");
    }


    private static void probarCatalogoYFlujoCompletoDeCanjes() {
        FintechController sistema = new FintechController();
        comprobar(sistema.registrarUsuario("BEN-A", "Beneficiario", "ben-a@demo.co"), "registro beneficiario");
        comprobar(sistema.registrarUsuario("BEN-B", "Otro usuario", "ben-b@demo.co"), "registro segundo beneficiario");
        comprobar(sistema.crearBilleteraAUsuario("BEN-A", 1901, "Principal", TipoBilletera.AHORRO, 0),
                "billetera beneficiario");
        comprobar(sistema.realizarRecarga(1901, 30_000, "BEN-A"), "generación de puntos para canje");

        comprobar(sistema.registrarBeneficio(" ben-extra ", "Bono extra", "Recompensa de prueba", 200),
                "registrar beneficio normalizando id");
        comprobar(!sistema.registrarBeneficio("BEN-EXTRA", "Duplicado", "No debe registrarse", 100),
                "rechazar id de beneficio duplicado sin distinguir mayúsculas");
        comprobar(!sistema.registrarBeneficio("BEN-MAL", "Costo inválido", "Descripción", 0),
                "rechazar costo de beneficio no positivo");
        comprobar("Bono extra".equals(sistema.buscarBeneficio(" ben-extra ").getNombre()),
                "búsqueda de beneficio normaliza identificador");

        CanjeBeneficio canje = sistema.canjearBeneficio("BEN-A", "ben-extra");
        comprobar(canje != null, "crear comprobante de canje");
        comprobar("DISPONIBLE".equals(canje.getEstado()), "canje inicia disponible");
        comprobar(canje.getPuntosGastados() == 200, "comprobante conserva puntos gastados");
        comprobar(sistema.buscarUsuario("BEN-A").getPuntosAcumulados() == 100,
                "el canje descuenta exactamente los puntos");
        comprobar(sistema.obtenerHistorialCanjes().getSize() == 1, "historial global de canjes");
        comprobar(sistema.obtenerHistorialCanjesUsuario("BEN-A").getSize() == 1,
                "historial individual de canjes");
        comprobar(sistema.obtenerCanjesDisponiblesUsuario("BEN-A").getSize() == 1,
                "consulta canjes todavía disponibles");
        comprobar(sistema.buscarCanjeBeneficio(canje.getCodigoRedencion()) != null,
                "búsqueda por código de redención");

        comprobar(sistema.modificarBeneficio("BEN-EXTRA", "Bono editado", "Descripción nueva", 250),
                "modificar beneficio activo");
        comprobar("Bono extra".equals(sistema.buscarCanjeBeneficio(canje.getId()).getNombreBeneficio()),
                "el comprobante conserva la fotografía histórica del beneficio");
        comprobar(sistema.desactivarBeneficio("BEN-EXTRA"), "desactivar beneficio");
        comprobar(!sistema.desactivarBeneficio("BEN-EXTRA"), "no repetir desactivación");
        comprobar(!sistema.obtenerBeneficiosDisponibles().isEmpty(), "catálogo conserva los beneficios de ejemplo activos");
        comprobar(sistema.activarBeneficio("BEN-EXTRA"), "reactivar beneficio");
        comprobar(!sistema.activarBeneficio("BEN-EXTRA"), "no repetir activación");

        comprobar(!sistema.utilizarCanjeBeneficio("BEN-B", canje.getCodigoRedencion()),
                "otro usuario no puede utilizar el canje ajeno");
        comprobar(sistema.utilizarCanjeBeneficio("BEN-A", canje.getCodigoRedencion()),
                "el propietario puede utilizar su canje");
        comprobar(!sistema.utilizarCanjeBeneficio("BEN-A", canje.getCodigoRedencion()),
                "no se permite utilizar dos veces el mismo canje");
        comprobar(sistema.buscarCanjeBeneficio(canje.getId()).isUtilizado(),
                "estado utilizado se conserva en el registro global");
        comprobar(sistema.obtenerCanjesDisponiblesUsuario("BEN-A").isEmpty(),
                "canje utilizado deja de aparecer entre los disponibles");

        int alertasCanje = 0;
        for (Alerta alerta : sistema.obtenerHistorialAlertasUsuario("BEN-A")) {
            if (alerta.getTipoAlerta().equals("CANJE_BENEFICIO")
                    || alerta.getTipoAlerta().equals("BENEFICIO_UTILIZADO")) alertasCanje++;
        }
        comprobar(alertasCanje == 2, "canje y utilización generan notificaciones en historial del usuario");

        CanjeBeneficio copia = sistema.buscarCanjeBeneficio(canje.getId());
        comprobar(!copia.marcarComoUtilizado(), "copias de canje ya utilizado no alteran ni reabren su estado");
    }

    private static void probarUmbralesDeNivelesYPuntosPorTipo() {
        FintechController sistema = new FintechController();
        comprobar(sistema.registrarUsuario("NIV", "Niveles", "niveles@demo.co"), "registro niveles");
        comprobar(sistema.crearBilleteraAUsuario("NIV", 2001, "Principal", TipoBilletera.AHORRO, 0),
                "billetera niveles");
        comprobar(sistema.realizarRecarga(2001, 50_000, "NIV"), "recarga para llegar a 500 puntos");
        comprobar(sistema.buscarUsuario("NIV").getPuntosAcumulados() == 500
                        && "Bronce".equals(sistema.buscarUsuario("NIV").getNivel()),
                "500 puntos pertenece a Bronce");
        comprobar(sistema.realizarRecarga(2001, 100, "NIV"), "recarga para llegar a 501 puntos");
        comprobar("Plata".equals(sistema.buscarUsuario("NIV").getNivel()), "501 puntos pertenece a Plata");
        comprobar(sistema.realizarRecarga(2001, 49_900, "NIV"), "recarga para llegar a 1000 puntos");
        comprobar("Plata".equals(sistema.buscarUsuario("NIV").getNivel()), "1000 puntos pertenece a Plata");
        comprobar(sistema.realizarRecarga(2001, 100, "NIV"), "recarga para llegar a 1001 puntos");
        comprobar("Oro".equals(sistema.buscarUsuario("NIV").getNivel()), "1001 puntos pertenece a Oro");
        comprobar(sistema.realizarRecarga(2001, 400_000, "NIV"), "recarga para llegar a 5001 puntos");
        comprobar("Platino".equals(sistema.buscarUsuario("NIV").getNivel()), "5001 puntos pertenece a Platino");
        comprobar(sistema.getAnalitica().obtenerUsuariosPorNivel("platino").getSize() == 1,
                "consulta de usuario por nivel coincide con los umbrales");

        FintechController puntos = new FintechController();
        comprobar(puntos.registrarUsuario("PTS", "Puntos", "puntos@demo.co"), "registro puntos por tipo");
        comprobar(puntos.registrarUsuario("DEST", "Destino", "dest@demo.co"), "registro destinatario puntos");
        comprobar(puntos.crearBilleteraAUsuario("PTS", 2010, "Origen", TipoBilletera.AHORRO, 20_000),
                "origen puntos por tipo");
        comprobar(puntos.crearBilleteraAUsuario("DEST", 2011, "Destino", TipoBilletera.AHORRO, 0),
                "destino puntos por tipo");
        comprobar(puntos.realizarRecarga(2010, 1_000, "PTS"), "recarga puntuable");
        comprobar(puntos.buscarUsuario("PTS").getPuntosAcumulados() == 10, "recarga suma un punto por cada 100");
        comprobar(puntos.realizarRetiro(2010, 1_000, "PTS"), "retiro puntuable");
        comprobar(puntos.buscarUsuario("PTS").getPuntosAcumulados() == 30, "retiro suma dos puntos por cada 100");
        comprobar(puntos.realizarTransferencia(2010, 2011, 1_000, "PTS"), "transferencia puntuable");
        comprobar(puntos.buscarUsuario("PTS").getPuntosAcumulados() == 60,
                "transferencia suma tres puntos por cada 100");
    }

    private static void probarFechaRealDeEjecucionProgramada() {
        FintechController sistema = new FintechController();
        comprobar(sistema.registrarUsuario("TIME", "Fecha", "fecha@demo.co"), "registro fecha programada");
        comprobar(sistema.crearBilleteraAUsuario("TIME", 2101, "Cuenta", TipoBilletera.AHORRO, 0),
                "billetera fecha programada");
        LocalDateTime prevista = LocalDateTime.of(2035, 6, 15, 10, 30);
        comprobar(sistema.programarOperacion("TIME-1", 1_000, 2101, 0, prevista,
                TipoTransaccion.RECARGA, Frecuencia.UNICA, "PENDIENTE"), "programar recarga con fecha controlada");
        comprobar(sistema.procesarOperacionesProgramadas(prevista) == 1, "procesar en fecha controlada");
        Transaccion transaccion = sistema.buscarTransaccion("TX-1");
        comprobar(transaccion != null && prevista.equals(transaccion.getFechaHora()),
                "transacción programada conserva la fecha efectiva de ejecución simulada");
        comprobar(transaccion.getPuntosGenerados() == 20,
                "operación programada añade el bono de 10 puntos al cálculo base");
        comprobar(sistema.obtenerHistorialAlertasUsuario("TIME").isEmpty(),
                "recarga normal no inventa alerta de saldo bajo");
    }

    private static void probarConsultasDeLaFachada() {
        FintechController sistema = new FintechController();
        comprobar(sistema.registrarUsuario("FACA", "Fachada A", "faca@demo.co"), "registro fachada A");
        comprobar(sistema.registrarUsuario("FACB", "Fachada B", "facb@demo.co"), "registro fachada B");
        comprobar(sistema.crearBilleteraAUsuario("FACA", 2201, "Cuenta A", TipoBilletera.AHORRO, 5_000),
                "billetera fachada A");
        comprobar(sistema.crearBilleteraAUsuario("FACB", 2202, "Cuenta B", TipoBilletera.AHORRO, 0),
                "billetera fachada B");
        comprobar(sistema.realizarTransferencia(2201, 2202, 500, "FACA"), "transferencia para consultas fachada");
        comprobar(sistema.obtenerUsuarios().getSize() == 2, "lista de usuarios activos desde fachada");
        comprobar(sistema.obtenerBilleterasUsuario("FACA").getSize() == 1, "billeteras de usuario desde fachada");
        comprobar(sistema.obtenerHistorialTransaccionesUsuario("FACA").getSize() == 1,
                "historial de transacciones por usuario desde fachada");
        comprobar(sistema.obtenerHistorialTransaccionesUsuario("FACB").getSize() == 1,
                "historial receptor de transferencia desde fachada");
        comprobar(sistema.obtenerHistorialTransaccionesBilletera(2201).getSize() == 1,
                "historial por billetera desde fachada");
        comprobar(sistema.buscarTransaccion(" tx-1 ") != null, "búsqueda de transacción no distingue mayúsculas");

        Usuario copia = sistema.obtenerUsuarios().getNodeValue(0);
        copia.agregarPuntos(99);
        comprobar(sistema.buscarUsuario("FACA").getPuntosAcumulados() == 15,
                "la lista de usuarios de fachada solo entrega copias");
        comprobar(sistema.eliminarUsuario("FACB"), "baja lógica para consultar historial histórico");
        comprobar(sistema.obtenerUsuarios().getSize() == 1, "la lista de usuarios solo contiene activos");
        comprobar(sistema.obtenerHistorialTransaccionesUsuario("FACB").getSize() == 1,
                "historial permanece disponible tras la baja lógica");
    }


    private static void probarDatosDeDemostracionIntegrados() {
        FintechController sistema = new FintechController();
        comprobar(co.edu.uniquindio.proyectofinal.vistas.DatosDemo.cargar(sistema),
                "carga de datos de demostración");
        comprobar(sistema.obtenerUsuarios().getSize() == 4, "demo registra cuatro usuarios");
        comprobar(sistema.obtenerOperacionesPendientes().getSize() == 2,
                "demo conserva las dos operaciones programadas");
        comprobar(!co.edu.uniquindio.proyectofinal.vistas.DatosDemo.cargar(sistema),
                "la carga demo no duplica usuarios ni operaciones");

        int ejecutadas = sistema.procesarOperacionesProgramadas(LocalDateTime.now().plusMinutes(1));
        comprobar(ejecutadas == 1, "demo procesa solamente la operación vencida");
        comprobar(sistema.obtenerOperacionesProcesadas().getSize() == 1,
                "demo archiva la operación única procesada");
        comprobar(sistema.obtenerOperacionesPendientes().getSize() == 1,
                "demo conserva el ahorro semanal futuro");
        comprobar(sistema.buscarBilletera(401).getSaldo() == 110_000,
                "recarga programada demo modifica el saldo correctamente");
    }

    private static void comprobar(boolean condicion, String nombre) {
        if (!condicion) throw new AssertionError("Falló: " + nombre);
        pruebasEjecutadas++;
    }
}
