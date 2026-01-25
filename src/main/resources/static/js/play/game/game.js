const roomId = window.roomId ?? 0;
const playerId = window.playerId ?? 0;
// Use safe initialization from model attribute, default to null if not present
let currentQuestionId = window.currentQuestionId ?? null;

console.log("Game initialized", { roomId, playerId, currentQuestionId });

const csrfToken = document.querySelector('input[name="_csrf"]').value;

function submitAnswer(option) {
  console.log("Submitting answer...", { option, currentQuestionId });

  if (!currentQuestionId) {
    console.error("No active question ID, cannot submit");
    alert("Error: No hay pregunta activa. Espera a que se sincronice.");
    return;
  }

  document.getElementById("gameScreen").style.display = "none";
  document.getElementById("waitingScreen").style.display = "flex";

  fetch(`/api/rooms/${roomId}/submit-answer`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "X-CSRF-TOKEN": csrfToken,
    },
    body: JSON.stringify({
      playerId: playerId,
      questionId: currentQuestionId,
      selectedOption: option,
    }),
  })
    .then((response) => {
      if (!response.ok) {
        console.error("Submission failed", response.status);
        // Optionally show error or revert UI
      } else {
        console.log("Answer submitted successfully");
      }
    })
    .catch((error) => console.error("Network error:", error));
}

// Poll for next question
setInterval(() => {
  fetch(`/api/rooms/${roomId}/current-question`)
    .then((res) => res.json())
    .then((data) => {
      if (data.id && data.id !== currentQuestionId) {
        // New question!
        window.location.reload();
      }
      if (data.finished) {
        // End game logic if needed (e.g. redirect to podium)
        // For now just reload which might show finished state or 404
      }
    });
}, 2000);
