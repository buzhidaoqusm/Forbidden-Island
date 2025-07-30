@echo off
setlocal EnableExtensions

cd /d "%~dp0"

echo [1/6] mvn clean package ...
mvn -q -DskipTests clean package || goto :err

echo [2/6] javafx:jlink ...
mvn -q javafx:jlink || goto :err

echo [3/6] 复制资源到 target\app\resources ...
if not exist "target\app\resources" mkdir "target\app\resources"
robocopy "src\main\resources" "target\app\resources" /MIR >nul
if %ERRORLEVEL% GTR 7 goto :err

echo [4/6] 复制主 JAR 到 target\app\lib ...
if not exist "target\app\lib" mkdir "target\app\lib"
if not exist "target\ForbiddenIsland_v2-1.0-SNAPSHOT.jar" (
  echo 未找到主JAR: target\ForbiddenIsland_v2-1.0-SNAPSHOT.jar
  goto :err
)
copy /Y "target\ForbiddenIsland_v2-1.0-SNAPSHOT.jar" "target\app\lib\" >nul || goto :err

echo [5/6] jpackage 生成应用映像 ...
if not exist "target\dist" mkdir "target\dist"

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

if errorlevel 1 goto :err

echo.
echo 打包完成：target\dist\ForbiddenIsland
exit /b 0

:err
echo.
echo *** 构建/打包失败，请检查上方日志 ***
exit /b 1
