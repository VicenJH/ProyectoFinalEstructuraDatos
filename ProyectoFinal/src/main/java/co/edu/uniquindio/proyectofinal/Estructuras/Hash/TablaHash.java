package co.edu.uniquindio.proyectofinal.Estructuras.Hash;

import java.util.Objects;

/**
 * Tabla hash genérica con resolución de colisiones por encadenamiento.
 *
 * La capacidad aumenta automáticamente cuando el factor de carga supera
 * 0.75, evitando que la tabla pierda rendimiento al crecer el sistema.
 * Las operaciones de inserción, búsqueda y eliminación tienen complejidad
 * promedio O(1) y peor caso O(n).
 */
public class TablaHash<K, V> {

    private static final double FACTOR_CARGA_MAXIMO = 0.75;

    private static class Entrada<K, V> {
        private final K clave;
        private V valor;
        private Entrada<K, V> siguiente;

        private Entrada(K clave, V valor, Entrada<K, V> siguiente) {
            this.clave = clave;
            this.valor = valor;
            this.siguiente = siguiente;
        }
    }

    private Entrada<K, V>[] tabla;
    private int capacidad;
    private int tamano;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public TablaHash(int capacidadInicial) {
        if (capacidadInicial <= 0) {
            throw new IllegalArgumentException("La capacidad inicial debe ser mayor que cero.");
        }
        this.capacidad = capacidadInicial;
        this.tabla = (Entrada<K, V>[]) new Entrada[capacidadInicial];
        this.tamano = 0;
    }

    private int obtenerIndice(K clave) {
        if (clave == null) {
            throw new IllegalArgumentException("La clave no puede ser null.");
        }
        return Math.floorMod(clave.hashCode(), capacidad);
    }

    /** Inserta una nueva entrada o reemplaza el valor de una clave existente. */
    public void insertar(K clave, V valor) {
        if (valor == null) {
            throw new IllegalArgumentException("El valor asociado a una clave no puede ser null.");
        }
        int indice = obtenerIndice(clave);
        Entrada<K, V> actual = tabla[indice];

        while (actual != null) {
            if (Objects.equals(actual.clave, clave)) {
                actual.valor = valor;
                return;
            }
            actual = actual.siguiente;
        }

        if ((double) (tamano + 1) / capacidad > FACTOR_CARGA_MAXIMO) {
            final int nuevaCapacidad;
            try {
                nuevaCapacidad = Math.multiplyExact(capacidad, 2);
            } catch (ArithmeticException ex) {
                throw new IllegalStateException("La tabla hash alcanzó su capacidad máxima.", ex);
            }
            redimensionar(nuevaCapacidad);
            indice = obtenerIndice(clave);
        }

        tabla[indice] = new Entrada<>(clave, valor, tabla[indice]);
        tamano++;
    }

    /** Devuelve el valor asociado a la clave o null cuando no existe. */
    public V buscar(K clave) {
        int indice = obtenerIndice(clave);
        Entrada<K, V> actual = tabla[indice];

        while (actual != null) {
            if (Objects.equals(actual.clave, clave)) {
                return actual.valor;
            }
            actual = actual.siguiente;
        }
        return null;
    }

    /** Permite distinguir una clave ausente de una clave asociada a null. */
    public boolean contieneClave(K clave) {
        int indice = obtenerIndice(clave);
        Entrada<K, V> actual = tabla[indice];
        while (actual != null) {
            if (Objects.equals(actual.clave, clave)) {
                return true;
            }
            actual = actual.siguiente;
        }
        return false;
    }

    public boolean eliminar(K clave) {
        int indice = obtenerIndice(clave);
        Entrada<K, V> actual = tabla[indice];
        Entrada<K, V> anterior = null;

        while (actual != null) {
            if (Objects.equals(actual.clave, clave)) {
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void redimensionar(int nuevaCapacidad) {
        if (nuevaCapacidad <= capacidad) {
            throw new IllegalArgumentException("La nueva capacidad debe ser mayor que la actual.");
        }
        Entrada<K, V>[] anterior = tabla;
        tabla = (Entrada<K, V>[]) new Entrada[nuevaCapacidad];
        int capacidadAnterior = capacidad;
        capacidad = nuevaCapacidad;

        for (int i = 0; i < capacidadAnterior; i++) {
            Entrada<K, V> actual = anterior[i];
            while (actual != null) {
                Entrada<K, V> siguiente = actual.siguiente;
                int nuevoIndice = obtenerIndice(actual.clave);
                actual.siguiente = tabla[nuevoIndice];
                tabla[nuevoIndice] = actual;
                actual = siguiente;
            }
        }
    }

    public int getTamano() {
        return tamano;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public double getFactorCarga() {
        return (double) tamano / capacidad;
    }

    public boolean isEmpty() {
        return tamano == 0;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void clear() {
        tabla = (Entrada<K, V>[]) new Entrada[capacidad];
        tamano = 0;
    }
}
