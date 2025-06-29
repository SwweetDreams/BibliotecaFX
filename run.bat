@echo off
echo Iniciando BibliotecaFX...

java --add-opens java.base/java.lang.reflect=ALL-UNNAMED ^
     --add-opens java.base/java.lang=ALL-UNNAMED ^
     --add-opens java.base/java.util=ALL-UNNAMED ^
     --add-opens javafx.graphics/javafx.scene=ALL-UNNAMED ^
     --add-opens javafx.controls/javafx.scene.control=ALL-UNNAMED ^
     --add-opens javafx.base/javafx.beans=ALL-UNNAMED ^
     --add-opens javafx.base/javafx.beans.property=ALL-UNNAMED ^
     --add-opens javafx.base/javafx.beans.value=ALL-UNNAMED ^
     --add-opens javafx.base/javafx.collections=ALL-UNNAMED ^
     --add-opens javafx.graphics/javafx.scene.layout=ALL-UNNAMED ^
     --add-opens javafx.graphics/javafx.scene.text=ALL-UNNAMED ^
     -cp "target/classes;target/dependency/*" ^
     pe.edu.upeu.bibliotecafx.ApplicationMain

pause 