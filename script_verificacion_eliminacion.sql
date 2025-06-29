-- Script de Verificación y Forzado de Eliminaciones
-- BibliotecaFX - Universidad Peruana Unión

USE bibliotecasql;

-- =====================================================
-- VERIFICACIÓN DE DATOS ACTUALES
-- =====================================================

-- Ver todos los libros
SELECT 'LIBROS' as tabla, COUNT(*) as total FROM libros;
SELECT id, titulo, autor, cantidad_disponible, cantidad_total FROM libros ORDER BY id;

-- Ver todos los usuarios
SELECT 'USUARIOS' as tabla, COUNT(*) as total FROM usuarios;
SELECT id, nombre, apellido, email, rol, activo FROM usuarios ORDER BY id;

-- Ver todos los préstamos
SELECT 'PRESTAMOS' as tabla, COUNT(*) as total FROM prestamos;
SELECT id, libro_id, usuario_id, estado, fecha_prestamo FROM prestamos ORDER BY id;

-- =====================================================
-- COMANDOS PARA ELIMINACIÓN DIRECTA (USAR CON PRECAUCIÓN)
-- =====================================================

-- Eliminar un libro específico (reemplazar [ID] con el ID real)
-- DELETE FROM libros WHERE id = [ID];

-- Desactivar un usuario específico (reemplazar [ID] con el ID real)
-- UPDATE usuarios SET activo = false WHERE id = [ID];

-- Eliminar un préstamo específico (reemplazar [ID] con el ID real)
-- DELETE FROM prestamos WHERE id = [ID];

-- =====================================================
-- EJEMPLOS DE ELIMINACIÓN
-- =====================================================

-- Ejemplo: Eliminar libro con ID 15
-- DELETE FROM libros WHERE id = 15;

-- Ejemplo: Desactivar usuario con ID 8
-- UPDATE usuarios SET activo = false WHERE id = 8;

-- Ejemplo: Eliminar préstamo con ID 12
-- DELETE FROM prestamos WHERE id = 12;

-- =====================================================
-- VERIFICACIÓN DESPUÉS DE ELIMINACIÓN
-- =====================================================

-- Verificar si un libro específico existe
-- SELECT * FROM libros WHERE id = [ID];

-- Verificar si un usuario específico está activo
-- SELECT * FROM usuarios WHERE id = [ID] AND activo = true;

-- Verificar si un préstamo específico existe
-- SELECT * FROM prestamos WHERE id = [ID];

-- =====================================================
-- COMANDOS DE LIMPIEZA MASIVA (USAR CON MUCHA PRECAUCIÓN)
-- =====================================================

-- Eliminar todos los libros (¡PELIGROSO!)
-- DELETE FROM libros;

-- Desactivar todos los usuarios excepto administradores (¡PELIGROSO!)
-- UPDATE usuarios SET activo = false WHERE rol != 'ADMINISTRADOR';

-- Eliminar todos los préstamos (¡PELIGROSO!)
-- DELETE FROM prestamos;

-- =====================================================
-- REINICIAR SECUENCIAS DE AUTO_INCREMENT
-- =====================================================

-- Reiniciar secuencia de libros
-- ALTER TABLE libros AUTO_INCREMENT = 1;

-- Reiniciar secuencia de usuarios
-- ALTER TABLE usuarios AUTO_INCREMENT = 1;

-- Reiniciar secuencia de préstamos
-- ALTER TABLE prestamos AUTO_INCREMENT = 1;

-- =====================================================
-- VERIFICACIÓN DE INTEGRIDAD REFERENCIAL
-- =====================================================

-- Verificar préstamos huérfanos (sin libro válido)
SELECT p.id, p.libro_id, p.usuario_id 
FROM prestamos p 
LEFT JOIN libros l ON p.libro_id = l.id 
WHERE l.id IS NULL;

-- Verificar préstamos huérfanos (sin usuario válido)
SELECT p.id, p.libro_id, p.usuario_id 
FROM prestamos p 
LEFT JOIN usuarios u ON p.usuario_id = u.id 
WHERE u.id IS NULL;

-- =====================================================
-- COMANDOS DE MANTENIMIENTO
-- =====================================================

-- Optimizar tablas
OPTIMIZE TABLE libros;
OPTIMIZE TABLE usuarios;
OPTIMIZE TABLE prestamos;

-- Verificar estado de las tablas
CHECK TABLE libros;
CHECK TABLE usuarios;
CHECK TABLE prestamos;

-- =====================================================
-- INSTRUCCIONES DE USO
-- =====================================================

/*
INSTRUCCIONES PARA USAR ESTE SCRIPT:

1. CONECTARSE A MYSQL:
   mysql -u root -p

2. SELECCIONAR LA BASE DE DATOS:
   USE bibliotecasql;

3. EJECUTAR VERIFICACIONES:
   - Ejecutar las consultas SELECT para ver el estado actual
   - Identificar los registros que necesitan ser eliminados

4. ELIMINAR REGISTROS ESPECÍFICOS:
   - Descomentar la línea DELETE o UPDATE correspondiente
   - Reemplazar [ID] con el ID real del registro
   - Ejecutar el comando

5. VERIFICAR LA ELIMINACIÓN:
   - Ejecutar las consultas de verificación
   - Confirmar que el registro ya no existe

6. REINICIAR SECUENCIAS (OPCIONAL):
   - Si quieres que los nuevos registros empiecen desde ID 1
   - Descomentar y ejecutar los comandos ALTER TABLE

PRECAUCIÓN: 
- Siempre hacer backup antes de eliminar datos
- Verificar dos veces antes de ejecutar comandos DELETE
- Usar WHERE clauses específicas para evitar eliminaciones accidentales
*/ 