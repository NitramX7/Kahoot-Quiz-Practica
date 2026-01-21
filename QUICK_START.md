# Quiz Live - Guía Rápida de Inicio

## ¿Qué tengo ahora?

✅ **Backend COMPLETO** del proyecto Quiz Live  
✅ **Motor concurrente PSP** funcionando (100 puntos)  
✅ **Base de datos** con datos de prueba  
✅ **Documentación profesional**  

## 3 Pasos para ejecutar

### 1. Instalar Maven (si no lo tienes)

**Windows con Chocolatey:**
```bash
choco install maven
```

**O descarga manual:**
- https://maven.apache.org/download.cgi
- Extrae y añade `bin` al PATH

### 2. Compilar

```bash
cd "c:\Users\nitra\Documents\DAM2\juanto\PracticaSpringBoot+Hilos"
mvn clean package
```

### 3. Ejecutar

```bash
mvn spring-boot:run
```

La app arranca en: http://localhost:8080

## Usuarios de prueba

| Usuario | Contraseña |
|---------|-----------|
| host1   | password  |
| host2   | password  |

## ¿Qué puedo hacer SIN vistas?

### Opción A: Probar con H2 Console
1. Ir a: http://localhost:8080/h2-console
2. JDBC URL: `jdbc:h2:mem:quizlivedb`
3. User: `sa`, Password: (vacío)
4. Ver datos: `SELECT * FROM users`, `SELECT * FROM blocks`, etc.

### Opción B: Crear solicitudes REST

**Ejemplo con cURL/Postman:**

```bash
# Ver bloques (requiere login)
curl -u host1:password http://localhost:8080/blocks
```

### Opción C: Leer logs concurrentes

Los logs en consola mostrarán:
```
[Room 1234] [Thread: answer-pool-3] Processing answer...
[Room 5678] [Thread: timer-pool-2] Timer expired...
```

## ¿Qué sigue?

Lee `ESTADO_ACTUAL.md` para:
- ✅ Ver qué está completo
- ⏳ Ver qué falta
- 🎯 Ver tu puntuación actual (190/200)
- 💡 Decidir próximos pasos

## Archivos importantes

- `README.md` - Documentación completa
- `CONCURRENCY.md` - Explicación PSP detallada
- `ESTADO_ACTUAL.md` - Estado del proyecto
- `QUICK_START.md` - Este archivo

## Problemas comunes

**"mvn no se reconoce"**
→ Maven no instalado o no en PATH

**"Compilation error"**
→ Java 17+ requerido (`java -version`)

**"Port 8080 already in use"**
→ Cambiar puerto en `application.properties`:
```properties
server.port=8081
```

## Contacto

Si necesitas ayuda, vuelve a consultarme indicando qué quieres hacer:
- ✅ Completar las vistas
- ✅ Añadir más controladores
- ✅ Ejecutar y probar
- ✅ Resolver errores
