package pe.edu.upeu.bibliotecafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class BibliotecaFxApplication extends Application {

	private ConfigurableApplicationContext springContext;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void init() throws Exception {
		// Inicializar el contexto de Spring Boot
		springContext = SpringApplication.run(BibliotecaFxApplication.class);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			// Configurar manejo de errores no capturados
			Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
				System.err.println("Error no capturado en hilo " + thread.getName() + ": " + throwable.getMessage());
				throwable.printStackTrace();
			});
			
			// Cargar la pantalla de login
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
			loader.setControllerFactory(springContext::getBean);
			
			Parent root = loader.load();
			
			// Configurar la ventana principal
			primaryStage.setTitle("BibliotecaFX - Login");
			primaryStage.setScene(new Scene(root));
			primaryStage.setResizable(false);
			primaryStage.setWidth(600);
			primaryStage.setHeight(700);
			
			// Centrar la ventana
			primaryStage.centerOnScreen();
			
			// Mostrar la ventana después de un breve delay para asegurar que todo esté inicializado
			Platform.runLater(() -> {
				try {
					primaryStage.show();
					System.out.println("Aplicación JavaFX iniciada correctamente");
				} catch (Exception e) {
					System.err.println("Error al mostrar la ventana: " + e.getMessage());
					e.printStackTrace();
					mostrarVentanaError(e);
				}
			});
			
		} catch (Exception e) {
			System.err.println("Error al cargar la pantalla de login: " + e.getMessage());
			e.printStackTrace();
			
			// Mostrar una ventana de error básica
			mostrarVentanaError(e);
		}
	}

	@Override
	public void stop() throws Exception {
		// Cerrar el contexto de Spring Boot
		if (springContext != null) {
			springContext.close();
		}
		Platform.exit();
		System.out.println("Aplicación detenida correctamente");
	}
	
	private void mostrarVentanaError(Exception e) {
		try {
			// Crear una ventana de error básica
			javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
				javafx.scene.control.Alert.AlertType.ERROR
			);
			alert.setTitle("Error de Inicio");
			alert.setHeaderText("Error al cargar la aplicación");
			alert.setContentText("No se pudo cargar la pantalla de login.\n\nError: " + e.getMessage());
			alert.showAndWait();
			
			// Salir de la aplicación
			Platform.exit();
			
		} catch (Exception ex) {
			System.err.println("Error al mostrar ventana de error: " + ex.getMessage());
			Platform.exit();
		}
	}
}
