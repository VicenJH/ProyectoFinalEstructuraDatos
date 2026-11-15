package co.edu.uniquindio.proyectofinal.Estructuras.Pilas;

import co.edu.uniquindio.proyectofinal.Estructuras.Colas.Cola;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Implementa una pila genérica mediante una lista simplemente enlazada.
 *
 * La pila sigue el principio LIFO (Last In, First Out):
 * el último elemento insertado es el primero en salir.
 *
 * Esta estructura se utiliza en el proyecto Fintech para mantener
 * el historial de transacciones que pueden ser revertidas.
 *
 * También sirve como estructura auxiliar en el conversor de expresiones.
 *
 * No utiliza las colecciones Stack, ArrayList ni LinkedList de Java.
 *
 * @param <T> tipo de elementos almacenados en la pila.
 */
public class Stack<T> {

    /**
     * Cantidad de elementos almacenados.
     */
    private int size;

    /**
     * Referencia al nodo que contiene el elemento ubicado en el tope.
     */
    private Node<T> top;

    /**
     * Construye una pila vacía.
     *
     * Complejidad: O(1).
     */
    public Stack() {
        this.size = 0;
        this.top = null;
    }

    /**
     * Obtiene la cantidad de elementos almacenados.
     *
     * @return número de elementos de la pila.
     *
     * Complejidad: O(1).
     */
    public int getSize() {
        return size;
    }

    /**
     * Comprueba si la pila está vacía.
     *
     * @return true si no contiene elementos; false en caso contrario.
     *
     * Complejidad: O(1).
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Inserta un elemento en el tope de la pila.
     *
     * Se permite almacenar valores null.
     *
     * @param data elemento que se desea insertar.
     *
     * Complejidad: O(1).
     */
    public void push(T data) {
        Node<T> newNode = new Node<>(data);

        newNode.setNext(top);
        top = newNode;

        size++;
    }

