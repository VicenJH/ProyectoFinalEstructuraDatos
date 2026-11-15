
package co.edu.uniquindio.proyectofinal.Estructuras.Arbol;

import co.edu.uniquindio.proyectofinal.Estructuras.Colas.Cola;

import java.util.NoSuchElementException;

/**
 * Implementa un árbol binario de búsqueda (ABB) genérico.
 *
 * Cada elemento debe implementar Comparable para establecer
 * el orden entre los nodos.
 *
 * La organización cumple las siguientes reglas:
 *
 * - Los elementos menores se almacenan en el subárbol izquierdo.
 * - Los elementos mayores se almacenan en el subárbol derecho.
 * - No se permiten elementos duplicados según compareTo().
 *
 * El árbol conserva referencias a los nodos padre, izquierdo y derecho.
 *
 * En el proyecto Fintech puede utilizarse para organizar usuarios
 * según un criterio de comparación, como sus puntos acumulados.
 *
 * @param <T> tipo de elementos almacenados en el árbol.
 */
public class ArbolBinario<T extends Comparable<T>> {

    /**
     * Cantidad de nodos almacenados en el árbol.
     */
    private int peso;

    /**
     * Nodo principal del árbol.
     */
    private NodeArbol<T> raiz;

    /**
     * Construye un árbol binario de búsqueda vacío.
     *
     * Complejidad: O(1).
     */
    public ArbolBinario() {
        this.peso = 0;
        this.raiz = null;
    }

    /**
     * Obtiene la cantidad de nodos almacenados.
     *
     * @return número de elementos del árbol.
     *
     * Complejidad: O(1).
     */
    public int getPeso() {
        return peso;
    }

    /**
     * Modifica manualmente el peso del árbol.
     *
     * Este método se conserva por compatibilidad con la versión
     * original, pero no se recomienda utilizarlo porque puede
     * generar inconsistencias entre el contador y los nodos reales.
     *
     * Para mantener la integridad, se recomienda utilizar
     * insertar(), eliminar() y setRaiz().
     *
     * @param peso nuevo valor del contador.
     * @throws IllegalArgumentException si el valor es negativo.
     *
     * Complejidad: O(1).
     */
    @Deprecated
    public void setPeso(int peso) {
        if (peso < 0) {
            throw new IllegalArgumentException(
                    "El peso del árbol no puede ser negativo."
            );
        }

        this.peso = peso;
    }

    /**
     * Obtiene la raíz del árbol.
     *
     * @return nodo raíz o null si el árbol está vacío.
     *
     * Complejidad: O(1).
     */
    public NodeArbol<T> getRaiz() {
        return raiz;
    }

    /**
     * Establece una nueva raíz y actualiza el peso del árbol.
     *
     * El nodo establecido como raíz no debe tener un padre.
     * Se cuenta la cantidad real de nodos de la estructura recibida.
     *
     * @param raiz nueva raíz del árbol.
     *
     * Complejidad: O(n), donde n es la cantidad de nodos recibidos.
     */
    public void setRaiz(NodeArbol<T> raiz) {
        this.raiz = raiz;

        if (this.raiz != null) {
            this.raiz.setPadre(null);
        }

        this.peso = obtenerPeso(this.raiz);
    }

    /**
     * Comprueba si el árbol está vacío.
     *
     * @return true si no tiene raíz; false en caso contrario.
     *
     * Complejidad: O(1).
     */
    public boolean isEmpty() {
        return raiz == null;
    }

    /**
     * Realiza el recorrido inorden.
     *
     * El recorrido visita primero el subárbol izquierdo,
     * después el nodo actual y finalmente el subárbol derecho.
     *
     * En un árbol binario de búsqueda, muestra los elementos
     * en orden ascendente según compareTo().
     *
     * Complejidad: O(n).
     */
    public void recorrerInorden() {
        recorrerInorden(raiz);
    }

