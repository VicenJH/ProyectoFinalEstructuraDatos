package co.edu.uniquindio.proyectofinal.Estructuras.Arbol;

/**
 * Clase de ejecución y pruebas básicas para la estructura ArbolBinario.
 *
 * Permite comprobar el funcionamiento de las operaciones principales
 * de un árbol binario de búsqueda:
 *
 * - Inserción de elementos.
 * - Recorrido inorden.
 * - Recorrido preorden.
 * - Recorrido postorden.
 * - Recorrido por amplitud.
 * - Búsqueda de elementos.
 * - Consulta del peso.
 * - Consulta de la altura.
 * - Eliminación de elementos.
 *
 * Esta clase tiene fines de demostración y verificación.
 * No forma parte de la lógica principal del sistema Fintech.
 *
 * @author Proyecto Fintech
 * @version 1.0
 */
public class RunArboles {

    /**
     * Método principal de ejecución.
     *
     * Crea un árbol binario de búsqueda de números enteros y ejecuta
     * diferentes operaciones para verificar su comportamiento.
     *
     * @param args argumentos recibidos desde la línea de comandos.
     */
    public static void main(String[] args) {

        // Creación del árbol binario de búsqueda.
        ArbolBinario<Integer> arbol = new ArbolBinario<>();

        // Inserción de elementos.
        int[] elementos = {5, 3, 4, 6, 2};

        System.out.println("===== PRUEBAS DEL ÁRBOL BINARIO =====");

        for (int elemento : elementos) {
            arbol.insertar(elemento);
        }

        // Verificación de recorridos.
        System.out.println("\nRecorrido inorden:");
        arbol.recorrerInorden();

        System.out.println("\n\nRecorrido preorden:");
        arbol.recorrerPreorden();

        System.out.println("\n\nRecorrido postorden:");
        arbol.recorrerPostorden();

        System.out.println("\n\nRecorrido por amplitud:");
        arbol.recorridoAmplitud();

        // Verificación de búsquedas.
        System.out.println("\n\n===== BÚSQUEDAS =====");

        System.out.println("¿Existe el elemento 4?: " + arbol.existe(4));
        System.out.println("¿Existe el elemento 9?: " + arbol.existe(9));

        // Verificación de peso y altura.
        System.out.println("\n===== INFORMACIÓN DEL ÁRBOL =====");

        System.out.println("Cantidad de nodos: " + arbol.obtenerPeso());
        System.out.println("Peso registrado: " + arbol.getPeso());
        System.out.println("Altura: " + arbol.ObtenerAltura());

        // Consulta del menor elemento.
        if (arbol.obtenerMenor() != null) {
            System.out.println(
                    "Elemento menor: " + arbol.obtenerMenor().getElemento()
            );
        }

        // Eliminación de un elemento.
        System.out.println("\n===== ELIMINACIÓN =====");

        System.out.println("Eliminando el elemento 3...");
        arbol.eliminar(3);

        System.out.println("Árbol después de eliminar:");

        arbol.recorrerInorden();

        // Verificación posterior a la eliminación.
        System.out.println("\n\n¿Existe el elemento 3?: " + arbol.existe(3));
        System.out.println("Cantidad de nodos después de eliminar: "
                + arbol.obtenerPeso());

        System.out.println("Peso registrado después de eliminar: "
                + arbol.getPeso());

        System.out.println("\n===== FIN DE LAS PRUEBAS =====");
    }
}