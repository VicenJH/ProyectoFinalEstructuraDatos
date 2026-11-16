package co.edu.uniquindio.proyectofinal;

import co.edu.uniquindio.proyectofinal.pruebas.PruebasRegresionFintech;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PruebasRegresionFintechTest {
    @Test
    void suiteCompletaDebePasar() {
        var resultados = PruebasRegresionFintech.ejecutar(null);
        assertTrue(resultados.stream().allMatch(PruebasRegresionFintech.Resultado::exitoso),
                () -> resultados.toString());
    }
}
