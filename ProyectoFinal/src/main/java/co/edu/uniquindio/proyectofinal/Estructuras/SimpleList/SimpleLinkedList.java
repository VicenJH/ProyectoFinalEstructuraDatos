package co.edu.uniquindio.proyectofinal.Estructuras.SimpleList;

import co.edu.uniquindio.proyectofinal.Estructuras.SimpleList.NodeSimple;

public class SimpleLinkedList<T> {
    private int size;
    private NodeSimple<T> firstNode;

    public SimpleLinkedList() {
        this.size = 0;
        this.firstNode = null;
    }

    public int getSize() { return size; }
    public boolean isEmpty() { return firstNode == null; }

    public void addFirst(T data) {
        NodeSimple<T> newNode = new NodeSimple<>(data);
        if (isEmpty()) {
            firstNode = newNode;
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
        } else {
            NodeSimple<T> aux = firstNode;
            while (aux.getNext() != null) {
                aux = aux.getNext();
            }
            aux.setNext(newNode);
        }
        size++;
    }

    public T getNodeValue(int position) {
        if (position < 0 || position >= size) {
            throw new IndexOutOfBoundsException("Posición inválida: " + position);
        }
        NodeSimple<T> aux = firstNode;
        for (int i = 0; i < position; i++) {
            aux = aux.getNext();
        }
        return aux.getData();
    }

    public boolean remove(T data) {
        if (isEmpty()) return false;

        if (firstNode.getData().equals(data)) {
            firstNode = firstNode.getNext();
            size--;
            return true;
        }

        NodeSimple<T> aux = firstNode;
        while (aux.getNext() != null && !aux.getNext().getData().equals(data)) {
            aux = aux.getNext();
        }

        if (aux.getNext() == null) return false;

        aux.setNext(aux.getNext().getNext());
        size--;
        return true;
    }

    public void clear() {
        size = 0;
        firstNode = null;
    }
}