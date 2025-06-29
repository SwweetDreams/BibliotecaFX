package pe.edu.upeu.bibliotecafx.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.bibliotecafx.model.Libro;

import java.util.List;
import java.util.Optional;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {
    
    // Buscar por título (ignorando mayúsculas/minúsculas)
    List<Libro> findByTituloContainingIgnoreCase(String titulo);
    
    // Buscar por autor
    List<Libro> findByAutorContainingIgnoreCase(String autor);
    
    // Buscar por ISBN
    Optional<Libro> findByIsbn(String isbn);
    
    // Buscar por categoría
    List<Libro> findByCategoria(String categoria);
    
    // Buscar libros disponibles (cantidad > 0)
    List<Libro> findByCantidadDisponibleGreaterThan(Integer cantidad);
    
    // Buscar por editorial
    List<Libro> findByEditorialContainingIgnoreCase(String editorial);
    
    // Consulta personalizada para buscar por múltiples criterios
    @Query("SELECT l FROM Libro l WHERE " +
           "(:titulo IS NULL OR LOWER(l.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))) AND " +
           "(:autor IS NULL OR LOWER(l.autor) LIKE LOWER(CONCAT('%', :autor, '%'))) AND " +
           "(:categoria IS NULL OR l.categoria = :categoria) AND " +
           "(:disponible IS NULL OR l.cantidadDisponible > 0)")
    List<Libro> buscarLibros(@Param("titulo") String titulo, 
                             @Param("autor") String autor, 
                             @Param("categoria") String categoria, 
                             @Param("disponible") Boolean disponible);
    
    // Contar libros por categoría
    @Query("SELECT l.categoria, COUNT(l) FROM Libro l GROUP BY l.categoria")
    List<Object[]> contarPorCategoria();
    
    // Obtener libros con stock bajo (menos de 5 disponibles)
    @Query("SELECT l FROM Libro l WHERE l.cantidadDisponible < 5")
    List<Libro> findLibrosConStockBajo();
    
    // Búsqueda general en todos los campos
    @Query("SELECT l FROM Libro l WHERE " +
           "LOWER(l.titulo) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(l.autor) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(l.isbn) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(l.categoria) LIKE LOWER(CONCAT('%', :termino, '%')) OR " +
           "LOWER(l.editorial) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Libro> buscarGeneral(@Param("termino") String termino);
} 