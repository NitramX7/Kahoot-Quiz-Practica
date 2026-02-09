# Demostración PSP - Quiz Live Concurrente

Esta guía proporciona instrucciones paso a paso para demostrar el cumplimiento de los requisitos PSP de concurrencia en Quiz Live.

## 🎯 Objetivo

Demostrar que el sistema Quiz Live implementa correctamente:
- **Multi-sala concurrente** con estados independientes
- **Temporizadores** independientes por sala y pregunta
- **Procesamiento paralelo** de respuestas de jugadores
- **Sincronización thread-safe** sin condiciones de carrera
- **Logging detallado** que evidencia la ejecución concurrente

---

## 📋 Requisitos Previos

- Application corriendo: `mvn spring-boot:run`
- 2 navegadores diferentes o modo incógnito
- Usuarios de prueba: `host1`/`password` y `host2`/`password`

---

## 🚀 Demostración Paso a Paso

### Paso 1: Preparar el Entorno

1. **Iniciar la aplicación con captura de logs**:
   ```bash
   mvn spring-boot:run > demo-logs.txt 2>&1
   ```
   
2. **Abrir consola de logs en otra ventana** (opcional):
   ```bash
   Get-Content demo-logs.txt -Wait -Tail 50
   ```

---

### Paso 2: Crear Múltiples Salas (PSP-A: Multi-sala)

#### Sala 1
1. Abrir navegador en modo incógnito → `http://localhost:8080`
2. Login: `host1` / `password`
3. Ir a "Mis Bloques" → Seleccionar "Matemáticas Básicas"
4. Crear sala con:
   - Número de preguntas: **5**
   - Modo: **Aleatorio**
   - Tiempo por pregunta: **15 segundos**
5. **Anotar PIN** (ej: `1234`)

#### Sala 2
1. Abrir otro navegador en modo incógnito → `http://localhost:8080`
2. Login: `host2` / `password`
3. Ir a "Mis Bloques" → Seleccionar "Historia Mundial"
4. Crear sala con:
   - Número de preguntas: **5**
   - Modo: **Aleatorio**
   - Tiempo por pregunta: **10 segundos**
5. **Anotar PIN** (ej: `5678`)

#### ✅ Verificación PSP-A
Revisar logs - deberías ver:
```
█ [GAME-START] Iniciando juego en sala PIN: 1234
█ [GAME-START] Sala 1234 inicializada con 5 preguntas
█ [GAME-START] Iniciando juego en sala PIN: 5678
█ [GAME-START] Sala 5678 inicializada con 5 preguntas
```

**Criterio**: Ambas salas se crean sin interferencia mutua.

---

### Paso 3: Unir Jugadores

#### A Sala 1234:
1. Abrir 3 pestañas nuevas en modo incógnito
2. Ir a `http://localhost:8080/play/join`
3. Unir jugadores:
   - PIN: `1234` | Nombre: `Ana`
   - PIN: `1234` | Nombre: `Juan`
   - PIN: `1234` | Nombre: `María`

#### A Sala 5678:
1. Abrir 3 pestañas nuevas en modo incógnito
2. Ir a `http://localhost:8080/play/join`
3. Unir jugadores:
   - PIN: `5678` | Nombre: `Luis`
   - PIN: `5678` | Nombre: `Carlos`
   - PIN: `5678` | Nombre: `Sofia`

---

### Paso 4: Iniciar Ambas Salas Simultáneamente

1. En la ventana del **host1**, hacer clic en "Iniciar Quiz"
2. **Inmediatamente después**, en la ventana del **host2**, hacer clic en "Iniciar Quiz"

#### ✅ Verificación PSP-B (Temporizadores)
Revisar logs - deberías ver:
```
[Room: 1234] ▶ [QUESTION-OPEN] Pregunta 42 abierta (orden: 1/5)
[Room: 1234] ⏱ [TIMER-START] Temporizador iniciado: 15 segundos
[Room: 5678] ▶ [QUESTION-OPEN] Pregunta 51 abierta (orden: 1/5)
[Room: 5678] ⏱ [TIMER-START] Temporizador iniciado: 10 segundos
```

**Criterio**: Ambos timers se inician independientemente.

---

### Paso 5: Responder Simultáneamente (PSP-C: Procesamiento Concurrente)

1. Cuando aparezca la primera pregunta en **ambas salas**
2. Hacer que **todos los jugadores** (6 en total) respondan **al mismo tiempo**
3. Observar los logs en tiempo real

