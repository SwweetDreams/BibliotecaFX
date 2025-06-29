package pe.edu.upeu.bibliotecafx.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @Column(nullable = false, length = 100)
    private String apellido;
    
    @Column(nullable = false, unique = true, length = 150)
    private String email;
    
    @Column(nullable = false, length = 100)
    private String password;
    
    @Column(length = 20)
    private String telefono;
    
    @Column(columnDefinition = "TEXT")
    private String direccion;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RolUsuario rol = RolUsuario.LECTOR;
    
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;
    
    @Column
    private Boolean activo = true;
    
    public enum RolUsuario {
        ADMINISTRADOR, BIBLIOTECARIO, LECTOR
    }
    
    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
        if (activo == null) {
            activo = true;
        }
        if (rol == null) {
            rol = RolUsuario.LECTOR;
        }
    }
    
    // Método para obtener el nombre completo
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
    
    // Método para verificar si es administrador
    public boolean isAdministrador() {
        return RolUsuario.ADMINISTRADOR.equals(rol);
    }
    
    // Método para verificar si es bibliotecario
    public boolean isBibliotecario() {
        return RolUsuario.BIBLIOTECARIO.equals(rol);
    }
    
    // Método para verificar si es lector
    public boolean isLector() {
        return RolUsuario.LECTOR.equals(rol);
    }
    
    // Método para verificar si puede acceder al sistema (admin o bibliotecario)
    public boolean puedeAccederAlSistema() {
        return isAdministrador() || isBibliotecario();
    }
} 