    /**
     * Retira y devuelve el elemento ubicado en el tope.
     *
     * @return elemento que se encontraba en el tope.
     * @throws NoSuchElementException si la pila está vacía.
     *
     * Complejidad: O(1).
     */
    public T pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("No se puede retirar un elemento de una pila vacía.");
        }

        T data = top.getData();
        top = top.getNext();
        size--;

        return data;
    }

    /**
     * Consulta el elemento ubicado en el tope sin retirarlo.
     *
     * @return elemento ubicado en el tope.
     * @throws NoSuchElementException si la pila está vacía.
     *
     * Complejidad: O(1).
     */
    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("No se puede consultar el tope de una pila vacía.");
        }

        return top.getData();
    }

    /**
     * Crea una copia independiente de la pila.
     *
     * Los elementos conservan el mismo orden que en la pila original.
     * La copia es estructural: no clona internamente los objetos almacenados.
     *
     * @return nueva pila con los mismos elementos y en el mismo orden.
     *
     * Complejidad: O(n), donde n es la cantidad de elementos.
     */
    public Stack<T> clonStack() {
        Stack<T> auxiliar = new Stack<>();
        Stack<T> copia = new Stack<>();

        Node<T> actual = top;

        while (actual != null) {
            auxiliar.push(actual.getData());
            actual = actual.getNext();
        }

        while (!auxiliar.isEmpty()) {
            copia.push(auxiliar.pop());
        }

        return copia;
    }

    /**
     * Imprime los elementos desde el tope hasta el fondo de la pila.
     *
     * Este método se conserva para facilitar las pruebas y la depuración.
     *
     * Complejidad: O(n).
     */
    public void print() {
        Node<T> actual = top;

        while (actual != null) {
            System.out.println("Elemento: " + actual.getData());
            actual = actual.getNext();
        }
    }

    /**
     * Inserta en esta pila todos los elementos de otra pila.
     *
     * Los elementos de la pila recibida quedan encima de los elementos
     * que ya estaban en esta pila, conservando su orden relativo.
     *
     * La pila de origen no se modifica.
     * Si la pila recibida es null, no se realiza ninguna operación.
     *
     * @param stack pila cuyos elementos se desean agregar.
     *
     * Complejidad: O(n), donde n es el tamaño de la pila recibida.
     */
    public void addAll(Stack<T> stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }

        Stack<T> auxiliar = new Stack<>();
        Node<T> actual = stack.top;

        int cantidad = stack.size;

        for (int i = 0; i < cantidad; i++) {
            auxiliar.push(actual.getData());
            actual = actual.getNext();
        }

        while (!auxiliar.isEmpty()) {
            this.push(auxiliar.pop());
        }
    }

    /**
     * Determina si un texto es palíndromo.
     *
     * Para realizar la comparación se ignoran los espacios, los signos
     * de puntuación y las diferencias entre mayúsculas y minúsculas.
     *
     * @param texto texto que se desea evaluar.
     * @return true si el texto es palíndromo; false en caso contrario.
     * @throws IllegalArgumentException si el texto es null.
     *
     * Complejidad: O(n), donde n es la longitud del texto.
     */
    public static boolean esPalindromo(String texto) {
        if (texto == null) {
            throw new IllegalArgumentException("El texto no puede ser null.");
        }

        StringBuilder limpio = new StringBuilder();

        for (int i = 0; i < texto.length(); i++) {
            char caracter = texto.charAt(i);

            if (Character.isLetterOrDigit(caracter)) {
                limpio.append(Character.toLowerCase(caracter));
            }
        }

        Stack<Character> pila = new Stack<>();

        for (int i = 0; i < limpio.length(); i++) {
            pila.push(limpio.charAt(i));
        }

        for (int i = 0; i < limpio.length(); i++) {
            if (limpio.charAt(i) != pila.pop()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Extrae los números pares de una pila y los almacena en una cola.
     *
     * La pila original se restaura después del procedimiento,
     * conservando su contenido y orden.
     *
     * Los elementos pares se agregan a la cola en el orden en que
     * fueron retirados de la pila, es decir, desde el tope hacia el fondo.
     *
     * @param pila pila de números enteros que se desea analizar.
     * @return cola con los números pares encontrados.
     * @throws IllegalArgumentException si la pila es null.
     *
     * Complejidad: O(n), donde n es el tamaño de la pila.
     */
    public static Cola<Integer> pasarPares(Stack<Integer> pila) {
        if (pila == null) {
            throw new IllegalArgumentException("La pila no puede ser null.");
        }

        Cola<Integer> cola = new Cola<>();
        Stack<Integer> auxiliar = new Stack<>();

        while (!pila.isEmpty()) {
            int numero = pila.pop();

            if (numero % 2 == 0) {
                cola.encolar(numero);
            }

            auxiliar.push(numero);
        }

        while (!auxiliar.isEmpty()) {
            pila.push(auxiliar.pop());
        }

        return cola;
    }

    /**
     * Comprueba si un número entero es primo.
     *
     * @param numero número que se desea evaluar.
     * @return true si es primo; false en caso contrario.
     *
     * Complejidad: O(raíz cuadrada de n), para números positivos.
     */
    private static boolean esPrimo(int numero) {
        if (numero < 2) {
            return false;
        }

        for (int i = 2; i <= numero / i; i++) {
            if (numero % i == 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Extrae los números primos de una pila y los almacena en una cola.
     *
     * La pila original se restaura después del procedimiento,
     * conservando su contenido y orden.
     *
     * @param pila pila de números enteros que se desea analizar.
     * @return cola con los números primos encontrados.
     * @throws IllegalArgumentException si la pila es null.
     *
     * Complejidad: O(n * raíz cuadrada de m), donde m es el mayor
     * número positivo evaluado.
     */
    public static Cola<Integer> pasarPrimos(Stack<Integer> pila) {
        if (pila == null) {
            throw new IllegalArgumentException("La pila no puede ser null.");
        }

        Cola<Integer> cola = new Cola<>();
        Stack<Integer> auxiliar = new Stack<>();

        while (!pila.isEmpty()) {
            int numero = pila.pop();

            if (esPrimo(numero)) {
                cola.encolar(numero);
            }

            auxiliar.push(numero);
        }

        while (!auxiliar.isEmpty()) {
            pila.push(auxiliar.pop());
        }

        return cola;
    }

    /**
     * Elimina la primera aparición de un valor en una pila.
     *
     * La búsqueda comienza en el tope. Los elementos anteriores al
     * valor buscado se retiran temporalmente y luego se restauran.
     *
     * @param pila pila en la que se realizará la búsqueda.
     * @param valor elemento que se desea eliminar.
     * @param <T> tipo de los elementos almacenados.
     * @return true si se encontró y eliminó el elemento; false si no existe.
     * @throws IllegalArgumentException si la pila es null.
     *
     * Complejidad: O(n).
     */
    public static <T> boolean eliminar(Stack<T> pila, T valor) {
        if (pila == null) {
            throw new IllegalArgumentException("La pila no puede ser null.");
        }

        Stack<T> auxiliar = new Stack<>();
        boolean eliminado = false;

        while (!pila.isEmpty() && !eliminado) {
            T dato = pila.pop();

            if (Objects.equals(dato, valor)) {
                eliminado = true;
            } else {
                auxiliar.push(dato);
            }
        }

        while (!auxiliar.isEmpty()) {
            pila.push(auxiliar.pop());
        }

        return eliminado;
    }

    /**
     * Elimina la primera aparición de un valor en esta pila.
     *
     * A diferencia del método estático eliminar, esta implementación
     * modifica directamente los enlaces de los nodos.
     *
     * @param valor elemento que se desea eliminar.
     * @return true si se encontró y eliminó; false si no existe.
     *
     * Complejidad: O(n).
     */
    public boolean eliminar(T valor) {
        if (isEmpty()) {
            return false;
        }

        if (Objects.equals(top.getData(), valor)) {
            top = top.getNext();
            size--;
            return true;
        }

        Node<T> anterior = top;

        while (anterior.getNext() != null
                && !Objects.equals(anterior.getNext().getData(), valor)) {
            anterior = anterior.getNext();
        }

        if (anterior.getNext() == null) {
            return false;
        }

        anterior.setNext(anterior.getNext().getNext());
        size--;

        return true;
    }

    /**
     * Elimina todos los elementos de la pila.
     *
     * Después de ejecutar este método, la pila queda vacía y puede
     * reutilizarse normalmente.
     *
     * Complejidad: O(1).
     */
    public void clear() {
        top = null;
        size = 0;
    }
}