// Game host page functionality
// Las variables roomId, timeLimit, timeLeft, currentQuestionId ya están definidas por el inline script
console.log('[HOST] Archivo game_host.js cargado. roomId:', roomId);

// Variables locales del archivo
let timerInterval = null;
let pollInterval = 1000; // Polling interval dinámico


const timerFill = document.getElementById('timerFill');
const timerDisplay = document.querySelector('#timerDisplay span');

// Función para iniciar el temporizador visual
function startTimer(duration) {
    timeLeft = duration;
    timeLimit = duration;
    
    // Limpiar temporizador anterior si existe
    if (timerInterval) {
        clearInterval(timerInterval);
    }
    
    if (timerFill && timerDisplay) {
        timerInterval = setInterval(() => {
            timeLeft--;
            timerDisplay.textContent = Math.max(0, timeLeft);
            timerFill.style.width = Math.max(0, (timeLeft / timeLimit * 100)) + '%';

            if (timeLeft <= 0) {
                clearInterval(timerInterval);
                timerInterval = null;
            }
        }, 1000);
    }
}

// Iniciar temporizador si hay una pregunta activa
if (currentQuestionId && timeLimit) {
    startTimer(timeLimit);
}

// Poll para sincronización automática con el backend (dinámico)
function pollHostStatus() {
    fetch(`/api/rooms/${roomId}/current-question`)
        .then(res => res.json())
        .then(data => {
            console.log('[HOST] Polling response:', data);
            
            // Si el juego terminó, redirigir al podio
            if (data.finished) {
                console.log('[HOST] Juego terminado, redirigiendo...');
                window.location.href = `/rooms/${roomId}/podium`;
                return;
            }
            
            // Si hay una nueva pregunta ACTIVA Y ABIERTA diferente, recargar
            // IMPORTANTE: Verificar isOpen para evitar recargar durante transición
            if (data.id && data.id !== currentQuestionId && data.state === 'ACTIVE' && data.isOpen) {
                console.log('[HOST] Nueva pregunta activa y abierta detectada:', data.id, 'recargando...');
                window.location.reload();
                return;
            }

            
            // Si no hay pregunta actual pero el estado inicial tenía una, significa que se cerró
            // ACELERAR polling durante transición
            if (currentQuestionId && !data.id && data.state === 'WAITING') {
                console.log('[HOST] Pregunta cerrada, esperando siguiente...');
                if (timerDisplay) {
                    timerDisplay.textContent = '...';
                }
                pollInterval = 500; // Acelerar
                setTimeout(pollHostStatus, pollInterval);
                return;
            }
            
            // Si hay una pregunta en camino, acelerar polling
            if (data.nextQuestionSoon) {
                console.log('[HOST] Siguiente pregunta en camino, acelerando polling...');
                pollInterval = 500;
                setTimeout(pollHostStatus, pollInterval);
                return;
            }
            
            // Actualizar contador de respuestas en tiempo real
            if (typeof data.answersCount === 'number') {
                const answersCountEl = document.getElementById('answersCount');
                if (answersCountEl) {
                    answersCountEl.textContent = data.answersCount;
                }
            }
            
            // Sincronizar temporizador si hay información de tiempo restante
            if (data.remainingSeconds !== undefined && Math.abs(timeLeft - data.remainingSeconds) > 2) {
                console.log('[HOST] Sincronizando temporizador:', data.remainingSeconds);
                startTimer(data.remainingSeconds);
            }
            
            // Polling normal
            pollInterval = 1000;
            setTimeout(pollHostStatus, pollInterval);
        })
        .catch(error => {
            console.error('[HOST] Error en polling:', error);
            setTimeout(pollHostStatus, 2000);
        });
}

// Iniciar polling
pollHostStatus();

