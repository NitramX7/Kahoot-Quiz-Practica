// Lobby page functionality
// Poll the server to get the updated list of players

// Room data (will be set by Thymeleaf inline script)
let roomPin = '000000';
let roomId = 0;

// Track existing players to avoid re-adding them
const existingPlayers = new Set();

/**
 * Refresh the players list from the server
 */
function refreshPlayers() {
    fetch(`/api/rooms/${roomId}/players`)
        .then(response => response.json())
        .then(players => {
            const container = document.getElementById('playersContainer');
            const countElem = document.getElementById('playerCount');

            countElem.innerText = players.length;

            if (players.length === 0) {
                container.innerHTML = `
                    <div style="grid-column: 1/-1; text-align: center; padding: 40px; color: #94A3B8; border: 2px dashed #E2E8F0; border-radius: 20px;">
                        <i class="fas fa-spinner fa-spin" style="font-size: 32px; margin-bottom: 12px;"></i>
                        <p>Esperando a que entren los jugadores...</p>
                    </div>`;
                existingPlayers.clear();
                return;
            }

            // Remove the "waiting" message if it exists
            const waitingMsg = container.querySelector('div[style*="grid-column"]');
            if (waitingMsg) {
                waitingMsg.remove();
            }

            // Add only new players
            players.forEach(player => {
                if (!existingPlayers.has(player.id)) {
                    existingPlayers.add(player.id);

                    const playerBadge = document.createElement('div');
                    playerBadge.className = 'player-badge';
                    playerBadge.setAttribute('data-player-id', player.id);
                    playerBadge.style.opacity = '0';
                    playerBadge.style.transform = 'scale(0.8)';
                    playerBadge.style.transition = 'all 0.3s ease';

                    playerBadge.innerHTML = `
                        <i class="fas fa-user-circle" style="display: block; font-size: 24px; margin-bottom: 8px; color: #2563EB;"></i>
                        <span>${player.name}</span>
                    `;

                    container.appendChild(playerBadge);

                    // Trigger animation
                    setTimeout(() => {
                        playerBadge.style.opacity = '1';
                        playerBadge.style.transform = 'scale(1)';
                    }, 10);
                }
            });

            // Remove players that left (optional - if you want to handle disconnections)
            const currentPlayerIds = new Set(players.map(p => p.id));
            const playerBadges = container.querySelectorAll('.player-badge');
            playerBadges.forEach(badge => {
                const playerId = parseInt(badge.getAttribute('data-player-id'));
                if (!currentPlayerIds.has(playerId)) {
                    badge.style.opacity = '0';
                    badge.style.transform = 'scale(0.8)';
                    setTimeout(() => badge.remove(), 300);
                    existingPlayers.delete(playerId);
                }
            });
        })
        .catch(error => {
            console.error('Error refreshing players:', error);
        });
}

// Poll every 2 seconds
setInterval(refreshPlayers, 2000);
