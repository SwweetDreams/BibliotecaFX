package pe.edu.upeu.bibliotecafx.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.bibliotecafx.model.Usuario;
import pe.edu.upeu.bibliotecafx.repository.UsuarioRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {
    
    private final UsuarioRepository usuarioRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * Autenticar usuario por email y password (solo admin y bibliotecarios)
     */
    public Optional<Usuario> autenticar(String email, String password) {
        log.info("Intentando autenticar usuario: {}", email);
        Optional<Usuario> usuario = usuarioRepository.findByEmailAndPassword(email, password);
        
        if (usuario.isPresent() && usuario.get().getActivo()) {
            Usuario user = usuario.get();
            
            // Verificar que el usuario pueda acceder al sistema
            if (user.puedeAccederAlSistema()) {
                log.info("Usuario autenticado exitosamente: {} (Rol: {})", email, user.getRol());
                return usuario;
            } else {
                log.warn("Usuario lector intentó acceder al sistema: {}", email);
                return Optional.empty();
            }
        } else {
            log.warn("Autenticación fallida para usuario: {}", email);
            return Optional.empty();
        }
    }
    
    /**
     * Registrar un nuevo lector
     */
    public Usuario registrarLector(Usuario usuario) {
        log.info("Registrando nuevo lector: {}", usuario.getEmail());
        
        // Verificar que el email no exista
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El email ya está registrado: " + usuario.getEmail());
        }
        
        // Asegurar que se registre como lector
        usuario.setRol(Usuario.RolUsuario.LECTOR);
        usuario.setActivo(true);
        
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        log.info("Lector registrado exitosamente: {}", usuario.getEmail());
        return usuarioGuardado;
    }
    
    /**
     * Registrar un nuevo bibliotecario (solo desde el sistema)
     */
    public Usuario registrarBibliotecario(Usuario usuario) {
        log.info("Registrando nuevo bibliotecario: {}", usuario.getEmail());
        
        // Verificar que el email no exista
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El email ya está registrado: " + usuario.getEmail());
        }
        
        // Asegurar que solo se registren bibliotecarios
        usuario.setRol(Usuario.RolUsuario.BIBLIOTECARIO);
        usuario.setActivo(true);
        
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        log.info("Bibliotecario registrado exitosamente: {}", usuario.getEmail());
        return usuarioGuardado;
    }
    
    /**
     * Crear el administrador inicial (solo se puede crear uno)
     */
    public Usuario crearAdministrador(Usuario usuario) {
        log.info("Creando administrador: {}", usuario.getEmail());
        
        // Verificar que no exista otro administrador
        List<Usuario> administradores = usuarioRepository.findAdministradores();
        if (!administradores.isEmpty()) {
            throw new RuntimeException("Ya existe un administrador en el sistema");
        }
        
        // Verificar que el email no exista
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El email ya está registrado: " + usuario.getEmail());
        }
        
        usuario.setRol(Usuario.RolUsuario.ADMINISTRADOR);
        usuario.setActivo(true);
        
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        log.info("Administrador creado exitosamente: {}", usuario.getEmail());
        return usuarioGuardado;
    }
    
    /**
     * Obtener usuario por email
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
    
    /**
     * Obtener usuario por ID
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }
    
    /**
     * Obtener todos los usuarios activos
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerUsuariosActivos() {
        return usuarioRepository.findByActivoTrue();
    }
    
    /**
     * Obtener bibliotecarios activos
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerBibliotecariosActivos() {
        return usuarioRepository.findByRolAndActivoTrue(Usuario.RolUsuario.BIBLIOTECARIO);
    }
    
    /**
     * Obtener lectores activos
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerLectoresActivos() {
        return usuarioRepository.findByRolAndActivoTrue(Usuario.RolUsuario.LECTOR);
    }
    
    /**
     * Obtener usuarios que pueden acceder al sistema (admin y bibliotecarios)
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerUsuariosSistema() {
        return usuarioRepository.findByRolInAndActivoTrue(List.of(
            Usuario.RolUsuario.ADMINISTRADOR, 
            Usuario.RolUsuario.BIBLIOTECARIO
        ));
    }
    
    /**
     * Actualizar usuario
     */
    public Usuario actualizarUsuario(Long id, Usuario usuario) {
        log.info("Actualizando usuario con ID: {}", id);
        
        Optional<Usuario> usuarioExistente = usuarioRepository.findById(id);
        if (usuarioExistente.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        
        Usuario usuarioActual = usuarioExistente.get();
        
        // No permitir cambiar el rol del administrador
        if (usuarioActual.isAdministrador()) {
            usuario.setRol(Usuario.RolUsuario.ADMINISTRADOR);
        }
        
        usuario.setId(id);
        Usuario usuarioActualizado = usuarioRepository.save(usuario);
        log.info("Usuario actualizado exitosamente: {}", usuario.getEmail());
        return usuarioActualizado;
    }
    
    /**
     * Desactivar usuario (método original)
     */
    @Transactional
    public void desactivarUsuario(Long id) {
        log.info("Desactivando usuario con ID: {}", id);
        
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isPresent()) {
            Usuario usuarioActual = usuario.get();
            
            // No permitir desactivar al administrador
            if (usuarioActual.isAdministrador()) {
                throw new RuntimeException("No se puede desactivar al administrador");
            }
            
            String email = usuarioActual.getEmail(); // Guardar para el log
            
            try {
                usuarioActual.setActivo(false);
                usuarioRepository.save(usuarioActual);
                usuarioRepository.flush(); // Forzar la sincronización con la base de datos
                
                log.info("Usuario desactivado exitosamente: {} (ID: {})", email, id);
            } catch (Exception e) {
                log.error("Error al desactivar usuario con ID {}: {}", id, e.getMessage());
                throw new RuntimeException("Error al desactivar usuario: " + e.getMessage());
            }
        } else {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
    }
    
    /**
     * Desactivar usuario usando EntityManager (método alternativo)
     */
    @Transactional
    public void desactivarUsuarioForzado(Long id) {
        log.info("Desactivando usuario forzado con ID: {}", id);
        
        try {
            Usuario usuario = entityManager.find(Usuario.class, id);
            if (usuario != null) {
                // No permitir desactivar al administrador
                if (usuario.isAdministrador()) {
                    throw new RuntimeException("No se puede desactivar al administrador");
                }
                
                String email = usuario.getEmail();
                
                // Desactivar usando EntityManager
                usuario.setActivo(false);
                entityManager.merge(usuario);
                entityManager.flush();
                entityManager.clear();
                
                log.info("Usuario desactivado forzadamente: {} (ID: {})", email, id);
            } else {
                throw new RuntimeException("Usuario no encontrado con ID: " + id);
            }
        } catch (Exception e) {
            log.error("Error al desactivar usuario forzado con ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al desactivar usuario forzado: " + e.getMessage());
        }
    }
    
    /**
     * Activar usuario
     */
    public void activarUsuario(Long id) {
        log.info("Activando usuario con ID: {}", id);
        
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isPresent()) {
            Usuario usuarioActual = usuario.get();
            usuarioActual.setActivo(true);
            usuarioRepository.save(usuarioActual);
            log.info("Usuario activado exitosamente: {}", usuarioActual.getEmail());
        } else {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
    }
    
    /**
     * Verificar si existe un administrador
     */
    @Transactional(readOnly = true)
    public boolean existeAdministrador() {
        List<Usuario> administradores = usuarioRepository.findAdministradores();
        return !administradores.isEmpty();
    }
    
    /**
     * Cambiar password de usuario
     */
    public void cambiarPassword(Long id, String nuevaPassword) {
        log.info("Cambiando password para usuario con ID: {}", id);
        
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isPresent()) {
            Usuario usuarioActual = usuario.get();
            usuarioActual.setPassword(nuevaPassword);
            usuarioRepository.save(usuarioActual);
            log.info("Password cambiado exitosamente para: {}", usuarioActual.getEmail());
        } else {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
    }
    
    /**
     * Verificar si un usuario puede acceder al sistema
     */
    @Transactional(readOnly = true)
    public boolean puedeAccederAlSistema(String email) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
        return usuario.isPresent() && usuario.get().puedeAccederAlSistema();
    }
} 