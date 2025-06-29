package pe.edu.upeu.bibliotecafx.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.upeu.bibliotecafx.model.Libro;
import pe.edu.upeu.bibliotecafx.model.Prestamo;
import pe.edu.upeu.bibliotecafx.model.Usuario;
import pe.edu.upeu.bibliotecafx.repository.LibroRepository;
import pe.edu.upeu.bibliotecafx.repository.PrestamoRepository;
import pe.edu.upeu.bibliotecafx.repository.UsuarioRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class PrestamoService {
    
    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    private static final Logger log = LoggerFactory.getLogger(PrestamoService.class);
    
    public List<Prestamo> obtenerTodosLosPrestamos() {
        return prestamoRepository.findAllWithLibroAndUsuario();
    }
    
    public List<Prestamo> obtenerPrestamosActivos() {
        return prestamoRepository.findByEstadoWithLibroAndUsuario(Prestamo.EstadoPrestamo.ACTIVO);
    }
    
    public List<Prestamo> obtenerPrestamosPorUsuario(Long usuarioId) {
        return prestamoRepository.findByUsuarioId(usuarioId);
    }
    
    public List<Prestamo> obtenerPrestamosPorLibro(Long libroId) {
        return prestamoRepository.findByLibroId(libroId);
    }
    
    public Optional<Prestamo> obtenerPrestamoPorId(Long id) {
        return prestamoRepository.findById(id);
    }
    
    public Prestamo crearPrestamo(Long libroId, Long usuarioId, int diasPrestamo) {
        Optional<Libro> libroOpt = libroRepository.findById(libroId);
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        
        if (libroOpt.isEmpty()) {
            throw new RuntimeException("Libro no encontrado");
        }
        
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }
        
        Libro libro = libroOpt.get();
        Usuario usuario = usuarioOpt.get();
        
        // Verificar disponibilidad
        if (libro.getCantidadDisponible() <= 0) {
            throw new RuntimeException("El libro no está disponible para préstamo");
        }
        
        // Verificar si el usuario ya tiene este libro prestado
        List<Prestamo> prestamosActivos = prestamoRepository.findByUsuarioIdAndLibroIdAndEstado(usuarioId, libroId, Prestamo.EstadoPrestamo.ACTIVO);
        if (!prestamosActivos.isEmpty()) {
            throw new RuntimeException("El usuario ya tiene este libro prestado");
        }
        
        // Crear préstamo
        Prestamo prestamo = new Prestamo();
        prestamo.setLibro(libro);
        prestamo.setUsuario(usuario);
        prestamo.setFechaPrestamo(LocalDateTime.now());
        prestamo.setFechaDevolucionEsperada(LocalDateTime.now().plusDays(diasPrestamo));
        prestamo.setEstado(Prestamo.EstadoPrestamo.ACTIVO);
        prestamo.setObservaciones("Préstamo creado automáticamente");
        
        // Actualizar cantidad disponible del libro
        libro.setCantidadDisponible(libro.getCantidadDisponible() - 1);
        libroRepository.save(libro);
        
        return prestamoRepository.save(prestamo);
    }
    
    public Prestamo devolverPrestamo(Long prestamoId) {
        Optional<Prestamo> prestamoOpt = prestamoRepository.findById(prestamoId);
        
        if (prestamoOpt.isEmpty()) {
            throw new RuntimeException("Préstamo no encontrado");
        }
        
        Prestamo prestamo = prestamoOpt.get();
        
        if (prestamo.getEstado() != Prestamo.EstadoPrestamo.ACTIVO) {
            throw new RuntimeException("El préstamo ya no está activo");
        }
        
        // Marcar como devuelto
        prestamo.setEstado(Prestamo.EstadoPrestamo.DEVUELTO);
        prestamo.setFechaDevolucionReal(LocalDateTime.now());
        
        // Actualizar cantidad disponible del libro
        Libro libro = prestamo.getLibro();
        libro.setCantidadDisponible(libro.getCantidadDisponible() + 1);
        libroRepository.save(libro);
        
        return prestamoRepository.save(prestamo);
    }
    
    public Prestamo renovarPrestamo(Long prestamoId, int diasAdicionales) {
        Optional<Prestamo> prestamoOpt = prestamoRepository.findById(prestamoId);
        
        if (prestamoOpt.isEmpty()) {
            throw new RuntimeException("Préstamo no encontrado");
        }
        
        Prestamo prestamo = prestamoOpt.get();
        
        if (prestamo.getEstado() != Prestamo.EstadoPrestamo.ACTIVO) {
            throw new RuntimeException("Solo se pueden renovar préstamos activos");
        }
        
        // Verificar si no está vencido
        if (prestamo.getFechaDevolucionEsperada().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("No se puede renovar un préstamo vencido");
        }
        
        // Renovar fecha de devolución
        prestamo.setFechaDevolucionEsperada(prestamo.getFechaDevolucionEsperada().plusDays(diasAdicionales));
        prestamo.setObservaciones("Préstamo renovado por " + diasAdicionales + " días adicionales");
        
        return prestamoRepository.save(prestamo);
    }
    
    public List<Prestamo> obtenerPrestamosVencidos() {
        return prestamoRepository.findByEstadoAndFechaDevolucionEsperadaBefore(Prestamo.EstadoPrestamo.ACTIVO, LocalDateTime.now());
    }
    
    public List<Prestamo> obtenerPrestamosPorVencer(int dias) {
        LocalDateTime fechaLimite = LocalDateTime.now().plusDays(dias);
        return prestamoRepository.findByEstadoAndFechaDevolucionEsperadaBetween(Prestamo.EstadoPrestamo.ACTIVO, LocalDateTime.now(), fechaLimite);
    }
    
    public void eliminarPrestamo(Long id) {
        log.info("Eliminando préstamo con ID: {}", id);
        
        Optional<Prestamo> prestamoOpt = prestamoRepository.findById(id);
        if (prestamoOpt.isEmpty()) {
            throw new RuntimeException("Préstamo no encontrado con ID: " + id);
        }
        
        Prestamo prestamo = prestamoOpt.get();
        String info = "Libro: " + prestamo.getLibro().getTitulo() + ", Usuario: " + prestamo.getUsuario().getNombreCompleto();
        
        try {
            prestamoRepository.deleteById(id);
            prestamoRepository.flush(); // Forzar la sincronización con la base de datos
            
            log.info("Préstamo eliminado exitosamente: {} (ID: {})", info, id);
        } catch (Exception e) {
            log.error("Error al eliminar préstamo con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al eliminar préstamo: " + e.getMessage());
        }
    }
    
    /**
     * Eliminar un préstamo usando EntityManager (método alternativo)
     */
    public void eliminarPrestamoForzado(Long id) {
        log.info("Eliminando préstamo forzado con ID: {}", id);
        
        try {
            Prestamo prestamo = entityManager.find(Prestamo.class, id);
            if (prestamo != null) {
                String info = "Libro: " + prestamo.getLibro().getTitulo() + ", Usuario: " + prestamo.getUsuario().getNombreCompleto();
                
                // Eliminar usando EntityManager
                entityManager.remove(prestamo);
                entityManager.flush();
                entityManager.clear();
                
                log.info("Préstamo eliminado forzadamente: {} (ID: {})", info, id);
            } else {
                throw new RuntimeException("Préstamo no encontrado con ID: " + id);
            }
        } catch (Exception e) {
            log.error("Error al eliminar préstamo forzado con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al eliminar préstamo forzado: " + e.getMessage());
        }
    }
    
    public long contarPrestamosActivos() {
        return prestamoRepository.countByEstado(Prestamo.EstadoPrestamo.ACTIVO);
    }
    
    public long contarPrestamosVencidos() {
        return prestamoRepository.countByEstadoAndFechaDevolucionEsperadaBefore(Prestamo.EstadoPrestamo.ACTIVO, LocalDateTime.now());
    }
    
    public List<Prestamo> obtenerPrestamosActivosPorUsuario(Long usuarioId) {
        return prestamoRepository.findPrestamosActivosPorUsuario(usuarioId, Prestamo.EstadoPrestamo.ACTIVO);
    }
    
    public List<Prestamo> obtenerPrestamosActivosPorLibro(Long libroId) {
        return prestamoRepository.findPrestamosActivosPorLibro(libroId, Prestamo.EstadoPrestamo.ACTIVO);
    }
    
    public long contarPrestamosActivosPorUsuario(Long usuarioId) {
        return prestamoRepository.countPrestamosActivosPorUsuario(usuarioId, Prestamo.EstadoPrestamo.ACTIVO);
    }
    
    public long contarPrestamosActivosPorLibro(Long libroId) {
        return prestamoRepository.countPrestamosActivosPorLibro(libroId, Prestamo.EstadoPrestamo.ACTIVO);
    }
} 