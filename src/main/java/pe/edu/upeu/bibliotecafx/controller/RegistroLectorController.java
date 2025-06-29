package pe.edu.upeu.bibliotecafx.controller;

import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.edu.upeu.bibliotecafx.model.Usuario;
import pe.edu.upeu.bibliotecafx.service.AuthService;

@Component
@RequiredArgsConstructor
public class RegistroLectorController {
    
    private final AuthService authService;
    
    @FXML
    private JFXTextField nombreField;
    
    @FXML
    private JFXTextField apellidoField;
    
    @FXML
    private JFXTextField emailField;
    
    @FXML
    private JFXPasswordField passwordField;
    
    @FXML
    private JFXPasswordField confirmPasswordField;
    
    @FXML
    private JFXTextField telefonoField;
    
    @FXML
    private JFXTextArea direccionField;
    
    @FXML
    private JFXCheckBox termsCheckBox;
    
    @FXML
    private JFXButton registerButton;
    
    @FXML
    private JFXButton cancelButton;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private VBox registroContainer;
    
    @FXML
    public void initialize() {
        // Configurar eventos
        registerButton.setOnAction(e -> handleRegistro());
        cancelButton.setOnAction(e -> cerrarVentana());
        
        // Permitir registro con Enter
        confirmPasswordField.setOnAction(e -> handleRegistro());
    }
    
    @FXML
    private void handleRegistro() {
        // Validar campos
        if (!validarCampos()) {
            return;
        }
        
        // Validar términos y condiciones
        if (!termsCheckBox.isSelected()) {
            mostrarError("Debe aceptar los términos y condiciones");
            return;
        }
        
        try {
            // Mostrar indicador de carga
            registerButton.setDisable(true);
            registerButton.setText("REGISTRANDO...");
            
            // Crear usuario lector
            Usuario lector = new Usuario();
            lector.setNombre(nombreField.getText().trim());
            lector.setApellido(apellidoField.getText().trim());
            lector.setEmail(emailField.getText().trim().toLowerCase());
            lector.setPassword(passwordField.getText());
            lector.setTelefono(telefonoField.getText().trim());
            lector.setDireccion(direccionField.getText().trim());
            
            // Registrar lector
            Usuario lectorRegistrado = authService.registrarLector(lector);
            
            mostrarExito("¡Lector registrado exitosamente!\n\n" +
                        "Nombre: " + lectorRegistrado.getNombreCompleto() + "\n" +
                        "Email: " + lectorRegistrado.getEmail() + "\n\n" +
                        "Nota: Los lectores no pueden acceder al sistema de gestión.\n" +
                        "Solo bibliotecarios y administradores pueden iniciar sesión.");
            
            // Limpiar campos después de un breve delay
            new Thread(() -> {
                try {
                    Thread.sleep(4000);
                    Platform.runLater(this::limpiarCampos);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
            
        } catch (Exception ex) {
            mostrarError("Error al registrar lector: " + ex.getMessage());
        } finally {
            // Restaurar botón
            registerButton.setDisable(false);
            registerButton.setText("REGISTRARSE");
        }
    }
    
    private boolean validarCampos() {
        // Validar nombre
        if (nombreField.getText().trim().isEmpty()) {
            mostrarError("El nombre es obligatorio");
            nombreField.requestFocus();
            return false;
        }
        
        if (nombreField.getText().trim().length() < 2) {
            mostrarError("El nombre debe tener al menos 2 caracteres");
            nombreField.requestFocus();
            return false;
        }
        
        // Validar apellido
        if (apellidoField.getText().trim().isEmpty()) {
            mostrarError("El apellido es obligatorio");
            apellidoField.requestFocus();
            return false;
        }
        
        if (apellidoField.getText().trim().length() < 2) {
            mostrarError("El apellido debe tener al menos 2 caracteres");
            apellidoField.requestFocus();
            return false;
        }
        
        // Validar email
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            mostrarError("El email es obligatorio");
            emailField.requestFocus();
            return false;
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            mostrarError("El formato del email no es válido");
            emailField.requestFocus();
            return false;
        }
        
        // Validar teléfono
        String telefono = telefonoField.getText().trim();
        if (telefono.isEmpty()) {
            mostrarError("El teléfono es obligatorio");
            telefonoField.requestFocus();
            return false;
        }
        
        if (!telefono.matches("^[0-9]{9,15}$")) {
            mostrarError("El teléfono debe tener entre 9 y 15 dígitos");
            telefonoField.requestFocus();
            return false;
        }
        
        // Validar dirección
        if (direccionField.getText().trim().isEmpty()) {
            mostrarError("La dirección es obligatoria");
            direccionField.requestFocus();
            return false;
        }
        
        // Validar password
        String password = passwordField.getText();
        if (password.isEmpty()) {
            mostrarError("La contraseña es obligatoria");
            passwordField.requestFocus();
            return false;
        }
        
        if (password.length() < 6) {
            mostrarError("La contraseña debe tener al menos 6 caracteres");
            passwordField.requestFocus();
            return false;
        }
        
        // Validar confirmación de password
        String confirmPassword = confirmPasswordField.getText();
        if (!password.equals(confirmPassword)) {
            mostrarError("Las contraseñas no coinciden");
            confirmPasswordField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    @FXML
    private void cerrarVentana() {
        Stage stage = (Stage) registroContainer.getScene().getWindow();
        stage.close();
    }
    
    private void mostrarError(String mensaje) {
        statusLabel.setText(mensaje);
        statusLabel.setStyle("-fx-text-fill: red;");
        statusLabel.setVisible(true);
    }
    
    private void mostrarExito(String mensaje) {
        statusLabel.setText(mensaje);
        statusLabel.setStyle("-fx-text-fill: green;");
        statusLabel.setVisible(true);
    }
    
    private void limpiarCampos() {
        nombreField.clear();
        apellidoField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        telefonoField.clear();
        direccionField.clear();
        termsCheckBox.setSelected(false);
        statusLabel.setVisible(false);
    }
} 