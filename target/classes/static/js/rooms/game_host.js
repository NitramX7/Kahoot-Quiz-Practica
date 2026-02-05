
const timerFill = document.getElementById('timerFill');
const timerDisplay = document.querySelector('#timerDisplay span');
const nextBtn = document.getElementById('nextBtn');

if (timerFill && timerDisplay && nextBtn) {
    const timer = setInterval(() => {
        timeLeft--;
        timerDisplay.textContent = timeLeft;
        timerFill.style.width = (timeLeft / timeLimit * 100) + '%';

        if (timeLeft <= 0) {
            clearInterval(timer);
            nextBtn.style.display = 'inline-flex';
        }
    }, 1000);
}

function nextQuestion() {
    fetch(`/api/rooms/${roomId}/next-question`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]')?.value
        }
    })
        .then(res => res.json())
        .then(data => {
            if (data.finished) {
                window.location.href = `/rooms/${roomId}/podium`;
            } else {
                window.location.reload();
            }
        });
}

// Poll for updates (answers count / next question / finish)
setInterval(() => {
    fetch(`/api/rooms/${roomId}/current-question`)
        .then(res => res.json())
        .then(data => {
            if (data.finished) {
                window.location.href = `/rooms/${roomId}/podium`;
                return;
            }
            if (data.id && data.id !== currentQuestionId) {
                window.location.reload();
                return;
            }
            if (!currentQuestionId && data.id) {
                window.location.reload();
                return;
            }
            if (typeof data.answersCount === 'number') {
                const answersCountEl = document.getElementById('answersCount');
                if (answersCountEl) {
                    answersCountEl.textContent = data.answersCount;
                }
            }
        });
}, 1000);

// CSRF token helper for fetch
const csrfToken = document.querySelector('input[name="_csrf"]').value;
const originalFetch = window.fetch;
window.fetch = function () {
    let args = Array.from(arguments);
    if (args[1] && args[1].method === 'POST') {
        args[1].headers = {
            ...args[1].headers,
            'X-CSRF-TOKEN': csrfToken
        };
    }
    return originalFetch.apply(this, args);
};
