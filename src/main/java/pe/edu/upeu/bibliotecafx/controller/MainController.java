package pe.edu.upeu.bibliotecafx.controller;

import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import pe.edu.upeu.bibliotecafx.model.Libro;
import pe.edu.upeu.bibliotecafx.model.Prestamo;
import pe.edu.upeu.bibliotecafx.model.Usuario;
import pe.edu.upeu.bibliotecafx.service.AuthService;
import pe.edu.upeu.bibliotecafx.service.LibroService;
import pe.edu.upeu.bibliotecafx.service.PrestamoService;
import pe.edu.upeu.bibliotecafx.service.ReporteService;
import pe.edu.upeu.bibliotecafx.service.JasperReporteService;
import pe.edu.upeu.bibliotecafx.util.SessionManager;
import pe.edu.upeu.bibliotecafx.repository.PrestamoRepository;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class MainController {
    
    private final ApplicationContext applicationContext;
    private final AuthService authService;
    private final LibroService libroService;
    private final PrestamoService prestamoService;
    private final PrestamoRepository prestamoRepository;
    private final ReporteService reporteService;
    private final JasperReporteService jasperReporteService;
    
    // FXML Elements
    @FXML private Text userInfoText;
    @FXML private Text roleInfoText;
    @FXML private JFXButton logoutButton;
    
    // Menu Buttons
    @FXML private JFXButton dashboardButton;
    @FXML private JFXButton librosButton;
    @FXML private JFXButton usuariosButton;
    @FXML private JFXButton prestamosButton;
    @FXML private JFXButton reportesButton;
    
    // Content Elements
    @FXML private Text contentTitleText;
    @FXML private JFXButton refreshButton;
    @FXML private JFXTextField searchField;
    @FXML private JFXComboBox<String> filterComboBox;
    @FXML private JFXButton searchButton;
    @FXML private JFXButton clearButton;
    @FXML private TableView<Object> dataTableView;
    
    // Pagination Elements
    @FXML private JFXButton firstPageButton;
    @FXML private JFXButton prevPageButton;
    @FXML private Spinner<Integer> pageSpinner;
    @FXML private Label totalPagesLabel;
    @FXML private JFXButton nextPageButton;
    @FXML private JFXButton lastPageButton;
    @FXML private JFXComboBox<Integer> pageSizeComboBox;
    
    // Action Buttons
    @FXML private JFXButton addButton;
    @FXML private JFXButton editButton;
    @FXML private JFXButton deleteButton;
    @FXML private JFXButton exportButton;
    
    // Status Elements
    @FXML private Label statusLabel;
    @FXML private Label totalItemsLabel;
    
    // Stats Elements
    @FXML private Text totalLibrosText;
    @FXML private Text totalUsuariosText;
    @FXML private Text prestamosActivosText;
    
    // Data Management
    private ObservableList<Object> currentData = FXCollections.observableArrayList();
    private int currentPage = 1;
    private int pageSize = 10;
    private int totalPages = 1;
    private int totalItems = 0;
    private String currentView = "dashboard";
    
    @FXML
    public void initialize() {
        setupUserInfo();
        setupEventHandlers();
        setupPagination();
        setupTable();
        loadDashboard();
        updateStats();
    }
    
    private void setupUserInfo() {
        userInfoText.setText("Usuario: " + SessionManager.getNombreUsuario());
        roleInfoText.setText("Rol: " + SessionManager.getRolUsuario());
    }
    
    private void setupEventHandlers() {
        // Menu buttons
        dashboardButton.setOnAction(e -> loadDashboard());
        librosButton.setOnAction(e -> loadLibros());
        usuariosButton.setOnAction(e -> loadUsuarios());
        prestamosButton.setOnAction(e -> loadPrestamos());
        reportesButton.setOnAction(e -> loadReportes());
        
        // Logout
        logoutButton.setOnAction(e -> handleLogout());
        
        // Search and refresh
        refreshButton.setOnAction(e -> refreshCurrentView());
        searchButton.setOnAction(e -> performSearch());
        clearButton.setOnAction(e -> clearSearch());
        
        // Real-time search (search as you type)
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                // Perform search after a short delay to avoid too many searches
                Platform.runLater(() -> {
                    if (searchField.getText().equals(newValue)) {
                        performSearch();
                    }
                });
            } else if (newValue == null || newValue.trim().isEmpty()) {
                refreshCurrentView();
            }
        });
        
        // Pagination
        firstPageButton.setOnAction(e -> goToFirstPage());
        prevPageButton.setOnAction(e -> goToPreviousPage());
        nextPageButton.setOnAction(e -> goToNextPage());
        lastPageButton.setOnAction(e -> goToLastPage());
        
        // Action buttons
        addButton.setOnAction(e -> handleAdd());
        editButton.setOnAction(e -> handleEdit());
        deleteButton.setOnAction(e -> handleDelete());
        exportButton.setOnAction(e -> handleExport());
        
        // Page size change
        pageSizeComboBox.setOnAction(e -> handlePageSizeChange());
    }
    
    private void setupPagination() {
        // Setup page size options
        pageSizeComboBox.setItems(FXCollections.observableArrayList(5, 10, 20, 50, 100));
        pageSizeComboBox.setValue(10);
        
        // Setup page spinner
        SpinnerValueFactory<Integer> spinnerFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1, 1);
        pageSpinner.setValueFactory(spinnerFactory);
        
        updatePaginationControls();
    }
    
    private void setupTable() {
        // Setup table selection
        dataTableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        dataTableView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> updateActionButtons()
        );
    }
    
    private void loadDashboard() {
        currentView = "dashboard";
        contentTitleText.setText("Dashboard - Resumen del Sistema");
        
        // Setup dashboard table
        setupDashboardTable();
        
        // Load dashboard data (summary cards)
        loadDashboardData();
        updatePaginationControls();
    }
    
    private void loadLibros() {
        currentView = "libros";
        contentTitleText.setText("Gestión de Libros");
        
        // Setup filters for libros
        setupLibrosFilters();
        
        // Setup table columns for libros
        setupLibrosTable();
        
        // Load libros data
        loadLibrosData();
        updatePaginationControls();
    }
    
    private void loadUsuarios() {
        currentView = "usuarios";
        contentTitleText.setText("Gestión de Usuarios");
        
        // Setup filters for usuarios
        setupUsuariosFilters();
        
        // Setup table columns for usuarios
        setupUsuariosTable();
        
        // Load usuarios data
        loadUsuariosData();
        updatePaginationControls();
    }
    
    private void loadPrestamos() {
        currentView = "prestamos";
        contentTitleText.setText("Gestión de Préstamos");
        
        // Setup filters for prestamos
        setupPrestamosFilters();
        
        // Setup table columns for prestamos
        setupPrestamosTable();
        
        // Load prestamos data
        loadPrestamosData();
        updatePaginationControls();
    }
    
    private void loadReportes() {
        currentView = "reportes";
        contentTitleText.setText("Reportes y Estadísticas");
        
        // Setup reportes table
        setupReportesTable();
        
        // Load reportes data
        loadReportesData();
        updatePaginationControls();
    }
    
    private void setupLibrosTable() {
        dataTableView.getColumns().clear();
        
        TableColumn<Object, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        
        TableColumn<Object, String> tituloCol = new TableColumn<>("Título");
        tituloCol.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        tituloCol.setPrefWidth(200);
        
        TableColumn<Object, String> autorCol = new TableColumn<>("Autor");
        autorCol.setCellValueFactory(new PropertyValueFactory<>("autor"));
        autorCol.setPrefWidth(150);
        
        TableColumn<Object, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        isbnCol.setPrefWidth(100);
        
        TableColumn<Object, String> categoriaCol = new TableColumn<>("Categoría");
        categoriaCol.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        categoriaCol.setPrefWidth(100);
        
        TableColumn<Object, Integer> disponibleCol = new TableColumn<>("Disponible");
        disponibleCol.setCellValueFactory(new PropertyValueFactory<>("cantidadDisponible"));
        disponibleCol.setPrefWidth(80);
        
        TableColumn<Object, Integer> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("cantidadTotal"));
        totalCol.setPrefWidth(80);
        
        dataTableView.getColumns().addAll(idCol, tituloCol, autorCol, isbnCol, categoriaCol, disponibleCol, totalCol);
    }
    
    private void setupUsuariosTable() {
        dataTableView.getColumns().clear();
        
        TableColumn<Object, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        
        TableColumn<Object, String> nombreCol = new TableColumn<>("Nombre");
        nombreCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        nombreCol.setPrefWidth(120);
        
        TableColumn<Object, String> apellidoCol = new TableColumn<>("Apellido");
        apellidoCol.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        apellidoCol.setPrefWidth(120);
        
        TableColumn<Object, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);
        
        TableColumn<Object, String> rolCol = new TableColumn<>("Rol");
        rolCol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        rolCol.setPrefWidth(100);
        
        TableColumn<Object, String> telefonoCol = new TableColumn<>("Teléfono");
        telefonoCol.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        telefonoCol.setPrefWidth(100);
        
        TableColumn<Object, Boolean> activoCol = new TableColumn<>("Activo");
        activoCol.setCellValueFactory(new PropertyValueFactory<>("activo"));
        activoCol.setPrefWidth(60);
        
        dataTableView.getColumns().addAll(idCol, nombreCol, apellidoCol, emailCol, rolCol, telefonoCol, activoCol);
    }
    
    private void setupPrestamosTable() {
        dataTableView.getColumns().clear();
        
        TableColumn<Object, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        
        TableColumn<Object, String> libroCol = new TableColumn<>("Libro");
        libroCol.setCellValueFactory(cellData -> {
            Prestamo prestamo = (Prestamo) cellData.getValue();
            try {
                return new SimpleStringProperty(prestamo.getLibro() != null ? prestamo.getLibro().getTitulo() : "N/A");
            } catch (Exception e) {
                return new SimpleStringProperty("Error");
            }
        });
        libroCol.setPrefWidth(200);
        
        TableColumn<Object, String> usuarioCol = new TableColumn<>("Usuario");
        usuarioCol.setCellValueFactory(cellData -> {
            Prestamo prestamo = (Prestamo) cellData.getValue();
            try {
                return new SimpleStringProperty(prestamo.getUsuario() != null ? prestamo.getUsuario().getNombreCompleto() : "N/A");
            } catch (Exception e) {
                return new SimpleStringProperty("Error");
            }
        });
        usuarioCol.setPrefWidth(150);
        
        TableColumn<Object, String> fechaPrestamoCol = new TableColumn<>("Fecha Préstamo");
        fechaPrestamoCol.setCellValueFactory(cellData -> {
            Prestamo prestamo = (Prestamo) cellData.getValue();
            try {
                if (prestamo.getFechaPrestamo() != null) {
                    return new SimpleStringProperty(prestamo.getFechaPrestamo().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                }
                return new SimpleStringProperty("N/A");
            } catch (Exception e) {
                return new SimpleStringProperty("Error");
            }
        });
        fechaPrestamoCol.setPrefWidth(120);
        
        TableColumn<Object, String> fechaDevolucionCol = new TableColumn<>("Fecha Devolución");
        fechaDevolucionCol.setCellValueFactory(cellData -> {
            Prestamo prestamo = (Prestamo) cellData.getValue();
            try {
                if (prestamo.getFechaDevolucionEsperada() != null) {
                    return new SimpleStringProperty(prestamo.getFechaDevolucionEsperada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                }
                return new SimpleStringProperty("N/A");
            } catch (Exception e) {
                return new SimpleStringProperty("Error");
            }
        });
        fechaDevolucionCol.setPrefWidth(120);
        
        TableColumn<Object, String> estadoCol = new TableColumn<>("Estado");
        estadoCol.setCellValueFactory(cellData -> {
            Prestamo prestamo = (Prestamo) cellData.getValue();
            try {
                return new SimpleStringProperty(prestamo.getEstado() != null ? prestamo.getEstado().toString() : "N/A");
            } catch (Exception e) {
                return new SimpleStringProperty("Error");
            }
        });
        estadoCol.setPrefWidth(80);
        
        dataTableView.getColumns().addAll(idCol, libroCol, usuarioCol, fechaPrestamoCol, fechaDevolucionCol, estadoCol);
    }
    
    private void setupReportesTable() {
        dataTableView.getColumns().clear();
        
        TableColumn<Object, String> tipoReporteCol = new TableColumn<>("Tipo de Reporte");
        tipoReporteCol.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        tipoReporteCol.setPrefWidth(200);
        
        TableColumn<Object, String> descripcionCol = new TableColumn<>("Descripción");
        descripcionCol.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        descripcionCol.setPrefWidth(300);
        
        TableColumn<Object, String> formatoCol = new TableColumn<>("Formato");
        formatoCol.setCellValueFactory(new PropertyValueFactory<>("formato"));
        formatoCol.setPrefWidth(100);
        
        TableColumn<Object, String> accionCol = new TableColumn<>("Acción");
        accionCol.setCellValueFactory(new PropertyValueFactory<>("accion"));
        accionCol.setPrefWidth(100);
        
        dataTableView.getColumns().addAll(tipoReporteCol, descripcionCol, formatoCol, accionCol);
    }
    
    private void setupDashboardTable() {
        dataTableView.getColumns().clear();
        
        TableColumn<Object, String> categoriaCol = new TableColumn<>("Categoría");
        categoriaCol.setCellValueFactory(cellData -> {
            DashboardItem item = (DashboardItem) cellData.getValue();
            return new SimpleStringProperty(item.getTitle());
        });
        categoriaCol.setPrefWidth(300);
        
        TableColumn<Object, String> valorCol = new TableColumn<>("Valor");
        valorCol.setCellValueFactory(cellData -> {
            DashboardItem item = (DashboardItem) cellData.getValue();
            return new SimpleStringProperty(item.getValue());
        });
        valorCol.setPrefWidth(150);
        
        dataTableView.getColumns().addAll(categoriaCol, valorCol);
        
        // Configurar estilo para el dashboard
        dataTableView.setStyle("-fx-font-size: 12px;");
    }
    
    private void loadDashboardData() {
        try {
            // Load comprehensive dashboard data
            currentData.clear();
            
            // Estadísticas generales
            int totalLibros = libroService.obtenerTodosLosLibros().size();
            int totalUsuarios = authService.obtenerUsuariosActivos().size();
            long prestamosActivos = prestamoService.contarPrestamosActivos();
            long prestamosVencidos = prestamoService.contarPrestamosVencidos();
            
            // Libros disponibles
            List<Libro> librosDisponibles = libroService.obtenerLibrosDisponibles();
            int librosDisponiblesCount = librosDisponibles.size();
            
            // Libros con stock bajo
            List<Libro> librosStockBajo = libroService.obtenerLibrosConStockBajo();
            int librosStockBajoCount = librosStockBajo.size();
            
            // Usuarios por rol
            List<Usuario> administradores = authService.obtenerUsuariosSistema().stream()
                .filter(u -> u.getRol() == Usuario.RolUsuario.ADMINISTRADOR)
                .collect(Collectors.toList());
            List<Usuario> bibliotecarios = authService.obtenerBibliotecariosActivos();
            List<Usuario> lectores = authService.obtenerLectoresActivos();
            
            // Préstamos por estado
            List<Prestamo> prestamosDevueltos = prestamoService.obtenerTodosLosPrestamos().stream()
                .filter(p -> p.getEstado() == Prestamo.EstadoPrestamo.DEVUELTO)
                .collect(Collectors.toList());
            List<Prestamo> prestamosPerdidos = prestamoService.obtenerTodosLosPrestamos().stream()
                .filter(p -> p.getEstado() == Prestamo.EstadoPrestamo.PERDIDO)
                .collect(Collectors.toList());
            
            // Agregar estadísticas al dashboard
            currentData.add(new DashboardItem("📚 LIBROS", ""));
            currentData.add(new DashboardItem("  • Total de Libros", String.valueOf(totalLibros)));
            currentData.add(new DashboardItem("  • Libros Disponibles", String.valueOf(librosDisponiblesCount)));
            currentData.add(new DashboardItem("  • Libros con Stock Bajo", String.valueOf(librosStockBajoCount)));
            currentData.add(new DashboardItem("  • Porcentaje Disponible", String.format("%.1f%%", (double) librosDisponiblesCount / totalLibros * 100)));
            
            currentData.add(new DashboardItem("👥 USUARIOS", ""));
            currentData.add(new DashboardItem("  • Total de Usuarios", String.valueOf(totalUsuarios)));
            currentData.add(new DashboardItem("  • Administradores", String.valueOf(administradores.size())));
            currentData.add(new DashboardItem("  • Bibliotecarios", String.valueOf(bibliotecarios.size())));
            currentData.add(new DashboardItem("  • Lectores", String.valueOf(lectores.size())));
            
            currentData.add(new DashboardItem("📖 PRÉSTAMOS", ""));
            currentData.add(new DashboardItem("  • Préstamos Activos", String.valueOf(prestamosActivos)));
            currentData.add(new DashboardItem("  • Préstamos Devueltos", String.valueOf(prestamosDevueltos.size())));
            currentData.add(new DashboardItem("  • Préstamos Vencidos", String.valueOf(prestamosVencidos)));
            currentData.add(new DashboardItem("  • Préstamos Perdidos", String.valueOf(prestamosPerdidos.size())));
            
            // Alertas y notificaciones
            currentData.add(new DashboardItem("⚠️ ALERTAS", ""));
            if (librosStockBajoCount > 0) {
                currentData.add(new DashboardItem("  • Libros con Stock Bajo", String.valueOf(librosStockBajoCount) + " libros"));
            }
            if (prestamosVencidos > 0) {
                currentData.add(new DashboardItem("  • Préstamos Vencidos", String.valueOf(prestamosVencidos) + " préstamos"));
            }
            if (prestamosPerdidos.size() > 0) {
                currentData.add(new DashboardItem("  • Préstamos Perdidos", String.valueOf(prestamosPerdidos.size()) + " préstamos"));
            }
            
            // Información del sistema
            currentData.add(new DashboardItem("ℹ️ SISTEMA", ""));
            currentData.add(new DashboardItem("  • Usuario Actual", SessionManager.getNombreUsuario()));
            currentData.add(new DashboardItem("  • Rol", SessionManager.getRolUsuario()));
            currentData.add(new DashboardItem("  • Fecha", java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
            
            // Préstamos recientes (últimos 5)
            List<Prestamo> prestamosRecientes = prestamoService.obtenerTodosLosPrestamos().stream()
                .sorted((p1, p2) -> p2.getFechaPrestamo().compareTo(p1.getFechaPrestamo()))
                .limit(5)
                .collect(Collectors.toList());
            
            if (!prestamosRecientes.isEmpty()) {
                currentData.add(new DashboardItem("🕒 PRÉSTAMOS RECIENTES", ""));
                for (Prestamo prestamo : prestamosRecientes) {
                    String info = prestamo.getLibro().getTitulo() + " → " + 
                                 prestamo.getUsuario().getNombreCompleto() + 
                                 " (" + prestamo.getEstado() + ")";
                    currentData.add(new DashboardItem("  • " + prestamo.getFechaPrestamo().format(DateTimeFormatter.ofPattern("dd/MM")), info));
                }
            }
            
            // Libros más populares (por número de préstamos)
            Map<Long, Long> prestamosPorLibro = prestamoService.obtenerTodosLosPrestamos().stream()
                .collect(Collectors.groupingBy(p -> p.getLibro().getId(), Collectors.counting()));
            
            List<Map.Entry<Long, Long>> librosPopulares = prestamosPorLibro.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());
            
            if (!librosPopulares.isEmpty()) {
                currentData.add(new DashboardItem("📈 LIBROS MÁS POPULARES", ""));
                for (Map.Entry<Long, Long> entry : librosPopulares) {
                    Libro libro = libroService.buscarPorId(entry.getKey()).orElse(null);
                    if (libro != null) {
                        currentData.add(new DashboardItem("  • " + libro.getTitulo(), entry.getValue() + " préstamos"));
                    }
                }
            }
            
            dataTableView.setItems(currentData);
            totalItems = currentData.size();
            updateStatus("Dashboard cargado con " + totalItems + " elementos de información");
            
        } catch (Exception e) {
            showError("Error al cargar dashboard: " + e.getMessage());
        }
    }
    
    private void loadLibrosData() {
        try {
            List<Libro> libros = libroService.obtenerTodosLosLibros();
            currentData.clear();
            currentData.addAll(libros);
            
            // Apply pagination
            applyPagination();
            
            totalItems = libros.size();
            updateStatus("Libros cargados: " + totalItems + " elementos");
            
        } catch (Exception e) {
            showError("Error al cargar libros: " + e.getMessage());
        }
    }
    
    private void loadUsuariosData() {
        try {
            List<Usuario> usuarios = authService.obtenerUsuariosActivos();
            currentData.clear();
            currentData.addAll(usuarios);
            
            // Apply pagination
            applyPagination();
            
            totalItems = usuarios.size();
            updateStatus("Usuarios cargados: " + totalItems + " elementos");
            
        } catch (Exception e) {
            showError("Error al cargar usuarios: " + e.getMessage());
        }
    }
    
    private void loadPrestamosData() {
        try {
            // Agregar logging para debug
            System.out.println("Cargando préstamos...");
            
            List<Prestamo> prestamos = prestamoService.obtenerTodosLosPrestamos();
            
            System.out.println("Préstamos encontrados: " + prestamos.size());
            
            currentData.clear();
            currentData.addAll(prestamos);
            
            // Apply pagination
            applyPagination();
            
            totalItems = prestamos.size();
            updateStatus("Préstamos cargados: " + totalItems + " elementos");
            
        } catch (Exception e) {
            e.printStackTrace(); // Para debug
            showError("Error al cargar préstamos: " + e.getMessage());
        }
    }
    
    private void loadReportesData() {
        currentData.clear();
        
        // Agregar reportes disponibles con formatos soportados
        // Reporte de Libros
        currentData.add(new ReporteItem("📚 Reporte de Libros", "Lista completa de libros en el sistema", "PDF", "PDF"));
        currentData.add(new ReporteItem("📚 Reporte de Libros", "Lista completa de libros en el sistema", "CSV", "CSV"));
        
        // Reporte de Usuarios
        currentData.add(new ReporteItem("👥 Reporte de Usuarios", "Lista completa de usuarios registrados", "PDF", "PDF"));
        currentData.add(new ReporteItem("👥 Reporte de Usuarios", "Lista completa de usuarios registrados", "CSV", "CSV"));
        
        // Reporte de Préstamos
        currentData.add(new ReporteItem("📖 Reporte de Préstamos", "Historial completo de préstamos", "PDF", "PDF"));
        currentData.add(new ReporteItem("📖 Reporte de Préstamos", "Historial completo de préstamos", "CSV", "CSV"));
        
        // Reporte Estadístico
        currentData.add(new ReporteItem("📊 Reporte Estadístico", "Estadísticas generales del sistema", "PDF", "PDF"));
        currentData.add(new ReporteItem("📊 Reporte Estadístico", "Estadísticas generales del sistema", "CSV", "CSV"));
        
        totalItems = currentData.size();
        applyPagination();
        updateStatus("Reportes disponibles cargados (PDF y CSV)");
    }
    
    private void applyPagination() {
        totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (totalPages == 0) totalPages = 1;
        
        if (currentPage > totalPages) {
            currentPage = totalPages;
        }
        
        int startIndex = (currentPage - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);
        
        ObservableList<Object> pageData = FXCollections.observableArrayList();
        for (int i = startIndex; i < endIndex; i++) {
            if (i < currentData.size()) {
                pageData.add(currentData.get(i));
            }
        }
        
        dataTableView.setItems(pageData);
        updatePaginationControls();
    }
    
    private void updatePaginationControls() {
        totalPagesLabel.setText(String.valueOf(totalPages));
        
        SpinnerValueFactory<Integer> spinnerFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, totalPages, currentPage);
        pageSpinner.setValueFactory(spinnerFactory);
        
        firstPageButton.setDisable(currentPage == 1);
        prevPageButton.setDisable(currentPage == 1);
        nextPageButton.setDisable(currentPage == totalPages);
        lastPageButton.setDisable(currentPage == totalPages);
        
        totalItemsLabel.setText("Total: " + totalItems + " elementos");
    }
    
    private void goToFirstPage() {
        currentPage = 1;
        applyPagination();
    }
    
    private void goToPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            applyPagination();
        }
    }
    
    private void goToNextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            applyPagination();
        }
    }
    
    private void goToLastPage() {
        currentPage = totalPages;
        applyPagination();
    }
    
    private void handlePageSizeChange() {
        pageSize = pageSizeComboBox.getValue();
        currentPage = 1;
        applyPagination();
    }
    
    private void refreshCurrentView() {
        switch (currentView) {
            case "dashboard":
                loadDashboard();
                break;
            case "libros":
                loadLibros();
                break;
            case "usuarios":
                loadUsuarios();
                break;
            case "prestamos":
                loadPrestamos();
                break;
            case "reportes":
                loadReportes();
                break;
        }
        updateStats();
    }
    
    private void performSearch() {
        String searchTerm = searchField.getText().trim();
        String filterType = filterComboBox.getValue();
        
        if (searchTerm.isEmpty()) {
            refreshCurrentView();
            return;
        }
        
        try {
            switch (currentView) {
                case "libros":
                    performLibrosSearch(searchTerm, filterType);
                    break;
                case "usuarios":
                    performUsuariosSearch(searchTerm, filterType);
                    break;
                case "prestamos":
                    performPrestamosSearch(searchTerm, filterType);
                    break;
                default:
                    updateStatus("Búsqueda no disponible para esta vista");
                    break;
            }
        } catch (Exception e) {
            showError("Error en la búsqueda: " + e.getMessage());
        }
    }
    
    private void performLibrosSearch(String searchTerm, String filterType) {
        List<Libro> resultados = new ArrayList<>();
        
        try {
            switch (filterType) {
                case "Título":
                    resultados = libroService.buscarPorTitulo(searchTerm);
                    break;
                case "Autor":
                    resultados = libroService.buscarPorAutor(searchTerm);
                    break;
                case "ISBN":
                    Optional<Libro> libroOpt = libroService.buscarPorIsbn(searchTerm);
                    if (libroOpt.isPresent()) {
                        resultados.add(libroOpt.get());
                    }
                    break;
                case "Categoría":
                    resultados = libroService.buscarPorCategoria(searchTerm);
                    break;
                case "Disponible":
                    if (searchTerm.equalsIgnoreCase("si") || searchTerm.equalsIgnoreCase("disponible")) {
                        resultados = libroService.obtenerLibrosDisponibles();
                    }
                    break;
                default:
                    // Búsqueda general mejorada - usar el método del servicio
                    resultados = libroService.buscarGeneral(searchTerm);
                    break;
            }
            
            currentData.clear();
            currentData.addAll(resultados);
            applyPagination();
            
            totalItems = resultados.size();
            updateStatus("Búsqueda de libros: " + totalItems + " resultados para '" + searchTerm + "'");
            
        } catch (Exception e) {
            showError("Error en búsqueda de libros: " + e.getMessage());
        }
    }
    
    private void performUsuariosSearch(String searchTerm, String filterType) {
        List<Usuario> resultados = new ArrayList<>();
        
        try {
            List<Usuario> todosUsuarios = authService.obtenerUsuariosActivos();
            
            switch (filterType) {
                case "Nombre":
                    resultados = todosUsuarios.stream()
                        .filter(u -> u.getNombre().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                   u.getApellido().toLowerCase().contains(searchTerm.toLowerCase()))
                        .collect(Collectors.toList());
                    break;
                case "Email":
                    resultados = todosUsuarios.stream()
                        .filter(u -> u.getEmail().toLowerCase().contains(searchTerm.toLowerCase()))
                        .collect(Collectors.toList());
                    break;
                case "Rol":
                    resultados = todosUsuarios.stream()
                        .filter(u -> u.getRol().toString().toLowerCase().contains(searchTerm.toLowerCase()))
                        .collect(Collectors.toList());
                    break;
                default:
                    // Búsqueda general
                    resultados = todosUsuarios.stream()
                        .filter(u -> u.getNombre().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                   u.getApellido().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                   u.getEmail().toLowerCase().contains(searchTerm.toLowerCase()) ||
                                   u.getRol().toString().toLowerCase().contains(searchTerm.toLowerCase()))
                        .collect(Collectors.toList());
                    break;
            }
            
            currentData.clear();
            currentData.addAll(resultados);
            applyPagination();
            
            totalItems = resultados.size();
            updateStatus("Búsqueda de usuarios: " + totalItems + " resultados para '" + searchTerm + "'");
            
        } catch (Exception e) {
            showError("Error en búsqueda de usuarios: " + e.getMessage());
        }
    }
    
    private void performPrestamosSearch(String searchTerm, String filterType) {
        List<Prestamo> resultados = new ArrayList<>();
        
        try {
            List<Prestamo> todosPrestamos = prestamoService.obtenerTodosLosPrestamos();
            
            switch (filterType) {
                case "Libro":
                    resultados = todosPrestamos.stream()
                        .filter(p -> p.getLibro() != null && 
                                   p.getLibro().getTitulo().toLowerCase().contains(searchTerm.toLowerCase()))
                        .collect(Collectors.toList());
                    break;
                case "Usuario":
                    resultados = todosPrestamos.stream()
                        .filter(p -> p.getUsuario() != null && 
                                   p.getUsuario().getNombreCompleto().toLowerCase().contains(searchTerm.toLowerCase()))
                        .collect(Collectors.toList());
                    break;
                case "Estado":
                    resultados = todosPrestamos.stream()
                        .filter(p -> p.getEstado() != null && 
                                   p.getEstado().toString().toLowerCase().contains(searchTerm.toLowerCase()))
                        .collect(Collectors.toList());
                    break;
                case "Activos":
                    if (searchTerm.equalsIgnoreCase("activo") || searchTerm.equalsIgnoreCase("activos")) {
                        resultados = prestamoService.obtenerPrestamosActivos();
                    }
                    break;
                default:
                    // Búsqueda general
                    resultados = todosPrestamos.stream()
                        .filter(p -> (p.getLibro() != null && p.getLibro().getTitulo().toLowerCase().contains(searchTerm.toLowerCase())) ||
                                   (p.getUsuario() != null && p.getUsuario().getNombreCompleto().toLowerCase().contains(searchTerm.toLowerCase())) ||
                                   (p.getEstado() != null && p.getEstado().toString().toLowerCase().contains(searchTerm.toLowerCase())))
                        .collect(Collectors.toList());
                    break;
            }
            
            currentData.clear();
            currentData.addAll(resultados);
            applyPagination();
            
            totalItems = resultados.size();
            updateStatus("Búsqueda de préstamos: " + totalItems + " resultados para '" + searchTerm + "'");
            
        } catch (Exception e) {
            showError("Error en búsqueda de préstamos: " + e.getMessage());
        }
    }
    
    private void clearSearch() {
        searchField.clear();
        filterComboBox.setValue(null);
        refreshCurrentView();
    }
    
    private void updateActionButtons() {
        boolean hasSelection = dataTableView.getSelectionModel().getSelectedItem() != null;
        editButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
    }
    
    private void handleAdd() {
        switch (currentView) {
            case "libros":
                mostrarFormularioLibro(null);
                break;
            case "usuarios":
                mostrarFormularioUsuario(null);
                break;
            case "prestamos":
                mostrarFormularioPrestamo(null);
                break;
            default:
                showInfo("Función de agregar no disponible para esta vista");
                break;
        }
    }
    
    private void handleEdit() {
        Object selected = dataTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            switch (currentView) {
                case "libros":
                    mostrarFormularioLibro((Libro) selected);
                    break;
                case "usuarios":
                    mostrarFormularioUsuario((Usuario) selected);
                    break;
                case "prestamos":
                    mostrarFormularioPrestamo((Prestamo) selected);
                    break;
                default:
                    showInfo("Función de editar no disponible para esta vista");
                    break;
            }
        } else {
            showError("Por favor selecciona un elemento para editar");
        }
    }
    
    private void handleDelete() {
        Object selected = dataTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            switch (currentView) {
                case "libros":
                    confirmarEliminarLibro((Libro) selected);
                    break;
                case "usuarios":
                    confirmarEliminarUsuario((Usuario) selected);
                    break;
                case "prestamos":
                    confirmarEliminarPrestamo((Prestamo) selected);
                    break;
                default:
                    showInfo("Función de eliminar no disponible para esta vista");
                    break;
            }
        } else {
            showError("Por favor selecciona un elemento para eliminar");
        }
    }
    
    private void handleExport() {
        if ("reportes".equals(currentView)) {
            Object selected = dataTableView.getSelectionModel().getSelectedItem();
            if (selected != null && selected instanceof ReporteItem) {
                ReporteItem reporte = (ReporteItem) selected;
                generarReporte(reporte);
            } else {
                showError("Por favor selecciona un reporte para generar");
            }
        } else {
            showInfo("Función de exportar disponible solo en la vista de reportes");
        }
    }
    
    private void generarReporte(ReporteItem reporte) {
        try {
            String rutaArchivo = "";
            String mensaje = "";
            
            // Determinar el formato basado en la acción
            String formato = reporte.getAccion().toLowerCase();
            
            switch (reporte.getTipo()) {
                case "📚 Reporte de Libros":
                    switch (formato) {
                        case "pdf":
                            rutaArchivo = jasperReporteService.generarReporteLibrosPDF();
                            break;
                        default:
                            rutaArchivo = reporteService.generarReporteLibrosArchivo();
                            break;
                    }
                    mensaje = "Reporte de libros generado exitosamente en formato " + formato.toUpperCase();
                    break;
                    
                case "👥 Reporte de Usuarios":
                    switch (formato) {
                        case "pdf":
                            rutaArchivo = jasperReporteService.generarReporteUsuariosPDF();
                            break;
                        default:
                            rutaArchivo = reporteService.generarReporteUsuariosArchivo();
                            break;
                    }
                    mensaje = "Reporte de usuarios generado exitosamente en formato " + formato.toUpperCase();
                    break;
                    
                case "📖 Reporte de Préstamos":
                    switch (formato) {
                        case "pdf":
                            rutaArchivo = jasperReporteService.generarReportePrestamosPDF();
                            break;
                        default:
                            rutaArchivo = reporteService.generarReportePrestamosArchivo();
                            break;
                    }
                    mensaje = "Reporte de préstamos generado exitosamente en formato " + formato.toUpperCase();
                    break;
                    
                case "📊 Reporte Estadístico":
                    switch (formato) {
                        case "pdf":
                            rutaArchivo = jasperReporteService.generarReporteEstadisticoPDF();
                            break;
                        default:
                            rutaArchivo = reporteService.generarReporteEstadisticoArchivo();
                            break;
                    }
                    mensaje = "Reporte estadístico generado exitosamente en formato " + formato.toUpperCase();
                    break;
                    
                default:
                    showError("Tipo de reporte no reconocido");
                    return;
            }
            
            showInfo(mensaje + "\nArchivo guardado en: " + rutaArchivo);
            updateStatus("Reporte generado: " + reporte.getTipo() + " (" + formato.toUpperCase() + ")");
            
        } catch (Exception e) {
            showError("Error generando reporte: " + e.getMessage());
            log.error("Error generando reporte", e);
        }
    }
    
    private void handleLogout() {
        SessionManager.cerrarSesion();
        
        // Close current window and return to login
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.close();
        
        // Show login window
        showLoginWindow();
    }
    
    private void showLoginWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            loader.setControllerFactory(applicationContext::getBean);
            
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Sistema de Biblioteca - Login");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.setWidth(400);
            stage.setHeight(500);
            stage.centerOnScreen();
            stage.show();
            
        } catch (IOException e) {
            showError("Error al mostrar ventana de login: " + e.getMessage());
        }
    }
    
    private void updateStats() {
        try {
            int totalLibros = libroService.obtenerTodosLosLibros().size();
            int totalUsuarios = authService.obtenerUsuariosActivos().size();
            long prestamosActivos = prestamoService.contarPrestamosActivos();
            
            totalLibrosText.setText("Libros: " + totalLibros);
            totalUsuariosText.setText("Usuarios: " + totalUsuarios);
            prestamosActivosText.setText("Préstamos Activos: " + prestamosActivos);
            
        } catch (Exception e) {
            showError("Error al actualizar estadísticas: " + e.getMessage());
        }
    }
    
    private void updateStatus(String message) {
        statusLabel.setText(message);
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Helper class for dashboard items
    public static class DashboardItem {
        private final String title;
        private final String value;
        
        public DashboardItem(String title, String value) {
            this.title = title;
            this.value = value;
        }
        
        public String getTitle() { return title; }
        public String getValue() { return value; }
    }
    
    public static class ReporteItem {
        private final String tipo;
        private final String descripcion;
        private final String formato;
        private final String accion;
        
        public ReporteItem(String tipo, String descripcion, String formato, String accion) {
            this.tipo = tipo;
            this.descripcion = descripcion;
            this.formato = formato;
            this.accion = accion;
        }
        
        public String getTipo() { return tipo; }
        public String getDescripcion() { return descripcion; }
        public String getFormato() { return formato; }
        public String getAccion() { return accion; }
    }
    
    private void setupLibrosFilters() {
        filterComboBox.setItems(FXCollections.observableArrayList(
            "General", "Título", "Autor", "ISBN", "Categoría", "Disponible"
        ));
        filterComboBox.setValue("General");
    }
    
    private void setupUsuariosFilters() {
        filterComboBox.setItems(FXCollections.observableArrayList(
            "General", "Nombre", "Email", "Rol"
        ));
        filterComboBox.setValue("General");
    }
    
    private void setupPrestamosFilters() {
        filterComboBox.setItems(FXCollections.observableArrayList(
            "General", "Libro", "Usuario", "Estado", "Activos"
        ));
        filterComboBox.setValue("General");
    }
    
    // ========== MÉTODOS PARA FORMULARIOS ==========
    
    private void mostrarFormularioLibro(Libro libro) {
        try {
            // Crear formulario simple con diálogo
            Dialog<Libro> dialog = new Dialog<>();
            dialog.setTitle(libro == null ? "Agregar Libro" : "Editar Libro");
            dialog.setHeaderText(null);
            
            // Configurar botones
            ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            // Crear formulario
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            TextField tituloField = new TextField();
            tituloField.setPromptText("Título");
            TextField autorField = new TextField();
            autorField.setPromptText("Autor");
            TextField isbnField = new TextField();
            isbnField.setPromptText("ISBN");
            TextField categoriaField = new TextField();
            categoriaField.setPromptText("Categoría");
            TextField editorialField = new TextField();
            editorialField.setPromptText("Editorial");
            TextField anioField = new TextField();
            anioField.setPromptText("Año de publicación");
            TextField cantidadField = new TextField();
            cantidadField.setPromptText("Cantidad total");
            TextArea descripcionArea = new TextArea();
            descripcionArea.setPromptText("Descripción");
            descripcionArea.setPrefRowCount(3);
            
            // Llenar campos si es edición
            if (libro != null) {
                tituloField.setText(libro.getTitulo());
                autorField.setText(libro.getAutor());
                isbnField.setText(libro.getIsbn());
                categoriaField.setText(libro.getCategoria());
                editorialField.setText(libro.getEditorial());
                if (libro.getAnioPublicacion() != null) {
                    anioField.setText(String.valueOf(libro.getAnioPublicacion()));
                }
                if (libro.getCantidadTotal() != null) {
                    cantidadField.setText(String.valueOf(libro.getCantidadTotal()));
                }
                descripcionArea.setText(libro.getDescripcion());
            }
            
            grid.add(new Label("Título:"), 0, 0);
            grid.add(tituloField, 1, 0);
            grid.add(new Label("Autor:"), 0, 1);
            grid.add(autorField, 1, 1);
            grid.add(new Label("ISBN:"), 0, 2);
            grid.add(isbnField, 1, 2);
            grid.add(new Label("Categoría:"), 0, 3);
            grid.add(categoriaField, 1, 3);
            grid.add(new Label("Editorial:"), 0, 4);
            grid.add(editorialField, 1, 4);
            grid.add(new Label("Año:"), 0, 5);
            grid.add(anioField, 1, 5);
            grid.add(new Label("Cantidad:"), 0, 6);
            grid.add(cantidadField, 1, 6);
            grid.add(new Label("Descripción:"), 0, 7);
            grid.add(descripcionArea, 1, 7);
            
            dialog.getDialogPane().setContent(grid);
            
            // Configurar resultado
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        Libro nuevoLibro = libro != null ? libro : new Libro();
                        nuevoLibro.setTitulo(tituloField.getText());
                        nuevoLibro.setAutor(autorField.getText());
                        nuevoLibro.setIsbn(isbnField.getText());
                        nuevoLibro.setCategoria(categoriaField.getText());
                        nuevoLibro.setEditorial(editorialField.getText());
                        
                        if (!anioField.getText().isEmpty()) {
                            nuevoLibro.setAnioPublicacion(Integer.parseInt(anioField.getText()));
                        }
                        
                        if (!cantidadField.getText().isEmpty()) {
                            int cantidad = Integer.parseInt(cantidadField.getText());
                            nuevoLibro.setCantidadTotal(cantidad);
                            nuevoLibro.setCantidadDisponible(cantidad);
                        }
                        
                        nuevoLibro.setDescripcion(descripcionArea.getText());
                        
                        return nuevoLibro;
                    } catch (NumberFormatException e) {
                        showError("Por favor ingresa números válidos para año y cantidad");
                        return null;
                    }
                }
                return null;
            });
            
            Optional<Libro> result = dialog.showAndWait();
            result.ifPresent(libroGuardado -> {
                try {
                    if (libro == null) {
                        libroService.guardarLibro(libroGuardado);
                        showInfo("Libro agregado exitosamente");
                    } else {
                        libroService.actualizarLibro(libro.getId(), libroGuardado);
                        showInfo("Libro actualizado exitosamente");
                    }
                    refreshCurrentView();
                } catch (Exception e) {
                    showError("Error al guardar libro: " + e.getMessage());
                }
            });
            
        } catch (Exception e) {
            showError("Error al mostrar formulario: " + e.getMessage());
        }
    }
    
    private void mostrarFormularioUsuario(Usuario usuario) {
        try {
            Dialog<Usuario> dialog = new Dialog<>();
            dialog.setTitle(usuario == null ? "Agregar Usuario" : "Editar Usuario");
            dialog.setHeaderText(null);
            
            ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            TextField nombreField = new TextField();
            nombreField.setPromptText("Nombre");
            TextField apellidoField = new TextField();
            apellidoField.setPromptText("Apellido");
            TextField emailField = new TextField();
            emailField.setPromptText("Email");
            TextField passwordField = new TextField();
            passwordField.setPromptText("Contraseña");
            ComboBox<Usuario.RolUsuario> rolComboBox = new ComboBox<>();
            rolComboBox.getItems().addAll(Usuario.RolUsuario.values());
            TextField telefonoField = new TextField();
            telefonoField.setPromptText("Teléfono");
            TextArea direccionArea = new TextArea();
            direccionArea.setPromptText("Dirección");
            direccionArea.setPrefRowCount(2);
            
            if (usuario != null) {
                nombreField.setText(usuario.getNombre());
                apellidoField.setText(usuario.getApellido());
                emailField.setText(usuario.getEmail());
                passwordField.setText(usuario.getPassword());
                rolComboBox.setValue(usuario.getRol());
                telefonoField.setText(usuario.getTelefono());
                direccionArea.setText(usuario.getDireccion());
            } else {
                rolComboBox.setValue(Usuario.RolUsuario.LECTOR);
            }
            
            grid.add(new Label("Nombre:"), 0, 0);
            grid.add(nombreField, 1, 0);
            grid.add(new Label("Apellido:"), 0, 1);
            grid.add(apellidoField, 1, 1);
            grid.add(new Label("Email:"), 0, 2);
            grid.add(emailField, 1, 2);
            grid.add(new Label("Contraseña:"), 0, 3);
            grid.add(passwordField, 1, 3);
            grid.add(new Label("Rol:"), 0, 4);
            grid.add(rolComboBox, 1, 4);
            grid.add(new Label("Teléfono:"), 0, 5);
            grid.add(telefonoField, 1, 5);
            grid.add(new Label("Dirección:"), 0, 6);
            grid.add(direccionArea, 1, 6);
            
            dialog.getDialogPane().setContent(grid);
            
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    Usuario nuevoUsuario = usuario != null ? usuario : new Usuario();
                    nuevoUsuario.setNombre(nombreField.getText());
                    nuevoUsuario.setApellido(apellidoField.getText());
                    nuevoUsuario.setEmail(emailField.getText());
                    nuevoUsuario.setPassword(passwordField.getText());
                    nuevoUsuario.setRol(rolComboBox.getValue());
                    nuevoUsuario.setTelefono(telefonoField.getText());
                    nuevoUsuario.setDireccion(direccionArea.getText());
                    return nuevoUsuario;
                }
                return null;
            });
            
            Optional<Usuario> result = dialog.showAndWait();
            result.ifPresent(usuarioGuardado -> {
                try {
                    if (usuario == null) {
                        // Usar el método correcto según el rol
                        if (usuarioGuardado.getRol() == Usuario.RolUsuario.ADMINISTRADOR) {
                            authService.crearAdministrador(usuarioGuardado);
                        } else if (usuarioGuardado.getRol() == Usuario.RolUsuario.BIBLIOTECARIO) {
                            authService.registrarBibliotecario(usuarioGuardado);
                        } else {
                            authService.registrarLector(usuarioGuardado);
                        }
                        showInfo("Usuario agregado exitosamente");
                    } else {
                        authService.actualizarUsuario(usuario.getId(), usuarioGuardado);
                        showInfo("Usuario actualizado exitosamente");
                    }
                    refreshCurrentView();
                } catch (Exception e) {
                    showError("Error al guardar usuario: " + e.getMessage());
                }
            });
            
        } catch (Exception e) {
            showError("Error al mostrar formulario: " + e.getMessage());
        }
    }
    
    private void mostrarFormularioPrestamo(Prestamo prestamo) {
        try {
            Dialog<Prestamo> dialog = new Dialog<>();
            dialog.setTitle(prestamo == null ? "Agregar Préstamo" : "Editar Préstamo");
            dialog.setHeaderText(null);
            
            ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            
            ComboBox<Libro> libroComboBox = new ComboBox<>();
            libroComboBox.getItems().addAll(libroService.obtenerTodosLosLibros());
            libroComboBox.setCellFactory(param -> new ListCell<Libro>() {
                @Override
                protected void updateItem(Libro item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getTitulo() + " - " + item.getAutor());
                    }
                }
            });
            libroComboBox.setButtonCell(libroComboBox.getCellFactory().call(null));
            
            ComboBox<Usuario> usuarioComboBox = new ComboBox<>();
            usuarioComboBox.getItems().addAll(authService.obtenerUsuariosActivos());
            usuarioComboBox.setCellFactory(param -> new ListCell<Usuario>() {
                @Override
                protected void updateItem(Usuario item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getNombreCompleto() + " (" + item.getRol() + ")");
                    }
                }
            });
            usuarioComboBox.setButtonCell(usuarioComboBox.getCellFactory().call(null));
            
            ComboBox<Prestamo.EstadoPrestamo> estadoComboBox = new ComboBox<>();
            estadoComboBox.getItems().addAll(Prestamo.EstadoPrestamo.values());
            
            TextField diasField = new TextField();
            diasField.setPromptText("Días de préstamo");
            TextArea observacionesArea = new TextArea();
            observacionesArea.setPromptText("Observaciones");
            observacionesArea.setPrefRowCount(2);
            
            if (prestamo != null) {
                libroComboBox.setValue(prestamo.getLibro());
                usuarioComboBox.setValue(prestamo.getUsuario());
                estadoComboBox.setValue(prestamo.getEstado());
                observacionesArea.setText(prestamo.getObservaciones());
            } else {
                estadoComboBox.setValue(Prestamo.EstadoPrestamo.ACTIVO);
                diasField.setText("30");
            }
            
            grid.add(new Label("Libro:"), 0, 0);
            grid.add(libroComboBox, 1, 0);
            grid.add(new Label("Usuario:"), 0, 1);
            grid.add(usuarioComboBox, 1, 1);
            grid.add(new Label("Estado:"), 0, 2);
            grid.add(estadoComboBox, 1, 2);
            grid.add(new Label("Días:"), 0, 3);
            grid.add(diasField, 1, 3);
            grid.add(new Label("Observaciones:"), 0, 4);
            grid.add(observacionesArea, 1, 4);
            
            dialog.getDialogPane().setContent(grid);
            
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    try {
                        if (prestamo == null) {
                            // Crear nuevo préstamo
                            int dias = Integer.parseInt(diasField.getText());
                            return prestamoService.crearPrestamo(
                                libroComboBox.getValue().getId(),
                                usuarioComboBox.getValue().getId(),
                                dias
                            );
                        } else {
                            // Actualizar préstamo existente
                            prestamo.setLibro(libroComboBox.getValue());
                            prestamo.setUsuario(usuarioComboBox.getValue());
                            prestamo.setEstado(estadoComboBox.getValue());
                            prestamo.setObservaciones(observacionesArea.getText());
                            return prestamo;
                        }
                    } catch (NumberFormatException e) {
                        showError("Por favor ingresa un número válido para los días");
                        return null;
                    }
                }
                return null;
            });
            
            Optional<Prestamo> result = dialog.showAndWait();
            result.ifPresent(prestamoGuardado -> {
                try {
                    if (prestamo == null) {
                        showInfo("Préstamo creado exitosamente");
                    } else {
                        // Guardar el préstamo actualizado directamente
                        prestamoRepository.save(prestamoGuardado);
                        showInfo("Préstamo actualizado exitosamente");
                    }
                    refreshCurrentView();
                } catch (Exception e) {
                    showError("Error al guardar préstamo: " + e.getMessage());
                }
            });
            
        } catch (Exception e) {
            showError("Error al mostrar formulario: " + e.getMessage());
        }
    }
    
    // ========== MÉTODOS PARA CONFIRMACIÓN DE ELIMINACIÓN ==========
    
    private void confirmarEliminarLibro(Libro libro) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Eliminar libro?");
        alert.setContentText("¿Estás seguro de que quieres eliminar el libro '" + libro.getTitulo() + "'?\n\nEsta acción no se puede deshacer.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Usar el método forzado para asegurar la eliminación
                libroService.eliminarLibroForzado(libro.getId());
                showInfo("Libro eliminado exitosamente");
                refreshCurrentView();
            } catch (Exception e) {
                showError("Error al eliminar libro: " + e.getMessage());
            }
        }
    }
    
    private void confirmarEliminarUsuario(Usuario usuario) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Eliminar usuario?");
        alert.setContentText("¿Estás seguro de que quieres eliminar al usuario '" + usuario.getNombreCompleto() + "'?\n\nEsta acción no se puede deshacer.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Usar el método forzado para asegurar la desactivación
                authService.desactivarUsuarioForzado(usuario.getId());
                showInfo("Usuario desactivado exitosamente");
                refreshCurrentView();
            } catch (Exception e) {
                showError("Error al desactivar usuario: " + e.getMessage());
            }
        }
    }
    
    private void confirmarEliminarPrestamo(Prestamo prestamo) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Eliminar préstamo?");
        alert.setContentText("¿Estás seguro de que quieres eliminar este préstamo?\n\nLibro: " + prestamo.getLibro().getTitulo() + "\nUsuario: " + prestamo.getUsuario().getNombreCompleto() + "\n\nEsta acción no se puede deshacer.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Usar el método forzado para asegurar la eliminación
                prestamoService.eliminarPrestamoForzado(prestamo.getId());
                showInfo("Préstamo eliminado exitosamente");
                refreshCurrentView();
            } catch (Exception e) {
                showError("Error al eliminar préstamo: " + e.getMessage());
            }
        }
    }
} 