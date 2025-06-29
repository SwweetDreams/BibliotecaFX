Write-Host "Iniciando BibliotecaFX..." -ForegroundColor Green

# Verificar si Maven está disponible
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Host "Error: Maven no está instalado o no está en el PATH" -ForegroundColor Red
    exit 1
}

# Verificar si Java está disponible
if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Host "Error: Java no está instalado o no está en el PATH" -ForegroundColor Red
    exit 1
}

# Mostrar versión de Java
Write-Host "Versión de Java:" -ForegroundColor Yellow
java -version

Write-Host "`nCompilando proyecto..." -ForegroundColor Yellow
mvn clean compile

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error en la compilación" -ForegroundColor Red
    exit 1
}

Write-Host "`nEjecutando aplicación..." -ForegroundColor Yellow

# Ejecutar con Maven y argumentos de JVM
mvn exec:java -Dexec.mainClass="pe.edu.upeu.bibliotecafx.ApplicationMain" -Dexec.args="--add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens javafx.graphics/javafx.scene=ALL-UNNAMED --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED --add-opens javafx.base/javafx.beans=ALL-UNNAMED --add-opens javafx.base/javafx.beans.property=ALL-UNNAMED --add-opens javafx.base/javafx.beans.value=ALL-UNNAMED --add-opens javafx.base/javafx.collections=ALL-UNNAMED --add-opens javafx.graphics/javafx.scene.layout=ALL-UNNAMED --add-opens javafx.graphics/javafx.scene.text=ALL-UNNAMED"

Write-Host "`nAplicación finalizada" -ForegroundColor Green 