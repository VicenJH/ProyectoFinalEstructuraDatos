package co.edu.uniquindio.proyectofinal.Estructuras.SimpleList;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Lista simplemente enlazada generica.
 *
 * Mantiene referencias al primer y ultimo nodo para que addLast sea O(1).
 * Los enlaces se gestionan manualmente, sin utilizar colecciones de Java.
 *
 * Implementa Iterable para poder recorrerla con for-each en O(n); recorrerla
 * con getNodeValue(i) cuesta O(n^2).
 */
public class SimpleLinkedList<T> implements Iterable<T> {

    private int size;
    private NodeSimple<T> firstNode;
    private NodeSimple<T> lastNode;

    public SimpleLinkedList() {
        this.size = 0;
        this.firstNode = null;
        this.lastNode = null;
    }

    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void addFirst(T data) {
        NodeSimple<T> newNode = new NodeSimple<>(data);

        if (isEmpty()) {
            firstNode = newNode;
            lastNode = newNode;
        } else {
            newNode.setNext(firstNode);
            firstNode = newNode;
        }

        size++;
    }

    public void addLast(T data) {
        NodeSimple<T> newNode = new NodeSimple<>(data);

        if (isEmpty()) {
            firstNode = newNode;
            lastNode = newNode;
        } else {
            lastNode.setNext(newNode);
            lastNode = newNode;
        }

        size++;
    }

    public T getNodeValue(int position) {
        if (position < 0 || position >= size) {
            throw new IndexOutOfBoundsException(
                    "Posicion invalida: " + position + ". Tamano actual: " + size
            );
        }

        NodeSimple<T> current = firstNode;

        for (int i = 0; i < position; i++) {
            current = current.getNext();
        }

        return current.getData();
    }

    /**
     * Elimina la primera aparicion del dato.
     * Objects.equals permite almacenar y buscar valores null sin provocar
     * NullPointerException.
     *
     * @return true si se elimino un nodo; false si no se encontro.
     */
    public boolean remove(T data) {
        if (isEmpty()) {
            return false;
        }

        if (Objects.equals(firstNode.getData(), data)) {
            firstNode = firstNode.getNext();
            size--;

            if (size == 0) {
                lastNode = null;
            }

            return true;
        }

        NodeSimple<T> previous = firstNode;
        NodeSimple<T> current = firstNode.getNext();

        while (current != null && !Objects.equals(current.getData(), data)) {
            previous = current;
            current = current.getNext();
        }

        if (current == null) {
            return false;
        }

        previous.setNext(current.getNext());

        if (current == lastNode) {
            lastNode = previous;
        }

        size--;
        return true;
    }

    public void clear() {
        firstNode = null;
        lastNode = null;
        size = 0;
    }

    /**
     * Recorre la lista de inicio a fin. No soporta eliminar durante el recorrido.
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