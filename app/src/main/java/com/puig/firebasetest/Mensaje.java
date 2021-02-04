package com.puig.firebasetest;

public class Mensaje {
    public String mensaje;
    public String fecha;
    public String nombre;
    public String email;
    public String photo;
    public String meme;

    public Mensaje(String mensaje, String fecha, String nombre, String email, String photo) {
        this.mensaje = mensaje;
        this.fecha = fecha;
        this.nombre = nombre;
        this.email = email;
        this.photo = photo;
    }

    public Mensaje(String mensaje, String fecha, String nombre, String email, String photo, String meme) {
        this.mensaje = mensaje;
        this.fecha = fecha;
        this.nombre = nombre;
        this.email = email;
        this.photo = photo;
        this.meme = meme;
    }
}
