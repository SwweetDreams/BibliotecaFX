package pe.edu.upeu.bibliotecafx.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "libros")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Libro {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String titulo;
    
    @Column(length = 100)
    private String autor;
    
    @Column(length = 50)
    private String isbn;
    
    @Column(length = 100)
    private String editorial;
    
    @Column
    private Integer anioPublicacion;
    
    @Column(length = 20)
    private String categoria;
    
    @Column
    private Integer cantidadDisponible;
    
    @Column
    private Integer cantidadTotal;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
    
    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }
} 