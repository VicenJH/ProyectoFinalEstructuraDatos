
package co.edu.uniquindio.proyectofinal.Estructuras.Arbol;

/**
 * Representa un nodo de un árbol binario.
 *
 * Cada nodo almacena un elemento y mantiene referencias hacia:
 *
 * - Su hijo izquierdo.
 * - Su hijo derecho.
 * - Su nodo padre.
 *
 * Esta clase constituye la unidad básica de almacenamiento
 * de la estructura ArbolBinario.
 *
 * Los enlaces permiten recorrer el árbol, insertar elementos,
 * eliminar nodos y mantener las relaciones entre sus componentes.
 *
 * La organización y las reglas del árbol binario de búsqueda
 * son responsabilidad de la clase ArbolBinario.
 *
 * @param <T> tipo de elemento almacenado en el nodo.
 */
public class NodeArbol<T> {

    /**
     * Elemento almacenado en el nodo.
     */
    private T elemento;

    /**
     * Referencia al hijo izquierdo.
     *
     * En un árbol binario de búsqueda, este nodo contiene
     * un elemento menor que el elemento del nodo actual.
     */
    private NodeArbol<T> izquierdo;

    /**
     * Referencia al hijo derecho.
     *
     * En un árbol binario de búsqueda, este nodo contiene
     * un elemento mayor que el elemento del nodo actual.
     */
    private NodeArbol<T> derecho;

    /**
     * Referencia al nodo padre.
     *
     * Es null cuando el nodo corresponde a la raíz del árbol.
     */
    private NodeArbol<T> padre;

    /** Altura del subárbol cuya raíz es este nodo (una hoja tiene altura 1). */
    private int altura;

    /**
     * Construye un nodo con un elemento y un nodo padre.
     *
     * Las referencias a los hijos se inicializan en null,
     * ya que inicialmente el nodo no tiene descendientes.
     *
     * @param elemento dato que se desea almacenar.
     * @param padre nodo padre del nuevo nodo.
     *
     * Complejidad: O(1).
     */
    public NodeArbol(T elemento, NodeArbol<T> padre) {
        this.elemento = elemento;
        this.padre = padre;
        this.izquierdo = null;
        this.derecho = null;
        this.altura = 1;
    }

    /**
     * Construye un nodo a partir de un elemento.
     *
     * El nodo se crea sin padre y sin hijos.
     * Es utilizado principalmente para crear la raíz
     * o nuevos nodos durante la inserción.
     *
     * @param elemento dato que se desea almacenar.
     *
     * Complejidad: O(1).
     */
    public NodeArbol(T elemento) {
        this(elemento, null);
    }

    /**
     * Obtiene el elemento almacenado.
     *
     * @return elemento contenido en el nodo.
     *
     * Complejidad: O(1).
     */
    public T getElemento() {
        return elemento;
    }

    /**
     * Modifica el elemento almacenado.
     *
     * La modificación del elemento debe realizarse con cuidado:
     * si el nodo pertenece a un árbol binario de búsqueda,
     * cambiar su valor directamente puede romper el orden del árbol.
     *
     * @param elemento nuevo elemento que se almacenará.
     *
     * Complejidad: O(1).
     */
    void setElemento(T elemento) {
        this.elemento = elemento;
    }

    /**
     * Obtiene el hijo izquierdo del nodo.
     *
     * @return nodo hijo izquierdo o null si no existe.
     *
     * Complejidad: O(1).
     */
    public NodeArbol<T> getIzquierdo() {
        return izquierdo;
    }

    /**
     * Establece el hijo izquierdo.
     *
     * Este método modifica únicamente la referencia del nodo actual.
     * La clase ArbolBinario debe encargarse de mantener correctamente
     * la referencia al padre del nodo enlazado.
     *
     * @param izquierdo nodo que se establecerá como hijo izquierdo.
     *
     * Complejidad: O(1).
     */
    void setIzquierdo(NodeArbol<T> izquierdo) {
        this.izquierdo = izquierdo;
    }

    /**
     * Obtiene el hijo derecho del nodo.
     *
     * @return nodo hijo derecho o null si no existe.
     *
     * Complejidad: O(1).
     */
    public NodeArbol<T> getDerecho() {
        return derecho;
    }

    /**
     * Establece el hijo derecho.
     *
     * Este método modifica únicamente la referencia del nodo actual.
     * La clase ArbolBinario debe encargarse de mantener correctamente
     * la referencia al padre del nodo enlazado.
     *
     * @param derecho nodo que se establecerá como hijo derecho.
     *
     * Complejidad: O(1).
     */
    void setDerecho(NodeArbol<T> derecho) {
        this.derecho = derecho;
    }

    /** Obtiene la altura almacenada para mantener el balance AVL en O(1). */
    public int getAltura() {
        return altura;
    }

    /** Actualiza la altura del nodo; el cálculo corresponde a ArbolBinario. */
    void setAltura(int altura) {
        if (altura < 1) {
            throw new IllegalArgumentException("La altura de un nodo debe ser positiva.");
        }
        this.altura = altura;
    }

    /**
     * Obtiene el nodo padre.
     *
     * @return nodo padre o null si el nodo no tiene padre.
     *
     * Complejidad: O(1).
     */
    public NodeArbol<T> getPadre() {
        return padre;
    }

    /**
     * Establece el nodo padre.
     *
     * Permite mantener la relación entre un nodo y su antecesor
     * inmediato dentro del árbol.
     *
     * La clase ArbolBinario debe garantizar que esta referencia
     * coincida con los enlaces de los nodos hijos.
     *
     * @param padre nodo que será el padre del nodo actual.
     *
     * Complejidad: O(1).
     */
    void setPadre(NodeArbol<T> padre) {
        this.padre = padre;
    }
}
