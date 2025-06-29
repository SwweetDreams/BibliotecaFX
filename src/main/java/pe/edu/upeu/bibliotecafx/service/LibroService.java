package pe.edu.upeu.bibliotecafx.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bibliotecafx.model.Libro;
import pe.edu.upeu.bibliotecafx.repository.LibroRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LibroService {
    
    private final LibroRepository libroRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * Guardar un nuevo libro
     */
    public Libro guardarLibro(Libro libro) {
        log.info("Guardando libro: {}", libro.getTitulo());
        return libroRepository.save(libro);
    }
    
    /**
     * Actualizar un libro existente
     */
    public Libro actualizarLibro(Long id, Libro libro) {
        log.info("Actualizando libro con ID: {}", id);
        if (!libroRepository.existsById(id)) {
            throw new RuntimeException("Libro no encontrado con ID: " + id);
        }
        libro.setId(id);
        return libroRepository.save(libro);
    }
    
    /**
     * Eliminar un libro (método original)
     */
    @Transactional
    public void eliminarLibro(Long id) {
        log.info("Eliminando libro con ID: {}", id);
        
        Optional<Libro> libroOpt = libroRepository.findById(id);
        if (libroOpt.isEmpty()) {
            throw new RuntimeException("Libro no encontrado con ID: " + id);
        }
        
        Libro libro = libroOpt.get();
        String titulo = libro.getTitulo(); // Guardar para el log
        
        try {
            // Verificar si hay préstamos activos para este libro
            // Si hay préstamos activos, no permitir eliminar
            // (Esto se puede implementar si es necesario)
            
            libroRepository.deleteById(id);
            libroRepository.flush(); // Forzar la sincronización con la base de datos
            
            log.info("Libro eliminado exitosamente: {} (ID: {})", titulo, id);
        } catch (Exception e) {
            log.error("Error al eliminar libro con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al eliminar libro: " + e.getMessage());
        }
    }
    
    /**
     * Eliminar un libro usando EntityManager (método alternativo)
     */
    @Transactional
    public void eliminarLibroForzado(Long id) {
        log.info("Eliminando libro forzado con ID: {}", id);
        
        try {
            Libro libro = entityManager.find(Libro.class, id);
            if (libro != null) {
                String titulo = libro.getTitulo();
                
                // Eliminar usando EntityManager
                entityManager.remove(libro);
                entityManager.flush();
                entityManager.clear();
                
                log.info("Libro eliminado forzadamente: {} (ID: {})", titulo, id);
            } else {
                throw new RuntimeException("Libro no encontrado con ID: " + id);
            }
        } catch (Exception e) {
            log.error("Error al eliminar libro forzado con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al eliminar libro forzado: " + e.getMessage());
        }
    }
    
    /**
     * Buscar libro por ID
     */
    @Transactional(readOnly = true)
    public Optional<Libro> buscarPorId(Long id) {
        return libroRepository.findById(id);
    }
    
    /**
     * Obtener todos los libros
     */
    @Transactional(readOnly = true)
    public List<Libro> obtenerTodosLosLibros() {
        return libroRepository.findAll();
    }
    
    /**
     * Buscar libros por título
     */
    @Transactional(readOnly = true)
    public List<Libro> buscarPorTitulo(String titulo) {
        return libroRepository.findByTituloContainingIgnoreCase(titulo);
    }
    
    /**
     * Buscar libros por autor
     */
    @Transactional(readOnly = true)
    public List<Libro> buscarPorAutor(String autor) {
        return libroRepository.findByAutorContainingIgnoreCase(autor);
    }
    
    /**
     * Buscar libro por ISBN
     */
    @Transactional(readOnly = true)
    public Optional<Libro> buscarPorIsbn(String isbn) {
        return libroRepository.findByIsbn(isbn);
    }
    
    /**
     * Buscar libros por categoría
     */
    @Transactional(readOnly = true)
    public List<Libro> buscarPorCategoria(String categoria) {
        return libroRepository.findByCategoria(categoria);
    }
    
    /**
     * Obtener libros disponibles
     */
    @Transactional(readOnly = true)
    public List<Libro> obtenerLibrosDisponibles() {
        return libroRepository.findByCantidadDisponibleGreaterThan(0);
    }
    
    /**
     * Buscar libros con múltiples criterios
     */
    @Transactional(readOnly = true)
    public List<Libro> buscarLibros(String titulo, String autor, String categoria, Boolean disponible) {
        return libroRepository.buscarLibros(titulo, autor, categoria, disponible);
    }
    
    /**
     * Obtener libros con stock bajo
     */
    @Transactional(readOnly = true)
    public List<Libro> obtenerLibrosConStockBajo() {
        return libroRepository.findLibrosConStockBajo();
    }
    
    /**
     * Prestar un libro (disminuir cantidad disponible)
     */
    @Transactional
    public boolean prestarLibro(Long id) {
        Optional<Libro> libroOpt = libroRepository.findById(id);
        if (libroOpt.isPresent()) {
            Libro libro = libroOpt.get();
            if (libro.getCantidadDisponible() > 0) {
                libro.setCantidadDisponible(libro.getCantidadDisponible() - 1);
                libroRepository.save(libro);
                log.info("Libro prestado: {}", libro.getTitulo());
                return true;
            }
        }
        return false;
    }
    
    /**
     * Devolver un libro (aumentar cantidad disponible)
     */
    @Transactional
    public boolean devolverLibro(Long id) {
        Optional<Libro> libroOpt = libroRepository.findById(id);
        if (libroOpt.isPresent()) {
            Libro libro = libroOpt.get();
            if (libro.getCantidadDisponible() < libro.getCantidadTotal()) {
                libro.setCantidadDisponible(libro.getCantidadDisponible() + 1);
                libroRepository.save(libro);
                log.info("Libro devuelto: {}", libro.getTitulo());
                return true;
            }
        }
        return false;
    }
    
    /**
     * Búsqueda general en todos los campos
     */
    @Transactional(readOnly = true)
    public List<Libro> buscarGeneral(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return obtenerTodosLosLibros();
        }
        
        return libroRepository.buscarGeneral(termino.trim());
    }
} 