package pe.edu.upeu.bibliotecafx.controller;

import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pe.edu.upeu.bibliotecafx.model.Usuario;
import pe.edu.upeu.bibliotecafx.service.AuthService;
import pe.edu.upeu.bibliotecafx.util.SessionManager;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class LoginController {
    
    private final AuthService authService;
    private final ApplicationContext applicationContext;
    
    @FXML
    private JFXTextField emailField;
    
    @FXML
    private JFXPasswordField passwordField;
    
    @FXML
    private JFXButton loginButton;
    
    @FXML
    private JFXButton registerButton;
    
    @FXML
    private JFXCheckBox rememberMeCheckBox;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private VBox loginContainer;
    
    @FXML
    private StackPane rootContainer;
    
    @FXML
    public void initialize() {
        try {
            // Configurar eventos
            loginButton.setOnAction(e -> handleLogin());
            registerButton.setOnAction(e -> handleRegister());
            
            // Permitir login con Enter
            passwordField.setOnAction(e -> handleLogin());
            
            // Configurar label de estado
            if (statusLabel != null) {
                statusLabel.setVisible(false);
            }
            
            // Verificar si existe administrador después de un breve delay
            Platform.runLater(() -> {
                if (!authService.existeAdministrador()) {
                    mostrarCrearAdministrador();
                }
            });
            
        } catch (Exception e) {
            System.err.println("Error en la inicialización del LoginController: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        if (email.isEmpty() || password.isEmpty()) {
            mostrarError("Por favor, complete todos los campos");
            return;
        }
        
        // Validar formato de email
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            mostrarError("Por favor, ingrese un email válido");
            emailField.requestFocus();
            return;
        }
        
        try {
            // Mostrar indicador de carga
            loginButton.setDisable(true);
            loginButton.setText("INICIANDO SESIÓN...");
            
            Optional<Usuario> usuario = authService.autenticar(email, password);
            
            if (usuario.isPresent()) {
                // Iniciar sesión
                SessionManager.iniciarSesion(usuario.get());
                mostrarExito("¡Bienvenido, " + usuario.get().getNombreCompleto() + "!");
                
                // Guardar preferencia de "recordar sesión" si está marcado
                if (rememberMeCheckBox.isSelected()) {
                    // Aquí se podría implementar la funcionalidad de recordar sesión
                }
                
                // Cerrar ventana de login y abrir aplicación principal
                Platform.runLater(() -> {
                    cerrarVentanaLogin();
                    abrirAplicacionPrincipal();
                });
                
            } else {
                // Verificar si el usuario existe pero es lector
                if (authService.obtenerPorEmail(email).isPresent()) {
                    mostrarError("Acceso denegado. Solo bibliotecarios y administradores pueden acceder al sistema.");
                } else {
                    mostrarError("Email o contraseña incorrectos");
                }
                passwordField.clear();
                passwordField.requestFocus();
            }
            
        } catch (Exception ex) {
            mostrarError("Error durante el login: " + ex.getMessage());
        } finally {
            // Restaurar botón
            loginButton.setDisable(false);
            loginButton.setText("INICIAR SESIÓN");
        }
    }
    
    @FXML
    private void handleRegister() {
        mostrarVentanaRegistro();
    }
    
    private void mostrarCrearAdministrador() {
        try {
            // Usar un Alert simple en lugar de JFXDialog para evitar problemas
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Configuración Inicial");
            alert.setHeaderText("Primera vez en el sistema");
            alert.setContentText("No existe un administrador. Se abrirá la ventana para crear el administrador inicial.");
            alert.showAndWait();
            
            mostrarVentanaCrearAdministrador();
            
        } catch (Exception e) {
            System.err.println("Error al mostrar diálogo de configuración inicial: " + e.getMessage());
            // Si falla el diálogo, intentar abrir directamente la ventana de crear administrador
            mostrarVentanaCrearAdministrador();
        }
    }
    
    private void mostrarVentanaCrearAdministrador() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crear-admin.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Crear Administrador");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(loginContainer.getScene().getWindow());
            
            stage.showAndWait();
            
        } catch (IOException e) {
            mostrarError("Error al abrir ventana de crear administrador: " + e.getMessage());
        }
    }
    
    private void mostrarVentanaRegistro() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/registro-lector.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Registro de Lector");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(loginContainer.getScene().getWindow());
            
            stage.showAndWait();
            
        } catch (IOException e) {
            mostrarError("Error al abrir ventana de registro: " + e.getMessage());
        }
    }
    
    private void abrirAplicacionPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("BibliotecaFX - " + SessionManager.getNombreUsuario());
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            
            stage.show();
            
        } catch (IOException e) {
            mostrarError("Error al abrir aplicación principal: " + e.getMessage());
        }
    }
    
    private void cerrarVentanaLogin() {
        Stage stage = (Stage) loginContainer.getScene().getWindow();
        stage.close();
    }
    
    private void mostrarError(String mensaje) {
        if (statusLabel != null) {
            statusLabel.setText(mensaje);
            statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff6b6b; -fx-text-alignment: center; -fx-wrap-text: true;");
            statusLabel.setVisible(true);
        }
    }
    
    private void mostrarExito(String mensaje) {
        if (statusLabel != null) {
            statusLabel.setText(mensaje);
            statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #51cf66; -fx-text-alignment: center; -fx-wrap-text: true;");
            statusLabel.setVisible(true);
        }
    }
    
    public void limpiarCampos() {
        emailField.clear();
        passwordField.clear();
        rememberMeCheckBox.setSelected(false);
        if (statusLabel != null) {
            statusLabel.setVisible(false);
        }
    }
} 