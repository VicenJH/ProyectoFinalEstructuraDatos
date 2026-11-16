package co.edu.uniquindio.proyectofinal.Estructuras.Arbol;

import co.edu.uniquindio.proyectofinal.Estructuras.Colas.Cola;
import co.edu.uniquindio.proyectofinal.Estructuras.SimpleList.SimpleLinkedList;

import java.util.function.ToIntFunction;

/**
 * Árbol binario de búsqueda autoequilibrado (AVL).
 *
 * Conserva la API del ABB original, pero realiza rotaciones para que la altura
 * permanezca O(log n). Esto evita que el árbol de usuarios se convierta en una
 * lista cuando los puntos o identificadores llegan en orden ascendente.
 */
public class ArbolBinario<T extends Comparable<T>> {

    private int peso;
    private NodeArbol<T> raiz;

    public ArbolBinario() {
        peso = 0;
        raiz = null;
    }

    public int getPeso() {
        return peso;
    }

    /** @deprecated El peso se administra automáticamente mediante insertar/eliminar. */
    @Deprecated
    public void setPeso(int peso) {
        if (peso < 0) {
            throw new IllegalArgumentException("El peso no puede ser negativo.");
        }
        if (peso != obtenerPeso(raiz)) {
            throw new IllegalArgumentException("El peso no puede modificarse manualmente; representa los nodos reales.");
        }
        this.peso = peso;
    }

    NodeArbol<T> getRaiz() {
        return raiz;
    }

    /** Establece una raíz externa, repara padres/alturas y calcula el peso real. */
    void setRaiz(NodeArbol<T> raiz) {
        this.raiz = raiz;
        if (raiz != null) {
            raiz.setPadre(null);
        }
        this.peso = repararMetadatos(raiz, null);
    }

    public boolean isEmpty() {
        return raiz == null;
    }

    public void recorrerInorden() {
        recorrerInorden(raiz);
    }

    private void recorrerInorden(NodeArbol<T> nodo) {
        if (nodo == null) return;
        recorrerInorden(nodo.getIzquierdo());
        System.out.print(nodo.getElemento() + " ");
        recorrerInorden(nodo.getDerecho());
    }

    public void recorrerPreorden() {
        recorrerPreorden(raiz);
    }

    private void recorrerPreorden(NodeArbol<T> nodo) {
        if (nodo == null) return;
        System.out.print(nodo.getElemento() + " ");
        recorrerPreorden(nodo.getIzquierdo());
        recorrerPreorden(nodo.getDerecho());
    }

    public void recorrerPostorden() {
        recorrerPostorden(raiz);
    }

    private void recorrerPostorden(NodeArbol<T> nodo) {
        if (nodo == null) return;
        recorrerPostorden(nodo.getIzquierdo());
        recorrerPostorden(nodo.getDerecho());
        System.out.print(nodo.getElemento() + " ");
    }

    /** El contador mantiene O(1), a diferencia de recorrer cada nodo para contarlo. */
    public int obtenerPeso() {
        return peso;
    }

    private int obtenerPeso(NodeArbol<T> nodo) {
        if (nodo == null) return 0;
        return 1 + obtenerPeso(nodo.getIzquierdo()) + obtenerPeso(nodo.getDerecho());
    }

    public int ObtenerAltura() {
        return altura(raiz);
    }

    public int obtenerAltura() {
        return ObtenerAltura();
    }

    private int altura(NodeArbol<T> nodo) {
        return nodo == null ? 0 : nodo.getAltura();
    }

    public void recorridoAmplitud() {
        if (raiz == null) return;
        Cola<NodeArbol<T>> cola = new Cola<>();
        cola.encolar(raiz);
        while (!cola.isEmpty()) {
            NodeArbol<T> actual = cola.desencolar();
            System.out.print(actual.getElemento() + " ");
            if (actual.getIzquierdo() != null) cola.encolar(actual.getIzquierdo());
            if (actual.getDerecho() != null) cola.encolar(actual.getDerecho());
        }
    }

