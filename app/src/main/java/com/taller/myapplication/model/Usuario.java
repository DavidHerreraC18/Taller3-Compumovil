package com.taller.myapplication.model;


public class Usuario  {

    private String nombreUsuario;

    private String apellidoUsuario;

    private String identificacion;

    private String ubicacionActual;

    private String disponible;

    public Usuario()
    {

    }

    public Usuario(String nombreUsuario, String apellidoUsuario, String identificacion, String ubicacionActual) {
        this.nombreUsuario = nombreUsuario;
        this.apellidoUsuario = apellidoUsuario;
        this.identificacion = identificacion;
        this.ubicacionActual = ubicacionActual;
        this.disponible = "false";
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getApellidoUsuario() {
        return apellidoUsuario;
    }

    public void setApellidoUsuario(String apellidoUsuario) {
        this.apellidoUsuario = apellidoUsuario;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public String getUbicacionActual() {
        return ubicacionActual;
    }

    public void setUbicacionActual(String ubicacionActual) {
        this.ubicacionActual = ubicacionActual;
    }

    public String getDisponible() {
        return disponible;
    }

    public void setDisponible(String disponible) {
        this.disponible = disponible;
    }
}
