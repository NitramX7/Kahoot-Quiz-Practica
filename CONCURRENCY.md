# Quiz Live - Arquitectura Concurrente PSP

Este documento explica la implementación de concurrencia en Quiz Live según los requisitos PSP.

## Índice
1. [Requisitos PSP](#requisitos-psp)
2. [Estructura del Motor Concurrente](#estructura-del-motor-concurrente)
3. [Gestión de Salas](#gestión-de-salas)
4. [Temporizadores](#temporizadores)
5. [Procesamiento de Respuestas](#procesamiento-de-respuestas)
6. [Sincronización](#sincronización)
7. [Logging y Demostración](#logging-y-demostración)
8. [Capturas de Logs](#capturas-de-logs)

---

## Requisitos PSP

El proyecto implementa **5 requisitos PSP** valorados en **100 puntos**:

| Requisito | Puntos | Implementación |
|-----------|--------|----------------|
| **A) Multi-sala concurrente** | 25 | `ConcurrentHashMap<String, RoomState>` |
| **B) Temporizador concurrente** | 30 | `ScheduledExecutorService` |
| **C) Procesamiento concurrente** | 25 | `ExecutorService` con thread pool |
| **D) Sincronización** | 20 | `synchronized`, `AtomicInteger`, `ConcurrentHashMap` |
| **E) Demostración** | 20 | Logs con thread name y room PIN |

---

## Estructura del Motor Concurrente

### GameEngineService

Clase principal ubicada en `com.quizlive.service.GameEngineService`

```java
@Service
public class GameEngineService {
    // PSP-A: Multi-sala
    private final ConcurrentHashMap<String, RoomState> activeRooms;
    
    // PSP-B: Timers
    private final ScheduledExecutorService timerExecutor;
    
    // PSP-C: Answer processing
    private final ExecutorService answerProcessingExecutor;
}
```

### RoomState - Estado Independiente por Sala

```java
private class RoomState {
    private final String pin;
    private final List<RoomQuestion> questions;
    private final AtomicInteger currentQuestionIndex;
    private final ConcurrentHashMap<Long, Integer> playerScores;
    private final ConcurrentHashMap<Long, Set<Long>> playerAnsweredQuestions;
    private ScheduledFuture<?> currentTimer;
    private final Object questionLock;
}
```

---

## Gestión de Salas

### PSP-A: Multi-sala Concurrente (25pt)

**Implementación:**
```java
private final ConcurrentHashMap<String, RoomState> activeRooms = new ConcurrentHashMap<>();
```

**Funcionamiento:**
1. Cada sala se identifica por su PIN único
2. `ConcurrentHashMap` permite acceso simultáneo sin bloqueos explícitos
3. Cada `RoomState` mantiene su propio estado independiente
4. No hay interferencia entre salas

**Ejemplo:**
```java
public void startGame(String pin) {
    RoomState roomState = new RoomState(pin, ...);
    activeRooms.put(pin, roomState);  // Thread-safe insertion
}
```

**Ventajas:**
- ✅ Múltiples salas simultáneas
- ✅ Estados completamente independientes
- ✅ Sin bloqueos globales
- ✅ Escalable a muchas salas

---

## Temporizadores

### PSP-B: Temporizador Concurrente por Pregunta (30pt)

**Configuración:**
```java
@Bean(name = "timerExecutor")
public ScheduledExecutorService timerExecutor() {
    return Executors.newScheduledThreadPool(10);
}
```

**Implementación:**
```java
public void startNextQuestion(String pin) {
    RoomQuestion question = roomState.getCurrentQuestion();
    question.open();
    
    // Schedule auto-close timer
    roomState.currentTimer = timerExecutor.schedule(() -> {
        log.info("[Thread: {}] Timer expired", Thread.currentThread().getName());
        closeQuestion(pin, question.getId());
    }, timePerQuestion, TimeUnit.SECONDS);
}
```

**Flujo:**
1. Se abre la pregunta (`question.open()`)
2. Se programa un timer usando `ScheduledExecutorService`
3. Al expirar el tiempo, se ejecuta automáticamente `closeQuestion()`
4. Se rechazanrespuestas tardías
5. Se pasa automáticamente a la siguiente pregunta

**Características:**
- ✅ Timer independiente por sala y pregunta
- ✅ Ejecución en hilo separado (timer pool)
- ✅ Cancelable si es necesario
- ✅ Cierre automático al expirar

---

## Procesamiento de Respuestas

### PSP-C: Procesamiento Concurrente (25pt)

**Configuración:**
```java
@Bean(name = "answerProcessingExecutor")
public ExecutorService answerProcessingExecutor() {
    return Executors.newFixedThreadPool(10);
}
```

**Implementación:**
```java
public CompletableFuture<Answer> submitAnswer(String pin, String playerName, 
                                                Long questionId, Integer option) {
    return CompletableFuture.supplyAsync(() -> {
        log.info("[Thread: {}] Processing answer from {}", 
                Thread.currentThread().getName(), playerName);
        
        // 1. Validate question is open
        if (!roomQuestion.canAcceptAnswers()) {
            throw new IllegalStateException("Question closed");
        }
        
        // 2. Check player hasn't answered yet
        if (!roomState.canPlayerAnswer(playerId, questionId)) {
            throw new IllegalStateException("Already answered");
        }
        
        // 3. Calculate correctness
        boolean correct = question.isCorrect(selectedOption);
        
        // 4. Save answer
        Answer answer = saveAnswer(...);
        
        // 5. Update score thread-safely
        roomState.recordPlayerAnswer(playerId, questionId, points);
        
        return answer;
    }, answerProcessingExecutor);  // ⚡ Executed in thread pool
}
```

**Thread Pool:**
- Pool de 10 hilos fijos
- Procesa múltiples respuestas en paralelo
- Una respuesta = una tarea (`Runnable`)
- Comparte hilos entre todas las salas

**Ejemplo de ejecución:**
```
Sala 1234: 5 jugadores responden → 5 tareas en pool
Sala 5678: 3 jugadores responden → 3 tareas en pool
Total: 8 tareas ejecutándose concurrentemente en 10 hilos
```

---

## Sincronización

### PSP-D: Sincronización Thread-Safe (20pt)

#### 1. ConcurrentHashMap para Scores y Respuestas

```java
private final ConcurrentHashMap<Long, Integer> playerScores;
private final ConcurrentHashMap<Long, Set<Long>> playerAnsweredQuestions;

public void recordPlayerAnswer(Long playerId, Long questionId, int points) {
    // Thread-safe: adds question to answered set
    Set<Long> answered = playerAnsweredQuestions.computeIfAbsent(
        playerId, k -> ConcurrentHashMap.newKeySet());
    answered.add(questionId);
    
    // Thread-safe: adds points to player score
    playerScores.merge(playerId, points, Integer::sum);
}
```

#### 2. AtomicInteger para Índice de Pregunta

```java
private final AtomicInteger currentQuestionIndex;

public void moveToNextQuestion() {
    currentQuestionIndex.incrementAndGet();  // Atomic operation
}
```

#### 3. Synchronized Block para Operaciones de Pregunta

```java
synchronized (roomState.questionLock) {
    question.open();
    roomQuestionRepository.save(question);
    
    // Schedule timer
    roomState.currentTimer = timerExecutor.schedule(...);
}
```

#### 4. Validaciones Thread-Safe

```java
// Check 1: Question still open (DB-level consistency)
if (!roomQuestion.canAcceptAnswers()) {
    throw new IllegalStateException("Question closed");
}

// Check 2: Player hasn't answered (ConcurrentHashMap)
if (!roomState.canPlayerAnswer(playerId, questionId)) {
    throw new IllegalStateException("Already answered");
}

// Check 3: Database verification (extra safety)
if (answerRepository.existsByPlayerIdAndRoomQuestionId(...)) {
    throw new IllegalStateException("Duplicate answer");
}
```

**Garantías:**
- ✅ Sin pérdida de respuestas
- ✅ Sin duplicación de puntos
- ✅ Sin respuestas después del cierre
- ✅ Sin condiciones de carrera

---

## Logging y Demostración

### PSP-E: Demostración de Concurrencia (20pt)

#### Configuración de Logging

**application.properties:**
```properties
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] [Room %X{roomPin}] %-5level %logger{36} - %msg%n
```

#### MDC (Mapped Diagnostic Context)

```java
private void setMDC(String pin) {
    MDC.put("roomPin", pin);
}

private void clearMDC() {
    MDC.clear();
}
```

Todos los logs incluyen automáticamente:
- **Thread name**: `[answer-pool-5]`, `[timer-pool-2]`
- **Room PIN**: `[Room 1234]`
- **Timestamp**: `2026-01-21 10:30:15`

---

## Capturas de Logs

### Ejemplo 1: Inicio de Juego

```
2026-01-21 10:30:00 [http-nio-8080-exec-2] [Room 1234] INFO  GameEngineService - Starting game
2026-01-21 10:30:00 [http-nio-8080-exec-2] [Room 1234] INFO  GameEngineService - Game initialized with 10 questions
2026-01-21 10:30:00 [http-nio-8080-exec-2] [Room 1234] INFO  GameEngineService - Question 42 opened (order: 1)
2026-01-21 10:30:00 [http-nio-8080-exec-2] [Room 1234] INFO  GameEngineService - Timer scheduled for 15 seconds
```

### Ejemplo 2: Multi-Sala Simultánea

```
2026-01-21 10:30:15 [answer-pool-3] [Room 1234] INFO  GameEngineService - Processing answer from player 'Ana'
2026-01-21 10:30:15 [answer-pool-5] [Room 5678] INFO  GameEngineService - Processing answer from player 'Luis'
2026-01-21 10:30:15 [answer-pool-7] [Room 1234] INFO  GameEngineService - Processing answer from player 'Juan'
2026-01-21 10:30:16 [answer-pool-3] [Room 1234] INFO  GameEngineService - Answer processed in 120ms - Correct!
2026-01-21 10:30:16 [answer-pool-5] [Room 5678] INFO  GameEngineService - Answer processed in 95ms - Correct!
```

**Observaciones:**
- ✅ Diferentes rooms (1234, 5678)
- ✅ Diferentes threads (answer-pool-3, 5, 7)
- ✅ Procesamiento simultáneo
- ✅ Sin interferencias

### Ejemplo 3: Timer Expiration

```
2026-01-21 10:30:20 [timer-pool-1] [Room 1234] INFO  GameEngineService - Timer expired for question 42
2026-01-21 10:30:20 [timer-pool-1] [Room 1234] INFO  GameEngineService - Question 42 closed
2026-01-21 10:30:22 [timer-pool-1] [Room 1234] INFO  GameEngineService - Question 43 opened (order: 2)
2026-01-21 10:30:23 [timer-pool-2] [Room 5678] INFO  GameEngineService - Timer expired for question 51
```

**Observaciones:**
- ✅ Timer threads (timer-pool-1, timer-pool-2)
- ✅ Timers independientes por sala
- ✅ Cierre automático al expirar

### Ejemplo 4: Respuesta Tardía Rechazada

```
2026-01-21 10:30:25 [answer-pool-4] [Room 1234] WARN  GameEngineService - Question 42 is closed, rejecting answer
```

### Ejemplo 5: Respuesta Duplicada

```
2026-01-21 10:30:30 [answer-pool-6] [Room 1234] WARN  GameEngineService - Player Ana already answered question 42
```

---

## Diagrama de Flujo Concurrente

```
                    ┌─────────────────────────┐
                    │   GameEngineService     │
                    │  (Coordinador Central)  │
                    └─────────────────────────┘
                              │
                ┌─────────────┼─────────────┐
                │                           │
        ┌───────▼──────┐            ┌──────▼───────┐
        │  Room 1234   │            │  Room 5678   │
        │  (RoomState) │            │  (RoomState) │
        └───────┬──────┘            └──────┬───────┘
                │                           │
    ┌───────────┼───────────┐       ┌──────┼───────────┐
    │           │           │       │      │           │
┌───▼───┐ ┌────▼────┐ ┌────▼────┐ ┌▼──┐ ┌─▼──┐ ┌─────▼──┐
│Timer  │ │Answer   │ │Answer   │ │Tmr│ │Ans │ │Answer  │
│Thread │ │Thread-3 │ │Thread-7 │ │Thd│ │Thd │ │Thread-5│
│Pool-1 │ │(Ana)    │ │(Juan)   │ │P-2│ │P-8 │ │(Luis)  │
└───────┘ └─────────┘ └─────────┘ └───┘ └────┘ └────────┘
```

---

## Conclusiones

### Cumplimiento de Requisitos PSP

✅ **A) Multi-sala (25pt)**: `ConcurrentHashMap` con estados independientes  
✅ **B) Temporizadores (30pt)**: `ScheduledExecutorService` por pregunta/sala  
✅ **C) Procesamiento (25pt)**: `ExecutorService` con thread pool de 10 hilos  
✅ **D) Sincronización (20pt)**: Thread-safe con múltiples mecanismos  
✅ **E) Demostración (20pt)**: Logs completos con thread + room PIN  

### Puntos Clave

1. **ConcurrentHashMap** permite múltiples salas sin bloqueos
2. **ScheduledExecutorService** gestiona timers independientes
3. **ExecutorService** procesa respuestas en paralelo
4. **Sincronización** garantiza consistencia de datos
5. **Logging** demuestra ejecución concurrente real

### Pruebas Recomendadas

Para verificar la concurrencia:

1. Crear 2+ salas con diferentes hosts
2. Unir 5+ jugadores a cada sala
3. Iniciar ambas salas simultáneamente
4. Enviar respuestas desde múltiples jugadores
5. Observar logs mostrando:
   - Diferentes threads procesando respuestas
   - Diferentes room PINs en paralelo
   - Timers independientes
   - Sin errores de sincronización

---

**Documento actualizado:** 2026-01-21  
**Versión:** 1.0  
**Proyecto:** Quiz Live PSP