    /**
     * Método auxiliar recursivo del recorrido inorden.
     *
     * @param nodo nodo desde el que comienza el recorrido.
     */
    private void recorrerInorden(NodeArbol<T> nodo) {
        if (nodo == null) {
            return;
        }

        recorrerInorden(nodo.getIzquierdo());
        System.out.print(nodo.getElemento() + " ");
        recorrerInorden(nodo.getDerecho());
    }

    /**
     * Realiza el recorrido preorden.
     *
     * Visita primero el nodo actual, después el subárbol izquierdo
     * y finalmente el subárbol derecho.
     *
     * Complejidad: O(n).
     */
    public void recorrerPreorden() {
        recorrerPreorden(raiz);
    }

    /**
     * Método auxiliar recursivo del recorrido preorden.
     *
     * @param nodo nodo desde el que comienza el recorrido.
     */
    private void recorrerPreorden(NodeArbol<T> nodo) {
        if (nodo == null) {
            return;
        }

        System.out.print(nodo.getElemento() + " ");
        recorrerPreorden(nodo.getIzquierdo());
        recorrerPreorden(nodo.getDerecho());
    }

    /**
     * Realiza el recorrido postorden.
     *
     * Visita primero el subárbol izquierdo, después el derecho
     * y finalmente el nodo actual.
     *
     * Complejidad: O(n).
     */
    public void recorrerPostorden() {
        recorrerPostorden(raiz);
    }

    /**
     * Método auxiliar recursivo del recorrido postorden.
     *
     * @param nodo nodo desde el que comienza el recorrido.
     */
    private void recorrerPostorden(NodeArbol<T> nodo) {
        if (nodo == null) {
            return;
        }

        recorrerPostorden(nodo.getIzquierdo());
        recorrerPostorden(nodo.getDerecho());
        System.out.print(nodo.getElemento() + " ");
    }

    /**
     * Calcula la cantidad real de nodos del árbol.
     *
     * Este método cuenta los nodos recorriendo toda la estructura.
     *
     * @return cantidad de nodos existentes.
     *
     * Complejidad: O(n).
     */
    public int obtenerPeso() {
        return obtenerPeso(raiz);
    }

    /**
     * Calcula recursivamente el peso de un subárbol.
     *
     * @param nodo raíz del subárbol que se desea contar.
     * @return cantidad de nodos del subárbol.
     */
    private int obtenerPeso(NodeArbol<T> nodo) {
        if (nodo == null) {
            return 0;
        }

        return 1
                + obtenerPeso(nodo.getIzquierdo())
                + obtenerPeso(nodo.getDerecho());
    }

    /**
     * Obtiene la altura del árbol.
     *
     * La altura se mide en niveles:
     * un árbol vacío tiene altura 0 y un árbol con una raíz
     * tiene altura 1.
     *
     * @return altura total del árbol.
     *
     * Complejidad: O(n).
     */
    public int ObtenerAltura() {
        return obtenerAltura(raiz);
    }

    /**
     * Calcula recursivamente la altura de un subárbol.
     *
     * @param nodo raíz del subárbol.
     * @return altura del subárbol.
     */
    private int obtenerAltura(NodeArbol<T> nodo) {
        if (nodo == null) {
            return 0;
        }

        int alturaIzquierda = obtenerAltura(nodo.getIzquierdo());
        int alturaDerecha = obtenerAltura(nodo.getDerecho());

        return 1 + Math.max(alturaIzquierda, alturaDerecha);
    }

    /**
     * Obtiene la altura del árbol utilizando el nombre convencional.
     *
     * Se conserva ObtenerAltura() por compatibilidad con el código
     * original.
     *
     * @return altura total del árbol.
     *
     * Complejidad: O(n).
     */
    public int obtenerAltura() {
        return ObtenerAltura();
    }

    /**
     * Realiza un recorrido por amplitud o niveles.
     *
     * Utiliza una cola para visitar primero la raíz y después
     * los nodos de cada nivel, de izquierda a derecha.
     *
     * @implNote Utiliza la estructura Cola implementada manualmente
     * en el proyecto.
     *
     * Complejidad: O(n).
     */
    public void recorridoAmplitud() {
        if (isEmpty()) {
            return;
        }

        Cola<NodeArbol<T>> cola = new Cola<>();
        cola.encolar(raiz);

        while (!cola.isEmpty()) {
            NodeArbol<T> actual = cola.desencolar();

            System.out.print(actual.getElemento() + " ");

            if (actual.getIzquierdo() != null) {
                cola.encolar(actual.getIzquierdo());
            }

            if (actual.getDerecho() != null) {
                cola.encolar(actual.getDerecho());
            }
        }
    }

