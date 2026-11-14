package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

public class Usuario {
    public int id;
    public String nombre;
    public String corrreo;
    public String contrasena;
    public int puntosAcomulados;
    public Nivel nivel;

    public Usuario(int id, String nombre, String correo, String contrasena, int puntosAcomulados, Nivel nivel) {
        this.id = id;
        this.nombre = nombre;
        this.corrreo = correo;
        this.contrasena = contrasena;
        this.puntosAcomulados = puntosAcomulados;
        this.nivel = nivel;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public String getCorreo() {
        return corrreo;
    }
    public void setCorreo(String correo) {
        this.corrreo = correo;
    }
    public String getContrasena() {
        return contrasena;
    }
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
    public int getPuntosAcomulados() {
        return puntosAcomulados;
    }
    public void setPuntosAcomulados(int puntosAcomulados) {
        this.puntosAcomulados = puntosAcomulados;
    }
    public Nivel getNivel() {
        return nivel;
    }
    public void setNivel(Nivel nivel) {
        this.nivel = nivel;
    }

}
