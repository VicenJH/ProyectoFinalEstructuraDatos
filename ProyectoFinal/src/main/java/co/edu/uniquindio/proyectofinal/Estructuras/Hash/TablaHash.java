package co.edu.uniquindio.proyectofinal.Estructuras.Hash;

public class TablaHash<K, V> {
    private class Entrada<K, V> {
        K clave;
        V valor;
        Entrada<K, V> siguiente;

        public Entrada(K clave, V valor) {
            this.clave = clave;
            this.valor = valor;
            this.siguiente = null;
        }
    }

    private Entrada<K, V>[] tabla;
    private int capacidad;
    private int tamano;

    @SuppressWarnings("unchecked")
    public TablaHash(int capacidad) {
        this.capacidad = capacidad;
        this.tabla = new Entrada[capacidad];
        this.tamano = 0;
    }

    private int obtenerIndice(K clave) {
        return Math.abs(clave.hashCode()) % capacidad;
    }

    public void insertar(K clave, V valor) {
        int indice = obtenerIndice(clave);
        Entrada<K, V> cabeza = tabla[indice];

        // Verificar si la clave ya existe para actualizarla
        Entrada<K, V> actual = cabeza;
        while (actual != null) {
            if (actual.clave.equals(clave)) {
                actual.valor = valor;
                return;
            }
            actual = actual.siguiente;
        }

        // Si no existe, insertar al inicio de la cubeta
        Entrada<K, V> nuevaEntrada = new Entrada<>(clave, valor);
        nuevaEntrada.siguiente = cabeza;
        tabla[indice] = nuevaEntrada;
        tamano++;
    }

    public V buscar(K clave) {
        int indice = obtenerIndice(clave);
        Entrada<K, V> actual = tabla[indice];

        while (actual != null) {
            if (actual.clave.equals(clave)) {
                return actual.valor;
            }
            actual = actual.siguiente;
        }
        return null; // No encontrado
    }

    public boolean eliminar(K clave) {
        int indice = obtenerIndice(clave);
        Entrada<K, V> actual = tabla[indice];
        Entrada<K, V> anterior = null;

        while (actual != null) {
            if (actual.clave.equals(clave)) {
                if (anterior == null) {
                    tabla[indice] = actual.siguiente;
                } else {
                    anterior.siguiente = actual.siguiente;
                }
                tamano--;
                return true;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
        return false;
    }

    public int getTamano() { return tamano; }
    public boolean isEmpty() { return tamano == 0; }
}