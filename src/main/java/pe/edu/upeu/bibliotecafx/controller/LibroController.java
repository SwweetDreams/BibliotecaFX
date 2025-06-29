package pe.edu.upeu.bibliotecafx.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.bibliotecafx.model.Libro;
import pe.edu.upeu.bibliotecafx.service.LibroService;

import java.util.List;

@RestController
@RequestMapping("/api/libros")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LibroController {
    
    private final LibroService libroService;
    
    @GetMapping
    public ResponseEntity<List<Libro>> obtenerTodosLosLibros() {
        return ResponseEntity.ok(libroService.obtenerTodosLosLibros());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Libro> obtenerLibroPorId(@PathVariable Long id) {
        return libroService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/buscar")
    public ResponseEntity<List<Libro>> buscarLibros(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String autor,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) Boolean disponible) {
        
        List<Libro> libros = libroService.buscarLibros(titulo, autor, categoria, disponible);
        return ResponseEntity.ok(libros);
    }
    
    @GetMapping("/disponibles")
    public ResponseEntity<List<Libro>> obtenerLibrosDisponibles() {
        return ResponseEntity.ok(libroService.obtenerLibrosDisponibles());
    }
    
    @GetMapping("/stock-bajo")
    public ResponseEntity<List<Libro>> obtenerLibrosConStockBajo() {
        return ResponseEntity.ok(libroService.obtenerLibrosConStockBajo());
    }
    
    @PostMapping
    public ResponseEntity<Libro> crearLibro(@RequestBody Libro libro) {
        Libro libroGuardado = libroService.guardarLibro(libro);
        return ResponseEntity.ok(libroGuardado);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Libro> actualizarLibro(@PathVariable Long id, @RequestBody Libro libro) {
        try {
            Libro libroActualizado = libroService.actualizarLibro(id, libro);
            return ResponseEntity.ok(libroActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarLibro(@PathVariable Long id) {
        try {
            libroService.eliminarLibro(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{id}/prestar")
    public ResponseEntity<String> prestarLibro(@PathVariable Long id) {
        boolean prestado = libroService.prestarLibro(id);
        if (prestado) {
            return ResponseEntity.ok("Libro prestado exitosamente");
        } else {
            return ResponseEntity.badRequest().body("No se pudo prestar el libro");
        }
    }
    
    @PostMapping("/{id}/devolver")
    public ResponseEntity<String> devolverLibro(@PathVariable Long id) {
        boolean devuelto = libroService.devolverLibro(id);
        if (devuelto) {
            return ResponseEntity.ok("Libro devuelto exitosamente");
        } else {
            return ResponseEntity.badRequest().body("No se pudo devolver el libro");
        }
    }
} 