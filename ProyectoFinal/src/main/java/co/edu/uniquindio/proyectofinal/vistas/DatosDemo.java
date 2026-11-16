package co.edu.uniquindio.proyectofinal.vistas;

import co.edu.uniquindio.proyectofinal.Estructuras.Controllers.FintechController;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.Frecuencia;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.TipoBilletera;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.TipoTransaccion;

import java.time.LocalDateTime;

/**
 * Carga un conjunto de datos de ejemplo para probar y demostrar el sistema.
 * Genera usuarios, billeteras, transferencias que forman un ciclo en el grafo
 * y operaciones programadas (una de ellas ya vencida, lista para procesar).
 */
public final class DatosDemo {

    private DatosDemo() {
    }

    /**
     * @return false si los datos de ejemplo ya estaban cargados.
     */
    public static boolean cargar(FintechController c) {
        if (c.buscarUsuario("U001") != null) {
            return false;
        }

        c.registrarUsuario("U001", "Ana Gómez", "ana@correo.com");
        c.registrarUsuario("U002", "Beto Ruiz", "beto@correo.com");
        c.registrarUsuario("U003", "Carla Díaz", "carla@correo.com");
        c.registrarUsuario("U004", "Diego Mora", "diego@correo.com");

        c.crearBilleteraAUsuario("U001", 101, "Gastos del mes", TipoBilletera.GASTO_DIARIO, 500000);
        c.crearBilleteraAUsuario("U001", 102, "Ahorro", TipoBilletera.AHORRO, 200000);
        c.crearBilleteraAUsuario("U002", 201, "Principal", TipoBilletera.GASTO_DIARIO, 300000);
        c.crearBilleteraAUsuario("U002", 202, "Compras", TipoBilletera.COMPRAS, 50000);
        c.crearBilleteraAUsuario("U003", 301, "Transporte", TipoBilletera.TRANSPORTE, 80000);
        c.crearBilleteraAUsuario("U003", 302, "Inversión", TipoBilletera.INVERSION, 1500000);
        c.crearBilleteraAUsuario("U004", 401, "Diario", TipoBilletera.GASTO_DIARIO, 100000);

        c.realizarRecarga(101, 100000, "U001");

        // Ciclo en el grafo: Ana → Beto → Carla → Diego → Ana.
        c.realizarTransferencia(101, 201, 50000, "U001");
        c.realizarTransferencia(201, 301, 20000, "U002");
        c.realizarTransferencia(301, 401, 10000, "U003");
        c.realizarTransferencia(401, 101, 5000, "U004");

        c.realizarRetiro(302, 200000, "U003");
        c.realizarTransferencia(102, 101, 30000, "U001");

        // Ahorro automático semanal (futuro) y una recarga ya vencida.
        c.programarOperacion("PRG-001", 20000, 101, 102,
                LocalDateTime.now().plusDays(1), TipoTransaccion.TRANSFERENCIA_INTERNA,
                Frecuencia.SEMANAL, "PENDIENTE");
        c.programarOperacion("PRG-002", 5000, 401, 401,
                LocalDateTime.now().minusHours(1), TipoTransaccion.RECARGA,
                Frecuencia.UNICA, "PENDIENTE");

        return true;
    }
}