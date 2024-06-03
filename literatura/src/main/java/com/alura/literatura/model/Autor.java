package com.alura.literatura.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "autores")
public class Autor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private Double fechaNacimiento;
    private Double fechaFallecimiento;

    @OneToMany(mappedBy = "autor", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Libro> libros;

    public Autor() {
    }

    public Autor(String nombre, Double fechaNacimiento, Double fechaFallecimiento) {
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
        this.fechaFallecimiento = fechaFallecimiento;
    }

    public List<Libro> getLibros(){
        if (libros == null) {
            libros = new ArrayList<>();
        }
        return libros;
    }

    public void setLibros(List<Libro> libros) {
        this.libros = libros;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }


    public Double getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(Double fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public Double getFechaFallecimiento() {
        return fechaFallecimiento;
    }

    public void setFechaFallecimiento(Double fechaFallecimiento) {
        this.fechaFallecimiento = fechaFallecimiento;
    }


    @Override
    public String toString() {

        return "*************************************" +
                "\nAutor: " +  nombre + '\n' +
                "Fecha de nacimiento: " + fechaNacimiento +'\n'+
                "Fecha de fallecimiento: " + fechaFallecimiento +'\n'+
                "Libros: " + libros.stream()
                .map(l->l.getTitulo())
                .collect(Collectors.toList())+'\n';

    }
}







