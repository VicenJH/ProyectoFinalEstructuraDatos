package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

import co.edu.uniquindio.proyectofinal.Estructuras.SimpleList.SimpleLinkedList;

public class Usuario implements Comparable<Usuario> {
    public String id;
    public String nombre;
    public String correo;
    public int puntosAcumulados;
    public String nivel;
    public SimpleLinkedList<Billetera> billeteras;
    public SimpleLinkedList<Transaccion> historialTransacciones;

    public Usuario(String id, String nombre, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.puntosAcumulados = 0;
        this.nivel = "Bronce";
        this.billeteras = new SimpleLinkedList<>();
        this.historialTransacciones = new SimpleLinkedList<>();
    }

    public void actualizarNivel() {
        if (puntosAcumulados > 5000) {
            this.nivel = "Platino";
        } else if (puntosAcumulados > 1000) {
            this.nivel = "Oro";
        } else if (puntosAcumulados > 500) {
            this.nivel = "Plata";
        } else {
            this.nivel = "Bronce";
        }
    }

    public void agregarPuntos(int puntos) {
        this.puntosAcumulados += puntos;
        actualizarNivel();
    }


    @Override
    public int compareTo(Usuario otro) {
        return Integer.compare(this.puntosAcumulados, otro.puntosAcumulados);
    }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public int getPuntosAcumulados() { return puntosAcumulados; }
    public void setPuntosAcumulados(int puntosAcumulados) { this.puntosAcumulados = puntosAcumulados; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public SimpleLinkedList<Billetera> getBilleteras() { return billeteras; }
    public SimpleLinkedList<Transaccion> getHistorialTransacciones() { return historialTransacciones; }
}