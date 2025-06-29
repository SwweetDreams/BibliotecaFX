# BibliotecaFX - Sistema de Gestión Bibliotecaria

## 🎨 Interfaz Moderna con JFoenix

BibliotecaFX es un sistema de gestión bibliotecaria desarrollado con **JavaFX** y **JFoenix**, que proporciona una interfaz moderna y elegante siguiendo los principios de **Material Design**.

## ✨ Características de la Interfaz

### 🎯 Diseño Material Design
- **Componentes JFoenix**: Utiliza componentes modernos como JFXButton, JFXTextField, JFXPasswordField, JFXComboBox, etc.
- **Gradientes y Efectos**: Interfaz con gradientes elegantes y efectos de sombra
- **Tipografía Moderna**: Fuentes optimizadas para mejor legibilidad
- **Colores Material**: Paleta de colores basada en Material Design

### 🔐 Pantalla de Login
- **Diseño Centrado**: Formulario de login con diseño centrado y gradiente de fondo
- **Validación en Tiempo Real**: Validación de campos con feedback visual
- **Checkbox "Recordar Sesión"**: Opción para recordar credenciales
- **Mensajes con Snackbar**: Notificaciones elegantes para errores y éxitos

### 📱 Panel Principal
- **Header Moderno**: Barra superior con información del usuario y botón de logout
- **Sidebar Elegante**: Menú lateral con estadísticas rápidas
- **Contenido Responsivo**: Área principal con tablas y controles modernos
- **Paginación Intuitiva**: Controles de paginación con JFoenix

### 👤 Registro de Lectores
- **Formulario Completo**: Campos para todos los datos del lector
- **Validación Avanzada**: Validación de email, teléfono, contraseñas, etc.
- **Términos y Condiciones**: Checkbox obligatorio para aceptar términos
- **Feedback Visual**: Mensajes de estado claros y visibles

## 🛠️ Tecnologías Utilizadas

### Frontend
- **JavaFX 17**: Framework de interfaz de usuario
- **JFoenix 9.0.10**: Biblioteca de componentes Material Design
- **CSS Inline**: Estilos aplicados directamente en los componentes

### Backend
- **Spring Boot 3.5.3**: Framework de aplicación
- **Spring Data JPA**: Persistencia de datos
- **MySQL**: Base de datos
- **JasperReports**: Generación de reportes

## 🚀 Instalación y Configuración

### Prerrequisitos
- Java 17 o superior
- Maven 3.6+
- MySQL 8.0+

### Pasos de Instalación

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/tu-usuario/BibliotecaFX.git
   cd BibliotecaFX
   ```

2. **Configurar la base de datos**
   - Crear base de datos MySQL
   - Ejecutar el script `database_setup.sql`
   - Configurar `application.properties`

3. **Compilar y ejecutar**
   ```bash
   mvn clean install
   mvn javafx:run
   ```

## 📋 Funcionalidades

### 🔐 Autenticación
- Login para bibliotecarios y administradores
- Registro de lectores
- Gestión de sesiones
- Validación de credenciales

### 📚 Gestión de Libros
- CRUD completo de libros
- Búsqueda y filtrado
- Gestión de inventario
- Categorización

### 👥 Gestión de Usuarios
- Registro de lectores
- Gestión de bibliotecarios
- Roles y permisos
- Perfiles de usuario

### 📖 Gestión de Préstamos
- Crear préstamos
- Devoluciones
- Historial de préstamos
- Estados de préstamos

### 📊 Reportes
- Reportes de libros
- Reportes de préstamos
- Estadísticas de uso
- Exportación a PDF/Excel

## 🎨 Características de Diseño

### Colores Principales
- **Primario**: #667eea (Azul Material)
- **Secundario**: #764ba2 (Púrpura Material)
- **Fondo**: #f5f5f5 (Gris claro)
- **Texto**: #333333 (Gris oscuro)

### Componentes JFoenix Utilizados
- `JFXButton`: Botones con efectos Material
- `JFXTextField`: Campos de texto con animaciones
- `JFXPasswordField`: Campos de contraseña seguros
- `JFXComboBox`: Listas desplegables modernas
- `JFXCheckBox`: Checkboxes con animaciones
- `JFXHamburger`: Menú hamburger animado
- `JFXSnackbar`: Notificaciones elegantes

### Efectos Visuales
- **Sombras**: Efectos de profundidad
- **Gradientes**: Fondos con gradientes suaves
- **Animaciones**: Transiciones suaves
- **Hover Effects**: Efectos al pasar el mouse

## 🔧 Configuración de Desarrollo

### Estructura del Proyecto
```
src/
├── main/
│   ├── java/
│   │   └── pe/edu/upeu/bibliotecafx/
│   │       ├── controller/     # Controladores JFoenix
│   │       ├── model/          # Modelos de datos
│   │       ├── service/        # Lógica de negocio
│   │       └── util/           # Utilidades
│   └── resources/
│       ├── fxml/              # Archivos FXML con JFoenix
│       └── application.properties
```

### Archivos FXML Principales
- `login.fxml`: Pantalla de login con JFoenix
- `main.fxml`: Panel principal con componentes modernos
- `registro-lector.fxml`: Formulario de registro elegante

## 📱 Responsive Design

La interfaz está diseñada para ser responsive y funcionar en diferentes tamaños de pantalla:
- **Desktop**: Interfaz completa con sidebar
- **Tablet**: Adaptación de elementos
- **Móvil**: Optimización para pantallas pequeñas

## 🎯 Mejoras Futuras

- [ ] Tema oscuro/claro
- [ ] Más animaciones
- [ ] Dashboard con gráficos
- [ ] Notificaciones push
- [ ] Modo offline
- [ ] Integración con APIs externas

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## 👥 Contribución

Las contribuciones son bienvenidas. Por favor, lee las guías de contribución antes de enviar un pull request.

## 📞 Soporte

Para soporte técnico o preguntas, contacta a:
- Email: soporte@bibliotecafx.com
- Documentación: [docs.bibliotecafx.com](https://docs.bibliotecafx.com)

---

**Desarrollado con ❤️ usando JavaFX y JFoenix** 