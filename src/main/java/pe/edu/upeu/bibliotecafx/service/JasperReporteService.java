package pe.edu.upeu.bibliotecafx.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.bibliotecafx.model.Libro;
import pe.edu.upeu.bibliotecafx.model.Prestamo;
import pe.edu.upeu.bibliotecafx.model.Usuario;
import pe.edu.upeu.bibliotecafx.repository.LibroRepository;
import pe.edu.upeu.bibliotecafx.repository.PrestamoRepository;
import pe.edu.upeu.bibliotecafx.repository.UsuarioRepository;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class JasperReporteService {

    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;
    private final PrestamoRepository prestamoRepository;

    /**
     * Genera un reporte de libros en PDF (simulado como HTML que se puede convertir a PDF)
     */
    public String generarReporteLibrosPDF() {
        try {
            List<Libro> libros = libroRepository.findAll();
            return generarReporteHTML("libros", libros, "Reporte de Libros", "reporte_libros");
        } catch (Exception e) {
            log.error("Error generando reporte de libros PDF", e);
            throw new RuntimeException("Error generando reporte de libros", e);
        }
    }

    /**
     * Genera un reporte de usuarios en PDF (simulado como HTML que se puede convertir a PDF)
     */
    public String generarReporteUsuariosPDF() {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            return generarReporteHTML("usuarios", usuarios, "Reporte de Usuarios", "reporte_usuarios");
        } catch (Exception e) {
            log.error("Error generando reporte de usuarios PDF", e);
            throw new RuntimeException("Error generando reporte de usuarios", e);
        }
    }

    /**
     * Genera un reporte de préstamos en PDF (simulado como HTML que se puede convertir a PDF)
     */
    public String generarReportePrestamosPDF() {
        try {
            List<Prestamo> prestamos = prestamoRepository.findAll();
            return generarReporteHTML("prestamos", prestamos, "Reporte de Préstamos", "reporte_prestamos");
        } catch (Exception e) {
            log.error("Error generando reporte de préstamos PDF", e);
            throw new RuntimeException("Error generando reporte de préstamos", e);
        }
    }

    /**
     * Genera un reporte estadístico en PDF (simulado como HTML que se puede convertir a PDF)
     */
    public String generarReporteEstadisticoPDF() {
        try {
            Map<String, Object> estadisticas = obtenerEstadisticas();
            return generarReporteEstadisticoHTML(estadisticas);
        } catch (Exception e) {
            log.error("Error generando reporte estadístico PDF", e);
            throw new RuntimeException("Error generando reporte estadístico", e);
        }
    }

    /**
     * Genera un reporte HTML genérico que se puede convertir a PDF
     */
    private String generarReporteHTML(String tipoReporte, List<?> datos, String titulo, String nombreArchivo) throws Exception {
        // Crear directorio si no existe
        Path reportesDir = Paths.get("reportes");
        if (!Files.exists(reportesDir)) {
            Files.createDirectories(reportesDir);
        }
        
        // Crear archivo HTML
        String rutaArchivo = reportesDir.resolve(nombreArchivo + "_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".html").toString();
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(rutaArchivo))) {
            // Escribir encabezado HTML
            writer.println("<!DOCTYPE html>");
            writer.println("<html lang='es'>");
            writer.println("<head>");
            writer.println("    <meta charset='UTF-8'>");
            writer.println("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            writer.println("    <title>" + titulo + "</title>");
            writer.println("    <style>");
            writer.println("        body { font-family: Arial, sans-serif; margin: 20px; }");
            writer.println("        .header { text-align: center; margin-bottom: 30px; }");
            writer.println("        .title { font-size: 24px; font-weight: bold; color: #2c3e50; }");
            writer.println("        .subtitle { font-size: 16px; color: #7f8c8d; margin-top: 5px; }");
            writer.println("        .info { margin-bottom: 20px; font-size: 12px; color: #95a5a6; }");
            writer.println("        table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
            writer.println("        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            writer.println("        th { background-color: #3498db; color: white; font-weight: bold; }");
            writer.println("        tr:nth-child(even) { background-color: #f2f2f2; }");
            writer.println("        .footer { margin-top: 30px; text-align: center; font-size: 12px; color: #95a5a6; }");
            writer.println("    </style>");
            writer.println("</head>");
            writer.println("<body>");
            
            // Encabezado
            writer.println("    <div class='header'>");
            writer.println("        <div class='title'>" + titulo + "</div>");
            writer.println("        <div class='subtitle'>Universidad Peruana Unión - Sistema de Biblioteca</div>");
            writer.println("    </div>");
            
            // Información del reporte
            writer.println("    <div class='info'>");
            writer.println("        <strong>Fecha de generación:</strong> " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) + "<br>");
            writer.println("        <strong>Total de registros:</strong> " + datos.size() + "<br>");
            writer.println("        <strong>Generado por:</strong> Sistema de Biblioteca UPeU");
            writer.println("    </div>");
            
            // Tabla de datos
            writer.println("    <table>");
            
            // Generar encabezados y datos según el tipo de reporte
            switch (tipoReporte) {
                case "libros":
                    generarTablaLibros(writer, datos);
                    break;
                case "usuarios":
                    generarTablaUsuarios(writer, datos);
                    break;
                case "prestamos":
                    generarTablaPrestamos(writer, datos);
                    break;
            }
            
            writer.println("    </table>");
            
            // Pie de página
            writer.println("    <div class='footer'>");
            writer.println("        <p>Reporte generado automáticamente por el Sistema de Biblioteca UPeU</p>");
            writer.println("        <p>Universidad Peruana Unión - " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy")) + "</p>");
            writer.println("    </div>");
            
            writer.println("</body>");
            writer.println("</html>");
        }
        
        return rutaArchivo;
    }

    /**
     * Genera tabla para reporte de libros
     */
    private void generarTablaLibros(PrintWriter writer, List<?> datos) {
        writer.println("        <thead>");
        writer.println("            <tr>");
        writer.println("                <th>ID</th>");
        writer.println("                <th>Título</th>");
        writer.println("                <th>Autor</th>");
        writer.println("                <th>ISBN</th>");
        writer.println("                <th>Categoría</th>");
        writer.println("                <th>Disponible</th>");
        writer.println("                <th>Total</th>");
        writer.println("            </tr>");
        writer.println("        </thead>");
        writer.println("        <tbody>");
        
        for (Object obj : datos) {
            Libro libro = (Libro) obj;
            writer.println("            <tr>");
            writer.println("                <td>" + libro.getId() + "</td>");
            writer.println("                <td>" + escapeHtml(libro.getTitulo()) + "</td>");
            writer.println("                <td>" + escapeHtml(libro.getAutor()) + "</td>");
            writer.println("                <td>" + escapeHtml(libro.getIsbn()) + "</td>");
            writer.println("                <td>" + escapeHtml(libro.getCategoria()) + "</td>");
            writer.println("                <td>" + libro.getCantidadDisponible() + "</td>");
            writer.println("                <td>" + libro.getCantidadTotal() + "</td>");
            writer.println("            </tr>");
        }
        
        writer.println("        </tbody>");
    }

    /**
     * Genera tabla para reporte de usuarios
     */
    private void generarTablaUsuarios(PrintWriter writer, List<?> datos) {
        writer.println("        <thead>");
        writer.println("            <tr>");
        writer.println("                <th>ID</th>");
        writer.println("                <th>Nombre</th>");
        writer.println("                <th>Apellido</th>");
        writer.println("                <th>Email</th>");
        writer.println("                <th>Rol</th>");
        writer.println("                <th>Teléfono</th>");
        writer.println("                <th>Activo</th>");
        writer.println("            </tr>");
        writer.println("        </thead>");
        writer.println("        <tbody>");
        
        for (Object obj : datos) {
            Usuario usuario = (Usuario) obj;
            writer.println("            <tr>");
            writer.println("                <td>" + usuario.getId() + "</td>");
            writer.println("                <td>" + escapeHtml(usuario.getNombre()) + "</td>");
            writer.println("                <td>" + escapeHtml(usuario.getApellido()) + "</td>");
            writer.println("                <td>" + escapeHtml(usuario.getEmail()) + "</td>");
            writer.println("                <td>" + escapeHtml(usuario.getRol().toString()) + "</td>");
            writer.println("                <td>" + escapeHtml(usuario.getTelefono()) + "</td>");
            writer.println("                <td>" + (usuario.getActivo() ? "Sí" : "No") + "</td>");
            writer.println("            </tr>");
        }
        
        writer.println("        </tbody>");
    }

    /**
     * Genera tabla para reporte de préstamos
     */
    private void generarTablaPrestamos(PrintWriter writer, List<?> datos) {
        writer.println("        <thead>");
        writer.println("            <tr>");
        writer.println("                <th>ID</th>");
        writer.println("                <th>Libro</th>");
        writer.println("                <th>Usuario</th>");
        writer.println("                <th>Fecha Préstamo</th>");
        writer.println("                <th>Fecha Devolución</th>");
        writer.println("                <th>Estado</th>");
        writer.println("            </tr>");
        writer.println("        </thead>");
        writer.println("        <tbody>");
        
        for (Object obj : datos) {
            Prestamo prestamo = (Prestamo) obj;
            writer.println("            <tr>");
            writer.println("                <td>" + prestamo.getId() + "</td>");
            writer.println("                <td>" + escapeHtml(prestamo.getLibro() != null ? prestamo.getLibro().getTitulo() : "N/A") + "</td>");
            writer.println("                <td>" + escapeHtml(prestamo.getUsuario() != null ? prestamo.getUsuario().getNombreCompleto() : "N/A") + "</td>");
            writer.println("                <td>" + (prestamo.getFechaPrestamo() != null ? 
                prestamo.getFechaPrestamo().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A") + "</td>");
            writer.println("                <td>" + (prestamo.getFechaDevolucionEsperada() != null ? 
                prestamo.getFechaDevolucionEsperada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A") + "</td>");
            writer.println("                <td>" + escapeHtml(prestamo.getEstado() != null ? prestamo.getEstado().toString() : "N/A") + "</td>");
            writer.println("            </tr>");
        }
        
        writer.println("        </tbody>");
    }

    /**
     * Escapa caracteres HTML para evitar problemas de seguridad
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }

    /**
     * Obtiene estadísticas generales del sistema
     */
    private Map<String, Object> obtenerEstadisticas() {
        Map<String, Object> estadisticas = new HashMap<>();
        
        long totalLibros = libroRepository.count();
        long totalUsuarios = usuarioRepository.count();
        long totalPrestamos = prestamoRepository.count();
        
        long prestamosActivos = prestamoRepository.findAll().stream()
            .filter(p -> p.getEstado() == Prestamo.EstadoPrestamo.ACTIVO)
            .count();
        
        long prestamosVencidos = prestamoRepository.findAll().stream()
            .filter(p -> p.getEstado() == Prestamo.EstadoPrestamo.VENCIDO)
            .count();
        
        estadisticas.put("totalLibros", totalLibros);
        estadisticas.put("totalUsuarios", totalUsuarios);
        estadisticas.put("totalPrestamos", totalPrestamos);
        estadisticas.put("prestamosActivos", prestamosActivos);
        estadisticas.put("prestamosVencidos", prestamosVencidos);
        estadisticas.put("fechaGeneracion", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        estadisticas.put("TITULO_REPORTE", "Reporte Estadístico del Sistema");
        estadisticas.put("USUARIO_GENERADOR", "Sistema de Biblioteca UPeU");
        estadisticas.put("INSTITUCION", "Universidad Peruana Unión");
        
        return estadisticas;
    }

    /**
     * Genera reporte estadístico en HTML
     */
    private String generarReporteEstadisticoHTML(Map<String, Object> estadisticas) throws Exception {
        // Crear directorio si no existe
        Path reportesDir = Paths.get("reportes");
        if (!Files.exists(reportesDir)) {
            Files.createDirectories(reportesDir);
        }
        
        // Crear archivo HTML
        String rutaArchivo = reportesDir.resolve("reporte_estadistico_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".html").toString();
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(rutaArchivo))) {
            // Escribir encabezado HTML
            writer.println("<!DOCTYPE html>");
            writer.println("<html lang='es'>");
            writer.println("<head>");
            writer.println("    <meta charset='UTF-8'>");
            writer.println("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            writer.println("    <title>Reporte Estadístico del Sistema</title>");
            writer.println("    <style>");
            writer.println("        body { font-family: Arial, sans-serif; margin: 20px; }");
            writer.println("        .header { text-align: center; margin-bottom: 30px; }");
            writer.println("        .title { font-size: 24px; font-weight: bold; color: #2c3e50; }");
            writer.println("        .subtitle { font-size: 16px; color: #7f8c8d; margin-top: 5px; }");
            writer.println("        .info { margin-bottom: 20px; font-size: 12px; color: #95a5a6; }");
            writer.println("        .stats { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 20px; margin: 30px 0; }");
            writer.println("        .stat-card { background: #3498db; color: white; padding: 20px; border-radius: 8px; text-align: center; }");
            writer.println("        .stat-number { font-size: 32px; font-weight: bold; margin-bottom: 5px; }");
            writer.println("        .stat-label { font-size: 14px; opacity: 0.9; }");
            writer.println("        .footer { margin-top: 30px; text-align: center; font-size: 12px; color: #95a5a6; }");
            writer.println("    </style>");
            writer.println("</head>");
            writer.println("<body>");
            
            // Encabezado
            writer.println("    <div class='header'>");
            writer.println("        <div class='title'>Reporte Estadístico del Sistema</div>");
            writer.println("        <div class='subtitle'>Universidad Peruana Unión - Sistema de Biblioteca</div>");
            writer.println("    </div>");
            
            // Información del reporte
            writer.println("    <div class='info'>");
            writer.println("        <strong>Fecha de generación:</strong> " + estadisticas.get("fechaGeneracion") + "<br>");
            writer.println("        <strong>Generado por:</strong> Sistema de Biblioteca UPeU");
            writer.println("    </div>");
            
            // Estadísticas
            writer.println("    <div class='stats'>");
            writer.println("        <div class='stat-card'>");
            writer.println("            <div class='stat-number'>" + estadisticas.get("totalLibros") + "</div>");
            writer.println("            <div class='stat-label'>Total de Libros</div>");
            writer.println("        </div>");
            writer.println("        <div class='stat-card'>");
            writer.println("            <div class='stat-number'>" + estadisticas.get("totalUsuarios") + "</div>");
            writer.println("            <div class='stat-label'>Total de Usuarios</div>");
            writer.println("        </div>");
            writer.println("        <div class='stat-card'>");
            writer.println("            <div class='stat-number'>" + estadisticas.get("totalPrestamos") + "</div>");
            writer.println("            <div class='stat-label'>Total de Préstamos</div>");
            writer.println("        </div>");
            writer.println("        <div class='stat-card'>");
            writer.println("            <div class='stat-number'>" + estadisticas.get("prestamosActivos") + "</div>");
            writer.println("            <div class='stat-label'>Préstamos Activos</div>");
            writer.println("        </div>");
            writer.println("        <div class='stat-card'>");
            writer.println("            <div class='stat-number'>" + estadisticas.get("prestamosVencidos") + "</div>");
            writer.println("            <div class='stat-label'>Préstamos Vencidos</div>");
            writer.println("        </div>");
            writer.println("    </div>");
            
            // Pie de página
            writer.println("    <div class='footer'>");
            writer.println("        <p>Reporte generado automáticamente por el Sistema de Biblioteca UPeU</p>");
            writer.println("        <p>Universidad Peruana Unión - " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy")) + "</p>");
            writer.println("    </div>");
            
            writer.println("</body>");
            writer.println("</html>");
        }
        
        return rutaArchivo;
    }
} 