    /**
     * Comprueba si un elemento existe en el árbol.
     *
     * La búsqueda utiliza la propiedad de orden del árbol binario
     * de búsqueda para descartar subárboles innecesarios.
     *
     * @param elemento elemento que se desea buscar.
     * @return true si existe; false en caso contrario.
     * @throws IllegalArgumentException si el elemento es null.
     *
     * Complejidad: O(h), donde h es la altura del árbol.
     */
    public boolean existe(T elemento) {
        validarElemento(elemento);
        return existe(raiz, elemento);
    }

    /**
     * Método auxiliar recursivo para buscar un elemento.
     *
     * @param nodo nodo actual de búsqueda.
     * @param elemento elemento buscado.
     * @return true si se encuentra; false en caso contrario.
     */
    private boolean existe(NodeArbol<T> nodo, T elemento) {
        if (nodo == null) {
            return false;
        }

        int comparacion = elemento.compareTo(nodo.getElemento());

        if (comparacion == 0) {
            return true;
        }

        if (comparacion < 0) {
            return existe(nodo.getIzquierdo(), elemento);
        }

        return existe(nodo.getDerecho(), elemento);
    }

    /**
     * Inserta un nuevo elemento en el árbol.
     *
     * El elemento se ubica según el resultado de compareTo().
     * No se permiten elementos que se consideren iguales
     * según dicha comparación.
     *
     * @param elemento elemento que se desea insertar.
     * @throws IllegalArgumentException si el elemento es null
     *                                  o ya existe en el árbol.
     *
     * Complejidad: O(h), donde h es la altura del árbol.
     */
    public void insertar(T elemento) {
        validarElemento(elemento);

        raiz = insertar(raiz, elemento, null);

        if (raiz != null) {
            raiz.setPadre(null);
        }
    }

    /**
     * Método auxiliar recursivo de inserción.
     *
     * @param nodo nodo actual.
     * @param elemento elemento que se desea insertar.
     * @param padre padre del nodo actual.
     * @return raíz del subárbol actualizado.
     */
    private NodeArbol<T> insertar(
            NodeArbol<T> nodo,
            T elemento,
            NodeArbol<T> padre
    ) {
        if (nodo == null) {
            NodeArbol<T> nuevoNodo = new NodeArbol<>(elemento);
            nuevoNodo.setPadre(padre);

            peso++;

            return nuevoNodo;
        }

        int comparacion = elemento.compareTo(nodo.getElemento());

        if (comparacion == 0) {
            throw new IllegalArgumentException(
                    "No se permiten elementos duplicados en el árbol."
            );
        }

        if (comparacion < 0) {
            nodo.setIzquierdo(
                    insertar(nodo.getIzquierdo(), elemento, nodo)
            );
        } else {
            nodo.setDerecho(
                    insertar(nodo.getDerecho(), elemento, nodo)
            );
        }

        return nodo;
    }

    /**
     * Elimina un elemento del árbol.
     *
     * Contempla los tres casos:
     *
     * 1. El nodo es una hoja.
     * 2. El nodo tiene un solo hijo.
     * 3. El nodo tiene dos hijos.
     *
     * Si tiene dos hijos, se reemplaza su elemento por el sucesor
     * inorden, que es el menor elemento de su subárbol derecho.
     *
     * El peso solo disminuye cuando el elemento realmente existe.
     *
     * @param elemento elemento que se desea eliminar.
     * @throws IllegalArgumentException si el elemento es null.
     *
     * Complejidad: O(h), donde h es la altura del árbol.
     */
    public void eliminar(T elemento) {
        validarElemento(elemento);

        if (!existe(elemento)) {
            return;
        }

        raiz = eliminarNodo(raiz, elemento);
        peso--;

        if (raiz != null) {
            raiz.setPadre(null);
        }
    }

