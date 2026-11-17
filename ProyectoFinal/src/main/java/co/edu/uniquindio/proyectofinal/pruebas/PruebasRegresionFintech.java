package co.edu.uniquindio.proyectofinal.pruebas;

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
import co.edu.uniquindio.proyectofinal.Estructuras.Persistencia.PersistenciaFintech;
import co.edu.uniquindio.proyectofinal.Estructuras.Persistencia.EstadoFintech;
import co.edu.uniquindio.proyectofinal.Estructuras.Persistencia.DatosDemo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import java.time.LocalDateTime;

/**
 * Suite de regresión ejecutable sin bibliotecas externas.
 * Ejecutar con java -cp target/classes co.edu.uniquindio.proyectofinal.pruebas.PruebasRegresionFintech.
 */
public final class PruebasRegresionFintech {

    private static int pruebasEjecutadas;

    private PruebasRegresionFintech() { }

    public record Resultado(String nombre, boolean exitoso, long duracionMs,
                            int comprobaciones, String detalle) { }

    public static void main(String[] args) {
        List<Resultado> resultados = ejecutar(null);
        resultados.forEach(r -> System.out.println((r.exitoso() ? "OK  " : "FAIL")
                + " | " + r.nombre() + " | " + r.duracionMs() + " ms | "
                + r.comprobaciones() + " comprobaciones | " + r.detalle()));
        long aprobadas = resultados.stream().filter(Resultado::exitoso).count();
        System.out.println("\nRESULTADO: " + aprobadas + "/" + resultados.size()
                + " grupos superados; " + pruebasEjecutadas + " comprobaciones ejecutadas.");
        if (aprobadas != resultados.size()) throw new AssertionError("Una o más pruebas fallaron.");
    }

    /** Ejecuta cada grupo por separado para que el monitor pueda mostrar tiempo y resultado. */
    public static List<Resultado> ejecutar(Consumer<Resultado> listener) {
        pruebasEjecutadas = 0;
        List<Resultado> resultados = new ArrayList<>();
        ejecutarGrupo("Listas, pilas y colas", PruebasRegresionFintech::probarEstructurasLineales, resultados, listener);
        ejecutarGrupo("Tabla hash y árbol AVL", PruebasRegresionFintech::probarTablaHashYArbolAvl, resultados, listener);
        ejecutarGrupo("Usuarios, billeteras, transferencias y reversión", PruebasRegresionFintech::probarRegistroTransferenciasYReversion, resultados, listener);
        ejecutarGrupo("Programación y notificaciones", PruebasRegresionFintech::probarProgramacionYNotificaciones, resultados, listener);
        ejecutarGrupo("Puntos, niveles y reversión de recompensas", PruebasRegresionFintech::probarRecompensasYReversionConPuntosGastados, resultados, listener);
        ejecutarGrupo("Analítica y grafos", PruebasRegresionFintech::probarAnaliticaYGrafo, resultados, listener);
        ejecutarGrupo("Detección de riesgo y auditoría", PruebasRegresionFintech::probarDetectorDeRiesgo, resultados, listener);
        ejecutarGrupo("Comparación de rendimiento", PruebasRegresionFintech::probarComparacionDeRendimiento, resultados, listener);
        ejecutarGrupo("Cupones y bonificaciones de saldo", PruebasRegresionFintech::probarCuponBonificacion, resultados, listener);
        ejecutarGrupo("Persistencia y restauración completa", PruebasRegresionFintech::probarPersistenciaCompleta, resultados, listener);
        ejecutarGrupo("Carga inicial de datos demo", PruebasRegresionFintech::probarCargaInicialDatosDemo, resultados, listener);
        ejecutarGrupo("Respaldo TSV", PruebasRegresionFintech::probarRespaldoSistema, resultados, listener);
        return List.copyOf(resultados);
    }

