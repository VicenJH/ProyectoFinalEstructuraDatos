package co.edu.uniquindio.proyectofinal.Estructuras.Persistencia;

import java.io.IOException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

/** Guarda y recupera el estado del sistema con escritura atómica. */
public final class PersistenciaFintech {

    private final Path archivo;

    public PersistenciaFintech() {
        this(Path.of(System.getProperty("user.home"), ".fintech-uniquindio", "estado-fintech.bin"));
    }

    public PersistenciaFintech(Path archivo) {
        if (archivo == null) throw new IllegalArgumentException("La ruta del archivo es obligatoria.");
        this.archivo = archivo.toAbsolutePath().normalize();
    }

    public Path getArchivo() {
        return archivo;
    }

    public synchronized void guardar(EstadoFintech estado) throws IOException {
        if (estado == null) throw new IllegalArgumentException("El estado no puede ser null.");
        Path carpeta = archivo.getParent();
        if (carpeta != null) Files.createDirectories(carpeta);
        Path temporal = archivo.resolveSibling(archivo.getFileName() + ".tmp");
        try {
            try (ObjectOutputStream salida = new ObjectOutputStream(Files.newOutputStream(temporal))) {
                salida.writeObject(estado);
                salida.flush();
            }
            try {
                Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporal);
        }
    }

    public synchronized Optional<EstadoFintech> cargar() throws IOException {
        if (!Files.exists(archivo)) return Optional.empty();
        try (ObjectInputStream entrada = new ObjectInputStream(Files.newInputStream(archivo))) {
            entrada.setObjectInputFilter(ObjectInputFilter.Config.createFilter(
                    "maxdepth=24;maxrefs=2000000;maxbytes=100000000;java.base/*;" +
                    "co.edu.uniquindio.proyectofinal.Estructuras.Persistencia.EstadoFintech*;co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.*;!*"));
            Object objeto = entrada.readObject();
            if (!(objeto instanceof EstadoFintech estado)) {
                throw new IOException("El archivo no contiene un respaldo Fintech reconocido.");
            }
            if (estado.version() != 1) {
                throw new IOException("Versión de respaldo no compatible: " + estado.version());
            }
            return Optional.of(estado);
        } catch (ClassNotFoundException | RuntimeException ex) {
            throw new IOException("El respaldo existe, pero no se pudo interpretar. No fue cargado.", ex);
        }
    }
}
