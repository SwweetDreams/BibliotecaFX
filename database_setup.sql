-- Script de configuración de la base de datos para el Sistema de Biblioteca
-- Universidad Peruana Unión (UPeU)

-- Crear la base de datos
CREATE DATABASE IF NOT EXISTS biblioteca_upeu;
USE biblioteca_upeu;

-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    rol ENUM('ADMINISTRADOR', 'BIBLIOTECARIO', 'LECTOR') NOT NULL DEFAULT 'LECTOR',
    telefono VARCHAR(20),
    direccion TEXT,
    activo BOOLEAN DEFAULT TRUE,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tabla de libros
CREATE TABLE IF NOT EXISTS libros (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    autor VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    categoria VARCHAR(100),
    editorial VARCHAR(150),
    anio_publicacion INT,
    cantidad_total INT NOT NULL DEFAULT 1,
    cantidad_disponible INT NOT NULL DEFAULT 1,
    ubicacion VARCHAR(100),
    descripcion TEXT,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tabla de préstamos
CREATE TABLE IF NOT EXISTS prestamos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    libro_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_prestamo DATETIME NOT NULL,
    fecha_devolucion_esperada DATETIME NOT NULL,
    fecha_devolucion_real DATETIME,
    estado ENUM('ACTIVO', 'DEVUELTO', 'VENCIDO', 'PERDIDO') DEFAULT 'ACTIVO',
    observaciones TEXT,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (libro_id) REFERENCES libros(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

-- Índices para mejorar el rendimiento
CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_rol ON usuarios(rol);
CREATE INDEX idx_libros_isbn ON libros(isbn);
CREATE INDEX idx_libros_titulo ON libros(titulo);
CREATE INDEX idx_libros_autor ON libros(autor);
CREATE INDEX idx_prestamos_libro ON prestamos(libro_id);
CREATE INDEX idx_prestamos_usuario ON prestamos(usuario_id);
CREATE INDEX idx_prestamos_estado ON prestamos(estado);
CREATE INDEX idx_prestamos_fecha ON prestamos(fecha_prestamo, fecha_devolucion_esperada);

-- Insertar datos de ejemplo

-- Usuarios de ejemplo
INSERT INTO usuarios (nombre, apellido, email, password, rol, telefono, direccion) VALUES
-- Administrador
('Juan', 'Pérez', 'admin@upeu.edu.pe', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'ADMINISTRADOR', '999888777', 'Av. Universitaria 123, Lima'),
-- Bibliotecarios
('María', 'García', 'maria.garcia@upeu.edu.pe', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'BIBLIOTECARIO', '999888776', 'Av. Universitaria 124, Lima'),
('Carlos', 'López', 'carlos.lopez@upeu.edu.pe', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'BIBLIOTECARIO', '999888775', 'Av. Universitaria 125, Lima'),
-- Lectores
('Ana', 'Martínez', 'ana.martinez@upeu.edu.pe', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'LECTOR', '999888774', 'Av. Universitaria 126, Lima'),
('Luis', 'Rodríguez', 'luis.rodriguez@upeu.edu.pe', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'LECTOR', '999888773', 'Av. Universitaria 127, Lima'),
('Carmen', 'Hernández', 'carmen.hernandez@upeu.edu.pe', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'LECTOR', '999888772', 'Av. Universitaria 128, Lima'),
('Roberto', 'González', 'roberto.gonzalez@upeu.edu.pe', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'LECTOR', '999888771', 'Av. Universitaria 129, Lima'),
('Patricia', 'Morales', 'patricia.morales@upeu.edu.pe', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'LECTOR', '999888770', 'Av. Universitaria 130, Lima');

-- Libros de ejemplo
INSERT INTO libros (titulo, autor, isbn, categoria, editorial, anio_publicacion, cantidad_total, cantidad_disponible, ubicacion, descripcion) VALUES
('El Señor de los Anillos', 'J.R.R. Tolkien', '978-84-450-7179-3', 'Fantasía', 'Minotauro', 1954, 5, 5, 'Estante A1', 'Trilogía épica de fantasía'),
('Cien años de soledad', 'Gabriel García Márquez', '978-84-397-2077-7', 'Literatura', 'Real Academia Española', 1967, 3, 3, 'Estante B2', 'Obra maestra del realismo mágico'),
('Don Quijote de la Mancha', 'Miguel de Cervantes', '978-84-376-0494-7', 'Clásico', 'Cátedra', 1605, 4, 4, 'Estante C3', 'Obra cumbre de la literatura española'),
('1984', 'George Orwell', '978-84-206-0000-1', 'Ciencia Ficción', 'Alianza', 1949, 6, 6, 'Estante D4', 'Distopía sobre el control totalitario'),
('Harry Potter y la piedra filosofal', 'J.K. Rowling', '978-84-9838-095-8', 'Fantasía', 'Salamandra', 1997, 8, 8, 'Estante E5', 'Primera entrega de la saga de Harry Potter'),
('Los miserables', 'Victor Hugo', '978-84-206-0000-2', 'Clásico', 'Alianza', 1862, 3, 3, 'Estante F6', 'Novela histórica sobre la justicia social'),
('El principito', 'Antoine de Saint-Exupéry', '978-84-206-0000-3', 'Literatura Infantil', 'Alianza', 1943, 10, 10, 'Estante G7', 'Fábula filosófica para todas las edades'),
('Orgullo y prejuicio', 'Jane Austen', '978-84-206-0000-4', 'Romance', 'Alianza', 1813, 4, 4, 'Estante H8', 'Clásico del romance inglés'),
('Crimen y castigo', 'Fiódor Dostoyevski', '978-84-206-0000-5', 'Literatura', 'Alianza', 1866, 3, 3, 'Estante I9', 'Novela psicológica sobre la culpa'),
('Los juegos del hambre', 'Suzanne Collins', '978-84-9838-095-9', 'Ciencia Ficción', 'Salamandra', 2008, 7, 7, 'Estante J10', 'Primera entrega de la trilogía distópica'),
('El código Da Vinci', 'Dan Brown', '978-84-206-0000-6', 'Suspense', 'Alianza', 2003, 5, 5, 'Estante K11', 'Thriller de misterio religioso'),
('La sombra del viento', 'Carlos Ruiz Zafón', '978-84-206-0000-7', 'Misterio', 'Alianza', 2001, 4, 4, 'Estante L12', 'Novela de misterio ambientada en Barcelona'),
('El alquimista', 'Paulo Coelho', '978-84-206-0000-8', 'Filosofía', 'Alianza', 1988, 6, 6, 'Estante M13', 'Fábula sobre el destino personal'),
('Los pilares de la tierra', 'Ken Follett', '978-84-206-0000-9', 'Histórica', 'Alianza', 1989, 3, 3, 'Estante N14', 'Novela histórica sobre la construcción de una catedral'),
('El nombre del viento', 'Patrick Rothfuss', '978-84-206-0010-0', 'Fantasía', 'Alianza', 2007, 4, 4, 'Estante O15', 'Primera parte de la Crónica del Asesino de Reyes');

-- Préstamos de ejemplo
INSERT INTO prestamos (libro_id, usuario_id, fecha_prestamo, fecha_devolucion_esperada, estado, observaciones) VALUES
(1, 4, '2024-01-15 10:00:00', '2024-02-15 10:00:00', 'ACTIVO', 'Préstamo regular'),
(3, 5, '2024-01-10 14:30:00', '2024-02-10 14:30:00', 'ACTIVO', 'Préstamo para investigación'),
(5, 6, '2024-01-20 09:15:00', '2024-02-20 09:15:00', 'ACTIVO', 'Préstamo estudiantil'),
(7, 7, '2024-01-05 16:45:00', '2024-02-05 16:45:00', 'DEVUELTO', 'Devuelto en buen estado'),
(9, 8, '2024-01-12 11:20:00', '2024-02-12 11:20:00', 'ACTIVO', 'Préstamo para lectura recreativa'),
(2, 4, '2023-12-20 13:00:00', '2024-01-20 13:00:00', 'DEVUELTO', 'Devuelto con retraso'),
(4, 5, '2023-12-15 15:30:00', '2024-01-15 15:30:00', 'DEVUELTO', 'Devuelto a tiempo'),
(6, 6, '2023-12-10 08:45:00', '2024-01-10 08:45:00', 'VENCIDO', 'Préstamo vencido'),
(8, 7, '2024-01-18 12:10:00', '2024-02-18 12:10:00', 'ACTIVO', 'Préstamo reciente'),
(10, 8, '2024-01-22 17:25:00', '2024-02-22 17:25:00', 'ACTIVO', 'Préstamo para estudio');

-- Actualizar cantidades disponibles de libros
UPDATE libros SET cantidad_disponible = cantidad_disponible - 1 WHERE id IN (1, 3, 5, 9, 8, 10);
UPDATE libros SET cantidad_disponible = cantidad_disponible - 1 WHERE id IN (6) AND cantidad_disponible > 0;

-- Mostrar información de la base de datos
SELECT 'Base de datos biblioteca_upeu creada exitosamente' AS mensaje;
SELECT COUNT(*) AS total_usuarios FROM usuarios;
SELECT COUNT(*) AS total_libros FROM libros;
SELECT COUNT(*) AS total_prestamos FROM prestamos;
SELECT COUNT(*) AS prestamos_activos FROM prestamos WHERE estado = 'ACTIVO';

-- Comandos para reiniciar las secuencias de auto-incremento
-- Ejecutar estos comandos cuando se quiera reiniciar los IDs

-- Reiniciar secuencia de usuarios
-- ALTER TABLE usuarios AUTO_INCREMENT = 1;

-- Reiniciar secuencia de libros  
-- ALTER TABLE libros AUTO_INCREMENT = 1;

-- Reiniciar secuencia de préstamos
-- ALTER TABLE prestamos AUTO_INCREMENT = 1;

-- Comando para obtener el siguiente ID disponible para cada tabla
-- SELECT MAX(id) + 1 as siguiente_id FROM usuarios;
-- SELECT MAX(id) + 1 as siguiente_id FROM libros;
-- SELECT MAX(id) + 1 as siguiente_id FROM prestamos; 