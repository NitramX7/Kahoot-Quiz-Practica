const roomId = window.roomId ?? 0;
const playerId = window.playerId ?? 0;
// Usar inicialización segura desde el atributo del modelo, por defecto null si no existe
let currentQuestionId = window.currentQuestionId ?? null;

console.log("Game initialized", { roomId, playerId, currentQuestionId });

function getCsrf() {
  const tokenMeta = document.querySelector('meta[name="_csrf"]');
  const headerMeta = document.querySelector('meta[name="_csrf_header"]');
  const tokenInput = document.querySelector('input[name="_csrf"]');
  const token = (tokenMeta && tokenMeta.content) || (tokenInput && tokenInput.value) || null;
  const header = (headerMeta && headerMeta.content) || "X-CSRF-TOKEN";
  return { token, header };
}

function submitAnswer(option) {
  // Deshabilitar TODOS los botones inmediatamente para evitar doble clic
  const buttons = document.querySelectorAll('.option-btn');
  buttons.forEach(btn => {
    btn.disabled = true;
    btn.style.opacity = '0.5';
    btn.style.cursor = 'not-allowed';
  });

  // Ocultar opciones y mostrar pantalla de espera
  document.getElementById("questionContainer").style.display = "none";
  document.getElementById("waitingScreen").style.display = "flex";

  console.log("Submitting answer...", { option, currentQuestionId });

  if (!currentQuestionId) {
    console.error("No active question ID, cannot submit");
    alert("Error: No hay pregunta activa. Espera a que se sincronice.");
    return;
  }

  const { token, header } = getCsrf();
  const headers = {
    "Content-Type": "application/json",
  };
  if (token) {
    headers[header] = token;
  }

  fetch(`/api/rooms/${roomId}/submit-answer`, {
    method: "POST",
    headers,
    body: JSON.stringify({
      playerId: playerId,
      questionId: currentQuestionId,
      selectedOption: option,
    }),
  })
    .then((response) => {
      console.log("Submit answer response:", response.status, response.ok);
      if (!response.ok) {
        console.error("Submission failed", response.status);
        return null;
      }
      return response.json();
    })
    .then((data) => {
      console.log("Submit answer data:", data);
      if (!data) return;
      const title = document.getElementById("resultTitle");
      const text = document.getElementById("resultText");
      const icon = document.getElementById("resultIcon");
      
      if (data.correct) {
        if (title) title.textContent = "¡Correcta!";
        if (text) text.textContent = `+${data.points} puntos (total: ${data.totalScore})`;
        if (icon) {
          icon.className = "fas fa-check-circle";
          icon.style.color = "#10B981";
        }
      } else {
        if (title) title.textContent = "Incorrecta";
        if (text) text.textContent = `0 puntos (total: ${data.totalScore})`;
        if (icon) {
          icon.className = "fas fa-times-circle";
          icon.style.color = "#EF4444";
        }
      }
    })
    .catch((error) => {
      console.error("Network error:", error);
      // Mostrar mensaje de error al usuario
      const title = document.getElementById("resultTitle");
      const text = document.getElementById("resultText");
      if (title) title.textContent = "Error al enviar";
      if (text) text.textContent = "Hubo un problema. Espera la siguiente pregunta...";
    });
}

// Consultar la siguiente pregunta
setInterval(() => {
  fetch(`/api/rooms/${roomId}/current-question`)
    .then((res) => res.json())
    .then((data) => {
      console.log("Polling response completo:", data);
      console.log("data.finished =", data.finished);
      console.log("Todas las propiedades:", Object.keys(data));
      
      if (data.id && data.id !== currentQuestionId) {
        // ¡Nueva pregunta!
        console.log("Nueva pregunta detectada, recargando...");
        window.location.reload();
      }
      if (data.finished) {
        console.log("¡Juego terminado! Redirigiendo a podio...", { roomId });
        window.location.href = `/rooms/${roomId}/podium?playerName=${encodeURIComponent(window.playerName)}`;
      }
    })
    .catch((error) => console.error("Error en polling:", error));
}, 2000);