#### ✅ Verificación PSP-C
Revisar logs - deberías ver:
```
[answer-pool-3] [Room: 1234] [Q:42] ⚡ [ANSWER-START] Procesando respuesta de 'Ana'
[answer-pool-5] [Room: 5678] [Q:51] ⚡ [ANSWER-START] Procesando respuesta de 'Luis'
[answer-pool-7] [Room: 1234] [Q:42] ⚡ [ANSWER-START] Procesando respuesta de 'Juan'
[answer-pool-2] [Room: 5678] [Q:51] ⚡ [ANSWER-START] Procesando respuesta de 'Carlos'
[answer-pool-3] [Room: 1234] [Q:42] ✓ [ANSWER-DONE] Jugador: 'Ana' | Puntos: 100
[answer-pool-5] [Room: 5678] [Q:51] ✓ [ANSWER-DONE] Jugador: 'Luis' | Puntos: 100
```

**Criterios**:
- ✅ Diferentes threads: `answer-pool-3`, `answer-pool-5`, etc.
- ✅ Diferentes salas procesándose en paralelo
- ✅ Thread names visibles en los logs

---

### Paso 6: Verificar Temporizadores (PSP-B)

1. En una de las preguntas, **no responder nada**
2. Esperar a que expire el temporizador
3. Observar logs

#### ✅ Verificación PSP-B
Deberías ver:
```
[timer-pool-1] [Room: 1234] [Q:43] ⏰ [TIMER-EXPIRED] Tiempo agotado para pregunta 43 (15s)
[Room: 1234] [Q:43] ■ [QUESTION-CLOSE] Pregunta 43 cerrada (2 respuestas recibidas)
[timer-pool-2] [Room: 5678] [Q:52] ⏰ [TIMER-EXPIRED] Tiempo agotado para pregunta 52 (10s)
[Room: 5678] [Q:52] ■ [QUESTION-CLOSE] Pregunta 52 cerrada (1 respuestas recibidas)
```

**Criterios**:
- ✅ Timers ejecutándose en threads separados: `timer-pool-1`, `timer-pool-2`
- ✅ Cierre automático al expirar
- ✅ Tiempos configurados correctos (15s vs 10s)

---

### Paso 7: Probar Respuesta Tardía (PSP-D: Sincronización)

1. Esperar a que una pregunta esté por cerrarse
2. Intentar responder **después** de que expire el tiempo
3. La respuesta debe ser **rechazada**

#### ✅ Verificación PSP-D
Deberías ver:
```
[answer-pool-4] [Room: 1234] [Q:44] ⛔ [ANSWER-REJECT] Pregunta 44 cerrada - Respuesta de 'Ana' rechazada
```

**Criterio**: No se aceptan respuestas después del cierre.

---

### Paso 8: Probar Respuesta Duplicada (PSP-D)

1. Responder una pregunta
2. **Intentar responder de nuevo** la misma pregunta con el mismo jugador
3. La segunda respuesta debe ser **rechazada**

#### ✅ Verificación PSP-D
Deberías ver:
```
[answer-pool-6] [Room: 1234] [Q:45] ⛔ [ANSWER-DUPLICATE] Jugador 'Juan' ya respondió pregunta 45
```

**Criterio**: No se permiten respuestas duplicadas.

---

### Paso 9: Verificar Cierre Automático (PSP-C)

1. Hacer que **todos los jugadores** de una sala respondan la misma pregunta
2. El sistema debe cerrar la pregunta **antes** de que expire el timer

#### ✅ Verificación
Deberías ver:
```
✓ [ANSWER-DONE] Jugador: 'María' | Puntos: 100
⚡ [AUTO-CLOSE] Todos los jugadores (3) respondieron - Cerrando pregunta anticipadamente
■ [QUESTION-CLOSE] Pregunta 42 cerrada (3 respuestas recibidas)
```

**Criterio**: El sistema optimiza cerrando preguntas cuando todos responden.

---

### Paso 10: Finalizar y Verificar (PSP-E: Demostración)

1. Completar todas las preguntas en ambas salas
2. Verificar que ambos juegos finalizan correctamente

#### ✅ Verificación PSP-E
Deberías ver:
```
✓ [GAME-COMPLETE] Todas las preguntas completadas en sala 1234
🏁 [GAME-END] Finalizando juego en sala 1234
🏁 [GAME-END] Sala 1234 eliminada de salas activas (1 salas restantes)
✓ [GAME-COMPLETE] Todas las preguntas completadas en sala 5678
🏁 [GAME-END] Finalizando juego en sala 5678
🏁 [GAME-END] Sala 5678 eliminada de salas activas (0 salas restantes)
```

---

## 📊 Monitoreo en Tiempo Real (Adicional)

