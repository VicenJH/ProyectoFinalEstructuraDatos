package co.edu.uniquindio.proyectofinal.Estructuras.Servicios;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Autenticación local académica para separar el acceso de administración. */
public final class AutenticacionService {

    /** Recomendado: definir FINTECH_ADMIN_PASSWORD en el entorno al distribuir la aplicación. */
    private static final String CLAVE_DEMO = "Admin2026*";

    public boolean autenticarAdministrador(String claveIngresada) {
        if (claveIngresada == null) return false;
        String configurada = System.getenv("FINTECH_ADMIN_PASSWORD");
        if (configurada == null || configurada.isBlank()) configurada = CLAVE_DEMO;
        return MessageDigest.isEqual(configurada.getBytes(StandardCharsets.UTF_8),
                claveIngresada.getBytes(StandardCharsets.UTF_8));
    }

    public String obtenerNotaConfiguracion() {
        String configurada = System.getenv("FINTECH_ADMIN_PASSWORD");
        return configurada != null && !configurada.isBlank()
                ? "Clave configurada mediante FINTECH_ADMIN_PASSWORD."
                : "Clave demo: Admin2026*. Cambiar FINTECH_ADMIN_PASSWORD para otra clave.";
    }
}
