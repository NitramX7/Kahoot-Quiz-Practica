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
  console.log("Submitting answer...", { option, currentQuestionId });

  if (!currentQuestionId) {
    console.error("No active question ID, cannot submit");
    alert("Error: No hay pregunta activa. Espera a que se sincronice.");
    return;
  }

  document.getElementById("gameScreen").style.display = "none";
  document.getElementById("waitingScreen").style.display = "flex";

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
      if (!response.ok) {
        console.error("Submission failed", response.status);
        return null;
      }
      return response.json();
    })
    .then((data) => {
      if (!data) return;
      const title = document.getElementById("resultTitle");
      const text = document.getElementById("resultText");
      if (data.correct) {
        if (title) title.textContent = "¡Correcta!";
        if (text) text.textContent = `+${data.points} puntos (total: ${data.totalScore})`;
      } else {
        if (title) title.textContent = "Incorrecta";
        if (text) text.textContent = `0 puntos (total: ${data.totalScore})`;
      }
    })
    .catch((error) => console.error("Network error:", error));
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
        window.location.href = `/rooms/${roomId}/podium`;
      }
    })
    .catch((error) => console.error("Error en polling:", error));
}, 2000);