    private static void ejecutarGrupo(String nombre, Runnable prueba, List<Resultado> resultados,
                                      Consumer<Resultado> listener) {
        int antes = pruebasEjecutadas;
        long inicio = System.nanoTime();
        Resultado resultado;
        try {
            prueba.run();
            resultado = new Resultado(nombre, true,
                    (System.nanoTime() - inicio) / 1_000_000,
                    pruebasEjecutadas - antes, "Todas las comprobaciones pasaron.");
        } catch (Throwable error) {
            resultado = new Resultado(nombre, false,
                    (System.nanoTime() - inicio) / 1_000_000,
                    pruebasEjecutadas - antes, error.getClass().getSimpleName() + ": "
                    + (error.getMessage() == null ? "sin detalle" : error.getMessage()));
        }
        resultados.add(resultado);
        if (listener != null) listener.accept(resultado);
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
        comprobar(!sistema.realizarRetiro(101, 1_000, "B"), "usuario no puede retirar desde billetera ajena");
        comprobar(!sistema.realizarTransferencia(101, 201, 1_000, "B"), "usuario no puede transferir desde billetera ajena");
        comprobar(sistema.buscarBilletera(101).getSaldo() == 50_000, "intentos ajenos no alteran saldo");
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
        comprobar("REVERTIDA".equals(transferencia.getEstado()), "estado de transacción revertida");
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

        // Una operación mensual del día 31 no debe "perder" el día tras un mes corto.
        LocalDateTime finDeMes = LocalDateTime.of(2030, 1, 31, 12, 0);
        comprobar(sistema.programarOperacion("P-M", 10, 500, 0, finDeMes,
                TipoTransaccion.RECARGA, Frecuencia.MENSUAL, "PENDIENTE"), "programación mensual a fin de mes");
        sistema.procesarOperacionesProgramadas(finDeMes);
        sistema.procesarOperacionesProgramadas(LocalDateTime.of(2030, 2, 28, 12, 0));
        LocalDateTime proxima = null;
        for (OperacionProgramada pendiente : sistema.obtenerOperacionesPendientes()) {
            if (pendiente.getId().equals("P-M")) proxima = pendiente.getFechaHora();
        }
        comprobar(LocalDateTime.of(2030, 3, 31, 12, 0).equals(proxima),
                "mensual conserva el día 31 después de febrero");
    }

    private static void probarRecompensasYReversionConPuntosGastados() {
        FintechController sistema = new FintechController();
        comprobar(sistema.registrarUsuario("R", "Rita", "rita@demo.co"), "registro recompensas");
        comprobar(sistema.crearBilleteraAUsuario("R", 700, "Principal", TipoBilletera.GASTO_DIARIO, 0), "billetera recompensas");
        comprobar(sistema.realizarRecarga(700, 100_000, "R"), "recarga puntos");
        Usuario usuario = sistema.buscarUsuario("R");
        comprobar(usuario.getPuntosAcumulados() == 1_000 && "Plata".equals(usuario.getNivel()), "puntos y nivel Plata");
        CanjeBeneficio canje = sistema.canjearBeneficio("R", "BEN-001");
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
    }

    private static void probarComparacionDeRendimiento() {
        SimpleLinkedList<ComparadorRendimiento.ResultadoComparacion> resultados =
                new ComparadorRendimiento().compararBusquedaUsuarios(500, 1_000);
        comprobar(resultados.getSize() == 3, "compara tres estructuras");
        for (ComparadorRendimiento.ResultadoComparacion resultado : resultados) {
            comprobar(resultado.getAciertos() == 1_000, "mismas consultas en " + resultado.getEstructura());
        }
    }

    private static void probarCuponBonificacion() {
        FintechController sistema = new FintechController();
        comprobar(sistema.registrarUsuario("CP", "Cupo N", "cupon@demo.co"), "registro usuario cupones");
        comprobar(sistema.crearBilleteraAUsuario("CP", 950, "Principal", TipoBilletera.AHORRO, 1_000),
                "billetera cupones");
        comprobar(sistema.realizarRecarga(950, 50_000, "CP"), "recarga para ganar puntos");
        CanjeBeneficio cupon = sistema.canjearBeneficio("CP", "BEN-001");
        comprobar(cupon != null && cupon.getValorBono() == 5_000, "canje genera cupón con bono monetario");
        int puntosAntes = sistema.buscarUsuario("CP").getPuntosAcumulados();
        Transaccion aplicada = sistema.aplicarCupon("CP", 950, cupon.getCodigoRedencion());
        comprobar(aplicada != null && aplicada.getTipo() == TipoTransaccion.BONIFICACION,
                "cupón se registra como bonificación");
        comprobar(sistema.buscarBilletera(950).getSaldo() == 56_000, "bono se abona al saldo de la billetera");
        comprobar(sistema.buscarUsuario("CP").getPuntosAcumulados() == puntosAntes,
                "bonificación no genera puntos recursivos");
        comprobar(sistema.aplicarCupon("CP", 950, cupon.getCodigoRedencion()) == null,
                "cupón no se puede aplicar dos veces");
        comprobar(sistema.aplicarCupon("OTRO", 950, cupon.getCodigoRedencion()) == null,
                "cupón no se puede aplicar por otro usuario");
    }

