package com.alura.literatura.model;

import jakarta.persistence.*;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

@Entity
@Table(name = "libros")
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String titulo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "autor_id")
    private Autor autor;

    private String idioma;

    private Double descargas;

    public Libro(){

    }

    public Libro(String titulo, Autor autor, List<String> idiomas, double descargas) {
        this.titulo = titulo;
        this.autor = autor;
        this.idioma = idiomas != null && !idiomas.isEmpty() ? String.join(",", idiomas) : null;
        this.descargas = OptionalDouble.of(descargas).orElse(0);
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Autor getAutor() {
        return autor;
    }

    public void setAutor(Autor autor) {
        this.autor = autor;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Double getDescargas() {
        return descargas;
    }

    public void setDescargas(Double descargas) {
        this.descargas = descargas;
    }

    @Override
    public String toString() {
      return "*************************************" +
              "\nTítulo: " + titulo + "\n" +
              "Autor: " + autor.getNombre() + "\n" +
              "Idioma: " + idioma + "\n" +
              "Número de descargas: " + descargas +'\n';

    }
}