    public boolean existe(T elemento) {
        validarElemento(elemento);
        NodeArbol<T> actual = raiz;
        while (actual != null) {
            int comparacion = elemento.compareTo(actual.getElemento());
            if (comparacion == 0) return true;
            actual = comparacion < 0 ? actual.getIzquierdo() : actual.getDerecho();
        }
        return false;
    }

    public void insertar(T elemento) {
        validarElemento(elemento);
        raiz = insertar(raiz, elemento, null);
        if (raiz != null) raiz.setPadre(null);
    }

    private NodeArbol<T> insertar(NodeArbol<T> nodo, T elemento, NodeArbol<T> padre) {
        if (nodo == null) {
            peso++;
            return new NodeArbol<>(elemento, padre);
        }

        int comparacion = elemento.compareTo(nodo.getElemento());
        if (comparacion == 0) {
            throw new IllegalArgumentException("No se permiten elementos duplicados según compareTo().");
        }

        if (comparacion < 0) {
            NodeArbol<T> hijo = insertar(nodo.getIzquierdo(), elemento, nodo);
            nodo.setIzquierdo(hijo);
            hijo.setPadre(nodo);
        } else {
            NodeArbol<T> hijo = insertar(nodo.getDerecho(), elemento, nodo);
            nodo.setDerecho(hijo);
            hijo.setPadre(nodo);
        }
        return equilibrar(nodo);
    }

    public void eliminar(T elemento) {
        validarElemento(elemento);
        if (!existe(elemento)) return;
        raiz = eliminarNodo(raiz, elemento);
        peso--;
        if (raiz != null) raiz.setPadre(null);
    }

    private NodeArbol<T> eliminarNodo(NodeArbol<T> nodo, T elemento) {
        if (nodo == null) return null;

        int comparacion = elemento.compareTo(nodo.getElemento());
        if (comparacion < 0) {
            NodeArbol<T> hijo = eliminarNodo(nodo.getIzquierdo(), elemento);
            nodo.setIzquierdo(hijo);
            if (hijo != null) hijo.setPadre(nodo);
        } else if (comparacion > 0) {
            NodeArbol<T> hijo = eliminarNodo(nodo.getDerecho(), elemento);
            nodo.setDerecho(hijo);
            if (hijo != null) hijo.setPadre(nodo);
        } else {
            if (nodo.getIzquierdo() == null || nodo.getDerecho() == null) {
                NodeArbol<T> hijo = nodo.getIzquierdo() != null ? nodo.getIzquierdo() : nodo.getDerecho();
                if (hijo != null) hijo.setPadre(nodo.getPadre());
                return hijo;
            }

            NodeArbol<T> sucesor = minimo(nodo.getDerecho());
            nodo.setElemento(sucesor.getElemento());
            NodeArbol<T> derecho = eliminarMinimo(nodo.getDerecho());
            nodo.setDerecho(derecho);
            if (derecho != null) derecho.setPadre(nodo);
        }
        return equilibrar(nodo);
    }

    private NodeArbol<T> eliminarMinimo(NodeArbol<T> nodo) {
        if (nodo.getIzquierdo() == null) {
            NodeArbol<T> derecho = nodo.getDerecho();
            if (derecho != null) derecho.setPadre(nodo.getPadre());
            return derecho;
        }
        NodeArbol<T> izquierdo = eliminarMinimo(nodo.getIzquierdo());
        nodo.setIzquierdo(izquierdo);
        if (izquierdo != null) izquierdo.setPadre(nodo);
        return equilibrar(nodo);
    }

    private NodeArbol<T> equilibrar(NodeArbol<T> nodo) {
        actualizarAltura(nodo);
        int balance = factorBalance(nodo);

        if (balance > 1) {
            if (factorBalance(nodo.getIzquierdo()) < 0) {
                NodeArbol<T> izquierdo = rotarIzquierda(nodo.getIzquierdo());
                nodo.setIzquierdo(izquierdo);
                izquierdo.setPadre(nodo);
            }
            return rotarDerecha(nodo);
        }

        if (balance < -1) {
            if (factorBalance(nodo.getDerecho()) > 0) {
                NodeArbol<T> derecho = rotarDerecha(nodo.getDerecho());
                nodo.setDerecho(derecho);
                derecho.setPadre(nodo);
            }
            return rotarIzquierda(nodo);
        }
        return nodo;
    }

