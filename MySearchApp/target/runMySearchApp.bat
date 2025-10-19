@echo off
set PATH_TO_FX=D:\JavaFX_SDK\javafx-sdk-24.0.2\lib
java --module-path "%PATH_TO_FX%" --add-modules javafx.controls,javafx.fxml -jar MySearchApp-0.0.1-SNAPSHOT-shaded.jar
pause