    private static void probarPersistenciaCompleta() {
        FintechController original = new FintechController();
        comprobar(original.registrarUsuario("PX", "Persistente", "persistente@demo.co"), "registro persistencia");
        comprobar(original.crearBilleteraAUsuario("PX", 970, "Ahorros", TipoBilletera.AHORRO, 10_000),
                "billetera persistencia");
        comprobar(original.registrarUsuario("PY", "Destino", "destino@demo.co"), "registro destinatario persistencia");
        comprobar(original.crearBilleteraAUsuario("PY", 971, "Principal", TipoBilletera.GASTO_DIARIO, 0),
                "billetera destinatario persistencia");
        comprobar(original.realizarRecarga(970, 25_000, "PX"), "recarga puntos persistencia");
        comprobar(original.realizarTransferencia(970, 971, 2_000, "PX"), "transferencia persistencia");
        CanjeBeneficio cupon = original.canjearBeneficio("PX", "BEN-001");
        comprobar(cupon != null, "canje persistencia");
        original.aplicarCupon("PX", 970, cupon.getCodigoRedencion());
        comprobar(original.programarOperacion("PX-PRG", 100, 970, 0, LocalDateTime.now().plusDays(2),
                TipoTransaccion.RECARGA, Frecuencia.UNICA, "PENDIENTE"), "programación persistencia");

        Path archivo = null;
        try {
            archivo = Files.createTempFile("fintech-estado-", ".bin");
            PersistenciaFintech persistencia = new PersistenciaFintech(archivo);
            persistencia.guardar(original.crearEstadoPersistible());
            EstadoFintech estado = persistencia.cargar().orElseThrow();
            FintechController recuperado = new FintechController();
            recuperado.restaurarEstadoPersistible(estado);
            comprobar(recuperado.obtenerUsuarios().getSize() == 2, "usuarios restaurados");
            comprobar(recuperado.buscarBilletera(970).getSaldo() == original.buscarBilletera(970).getSaldo(),
                    "saldo restaurado sin repetir movimientos");
            comprobar(recuperado.obtenerHistorialGlobal().getSize() == original.obtenerHistorialGlobal().getSize(),
                    "historial global restaurado");
            comprobar(recuperado.obtenerHistorialCanjes().getSize() == 1, "canjes restaurados");
            comprobar(recuperado.obtenerOperacionesPendientes().getSize() == 1, "cola prioritaria restaurada");
            comprobar(recuperado.buscarCanjeBeneficio(cupon.getCodigoRedencion()).isUtilizado(),
                    "estado del cupón restaurado");
            comprobar(recuperado.existeRutaTransferencias("PX", "PY"), "grafo de transferencias reconstruido");
        } catch (java.io.IOException e) {
            throw new AssertionError("No se pudo comprobar la persistencia.", e);
        } finally {
            if (archivo != null) try { Files.deleteIfExists(archivo); } catch (java.io.IOException ignored) { }
        }
    }

    private static void probarCargaInicialDatosDemo() {
        FintechController sistema = new FintechController();
        DatosDemo.cargar(sistema);
        comprobar(sistema.obtenerUsuarios().getSize() == 4, "carga inicial incluye cuatro usuarios");
        comprobar(sistema.obtenerBilleteras().getSize() == 7, "carga inicial incluye siete billeteras");
        comprobar(sistema.obtenerHistorialGlobal().getSize() == 7, "carga inicial incluye movimientos de ejemplo");
        comprobar(sistema.obtenerOperacionesPendientes().getSize() == 2, "carga inicial incluye operaciones programadas");
        DatosDemo.cargar(sistema);
        comprobar(sistema.obtenerUsuarios().getSize() == 4, "carga inicial no duplica datos al repetirse");
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

    private static void comprobar(boolean condicion, String nombre) {
        if (!condicion) throw new AssertionError("Falló: " + nombre);
        pruebasEjecutadas++;
    }
}
