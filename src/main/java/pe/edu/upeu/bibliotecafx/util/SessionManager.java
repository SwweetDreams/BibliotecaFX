package pe.edu.upeu.bibliotecafx.util;

import lombok.Getter;
import lombok.Setter;
import pe.edu.upeu.bibliotecafx.model.Usuario;

public class SessionManager {
    
    @Getter
    @Setter
    private static Usuario usuarioActual;
    
    @Getter
    @Setter
    private static boolean autenticado = false;
    
    /**
     * Iniciar sesión
     */
    public static void iniciarSesion(Usuario usuario) {
        usuarioActual = usuario;
        autenticado = true;
    }
    
    /**
     * Cerrar sesión
     */
    public static void cerrarSesion() {
        usuarioActual = null;
        autenticado = false;
    }
    
    /**
     * Verificar si hay un usuario autenticado
     */
    public static boolean isAutenticado() {
        return autenticado && usuarioActual != null;
    }
    
    /**
     * Verificar si el usuario actual es administrador
     */
    public static boolean isAdministrador() {
        return isAutenticado() && usuarioActual.isAdministrador();
    }
    
    /**
     * Verificar si el usuario actual es bibliotecario
     */
    public static boolean isBibliotecario() {
        return isAutenticado() && usuarioActual.isBibliotecario();
    }
    
    /**
     * Obtener el nombre completo del usuario actual
     */
    public static String getNombreUsuario() {
        return isAutenticado() ? usuarioActual.getNombreCompleto() : "No autenticado";
    }
    
    /**
     * Obtener el email del usuario actual
     */
    public static String getEmailUsuario() {
        return isAutenticado() ? usuarioActual.getEmail() : "";
    }
    
    /**
     * Obtener el rol del usuario actual
     */
    public static String getRolUsuario() {
        return isAutenticado() ? usuarioActual.getRol().toString() : "";
    }
} 