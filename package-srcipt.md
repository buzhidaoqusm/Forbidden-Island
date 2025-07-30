### 生成JavaFX应用程序的cmd命令，请逐行执行
mvn -DskipTests clean package 

mvn javafx:jlink

if not exist "target\app\resources" mkdir "target\app\resources"
robocopy "src\main\resources" "target\app\resources" /MIR >nul

if not exist "target\app\lib" mkdir "target\app\lib"
copy /Y "target\ForbiddenIsland_v2-1.0-SNAPSHOT.jar" "target\app\lib\" >nul 

jpackage ^
  --type app-image ^
  --name ForbiddenIsland ^
  --input target/app/lib ^
  --main-jar ForbiddenIsland_v2-1.0-SNAPSHOT.jar ^
  --main-class com.island.launcher.GameStart ^
  --runtime-image target/app ^
  --java-options "--add-modules javafx.controls,javafx.fxml" ^
  --resource-dir target/app/resources ^
  --dest target/dist
