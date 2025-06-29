package pe.edu.upeu.bibliotecafx.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upeu.bibliotecafx.model.Usuario;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Buscar por email (para login)
    Optional<Usuario> findByEmail(String email);
    
    // Buscar por email y password (para autenticación)
    Optional<Usuario> findByEmailAndPassword(String email, String password);
    
    // Buscar usuarios activos
    List<Usuario> findByActivoTrue();
    
    // Buscar por rol
    List<Usuario> findByRol(Usuario.RolUsuario rol);
    
    // Buscar bibliotecarios activos
    List<Usuario> findByRolAndActivoTrue(Usuario.RolUsuario rol);
    
    // Buscar por múltiples roles y activos
    List<Usuario> findByRolInAndActivoTrue(List<Usuario.RolUsuario> roles);
    
    // Verificar si existe un email
    boolean existsByEmail(String email);
    
    // Buscar por nombre o apellido (ignorando mayúsculas/minúsculas)
    List<Usuario> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(String nombre, String apellido);
    
    // Consulta personalizada para buscar usuarios
    @Query("SELECT u FROM Usuario u WHERE " +
           "(:nombre IS NULL OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
           "(:apellido IS NULL OR LOWER(u.apellido) LIKE LOWER(CONCAT('%', :apellido, '%'))) AND " +
           "(:rol IS NULL OR u.rol = :rol) AND " +
           "(:activo IS NULL OR u.activo = :activo)")
    List<Usuario> buscarUsuarios(@Param("nombre") String nombre, 
                                 @Param("apellido") String apellido, 
                                 @Param("rol") Usuario.RolUsuario rol, 
                                 @Param("activo") Boolean activo);
    
    // Contar usuarios por rol
    @Query("SELECT u.rol, COUNT(u) FROM Usuario u GROUP BY u.rol")
    List<Object[]> contarPorRol();
    
    // Obtener el administrador (debería ser solo uno)
    @Query("SELECT u FROM Usuario u WHERE u.rol = 'ADMINISTRADOR'")
    List<Usuario> findAdministradores();
    
    // Obtener usuarios que pueden acceder al sistema
    @Query("SELECT u FROM Usuario u WHERE u.rol IN ('ADMINISTRADOR', 'BIBLIOTECARIO') AND u.activo = true")
    List<Usuario> findUsuariosSistema();
} 