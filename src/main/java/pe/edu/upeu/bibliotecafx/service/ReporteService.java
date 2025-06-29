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

import java.io.IOException;
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
public class ReporteService {

    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;
    private final PrestamoRepository prestamoRepository;

    /**
     * Genera un reporte de libros en formato CSV
     */
    public String generarReporteLibrosCSV() {
        try {
            List<Libro> libros = libroRepository.findAll();
            StringBuilder csv = new StringBuilder();
            
            // Encabezados
            csv.append("ID,Título,Autor,ISBN,Categoría,Cantidad Disponible,Cantidad Total\n");
            
            // Datos
            for (Libro libro : libros) {
                csv.append(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",%d,%d\n",
                    libro.getId(),
                    libro.getTitulo(),
                    libro.getAutor(),
                    libro.getIsbn(),
                    libro.getCategoria(),
                    libro.getCantidadDisponible(),
                    libro.getCantidadTotal()
                ));
            }
            
            return csv.toString();
        } catch (Exception e) {
            log.error("Error generando reporte de libros CSV", e);
            throw new RuntimeException("Error generando reporte de libros", e);
        }
    }

    /**
     * Genera un reporte de usuarios en formato CSV
     */
    public String generarReporteUsuariosCSV() {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            StringBuilder csv = new StringBuilder();
            
            // Encabezados
            csv.append("ID,Nombre,Apellido,Email,Rol,Teléfono,Activo\n");
            
            // Datos
            for (Usuario usuario : usuarios) {
                csv.append(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s\n",
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getApellido(),
                    usuario.getEmail(),
                    usuario.getRol(),
                    usuario.getTelefono(),
                    usuario.getActivo()
                ));
            }
            
            return csv.toString();
        } catch (Exception e) {
            log.error("Error generando reporte de usuarios CSV", e);
            throw new RuntimeException("Error generando reporte de usuarios", e);
        }
    }

    /**
     * Genera un reporte de préstamos en formato CSV
     */
    public String generarReportePrestamosCSV() {
        try {
            List<Prestamo> prestamos = prestamoRepository.findAll();
            StringBuilder csv = new StringBuilder();
            
            // Encabezados
            csv.append("ID,Libro,Usuario,Fecha Préstamo,Fecha Devolución Esperada,Estado\n");
            
            // Datos
            for (Prestamo prestamo : prestamos) {
                csv.append(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    prestamo.getId(),
                    prestamo.getLibro() != null ? prestamo.getLibro().getTitulo() : "N/A",
                    prestamo.getUsuario() != null ? prestamo.getUsuario().getNombreCompleto() : "N/A",
                    prestamo.getFechaPrestamo() != null ? prestamo.getFechaPrestamo().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A",
                    prestamo.getFechaDevolucionEsperada() != null ? prestamo.getFechaDevolucionEsperada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A",
                    prestamo.getEstado()
                ));
            }
            
            return csv.toString();
        } catch (Exception e) {
            log.error("Error generando reporte de préstamos CSV", e);
            throw new RuntimeException("Error generando reporte de préstamos", e);
        }
    }

    /**
     * Genera un reporte estadístico en formato CSV
     */
    public String generarReporteEstadisticoCSV() {
        try {
            Map<String, Object> estadisticas = obtenerEstadisticas();
            StringBuilder csv = new StringBuilder();
            
            // Encabezados
            csv.append("Métrica,Valor\n");
            
            // Datos
            csv.append(String.format("Total de Libros,%d\n", (long) estadisticas.get("totalLibros")));
            csv.append(String.format("Total de Usuarios,%d\n", (long) estadisticas.get("totalUsuarios")));
            csv.append(String.format("Total de Préstamos,%d\n", (long) estadisticas.get("totalPrestamos")));
            csv.append(String.format("Préstamos Activos,%d\n", (long) estadisticas.get("prestamosActivos")));
            csv.append(String.format("Préstamos Vencidos,%d\n", (long) estadisticas.get("prestamosVencidos")));
            csv.append(String.format("Fecha de Generación,\"%s\"\n", estadisticas.get("fechaGeneracion")));
            
            return csv.toString();
        } catch (Exception e) {
            log.error("Error generando reporte estadístico CSV", e);
            throw new RuntimeException("Error generando reporte estadístico", e);
        }
    }

    /**
     * Guarda un reporte CSV en un archivo
     */
    public String guardarReporteCSV(String contenido, String nombreArchivo) {
        try {
            // Crear directorio de reportes si no existe
            Path reportesDir = Paths.get("reportes");
            if (!Files.exists(reportesDir)) {
                Files.createDirectories(reportesDir);
            }
            
            // Crear archivo
            Path archivo = reportesDir.resolve(nombreArchivo + ".csv");
            Files.write(archivo, contenido.getBytes("UTF-8"));
            
            return archivo.toAbsolutePath().toString();
        } catch (IOException e) {
            log.error("Error guardando reporte CSV", e);
            throw new RuntimeException("Error guardando reporte", e);
        }
    }

    /**
     * Genera y guarda reporte de libros
     */
    public String generarReporteLibrosArchivo() {
        String contenido = generarReporteLibrosCSV();
        return guardarReporteCSV(contenido, "reporte_libros_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
    }

    /**
     * Genera y guarda reporte de usuarios
     */
    public String generarReporteUsuariosArchivo() {
        String contenido = generarReporteUsuariosCSV();
        return guardarReporteCSV(contenido, "reporte_usuarios_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
    }

    /**
     * Genera y guarda reporte de préstamos
     */
    public String generarReportePrestamosArchivo() {
        String contenido = generarReportePrestamosCSV();
        return guardarReporteCSV(contenido, "reporte_prestamos_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
    }

    /**
     * Genera y guarda reporte estadístico
     */
    public String generarReporteEstadisticoArchivo() {
        String contenido = generarReporteEstadisticoCSV();
        return guardarReporteCSV(contenido, "reporte_estadistico_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));
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
        
        return estadisticas;
    }

    /**
     * Obtiene estadísticas para mostrar en la interfaz
     */
    public Map<String, Object> obtenerEstadisticasParaUI() {
        return obtenerEstadisticas();
    }
} 