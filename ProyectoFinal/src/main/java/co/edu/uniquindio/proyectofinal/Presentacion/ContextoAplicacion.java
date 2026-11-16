package co.edu.uniquindio.proyectofinal.Presentacion;

import co.edu.uniquindio.proyectofinal.Estructuras.Controllers.FintechController;
import co.edu.uniquindio.proyectofinal.Estructuras.Persistencia.DatosDemo;
import co.edu.uniquindio.proyectofinal.Estructuras.Persistencia.EstadoFintech;
import co.edu.uniquindio.proyectofinal.Estructuras.Persistencia.PersistenciaFintech;

import java.io.IOException;
import java.util.Optional;

/** Servicios compartidos por las ventanas durante una sesión de la aplicación. */
public final class ContextoAplicacion {

    private final FintechController sistema;
    private final PersistenciaFintech persistencia;

    private ContextoAplicacion(FintechController sistema, PersistenciaFintech persistencia) {
        this.sistema = sistema;
        this.persistencia = persistencia;
    }

    public static ContextoAplicacion iniciar() throws IOException {
        FintechController sistema = new FintechController();
        PersistenciaFintech persistencia = new PersistenciaFintech();
        Optional<EstadoFintech> respaldo = persistencia.cargar();
        if (respaldo.isPresent()) {
            sistema.restaurarEstadoPersistible(respaldo.get());
        } else {
            DatosDemo.cargar(sistema);
            persistencia.guardar(sistema.crearEstadoPersistible());
        }
        return new ContextoAplicacion(sistema, persistencia);
    }

    public FintechController getSistema() {
        return sistema;
    }

    public PersistenciaFintech getPersistencia() {
        return persistencia;
    }

    public synchronized void guardar() throws IOException {
        persistencia.guardar(sistema.crearEstadoPersistible());
    }

    public synchronized void recargar() throws IOException {
        EstadoFintech estado = persistencia.cargar()
                .orElseThrow(() -> new IOException("No existe un respaldo local para restaurar."));
        sistema.restaurarEstadoPersistible(estado);
    }
}
