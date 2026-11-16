
package co.edu.uniquindio.proyectofinal.Estructuras.Colas;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Implementa una cola genérica mediante nodos enlazados.
 *
 * La cola sigue el principio FIFO (First In, First Out):
 * el primer elemento que ingresa es el primero en salir.
 *
 * Se utiliza en el proyecto Fintech para administrar operaciones
 * programadas y procesar elementos en el orden de llegada.
 *
 * También funciona como estructura auxiliar para los recorridos
 * por niveles del árbol binario.
 *
 * La implementación es manual y no utiliza las colecciones
 * Queue, LinkedList ni ArrayDeque de Java.
 *
 * @param <T> tipo de elementos almacenados en la cola.
 */
public class Cola<T> implements Iterable<T> {

    /**
     * Cantidad de elementos almacenados.
     */
    private int size;

    /**
     * Referencia al primer nodo de la cola.
     * Es el siguiente elemento que será retirado.
     */
    private NodeSimple<T> firstNode;

    /**
     * Referencia al último nodo de la cola.
     * Es el punto donde se insertan los elementos nuevos.
     */
    private NodeSimple<T> lastNode;

    /**
     * Construye una cola vacía.
     *
     * Complejidad: O(1).
     */
    public Cola() {
        this.size = 0;
        this.firstNode = null;
        this.lastNode = null;
    }

    /**
     * Obtiene la cantidad de elementos almacenados.
     *
     * @return número de elementos de la cola.
     *
     * Complejidad: O(1).
     */
    public int getSize() {
        return size;
    }

    /**
     * Comprueba si la cola está vacía.
     *
     * @return true si no contiene elementos; false en caso contrario.
     *
     * Complejidad: O(1).
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Inserta un elemento al final de la cola.
     *
     * Este método representa la operación habitual de inserción FIFO.
     *
     * @param data elemento que se desea insertar.
     *
     * Complejidad: O(1).
     */
    public void encolar(T data) {
        encolarFinal(data);
    }

    /**
     * Inserta un elemento al final de la cola.
     *
     * Si la cola está vacía, el nuevo nodo será tanto el primero
     * como el último.
     *
     * @param data elemento que se desea insertar.
     *
     * Complejidad: O(1).
     */
    public void encolarFinal(T data) {
        NodeSimple<T> nuevo = new NodeSimple<>(data);

        if (isEmpty()) {
            firstNode = nuevo;
            lastNode = nuevo;
        } else {
            lastNode.setNext(nuevo);
            lastNode = nuevo;
        }

        size++;
    }

    /**
     * Inserta un elemento al inicio de la cola.
     *
     * Se conserva este método por compatibilidad con las operaciones
     * auxiliares de la estructura.
     *
     * Esta operación modifica el orden FIFO habitual si se utiliza
     * como sustituto de encolar().
     *
     * @param data elemento que se desea insertar.
     *
     * Complejidad: O(1).
     */
    public void encolarInicio(T data) {
        NodeSimple<T> nuevo = new NodeSimple<>(data);

        if (isEmpty()) {
            firstNode = nuevo;
            lastNode = nuevo;
        } else {
            nuevo.setNext(firstNode);
            firstNode = nuevo;
        }

        size++;
    }

    /**
     * Retira y devuelve el primer elemento de la cola.
     *
     * Después de retirar el nodo, actualiza la referencia al primero.
     * Si la cola queda vacía, también establece lastNode en null.
     *
     * @return elemento que se encontraba al inicio de la cola.
     * @throws NoSuchElementException si la cola está vacía.
     *
     * Complejidad: O(1).
     */
    public T desencolar() {
        if (isEmpty()) {
            throw new NoSuchElementException(
                    "No se puede retirar un elemento de una cola vacía."
            );
        }

        T data = firstNode.getData();

        firstNode = firstNode.getNext();
        size--;

        if (firstNode == null) {
            lastNode = null;
        }

        return data;
    }

