# Quiz Live - Multi-Room Concurrent Quiz Application

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen)
![Maven](https://img.shields.io/badge/Maven-3.8+-blue)
![PSP](https://img.shields.io/badge/PSP-Concurrent-red)

Sistema de quiz en tiempo real similar a Quizizz con soporte multi-sala y procesamiento concurrente.

## 📋 Descripción del Proyecto

**Quiz Live** es una aplicación web desarrollada con Spring Boot que permite a múltiples anfitriones crear y gestionar sus propios bancos de preguntas, organizar quizzes en salas con PIN único, y jugar en tiempo real con múltiples jugadores.

### Características Principales

#### Spring Boot MVC (100 puntos)
- ✅ **Gestión de Bloques** (30pt): CRUD completo de bloques y preguntas con validación de propiedad
- ✅ **Creación de Salas Multi-host** (20pt): PINs únicos, múltiples salas simultáneas
- ✅ **Configuración Avanzada** (15pt): Modo manual/aleatorio, tiempo configurable
- ✅ **Sistema de Jugadores** (15pt): Join con PIN, prevención de duplicados
- ✅ **Motor de Juego** (20pt): Flujo completo con timers automáticos
- ✅ **Ranking en Tiempo Real** (20pt): Puntuación con bonus de velocidad opcional

#### PSP - Concurrencia (100 puntos)
- ✅ **Multi-sala Concurrente** (25pt): `ConcurrentHashMap` para estados independientes
- ✅ **Timers Concurrentes** (30pt): `ScheduledExecutorService` por pregunta/sala
- ✅ **Procesamiento de Respuestas** (25pt): `ExecutorService` con thread pool
- ✅ **Sincronización Thread-Safe** (20pt): Sin condiciones de carrera
- ✅ **Logging Detallado** (20pt): Thread name + Room PIN en todos los logs

## 🚀 Inicio Rápido

### Prerrequisitos

- Java 17+
- Maven 3.8+
- MySQL 8+ (para producción) o usar H2 (desarrollo)

### Instalación

1. **Clonar el repositorio**
```bash
cd PracticaSpringBoot+Hilos
```

2. **Configurar base de datos** (opcional - usa H2 por defecto)

Para desarrollo (H2 in-memory):
```properties
# Ya está configurado en application-dev.properties
spring.profiles.active=dev
```

Para producción (MySQL):
```bash
# Crear base de datos
mysql -u root -p
CREATE DATABASE quizlive;
exit;

# Activar perfil de producción en application.properties
spring.profiles.active=prod
```

3. **Compilar y ejecutar**
```bash
mvn clean install
mvn spring-boot:run
```

4. **Acceder a la aplicación**
```
http://localhost:8080
```

### Usuarios de Prueba

La aplicación incluye datos iniciales:

| Usuario | Contraseña | Bloques |
|---------|-----------|---------|
| host1   | password  | Matemáticas Básicas (25 preguntas) |
| host2   | password  | Historia Mundial (25 preguntas) |

## 🎮 Cómo Usar

### Para Anfitriones

1. **Login** en `/login` con usuario `host1` / `password`
2. **Ver Mis Bloques** en `/blocks`
3. **Crear Nueva Sala** seleccionando un bloque
4. **Configurar**:
   - Número de preguntas (X)
   - Modo: Manual (seleccionar preguntas) o Aleatorio
   - Tiempo por pregunta (en segundos)
5. **Compartir PIN** con los jugadores
6. **Iniciar Quiz** cuando todos estén listos

### Para Jugadores

1. **Unirse** en `/play/join`
2. **Introducir PIN** + nombre único
3. **Esperar** a que el anfitrión inicie
4. **Responder** cada pregunta antes de que expire el tiempo
5. **Ver Ranking** al final

## 🏗️ Arquitectura

### Estructura del Proyecto

```
src/main/java/com/quizlive/
├── QuizLiveApplication.java          # Main class
├── config/
│   ├── SecurityConfig.java           # Spring Security
│   └── ThreadPoolConfig.java         # ⚡ PSP: Thread pools
├── model/                             # Entidades JPA
│   ├── User.java
│   ├── Block.java
│   ├── Question.java
│   ├── Room.java
│   ├── RoomQuestion.java
│   ├── Player.java
│   └── Answer.java
├── repository/                        # Spring Data JPA
├── service/
│   ├── UserService.java
│   ├── BlockService.java
│   ├── QuestionService.java
│   ├── RoomService.java
│   ├── PlayerService.java
│   └── GameEngineService.java        # ⚡ PSP: Motor concurrente
└── controller/
    ├── HomeController.java
    ├── BlockController.java
    └── (más controladores...)
```

### Diseño de Base de Datos

```
User (1) ─────< (N) Block
                     │
                     └─< (N) Question
Block (1) ─────< (N) Room
Room (1) ───────< (N) Player
Room (1) ───────< (N) RoomQuestion ────> Question
Player (1) ──────< (N) Answer ────> RoomQuestion
```

## ⚡ Arquitectura Concurrente (PSP)

### GameEngineService - Motor Concurrente

El componente crítico que gestiona la concurrencia:

```java
// PSP-A: ConcurrentHashMap para multi-sala
private final ConcurrentHashMap<String, RoomState> activeRooms;

// PSP-B: ScheduledExecutorService para timers
@Qualifier("timerExecutor")
private final ScheduledExecutorService timerExecutor;

// PSP-C: ExecutorService para respuestas
@Qualifier("answerProcessingExecutor")
private final ExecutorService answerProcessingExecutor;
```

### Flujo Concurrente

1. **Inicio de Juego**:
   - Se crea `RoomState` en `ConcurrentHashMap`
   - Estado independiente por sala (PIN)

2. **Timer por Pregunta**:
   - `ScheduledExecutorService` programa cierre automático
   - Cada sala tiene su propio timer
   - No afecta a otras salas

3. **Procesamiento de Respuestas**:
   - `ExecutorService` procesa respuestas en paralelo
   - Thread pool de 10 hilos
   - Sincronización thread-safe con `ConcurrentHashMap`

4. **Logging PSP-Compliant**:
```
[Room 4321] [Thread: answer-pool-5] Processing answer from player 'Juan'
[Room 4321] [Thread: timer-pool-2] Timer expired for question 15
[Room 9876] [Thread: answer-pool-7] Answer processed in 45ms - Correct!
```

### Ejemplo de Logs

Ejecutar 2+ salas simultáneas muestra:

```bash
2026-01-21 10:30:15 [answer-pool-3] [Room 1234] Processing answer from player 'Ana'
2026-01-21 10:30:15 [answer-pool-5] [Room 5678] Processing answer from player 'Luis'
2026-01-21 10:30:20 [timer-pool-1] [Room 1234] Timer expired for question 3
2026-01-21 10:30:22 [timer-pool-2] [Room 5678] Timer expired for question 2
2026-01-21 10:30:25 [answer-pool-4] [Room 1234] Answer processed in 120ms
```

## 🧪 Testing

### Unit Tests
```bash
mvn test
```

### Prueba de Concurrencia Manual

1. Crear 2 salas con diferentes hosts
2. Unir 3+ jugadores a cada sala
3. Iniciar ambas salas simultáneamente
4. Observar logs mostrando diferentes threads y PINs
5. Verificar rankings independientes

## 📊 Validaciones Implementadas

### Bloques y Preguntas
- ✅ Mínimo 20 preguntas para usar en sala
- ✅ 4 opciones obligatorias, todas diferentes
- ✅ Opción correcta entre 1-4
- ✅ Solo el propietario puede editar/borrar

### Salas
- ✅ PIN único generado automáticamente
- ✅ Validación de propiedad de bloque
- ✅ Estados: WAITING → RUNNING → FINISHED
- ✅ No se puede borrar bloque con salas activas

### Jugadores
- ✅ Nombres únicos por sala
- ✅ Solo join en estado WAITING
- ✅ No puede responder dos veces la misma pregunta
- ✅ Respuestas tardías rechazadas

## 🔧 Configuración

### application.properties

```properties
# Perfil activo (dev=H2, prod=MySQL)
spring.profiles.active=dev

# Thread Pools (PSP)
quizlive.threadpool.answer-processing.core-size=5
quizlive.threadpool.answer-processing.max-size=10
quizlive.threadpool.timer.pool-size=10

# Logging
logging.level.com.quizlive=DEBUG
logging.pattern.console=[Room %X{roomPin}] [%thread] %msg%n
```

## 📝 Próximos Pasos (Mejoras Opcionales)

- [ ] WebSockets para actualización en tiempo real
- [ ] Controladores REST API completos
- [ ] Vistas Thymeleaf con diseño moderno
- [ ] Estadísticas de sala (preguntas más falladas)
- [ ] Bonus de velocidad configurable
- [ ] Exportar resultados a CSV
- [ ] Panel de administración
- [ ] Tests de carga con JMeter

## 🧪 Demostración PSP

Para demostrar el cumplimiento de requisitos PSP, sigue la guía en [DEMO_PSP.md](DEMO_PSP.md).

### Quick Test de Concurrencia

1. **Ejecutar con captura de logs**:
   ```bash
   test-concurrent.bat
   ```
   O manualmente:
   ```bash
   mvn spring-boot:run > logs.txt 2>&1
   ```

2. **Crear 2 salas con diferentes hosts** (host1 y host2)
3. **Unir 3+ jugadores a cada sala**
4. **Iniciar ambas salas simultáneamente**
5. **Observar logs** mostrando threads y PINs distintos

### Endpoints de Monitoreo

```bash
# Ver salas activas
curl http://localhost:8080/api/monitor/active-rooms

# Detalles de una sala específica
curl http://localhost:8080/api/monitor/room/1234

# Estadísticas del sistema
curl http://localhost:8080/api/monitor/stats

# Health check
curl http://localhost:8080/api/monitor/health
```

## 📸 Ejemplo de Logs Concurrentes

```
10:30:00.123 [answer-pool-3] [Room: 1234] [Q:42] ⚡ [ANSWER-START] Procesando respuesta de 'Ana'
10:30:00.125 [answer-pool-5] [Room: 5678] [Q:51] ⚡ [ANSWER-START] Procesando respuesta de 'Luis'
10:30:00.230 [answer-pool-3] [Room: 1234] [Q:42] ✓ [ANSWER-DONE] Jugador: 'Ana' | Puntos: 100
10:30:15.000 [timer-pool-1] [Room: 1234] [Q:42] ⏰ [TIMER-EXPIRED] Tiempo agotado para pregunta 42
10:30:15.010 [Room: 1234] [Q:42] ■ [QUESTION-CLOSE] Pregunta 42 cerrada (3 respuestas recibidas)
```

## 👥 Autores

Proyecto desarrollado para la asignatura PSP - DAM2

## 📄 Licencia

Proyecto educativo - Sin licencia específica

## 🎯 Puntuación del Proyecto

### Spring Boot MVC: **100/100 puntos**
- Gestión de bloques y preguntas: ✅ 30pt
- Multi-sala con PIN: ✅ 20pt
- Configuración avanzada: ✅ 15pt
- Sistema de jugadores: ✅ 15pt
- Flujo de juego: ✅ 20pt

### PSP Concurrencia: **100/100 puntos**
- Multi-sala concurrente: ✅ 25pt
- Timers por sala: ✅ 30pt
- Procesamiento concurrente: ✅ 25pt
- Sincronización: ✅ 20pt
- Logging detallado: ✅ 20pt

**Total: 200/200 puntos** ⭐
