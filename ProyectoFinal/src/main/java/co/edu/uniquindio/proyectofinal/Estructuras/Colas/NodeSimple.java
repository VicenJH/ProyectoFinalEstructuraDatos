
package co.edu.uniquindio.proyectofinal.Estructuras.Colas;

/**
 * Representa un nodo de la lista enlazada utilizada por la estructura Cola.
 *
 * Cada nodo almacena un elemento genérico y una referencia al siguiente
 * nodo de la secuencia.
 *
 * En la cola, los nodos se enlazan desde el primero hasta el último,
 * permitiendo insertar elementos al final y retirarlos desde el inicio.
 *
 * Esta clase forma parte de la implementación manual de estructuras
 * de datos del proyecto Fintech.
 *
 * @param <T> tipo de dato que almacena el nodo.
 */
public class NodeSimple<T> {

    /**
     * Información almacenada en el nodo.
     */
    private T data;

    /**
     * Referencia al siguiente nodo de la secuencia.
     *
     * Si su valor es null, significa que no existe otro nodo
     * después del actual.
     */
    private NodeSimple<T> next;

    /**
     * Construye un nodo con el dato recibido.
     *
     * La referencia al siguiente nodo se inicializa en null.
     *
     * @param data elemento que se desea almacenar.
     *
     * Complejidad: O(1).
     */
    public NodeSimple(T data) {
        this.data = data;
        this.next = null;
    }

    /**
     * Obtiene el dato almacenado en el nodo.
     *
     * @return elemento contenido en el nodo.
     *
     * Complejidad: O(1).
     */
    public T getData() {
        return data;
    }

    /**
     * Modifica el dato almacenado en el nodo.
     *
     * @param data nuevo elemento que se desea almacenar.
     *
     * Complejidad: O(1).
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * Obtiene la referencia al siguiente nodo.
     *
     * @return siguiente nodo o null si no existe.
     *
     * Complejidad: O(1).
     */
    public NodeSimple<T> getNext() {
        return next;
    }

    /**
     * Establece el siguiente nodo de la secuencia.
     *
     * Este método permite conectar los nodos que conforman
     * la cola enlazada.
     *
     * @param next nodo que se desea enlazar después del actual.
     *
     * Complejidad: O(1).
     */
    public void setNext(NodeSimple<T> next) {
        this.next = next;
    }
}