    /**
     * Consulta el primer elemento sin retirarlo.
     *
     * @return elemento ubicado al inicio de la cola.
     * @throws NoSuchElementException si la cola está vacía.
     *
     * Complejidad: O(1).
     */
    public T consultarPrimero() {
        if (isEmpty()) {
            throw new NoSuchElementException(
                    "No se puede consultar una cola vacía."
            );
        }

        return firstNode.getData();
    }

    /**
     * Consulta el último elemento sin retirarlo.
     *
     * @return elemento ubicado al final de la cola.
     * @throws NoSuchElementException si la cola está vacía.
     *
     * Complejidad: O(1).
     */
    public T consultarUltimo() {
        if (isEmpty()) {
            throw new NoSuchElementException(
                    "No se puede consultar una cola vacía."
            );
        }

        return lastNode.getData();
    }

    /**
     * Crea una copia independiente de la estructura de la cola.
     *
     * Los elementos se copian conservando el orden original.
     * Los objetos almacenados no se clonan internamente.
     *
     * @return nueva cola con los mismos elementos y orden.
     *
     * Complejidad: O(n), donde n es el tamaño de la cola.
     */
    public Cola<T> clonarCola() {
        Cola<T> copia = new Cola<>();

        NodeSimple<T> actual = firstNode;

        while (actual != null) {
            copia.encolar(actual.getData());
            actual = actual.getNext();
        }

        return copia;
    }

    /**
     * Invierte el orden de los elementos de la cola.
     *
     * Se modifican los enlaces de los nodos directamente,
     * evitando utilizar recursividad o estructuras auxiliares.
     *
     * @implNote El elemento que estaba al final pasa a ser el primero.
     *
     * Complejidad: O(n), donde n es el tamaño de la cola.
     */
    public void invertirCola() {
        if (isEmpty() || size == 1) {
            return;
        }

        NodeSimple<T> anterior = null;
        NodeSimple<T> actual = firstNode;
        NodeSimple<T> siguiente = null;

        lastNode = firstNode;

        while (actual != null) {
            siguiente = actual.getNext();

            actual.setNext(anterior);

            anterior = actual;
            actual = siguiente;
        }

        firstNode = anterior;
    }

    /**
     * Compara dos colas para determinar si contienen los mismos
     * elementos en el mismo orden.
     *
     * La comparación se realiza utilizando Objects.equals,
     * por lo que también admite elementos null.
     *
     * La cola recibida no se modifica.
     *
     * @param otra cola que se desea comparar.
     * @return true si ambas colas tienen el mismo tamaño y contenido
     *         en el mismo orden; false en caso contrario.
     *
     * Complejidad: O(n), donde n es el tamaño de las colas.
     */
    public boolean sonIdenticas(Cola<T> otra) {
        if (otra == null || this.size != otra.size) {
            return false;
        }

        NodeSimple<T> actualA = this.firstNode;
        NodeSimple<T> actualB = otra.firstNode;

        while (actualA != null) {
            if (!Objects.equals(
                    actualA.getData(),
                    actualB.getData()
            )) {
                return false;
            }

            actualA = actualA.getNext();
            actualB = actualB.getNext();
        }

        return true;
    }

    /**
     * Elimina todos los elementos de la cola.
     *
     * La estructura queda vacía y puede reutilizarse normalmente.
     *
     * Complejidad: O(1).
     */
    public void clear() {
        firstNode = null;
        lastNode = null;
        size = 0;
    }

    /**
     * Permite recorrer una copia lógica de la cola en orden FIFO sin modificarla.
     * Complejidad total del recorrido: O(n).
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private NodeSimple<T> actual = firstNode;

            @Override
            public boolean hasNext() {
                return actual != null;
            }

            @Override
            public T next() {
                if (actual == null) {
                    throw new NoSuchElementException();
                }
                T dato = actual.getData();
                actual = actual.getNext();
                return dato;
            }
        };
    }
}
