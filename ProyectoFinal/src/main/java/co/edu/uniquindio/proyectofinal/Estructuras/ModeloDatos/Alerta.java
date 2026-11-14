package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

public class Alerta {
    public int id;
    public int fecha;
    public String mensaje;
    public String tipoAlerta;

    public Alerta(int id, int fecha, String mensaje, String tipoAlerta) {
        this.id = id;
        this.fecha = fecha;
        this.mensaje = mensaje;
        this.tipoAlerta = tipoAlerta;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFecha() {
        return fecha;
    }

    public void setFecha(int fecha) {
        this.fecha = fecha;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getTipoAlerta() {
        return tipoAlerta;
    }

    public void setTipoAlerta(String tipoAlerta) {
        this.tipoAlerta = tipoAlerta;
    }
}
