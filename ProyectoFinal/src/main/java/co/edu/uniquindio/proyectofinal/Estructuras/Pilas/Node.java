
package co.edu.uniquindio.proyectofinal.Estructuras.Pilas;

/**
 * Representa un nodo de una estructura enlazada utilizada por la pila.
 *
 * Cada nodo almacena un dato genérico y una referencia al siguiente nodo.
 * La conexión entre nodos permite implementar la pila sin utilizar
 * estructuras de almacenamiento de Java.
 *
 * En la clase Stack, el nodo ubicado en el tope apunta al siguiente
 * elemento de la pila.
 *
 * @param <T> tipo de dato almacenado en el nodo.
 */
public class Node<T> {

    /**
     * Dato almacenado en el nodo.
     */
    private T data;

    /**
     * Referencia al siguiente nodo de la estructura.
     * Si es null, significa que no existe otro nodo enlazado.
     */
    private Node<T> next;

    /**
     * Construye un nodo con el dato recibido.
     *
     * Inicialmente, el nodo no apunta a ningún otro nodo.
     *
     * @param data información que se desea almacenar.
     *
     * Complejidad: O(1).
     */
    public Node(T data) {
        this.data = data;
        this.next = null;
    }

    /**
     * Obtiene el dato almacenado.
     *
     * @return dato contenido en el nodo.
     *
     * Complejidad: O(1).
     */
    public T getData() {
        return data;
    }

    /**
     * Modifica el dato almacenado.
     *
     * @param data nuevo dato que se desea almacenar.
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
    public Node<T> getNext() {
        return next;
    }

    /**
     * Establece el siguiente nodo de la estructura.
     *
     * Permite enlazar este nodo con otro para construir
     * la secuencia de elementos de la pila.
     *
     * @param next nodo que se enlazará después del actual.
     *
     * Complejidad: O(1).
     */
    public void setNext(Node<T> next) {
        this.next = next;
    }
}
