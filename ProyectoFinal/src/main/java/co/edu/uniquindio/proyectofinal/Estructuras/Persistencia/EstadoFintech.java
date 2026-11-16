package co.edu.uniquindio.proyectofinal.Estructuras.Persistencia;

import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/** DTO serializable: conserva únicamente datos simples, nunca las estructuras internas. */
public record EstadoFintech(
        int version,
        LocalDateTime fechaRespaldo,
        List<UsuarioData> usuarios,
        List<BilleteraData> billeteras,
        List<TransaccionData> transacciones,
        List<AlertaData> alertas,
        List<BeneficioData> beneficios,
        List<CanjeData> canjes,
        List<OperacionData> pendientes,
        List<OperacionData> procesadas,
        List<EventoData> auditoria
) implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    public record UsuarioData(String id, String nombre, String correo, int puntos,
                              boolean activo) implements Serializable {
        @Serial private static final long serialVersionUID = 1L;
    }

    public record BilleteraData(int codigo, String idUsuario, String nombre, TipoBilletera tipo,
                                int saldo, boolean activa) implements Serializable {
        @Serial private static final long serialVersionUID = 1L;
    }

    public record TransaccionData(String id, double valor, String origen, String destino,
                                  LocalDateTime fecha, TipoTransaccion tipo, String estado,
                                  int puntos, NivelRiesgo riesgo) implements Serializable {
        @Serial private static final long serialVersionUID = 1L;
    }

    public record AlertaData(String idUsuario, int id, LocalDateTime fecha, String mensaje,
                             String tipo, boolean atendida) implements Serializable {
        @Serial private static final long serialVersionUID = 1L;
    }

    public record BeneficioData(String id, String nombre, String descripcion, int costoPuntos,
                                int valorBono, boolean activo) implements Serializable {
        @Serial private static final long serialVersionUID = 1L;
    }

    public record CanjeData(String id, String codigo, String idUsuario, String idBeneficio,
                            String nombre, String descripcion, LocalDateTime fecha,
                            int puntosGastados, int valorBono, String estado,
                            LocalDateTime fechaUso) implements Serializable {
        @Serial private static final long serialVersionUID = 1L;
    }

    public record OperacionData(String id, double valor, String origen, String destino,
                                LocalDateTime fecha, TipoTransaccion tipo, Frecuencia frecuencia,
                                String estado, String idUsuario, boolean notificacionEnviada)
            implements Serializable {
        @Serial private static final long serialVersionUID = 1L;
    }

    public record EventoData(int id, LocalDateTime fecha, String idUsuario, String idTransaccion,
                             NivelRiesgo nivel, String motivo) implements Serializable {
        @Serial private static final long serialVersionUID = 1L;
    }
}
