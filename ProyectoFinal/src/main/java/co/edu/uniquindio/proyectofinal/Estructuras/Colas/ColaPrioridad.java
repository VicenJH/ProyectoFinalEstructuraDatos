package co.edu.uniquindio.proyectofinal.Estructuras.Colas;

import co.edu.uniquindio.proyectofinal.Estructuras.SimpleList.SimpleLinkedList;

import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * Cola de prioridad genérica implementada como montículo binario (min-heap)
 * sobre un arreglo propio.
 *
 * El elemento con menor valor según el comparador sale primero. Si dos
 * elementos empatan, sale primero el que se insertó antes (orden estable).
 *
 * En el proyecto Fintech se usa para procesar las operaciones programadas
 * respetando la prioridad por fecha y hora de ejecución (requisito 4.3).
 *
 * Complejidad: encolar y desencolar O(log n); consultarPrimero O(1).
 *
 * @param <T> tipo de elementos almacenados.
 */
public class ColaPrioridad<T> {

    /** Elemento del montículo con su orden de llegada para desempates. */
    private static class Entrada<T> {
        private final T dato;
        private final long orden;

        private Entrada(T dato, long orden) {
            this.dato = dato;
            this.orden = orden;
        }
    }

    private Entrada<T>[] heap;
    private int size;
    private long contador;
    private final Comparator<T> comparador;

    /**
     * Construye una cola de prioridad vacía.
     *
     * @param comparador criterio de orden; el menor sale primero.
     * @throws IllegalArgumentException si el comparador es null.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ColaPrioridad(Comparator<T> comparador) {
        if (comparador == null) {
            throw new IllegalArgumentException("El comparador no puede ser null.");
        }
        this.comparador = comparador;
        this.heap = (Entrada<T>[]) new Entrada[16];
        this.size = 0;
        this.contador = 0;
    }

    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Inserta un elemento. Complejidad: O(log n).
     *
     * @throws IllegalArgumentException si el dato es null.
     */
    public void encolar(T dato) {
        if (dato == null) {
            throw new IllegalArgumentException("No se pueden encolar elementos null.");
        }

        if (size == heap.length) {
            crecer();
        }

        heap[size] = new Entrada<>(dato, contador++);
        subir(size);
        size++;
    }

    /**
     * Retira y devuelve el elemento de mayor prioridad. Complejidad: O(log n).
     *
     * @throws NoSuchElementException si la cola está vacía.
     */
    public T desencolar() {
        if (isEmpty()) {
            throw new NoSuchElementException("No se puede retirar un elemento de una cola vacía.");
        }

        T dato = heap[0].dato;

        size--;
        heap[0] = heap[size];
        heap[size] = null;

        if (size > 0) {
            bajar(0);
        }

        return dato;
    }

    /**
     * Consulta el elemento de mayor prioridad sin retirarlo. Complejidad: O(1).
     *
     * @throws NoSuchElementException si la cola está vacía.
     */
    public T consultarPrimero() {
        if (isEmpty()) {
            throw new NoSuchElementException("No se puede consultar una cola vacía.");
        }
        return heap[0].dato;
    }

    /**
     * Devuelve los elementos ordenados por prioridad sin modificar la cola.
     * Útil para mostrar las operaciones pendientes en la interfaz.
     *
     * Complejidad: O(n log n).
     */
    public SimpleLinkedList<T> aListaOrdenada() {
        ColaPrioridad<T> copia = new ColaPrioridad<>(comparador);
        copia.heap = newHeap(heap.length);
        System.arraycopy(heap, 0, copia.heap, 0, size);
        copia.size = size;
        copia.contador = contador;

        SimpleLinkedList<T> resultado = new SimpleLinkedList<>();
        while (!copia.isEmpty()) {
            resultado.addLast(copia.desencolar());
        }
        return resultado;
    }

    /** Elimina todos los elementos. */
    public void clear() {
        heap = newHeap(16);
        size = 0;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Entrada<T>[] newHeap(int capacidad) {
        return (Entrada<T>[]) new Entrada[capacidad];
    }

    private void crecer() {
        Entrada<T>[] nuevo = newHeap(heap.length * 2);
        System.arraycopy(heap, 0, nuevo, 0, size);
        heap = nuevo;
    }

    private boolean tieneMasPrioridad(Entrada<T> a, Entrada<T> b) {
        int comparacion = comparador.compare(a.dato, b.dato);
        return comparacion < 0 || (comparacion == 0 && a.orden < b.orden);
    }

    private void intercambiar(int i, int j) {
        Entrada<T> temporal = heap[i];
        heap[i] = heap[j];
        heap[j] = temporal;
    }

    private void subir(int i) {
        while (i > 0) {
            int padre = (i - 1) / 2;

            if (tieneMasPrioridad(heap[i], heap[padre])) {
                intercambiar(i, padre);
                i = padre;
            } else {
                break;
            }
        }
    }

    private void bajar(int i) {
        while (true) {
            int izquierdo = 2 * i + 1;
            int derecho = izquierdo + 1;
            int menor = i;

            if (izquierdo < size && tieneMasPrioridad(heap[izquierdo], heap[menor])) {
                menor = izquierdo;
            }
            if (derecho < size && tieneMasPrioridad(heap[derecho], heap[menor])) {
                menor = derecho;
            }
            if (menor == i) {
                break;
            }

            intercambiar(i, menor);
            i = menor;
        }
    }
}