@echo off
REM ========================================
REM  Quiz Live - Test de Concurrencia PSP
REM ========================================
echo.
echo ========================================
echo  Quiz Live - Test de Concurrencia PSP
echo ========================================
echo.
echo Este script captura los logs mientras pruebas multiples salas
echo.
echo Instrucciones:
echo 1. Ejecuta este script
echo 2. Abre 2 navegadores en modo incognito
echo 3. Crea 2 salas con diferentes hosts (host1 y host2)
echo 4. Unite con multiples jugadores a cada sala
echo 5. Inicia ambas salas simultaneamente
echo 6. Responde las preguntas
echo 7. Presiona Ctrl+C para detener la captura
echo.
echo Los logs se guardaran en: concurrency-test-logs.txt
echo.
pause

echo Iniciando aplicacion con captura de logs...
mvn spring-boot:run > concurrency-test-logs.txt 2>&1