    private NodeArbol<T> rotarDerecha(NodeArbol<T> y) {
        NodeArbol<T> x = y.getIzquierdo();
        NodeArbol<T> t2 = x.getDerecho();
        NodeArbol<T> padreAnterior = y.getPadre();

        x.setDerecho(y);
        x.setPadre(padreAnterior);
        y.setPadre(x);
        y.setIzquierdo(t2);
        if (t2 != null) t2.setPadre(y);

        actualizarAltura(y);
        actualizarAltura(x);
        return x;
    }

    private NodeArbol<T> rotarIzquierda(NodeArbol<T> x) {
        NodeArbol<T> y = x.getDerecho();
        NodeArbol<T> t2 = y.getIzquierdo();
        NodeArbol<T> padreAnterior = x.getPadre();

        y.setIzquierdo(x);
        y.setPadre(padreAnterior);
        x.setPadre(y);
        x.setDerecho(t2);
        if (t2 != null) t2.setPadre(x);

        actualizarAltura(x);
        actualizarAltura(y);
        return y;
    }

    private void actualizarAltura(NodeArbol<T> nodo) {
        nodo.setAltura(1 + Math.max(altura(nodo.getIzquierdo()), altura(nodo.getDerecho())));
    }

    private int factorBalance(NodeArbol<T> nodo) {
        return nodo == null ? 0 : altura(nodo.getIzquierdo()) - altura(nodo.getDerecho());
    }

    /**
     * Devuelve los elementos cuya clave entera está dentro del rango inclusivo.
     * El extractor debe respetar el orden principal definido por compareTo();
     * en ese caso se descartan subárboles y el recorrido cuesta O(h + k).
     */
    public SimpleLinkedList<T> buscarPorRango(ToIntFunction<? super T> extractor, int minimo, int maximo) {
        if (extractor == null) {
            throw new IllegalArgumentException("El extractor de clave no puede ser null.");
        }
        if (minimo > maximo) {
            throw new IllegalArgumentException("El mínimo no puede ser mayor que el máximo.");
        }
        SimpleLinkedList<T> resultado = new SimpleLinkedList<>();
        recolectarRango(raiz, extractor, minimo, maximo, resultado);
        return resultado;
    }

    private void recolectarRango(NodeArbol<T> nodo, ToIntFunction<? super T> extractor,
                                 int minimo, int maximo, SimpleLinkedList<T> resultado) {
        if (nodo == null) return;
        int clave = extractor.applyAsInt(nodo.getElemento());
        if (clave >= minimo) {
            recolectarRango(nodo.getIzquierdo(), extractor, minimo, maximo, resultado);
        }
        if (clave >= minimo && clave <= maximo) {
            resultado.addLast(nodo.getElemento());
        }
        if (clave <= maximo) {
            recolectarRango(nodo.getDerecho(), extractor, minimo, maximo, resultado);
        }
    }

    public NodeArbol<T> obtenerMenor() {
        return minimo(raiz);
    }

    private NodeArbol<T> minimo(NodeArbol<T> nodo) {
        if (nodo == null) return null;
        while (nodo.getIzquierdo() != null) nodo = nodo.getIzquierdo();
        return nodo;
    }

    private int repararMetadatos(NodeArbol<T> nodo, NodeArbol<T> padre) {
        if (nodo == null) return 0;
        nodo.setPadre(padre);
        int cantidadIzquierda = repararMetadatos(nodo.getIzquierdo(), nodo);
        int cantidadDerecha = repararMetadatos(nodo.getDerecho(), nodo);
        nodo.setAltura(1 + Math.max(altura(nodo.getIzquierdo()), altura(nodo.getDerecho())));
        return 1 + cantidadIzquierda + cantidadDerecha;
    }

    private void validarElemento(T elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("El elemento del árbol no puede ser null.");
        }
    }
}
