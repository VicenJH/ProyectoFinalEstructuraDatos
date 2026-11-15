package co.edu.uniquindio.proyectofinal.Estructuras.Grafos;

import co.edu.uniquindio.proyectofinal.Estructuras.SimpleList.SimpleLinkedList;

public class Grafo<T> {

    public static class Vertice<T> {
        private T dato; // Encapsulado
        private SimpleLinkedList<Arista<T>> adyacentes; // Encapsulado

        public Vertice(T dato) {
            this.dato = dato;
            this.adyacentes = new SimpleLinkedList<>();
        }

        public T getDato() { return dato; }

        // Getter necesario para acceder desde fuera del paquete
        public SimpleLinkedList<Arista<T>> getAdyacentes() {
            return adyacentes;
        }
    }

    public static class Arista<T> {
        private Vertice<T> destino;
        private double peso;

        public Arista(Vertice<T> destino, double peso) {
            this.destino = destino;
            this.peso = peso;
        }

        public Vertice<T> getDestino() { return destino; }
        public double getPeso() { return peso; }
    }

    private SimpleLinkedList<Vertice<T>> vertices; // Encapsulado

    public Grafo() {
        this.vertices = new SimpleLinkedList<>();
    }

    public void agregarVertice(T dato) {
        if (buscarVertice(dato) == null) {
            vertices.addLast(new Vertice<>(dato));
        }
    }

    public Vertice<T> buscarVertice(T dato) {
        for (int i = 0; i < vertices.getSize(); i++) {
            Vertice<T> v = vertices.getNodeValue(i);
            if (v.getDato().equals(dato)) {
                return v;
            }
        }
        return null;
    }

    public void agregarArista(T origen, T destino, double peso) {
        Vertice<T> vOrigen = buscarVertice(origen);
        Vertice<T> vDestino = buscarVertice(destino);

        if (vOrigen != null && vDestino != null) {
            // Usando el getter o accediendo de forma segura
            vOrigen.getAdyacentes().addLast(new Arista<>(vDestino, peso));
        }
    }

    public SimpleLinkedList<Vertice<T>> getVertices() {
        return vertices;
    }
}