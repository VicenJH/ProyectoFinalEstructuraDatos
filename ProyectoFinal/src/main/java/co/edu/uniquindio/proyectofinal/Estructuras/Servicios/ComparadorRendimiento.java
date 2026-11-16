package co.edu.uniquindio.proyectofinal.Estructuras.Servicios;

import co.edu.uniquindio.proyectofinal.Estructuras.Arbol.ArbolBinario;
import co.edu.uniquindio.proyectofinal.Estructuras.Hash.TablaHash;
import co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos.Usuario;
import co.edu.uniquindio.proyectofinal.Estructuras.SimpleList.SimpleLinkedList;

import java.util.Random;

/**
 * Comparador académico de estructuras para el requisito adicional de rendimiento.
 * Compara inserción y búsqueda de usuarios por identificación en una lista,
 * una tabla hash y un árbol AVL. No utiliza colecciones de Java como reemplazo
 * de las estructuras que se desean evaluar.
 *
 * Los tiempos son experimentales: dependen de CPU, JVM, carga del equipo y JIT.
 */
public class ComparadorRendimiento {

    public static class ResultadoComparacion {
        private final String estructura;
        private final int elementos;
        private final int consultas;
        private final long tiempoInsercionNanos;
        private final long tiempoBusquedaNanos;
        private final int aciertos;

        private ResultadoComparacion(String estructura, int elementos, int consultas,
                                     long tiempoInsercionNanos, long tiempoBusquedaNanos, int aciertos) {
            this.estructura = estructura;
            this.elementos = elementos;
            this.consultas = consultas;
            this.tiempoInsercionNanos = tiempoInsercionNanos;
            this.tiempoBusquedaNanos = tiempoBusquedaNanos;
            this.aciertos = aciertos;
        }

        public String getEstructura() { return estructura; }
        public int getElementos() { return elementos; }
        public int getConsultas() { return consultas; }
        public long getTiempoInsercionNanos() { return tiempoInsercionNanos; }
        public long getTiempoBusquedaNanos() { return tiempoBusquedaNanos; }
        public double getTiempoInsercionMilisegundos() { return tiempoInsercionNanos / 1_000_000.0; }
        public double getTiempoBusquedaMilisegundos() { return tiempoBusquedaNanos / 1_000_000.0; }
        public int getAciertos() { return aciertos; }

        @Override
        public String toString() {
            return estructura + ": inserción=" + getTiempoInsercionMilisegundos()
                    + " ms, búsqueda=" + getTiempoBusquedaMilisegundos()
                    + " ms, aciertos=" + aciertos + "/" + consultas;
        }
    }

    /**
     * Ejecuta un experimento reproducible con identificaciones consecutivas.
     * Se generan consultas aleatorias con semilla fija 42 para que las tres
     * estructuras reciban exactamente las mismas búsquedas.
     */
    public SimpleLinkedList<ResultadoComparacion> compararBusquedaUsuarios(int cantidadUsuarios, int cantidadConsultas) {
        if (cantidadUsuarios <= 0 || cantidadConsultas <= 0) {
            throw new IllegalArgumentException("La cantidad de usuarios y consultas debe ser positiva.");
        }

        TablaHash<String, Usuario> hash = new TablaHash<>(Math.max(16, cantidadUsuarios / 2));
        SimpleLinkedList<Usuario> lista = new SimpleLinkedList<>();
        ArbolBinario<Usuario> arbol = new ArbolBinario<>();

        String[] identificaciones = new String[cantidadUsuarios];
        Usuario[] usuarios = new Usuario[cantidadUsuarios];
        for (int i = 0; i < cantidadUsuarios; i++) {
            identificaciones[i] = String.format("USR-%010d", i);
            usuarios[i] = crearUsuario(identificaciones[i]);
        }

        // Los objetos se construyen antes de medir: así el tiempo representa
        // la inserción en cada estructura y no mezcla el coste del constructor.
        long inicio = System.nanoTime();
        for (int i = 0; i < cantidadUsuarios; i++) {
            hash.insertar(identificaciones[i], usuarios[i]);
        }
        long insercionHash = System.nanoTime() - inicio;

        inicio = System.nanoTime();
        for (Usuario usuario : usuarios) {
            lista.addLast(usuario);
        }
        long insercionLista = System.nanoTime() - inicio;

        inicio = System.nanoTime();
        for (Usuario usuario : usuarios) {
            arbol.insertar(usuario);
        }
        long insercionArbol = System.nanoTime() - inicio;

        String[] consultas = new String[cantidadConsultas];
        Usuario[] usuariosConsulta = new Usuario[cantidadConsultas];
        Random random = new Random(42);
        for (int i = 0; i < cantidadConsultas; i++) {
            int indice = random.nextInt(cantidadUsuarios);
            consultas[i] = identificaciones[indice];
            // También se crean las claves de búsqueda fuera de la medición.
            usuariosConsulta[i] = crearUsuario(consultas[i]);
        }

        // Calentamiento breve de la JVM, separado de las mediciones publicadas.
        for (int i = 0; i < Math.min(1000, cantidadConsultas); i++) {
            hash.buscar(consultas[i]);
            buscarEnLista(lista, consultas[i]);
            arbol.existe(usuariosConsulta[i]);
        }

        inicio = System.nanoTime();
        int aciertosHash = 0;
        for (String id : consultas) if (hash.buscar(id) != null) aciertosHash++;
        long busquedaHash = System.nanoTime() - inicio;

        inicio = System.nanoTime();
        int aciertosLista = 0;
        for (String id : consultas) if (buscarEnLista(lista, id) != null) aciertosLista++;
        long busquedaLista = System.nanoTime() - inicio;

        inicio = System.nanoTime();
        int aciertosArbol = 0;
        for (Usuario usuario : usuariosConsulta) if (arbol.existe(usuario)) aciertosArbol++;
        long busquedaArbol = System.nanoTime() - inicio;

        SimpleLinkedList<ResultadoComparacion> resultados = new SimpleLinkedList<>();
        resultados.addLast(new ResultadoComparacion("Tabla hash", cantidadUsuarios, cantidadConsultas,
                insercionHash, busquedaHash, aciertosHash));
        resultados.addLast(new ResultadoComparacion("Lista simplemente enlazada", cantidadUsuarios, cantidadConsultas,
                insercionLista, busquedaLista, aciertosLista));
        resultados.addLast(new ResultadoComparacion("Árbol AVL", cantidadUsuarios, cantidadConsultas,
                insercionArbol, busquedaArbol, aciertosArbol));
        return resultados;
    }

    private Usuario crearUsuario(String id) {
        return new Usuario(id, "Usuario de prueba", "prueba@ejemplo.com");
    }

    private Usuario buscarEnLista(SimpleLinkedList<Usuario> lista, String id) {
        for (Usuario usuario : lista) {
            if (usuario.getId().equals(id)) return usuario;
        }
        return null;
    }
}
