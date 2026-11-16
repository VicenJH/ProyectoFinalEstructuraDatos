package co.edu.uniquindio.proyectofinal.Estructuras.Persistencia;

import co.edu.uniquindio.proyectofinal.Estructuras.Controllers.FintechController;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.Frecuencia;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.TipoBilletera;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.TipoTransaccion;

import java.time.LocalDateTime;

/** Datos iniciales de demostración, cargados solo cuando todavía no existe un respaldo. */
public final class DatosDemo {
    private DatosDemo() { }

    public static void cargar(FintechController sistema) {
        if (sistema == null || sistema.buscarUsuario("U001") != null) return;

        sistema.registrarUsuario("U001", "Ana Gómez", "ana@correo.com");
        sistema.registrarUsuario("U002", "Beto Ruiz", "beto@correo.com");
        sistema.registrarUsuario("U003", "Carla Díaz", "carla@correo.com");
        sistema.registrarUsuario("U004", "Diego Mora", "diego@correo.com");

        sistema.crearBilleteraAUsuario("U001", 101, "Gastos del mes", TipoBilletera.GASTO_DIARIO, 500_000);
        sistema.crearBilleteraAUsuario("U001", 102, "Ahorro", TipoBilletera.AHORRO, 200_000);
        sistema.crearBilleteraAUsuario("U002", 201, "Principal", TipoBilletera.GASTO_DIARIO, 300_000);
        sistema.crearBilleteraAUsuario("U002", 202, "Compras", TipoBilletera.COMPRAS, 50_000);
        sistema.crearBilleteraAUsuario("U003", 301, "Transporte", TipoBilletera.TRANSPORTE, 80_000);
        sistema.crearBilleteraAUsuario("U003", 302, "Inversión", TipoBilletera.INVERSION, 1_500_000);
        sistema.crearBilleteraAUsuario("U004", 401, "Diario", TipoBilletera.GASTO_DIARIO, 100_000);

        sistema.realizarRecarga(101, 100_000, "U001");
        sistema.realizarTransferencia(101, 201, 50_000, "U001");
        sistema.realizarTransferencia(201, 301, 20_000, "U002");
        sistema.realizarTransferencia(301, 401, 10_000, "U003");
        sistema.realizarTransferencia(401, 101, 5_000, "U004");
        sistema.realizarRetiro(302, 200_000, "U003");
        sistema.realizarTransferencia(102, 101, 30_000, "U001");

        sistema.programarOperacion("PRG-001", 20_000, 101, 102,
                LocalDateTime.now().plusDays(1), TipoTransaccion.TRANSFERENCIA_INTERNA,
                Frecuencia.SEMANAL, "PENDIENTE");
        sistema.programarOperacion("PRG-002", 5_000, 401, 0,
                LocalDateTime.now().plusHours(8), TipoTransaccion.RECARGA,
                Frecuencia.UNICA, "PENDIENTE");
    }
}
