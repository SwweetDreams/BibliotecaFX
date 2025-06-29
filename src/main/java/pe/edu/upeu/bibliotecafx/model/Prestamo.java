package pe.edu.upeu.bibliotecafx.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "prestamos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prestamo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "libro_id", nullable = false)
    private Libro libro;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
    
    @Column(name = "fecha_prestamo")
    private LocalDateTime fechaPrestamo;
    
    @Column(name = "fecha_devolucion_esperada", nullable = false)
    private LocalDateTime fechaDevolucionEsperada;
    
    @Column(name = "fecha_devolucion_real")
    private LocalDateTime fechaDevolucionReal;
    
    @Enumerated(EnumType.STRING)
    @Column
    private EstadoPrestamo estado = EstadoPrestamo.ACTIVO;
    
    @Column(columnDefinition = "TEXT")
    private String observaciones;
    
    public enum EstadoPrestamo {
        ACTIVO, DEVUELTO, VENCIDO, PERDIDO
    }
    
    @PrePersist
    protected void onCreate() {
        if (fechaPrestamo == null) {
            fechaPrestamo = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoPrestamo.ACTIVO;
        }
    }
    
    // Método para verificar si el préstamo está vencido
    public boolean isVencido() {
        return estado == EstadoPrestamo.ACTIVO && 
               LocalDateTime.now().isAfter(fechaDevolucionEsperada);
    }
    
    // Método para calcular días de retraso
    public long getDiasRetraso() {
        if (estado == EstadoPrestamo.ACTIVO && isVencido()) {
            return java.time.Duration.between(fechaDevolucionEsperada, LocalDateTime.now()).toDays();
        }
        return 0;
    }
} 