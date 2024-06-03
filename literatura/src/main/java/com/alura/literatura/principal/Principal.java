package com.alura.literatura.principal;

import com.alura.literatura.model.Autor;
import com.alura.literatura.model.DatosAutor;
import com.alura.literatura.model.DatosLibro;
import com.alura.literatura.model.Libro;
import com.alura.literatura.repository.AutorRepository;
import com.alura.literatura.repository.LibroRepository;
import com.alura.literatura.service.ConsumoApi;
import com.alura.literatura.service.ConvierteDatos;
import com.alura.literatura.service.ConvierteDatosAutor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class Principal {
    private final String URL_BASE = "https://gutendex.com/books/";
    private Scanner teclado = new Scanner(System.in);
    private ConsumoApi consumoApi = new ConsumoApi();
    private ConvierteDatos conversor = new ConvierteDatos();
    private ConvierteDatosAutor conversorAutor = new ConvierteDatosAutor();
    private LibroRepository repositorio;
    private AutorRepository repositorio2;
    private List<Libro> libros;
    private List<Autor> autores;
    private Optional<Libro> libroBuscado;
    private Optional<Autor> autorBuscado;

    public Principal(LibroRepository repository, AutorRepository repository2) {
        this.repositorio = repository;
        this.repositorio2 = repository2;
        this.libros = new ArrayList<>();
        this.autores = new ArrayList<>();
    }

    public void muestraElMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    ******************************
                    \n Bienvenido/a a LiterAlura
                    \n******************************
                    \nEscriba el numero de la opcion que desea utilizar
                    \n1 - Buscar libro por titulo
                    2 - Listar libros registrados
                    3 - Listar autores registrados
                    4 - Listar autores vivos en un determinado año
                    5 - Listar libros por idioma
                    6 - Top 10 libros más descargados 
                    7 - Buscar autor por nombre
                    8 - Listar autores fallecidos despues de un determinado año
                    9 - Estadisticas
                    0 - Salir
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarLibroPorTitulo();
                    break;
                case 2:
                    mostrarLibrosRegistrados();
                    break;
                case 3:
                    mostrarAutoresRegistrados();
                    break;
                case 4:
                    mostrarAutoresPorFecha();
                    break;
                case 5:
                    mostrarLibrosPorIdioma();
                    break;
                case 6:
                    listarTopLibros();
                    break;
                case 7:
                    buscarAutorPorNombre();
                    break;
                case 8:
                    listarAutoresFallecidosDespuesDelAno();
                    break;
                case 9:
                    estadisticas();
                    break;
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }

    }

    private DatosLibro getDatosLibro(String nombreLibro) {
        var json = consumoApi.obtenerDatos(URL_BASE + "?search=" + nombreLibro.replace(" ", "+"));
        DatosLibro datos = conversor.obtenerDatos(json, DatosLibro.class);
        return datos;
    }

    private DatosAutor getDatosAutor(String nombreLibro) {
        var json = consumoApi.obtenerDatos(URL_BASE + "?search=" + nombreLibro.replace(" ", "+"));
        DatosAutor datos = conversorAutor.obtenerDatos(json, DatosAutor.class);
        return datos;
    }

    private void mostrarLibrosRegistrados() {
        try {
            List<Libro> libros = repositorio.findAll();
            libros.stream().sorted(Comparator.comparing(Libro::getDescargas)).forEach(System.out::println);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
            libros = new ArrayList<>();
        }
        System.out.println("\n¿Desea hacer otra consulta? (Si/No):");
        String otraConsultaEs = teclado.nextLine();
        if (otraConsultaEs.equalsIgnoreCase("Si")) {
            muestraElMenu();
        } else {
            System.out.println("Saliendo de la aplicación...");
            System.exit(0);
        }
    }

    private void buscarLibroPorTitulo() {
        System.out.println("Escribe el nombre del libro que deseas buscar");
        var nombreLibro = teclado.nextLine();

        libros = libros != null ? libros : new ArrayList<>();

        Optional<Libro> book = libros.stream().filter(l -> l.getTitulo().equalsIgnoreCase(nombreLibro)).findFirst();

        // Si el libro no está en la lista, buscar en la base de datos
        Optional<Libro> libroEncontrado = repositorio.findByTituloIgnoreCase(nombreLibro);

        if (libroEncontrado.isPresent()) {
            System.out.println("*** Libro Encontrado ***");
            System.out.println(libroEncontrado.get());
            System.out.println("El libro se encuentra en la base de datos");

            System.out.println("\n¿Desea hacer otra consulta? (Si/No):");
            String otraConsulta = teclado.nextLine();
            if (otraConsulta.equalsIgnoreCase("Si")) {
                buscarLibroPorTitulo();
            } else {
                System.out.println("Saliendo de la aplicación...");
                System.exit(0);
            }
        } else {
            try {
                DatosLibro datosLibro = getDatosLibro(nombreLibro);
                System.out.println(datosLibro);

                if (datosLibro != null) {
                    DatosAutor datosAutor = getDatosAutor(nombreLibro);
                    if (datosAutor != null) {
                        List<Autor> autores = repositorio2.findAll();
                        autores = autores != null ? autores : new ArrayList<>();

                        Optional<Autor> autoor = autores.stream().filter
                                (a -> datosAutor.nombre() != null && a.getNombre().toLowerCase()
                                        .contains(datosAutor.nombre().toLowerCase())).findFirst();

                        Autor autor = autoor.orElseGet(() -> {
                            Autor nuevoAutor = new Autor(datosAutor.nombre(),
                                    datosAutor.fechaNacimiento(), datosAutor.fechaFallecimiento());
                            repositorio2.save(nuevoAutor);
                            return nuevoAutor;
                        });

                        Libro libro = new Libro(datosLibro.titulo(), autor,
                                datosLibro.idioma() != null ? datosLibro.idioma() : Collections.emptyList(),
                                datosLibro.descargas());

                        libros.add(libro);
                        autor.setLibros(libros);

                        System.out.println(libro);
                        repositorio.save(libro);

                        System.out.println("Libro guardado exitosamente");
                    } else {
                        System.out.println("No se encontró el autor para el libro");
                    }
                } else {
                    System.out.println("No se encontró el libro");
                }
            } catch (Exception e) {
                System.out.println("excepción: " + e.getMessage());
            }

            System.out.println("\n¿Desea hacer otra consulta? (Si/No):");
            String otraConsulta = teclado.nextLine();
            if (otraConsulta.equalsIgnoreCase("Si")) {
                buscarLibroPorTitulo();
            } else {
                System.out.println("Saliendo de la aplicación...");
                System.exit(0);
            }
        }
    }

    private void mostrarAutoresRegistrados() {
        try {
            List<Autor> autores = repositorio2.findAll();
            autores.stream().sorted(Comparator.comparing(Autor::getFechaNacimiento)).forEach(System.out::println);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
            autores = new ArrayList<>();
        }
        System.out.println("\n¿Desea hacer otra consulta? (Si/No):");
        String otraConsulta = teclado.nextLine();
        if (otraConsulta.equalsIgnoreCase("Si")) {
            muestraElMenu();
        } else {
            System.out.println("Saliendo de la aplicación...");
            System.exit(0);
        }
    }

    private void mostrarAutoresPorFecha() {
        System.out.println("Por favor indica el año en el que deseas consultar los autores:");
        var anoDelAutor = teclado.nextInt();
        teclado.nextLine();
        autores = repositorio2.findAll();

        boolean hayAutoresVivos = autores.stream()
                .anyMatch(a -> (a.getFechaFallecimiento() >= anoDelAutor) && (a.getFechaNacimiento() <= anoDelAutor));

        if (!hayAutoresVivos) {
            System.out.println("No se encontraron autores vivos en la fecha proporcionada.");
        } else {
            List<String> autoresPorNombre = autores.stream()
                    .filter(a -> (a.getFechaFallecimiento() >= anoDelAutor) && (a.getFechaNacimiento() <= anoDelAutor))
                    .map(a -> a.getNombre() + " (Nacimiento: " + a.getFechaNacimiento().intValue() + ", Fallecimiento: " + a.getFechaFallecimiento().intValue() + ")")
                    .collect(Collectors.toList());
            System.out.println("Autores encontrados:");
            autoresPorNombre.forEach(System.out::println);
        }

        System.out.println("\n¿Desea hacer otra consulta? (Si/No):");
        String otraConsulta = teclado.nextLine();
        if (otraConsulta.equalsIgnoreCase("Si")) {
            mostrarAutoresPorFecha();
        } else {
            System.out.println("Saliendo de la aplicación...");
            System.exit(0);
        }
    }


    private void mostrarLibrosPorIdioma() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    Estos son  los idiomas en los que se encuentran los libros.
                    \nEscriba la opcion que desea consultar:
                    \n1-English/Ingles
                    2-Spanish/Español
                    3-Volver al menu
                    4-Salir
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    System.out.println("en - english");
                    List<Libro> librosBuscadosEn = repositorio.findByIdioma("en");

                    int conteoLibrosEn = librosBuscadosEn.size();
                    System.out.println("Número de libros con idioma '" + "en - english" + "': " + conteoLibrosEn);

                    if (conteoLibrosEn >= 1) {
                        System.out.println("¿Desea ver los libros? (Si/No):");
                        String opcionEn = teclado.nextLine();
                        if (opcionEn.equalsIgnoreCase("Si")) {
                            librosBuscadosEn.forEach(System.out::println);
                            System.out.println("¿Desea hacer otra consulta? (Si/No):");
                            String otraConsultaEn = teclado.nextLine();
                            if (otraConsultaEn.equalsIgnoreCase("Si")) {
                                mostrarLibrosPorIdioma();
                            } else {
                                System.out.println("Saliendo de la aplicación...");
                                System.exit(0);
                            }
                        } else {
                            System.out.println("¿Desea hacer otra consulta? (Si/No):");
                            String otraConsultaEn = teclado.nextLine();
                            if (otraConsultaEn.equalsIgnoreCase("Si")) {
                                mostrarLibrosPorIdioma();
                            } else {
                                System.out.println("No se encontraron libros en inglés.");
                                System.out.println("Saliendo de la aplicación...");
                                System.exit(0);
                            }
                        }
                    } else {
                        System.out.println("No se encontraron libros en inglés.");
                        System.out.println("¿Desea hacer otra consulta? (Si/No):");
                        String otraConsultaEn = teclado.nextLine();
                        if (otraConsultaEn.equalsIgnoreCase("Si")) {
                            mostrarLibrosPorIdioma();
                        } else {
                            System.out.println("Saliendo de la aplicación...");
                            System.exit(0);
                        }
                    }
                    break;
                case 2:
                    System.out.println("es - español");
                    List<Libro> librosBuscadosEs = repositorio.findByIdioma("es");

                    int conteoLibrosEs = librosBuscadosEs.size();
                    System.out.println("Número de libros con idioma '" + "es - español" + "': " + conteoLibrosEs);

                    if (conteoLibrosEs >= 1) {
                        System.out.println("¿Desea ver los libros? (Si/No):");
                        String opcionEs = teclado.nextLine();
                        if (opcionEs.equalsIgnoreCase("Si")) {
                            librosBuscadosEs.forEach(System.out::println);
                            System.out.println("¿Desea hacer otra consulta? (Si/No):");
                            String otraConsultaEs = teclado.nextLine();
                            if (otraConsultaEs.equalsIgnoreCase("Si")) {
                                mostrarLibrosPorIdioma();
                            } else {
                                System.out.println("Saliendo de la aplicación...");
                                System.exit(0);
                            }
                        } else {
                            System.out.println("¿Desea hacer otra consulta? (Si/No):");
                            String otraConsultaEs = teclado.nextLine();
                            if (otraConsultaEs.equalsIgnoreCase("Si")) {
                                mostrarLibrosPorIdioma();
                            } else {
                                System.out.println("Saliendo de la aplicación...");
                                System.exit(0);
                            }
                        }
                    } else {
                        System.out.println("No se encontraron libros en español.");
                        System.out.println("¿Desea hacer otra consulta? (Si/No):");
                        String otraConsultaEs = teclado.nextLine();
                        if (otraConsultaEs.equalsIgnoreCase("Si")) {
                            mostrarLibrosPorIdioma();
                        } else {
                            System.out.println("Saliendo de la aplicación...");
                            System.exit(0);
                        }
                    }
                    break;
                case 3:
                    muestraElMenu();
                    break;
                case 4:
                    System.out.println("Saliendo de la aplicación...");
                    System.exit(0);
                default:
                    System.out.println("***Opción inválida***");


            }
        }
    }

    private void listarTopLibros() {
        List<Libro> topLibros = repositorio.findTop10ByOrderByDescargas();
        System.out.println("****Este es el TOP 10 de libros mas descargados****");
        topLibros.forEach(l -> System.out.println("\nLibro: " + l.getTitulo() + ", Descargas: " + l.getDescargas()));
        System.out.println("\n¿Desea hacer otra consulta? (Si/No):");
        String otraConsultaEs = teclado.nextLine();
        if (otraConsultaEs.equalsIgnoreCase("Si")) {
            muestraElMenu();
        } else {
            System.out.println("Saliendo de la aplicación...");
            System.exit(0);
        }

    }

    private void buscarAutorPorNombre() {
        System.out.println("Escriba el nombre, apellido o pseudónimo del autor que desea buscar:");
        var nombreAutor = teclado.nextLine().trim();

        Optional<Autor> autorEncontrado = repositorio2.findByNombreContainingIgnoreCase(nombreAutor);
        if (!autorEncontrado.isEmpty()) {
            System.out.println("Autor encontrados en la base de datos:");
            System.out.println(autorEncontrado.get());
        } else {
            System.out.println("No se encontraron autores. Revise si los datos son correctos.");
        }

        System.out.println("\n¿Desea hacer otra consulta? (Si/No):");
        String otraConsultaEs = teclado.nextLine();
        if (otraConsultaEs.equalsIgnoreCase("Si")) {
            buscarAutorPorNombre();
        } else {
            System.out.println("Saliendo de la aplicación...");
            System.exit(0);
        }
    }

    private void listarAutoresFallecidosDespuesDelAno() {
        System.out.println("Por favor, indica el año en el que deseas consultar los autores:");
        var ano1 = teclado.nextInt();
        teclado.nextLine();
        autores = repositorio2.findAll();

        List<Autor> autoresFallecidos = new ArrayList<>();

        for (Autor autor : autores) {
            if (autor.getFechaFallecimiento() > ano1) {
                autoresFallecidos.add(autor);
            }
        }

        if (autoresFallecidos.isEmpty()) {
            System.out.println("No se encontraron autores que murieron después del año proporcionado.");
        } else {
            System.out.println("Autores que murieron después del año " + ano1 + ":");
            autoresFallecidos.forEach(autor -> System.out.println(autor.getNombre() + " (Fallecimiento: " + autor.getFechaFallecimiento().intValue() + ")"));
        }

        System.out.println("\n¿Desea hacer otra consulta? (Si/No):");
        String otraConsulta = teclado.nextLine();
        if (otraConsulta.equalsIgnoreCase("Si")) {
            muestraElMenu();
        } else {
            System.out.println("Saliendo de la aplicación...");
            System.exit(0);
        }
    }

    private void estadisticas() {
        List<Libro> listaLibros = repositorio.findByOrderByDescargasDesc();

        DoubleSummaryStatistics est = listaLibros.stream()
                .filter(e -> e.getDescargas() > 0.0)
                .collect(Collectors.summarizingDouble(Libro::getDescargas));

        // Encontrar el libro más descargado
        Optional<Libro> libroMasDescargado = listaLibros.stream()
                .filter(e -> e.getDescargas() > 0.0)
                .max(Comparator.comparingDouble(Libro::getDescargas));

        // Encontrar el libro menos descargado
        Optional<Libro> libroMenosDescargado = listaLibros.stream()
                .filter(e -> e.getDescargas() > 0.0)
                .min(Comparator.comparingDouble(Libro::getDescargas));

        System.out.println("***Estadisticas de su base de datos***");
        System.out.println("\nLa media de descargas de los libros almacenados es: " + est.getAverage());


        if (libroMasDescargado.isPresent()) {
            System.out.println("\nEl libro más descargado es: " + libroMasDescargado.get().getTitulo() + " con " + libroMasDescargado.get().getDescargas() + " descargas");
        } else {
            System.out.println("No hay libros registrados con descargas.");
        }

        if (libroMenosDescargado.isPresent()) {
            System.out.println("\nEl libro menos descargado es: " + libroMenosDescargado.get().getTitulo() + " con " + libroMenosDescargado.get().getDescargas() + " descargas");
        } else {
            System.out.println("No hay libros registrados con descargas.");
        }

        System.out.println("\n¿Desea hacer otra consulta? (Si/No):");
        String otraConsulta = teclado.nextLine();
        if (otraConsulta.equalsIgnoreCase("Si")) {
            muestraElMenu();
        } else {
            System.out.println("Saliendo de la aplicación...");
            System.exit(0);
        }
    }


}














