package co.edu.uniquindio.proyectofinal.Estructuras.ModeloDatos;

public class Billetera {
    public int codigo;
    public TipoBilletera tipo;
    public int saldo;
    public boolean activo;

    public Billetera(int codigo, TipoBilletera tipo, int saldo, boolean activo) {
        this.codigo = codigo;
        this.tipo = tipo;
        this.saldo = saldo;
        this.activo = activo;
    }


    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public TipoBilletera getTipo() {
        return tipo;
    }

    public void setTipo(TipoBilletera tipo) {
        this.tipo = tipo;
    }

    public int getSaldo() {
        return saldo;
    }

    public void setSaldo(int saldo) {
        this.saldo = saldo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