    /**
     * Método auxiliar recursivo para eliminar un nodo.
     *
     * @param nodo raíz del subárbol actual.
     * @param elemento elemento que se desea eliminar.
     * @return nueva raíz del subárbol.
     */
    private NodeArbol<T> eliminarNodo(
            NodeArbol<T> nodo,
            T elemento
    ) {
        if (nodo == null) {
            return null;
        }

        int comparacion = elemento.compareTo(nodo.getElemento());

        if (comparacion < 0) {
            NodeArbol<T> nuevoIzquierdo =
                    eliminarNodo(nodo.getIzquierdo(), elemento);

            nodo.setIzquierdo(nuevoIzquierdo);

            if (nuevoIzquierdo != null) {
                nuevoIzquierdo.setPadre(nodo);
            }

        } else if (comparacion > 0) {
            NodeArbol<T> nuevoDerecho =
                    eliminarNodo(nodo.getDerecho(), elemento);

            nodo.setDerecho(nuevoDerecho);

            if (nuevoDerecho != null) {
                nuevoDerecho.setPadre(nodo);
            }

        } else {

            // Caso 1: el nodo no tiene hijos.
            if (nodo.getIzquierdo() == null
                    && nodo.getDerecho() == null) {
                return null;
            }

            // Caso 2: el nodo solo tiene hijo derecho.
            if (nodo.getIzquierdo() == null) {
                NodeArbol<T> hijo = nodo.getDerecho();
                hijo.setPadre(nodo.getPadre());
                return hijo;
            }

            // Caso 2: el nodo solo tiene hijo izquierdo.
            if (nodo.getDerecho() == null) {
                NodeArbol<T> hijo = nodo.getIzquierdo();
                hijo.setPadre(nodo.getPadre());
                return hijo;
            }

            // Caso 3: el nodo tiene dos hijos.
            NodeArbol<T> sucesor = minimo(nodo.getDerecho());

            nodo.setElemento(sucesor.getElemento());

            NodeArbol<T> nuevoDerecho =
                    eliminarNodo(
                            nodo.getDerecho(),
                            sucesor.getElemento()
                    );

            nodo.setDerecho(nuevoDerecho);

            if (nuevoDerecho != null) {
                nuevoDerecho.setPadre(nodo);
            }
        }

        return nodo;
    }

    /**
     * Obtiene el nodo con el menor elemento de todo el árbol.
     *
     * @return nodo con el menor elemento o null si el árbol está vacío.
     *
     * Complejidad: O(h).
     */
    public NodeArbol<T> obtenerMenor() {
        return obtenerMenor(raiz);
    }

    /**
     * Obtiene el nodo mínimo de un subárbol.
     *
     * El menor elemento siempre se encuentra siguiendo
     * las referencias izquierdas hasta llegar al último nodo.
     *
     * @param actual raíz del subárbol que se desea analizar.
     * @return nodo mínimo o null si el subárbol está vacío.
     */
    private NodeArbol<T> obtenerMenor(NodeArbol<T> actual) {
        if (actual == null) {
            return null;
        }

        while (actual.getIzquierdo() != null) {
            actual = actual.getIzquierdo();
        }

        return actual;
    }

    /**
     * Obtiene el nodo mínimo de un subárbol no vacío.
     *
     * Este método auxiliar se utiliza durante la eliminación
     * de nodos con dos hijos.
     *
     * @param nodo raíz del subárbol.
     * @return nodo que contiene el menor elemento.
     */
    private NodeArbol<T> minimo(NodeArbol<T> nodo) {
        while (nodo.getIzquierdo() != null) {
            nodo = nodo.getIzquierdo();
        }

        return nodo;
    }

    /**
     * Valida que un elemento no sea null.
     *
     * @param elemento elemento que se desea validar.
     * @throws IllegalArgumentException si el elemento es null.
     */
    private void validarElemento(T elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException(
                    "El elemento del árbol no puede ser null."
            );
        }
    }
}
