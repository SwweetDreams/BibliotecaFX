package pe.edu.upeu.bibliotecafx.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.bibliotecafx.model.Prestamo;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    
    List<Prestamo> findByEstado(Prestamo.EstadoPrestamo estado);
    
    List<Prestamo> findByUsuarioId(Long usuarioId);
    
    List<Prestamo> findByLibroId(Long libroId);
    
    List<Prestamo> findByUsuarioIdAndLibroIdAndEstado(Long usuarioId, Long libroId, Prestamo.EstadoPrestamo estado);
    
    List<Prestamo> findByEstadoAndFechaDevolucionEsperadaBefore(Prestamo.EstadoPrestamo estado, LocalDateTime fecha);
    
    List<Prestamo> findByEstadoAndFechaDevolucionEsperadaBetween(Prestamo.EstadoPrestamo estado, LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    long countByEstado(Prestamo.EstadoPrestamo estado);
    
    long countByEstadoAndFechaDevolucionEsperadaBefore(Prestamo.EstadoPrestamo estado, LocalDateTime fecha);
    
    @Query("SELECT p FROM Prestamo p WHERE p.usuario.id = :usuarioId AND p.estado = :estado")
    List<Prestamo> findPrestamosActivosPorUsuario(@Param("usuarioId") Long usuarioId, @Param("estado") Prestamo.EstadoPrestamo estado);
    
    @Query("SELECT p FROM Prestamo p WHERE p.libro.id = :libroId AND p.estado = :estado")
    List<Prestamo> findPrestamosActivosPorLibro(@Param("libroId") Long libroId, @Param("estado") Prestamo.EstadoPrestamo estado);
    
    @Query("SELECT COUNT(p) FROM Prestamo p WHERE p.usuario.id = :usuarioId AND p.estado = :estado")
    long countPrestamosActivosPorUsuario(@Param("usuarioId") Long usuarioId, @Param("estado") Prestamo.EstadoPrestamo estado);
    
    @Query("SELECT COUNT(p) FROM Prestamo p WHERE p.libro.id = :libroId AND p.estado = :estado")
    long countPrestamosActivosPorLibro(@Param("libroId") Long libroId, @Param("estado") Prestamo.EstadoPrestamo estado);
    
    @Query("SELECT p FROM Prestamo p JOIN FETCH p.libro JOIN FETCH p.usuario ORDER BY p.fechaPrestamo DESC")
    List<Prestamo> findAllWithLibroAndUsuario();
    
    @Query("SELECT p FROM Prestamo p JOIN FETCH p.libro JOIN FETCH p.usuario WHERE p.estado = :estado ORDER BY p.fechaPrestamo DESC")
    List<Prestamo> findByEstadoWithLibroAndUsuario(@Param("estado") Prestamo.EstadoPrestamo estado);
} 