Durante la ejecución, puedes consultar los endpoints de monitoreo:

### Ver salas activas
```bash
curl http://localhost:8080/api/monitor/active-rooms
```

Respuesta esperada:
```json
{
  "activeRooms": 2,
  "rooms": [
    {
      "pin": "1234",
      "activePlayers": 3,
      "currentQuestionId": 42,
      "questionOrder": 2,
      "questionOpen": true
    },
    {
      "pin": "5678",
      "activePlayers": 3,
      "currentQuestionId": 51,
      "questionOrder": 3,
      "questionOpen": true
    }
  ]
}
```

### Ver estadísticas del sistema
```bash
curl http://localhost:8080/api/monitor/stats
```

---

## ✅ Checklist de Verificación PSP

Marca cada item al completar la demostración:

### PSP-A: Multi-sala Concurrente (25pt)
- [ ] ✅ Se crearon 2+ salas simultáneamente
- [ ] ✅ Cada sala mantiene estado independiente (PIN, preguntas, ranking)
- [ ] ✅ Las acciones en una sala no afectan a la otra
- [ ] ✅ Logs muestran `[Room: 1234]` y `[Room: 5678]` claramente

### PSP-B: Temporizadores Concurrentes (30pt)
- [ ] ✅ Cada sala tiene su propio temporizador
- [ ] ✅ Los timers se ejecutan en threads separados (`timer-pool-*`)
- [ ] ✅ Las preguntas se cierran automáticamente al expirar
- [ ] ✅ Respuestas tardías son rechazadas
- [ ] ✅ Transición automática a la siguiente pregunta

### PSP-C: Procesamiento Concurrente (25pt)
- [ ] ✅ Respuestas procesadas en thread pool (`answer-pool-*`)
- [ ] ✅ Múltiples respuestas procesadas en paralelo
- [ ] ✅ Diferentes salas procesan respuestas simultáneamente
- [ ] ✅ Validaciones funcionando (pregunta abierta, no duplicados)
- [ ] ✅ Puntuaciones actualizadas correctamente

### PSP-D: Sincronización (20pt)
- [ ] ✅ No se aceptan respuestas después del cierre
- [ ] ✅ No se permiten respuestas duplicadas
- [ ] ✅ Puntos sumados correctamente sin inconsistencias
- [ ] ✅ Sin errores de concurrencia en logs

### PSP-E: Demostración (20pt)
- [ ] ✅ Logs incluyen thread name (ej: `[answer-pool-5]`)
- [ ] ✅ Logs incluyen room PIN (ej: `[Room: 1234]`)
- [ ] ✅ Logs incluyen question ID (ej: `[Q:42]`)
- [ ] ✅ Se observan múltiples hilos ejecutándose
- [ ] ✅ Se ven múltiples salas activas simultáneamente
- [ ] ✅ Logs son claros y legibles

---

## 📸 Capturas Recomendadas

Para la entrega, captura los siguientes momentos:

1. **Logs de inicio de 2 salas simultáneas**
2. **Procesamiento paralelo de respuestas** (diferentes threads)
3. **Temporizadores expirando** (timer-pool threads)
4. **Respuesta rechazada** (tardía o duplicada)
5. **Cierre automático** cuando todos responden
6. **Finalización de juegos** con contador de salas activas

---

## 🎓 Aspectos Destacados para Evaluación

Al presentar este trabajo, enfatiza:

1. **ConcurrentHashMap**: Gestiona múltiples salas sin bloqueos
2. **ScheduledExecutorService**: Timers independientes en paralelo
3. **ExecutorService**: Pool de 10 hilos procesa respuestas concurrentemente
4. **Sincronización**: `synchronized`, `AtomicInteger`, validaciones thread-safe
5. **MDC Logging**: Contexto automático con PIN, questionId y thread name
6. **Iconos visuales**: Facilitan identificar tipos de eventos en logs

---

## 🔧 Troubleshooting

### Problema: No veo los logs
**Solución**: Asegúrate de redirigir stdout y stderr:
```bash
mvn spring-boot:run > demo-logs.txt 2>&1
```

### Problema: Solo veo una sala en los logs
**Solución**: Confirma que iniciaste ambas salas con少 pocos segundos de diferencia.

### Problema: Error de base de datos
**Solución**: Si usas MySQL, verifica que esté corriendo. Si no, cambia a modo dev (H2):
```properties
spring.profiles.active=dev
```

### Problema: Los endpoints de monitor no funcionan
**Solución**: Verifica que `RoomMonitorController` esté en el package `com.quizlive.controller` y que Spring lo detecte.

---

**Buena suerte con la demostración! 🎉**
