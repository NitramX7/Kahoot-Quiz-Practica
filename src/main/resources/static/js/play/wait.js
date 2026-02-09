// Wait page functionality
// Poll the server to check if the game has started

// Get room PIN from the page (will be set by Thymeleaf)
let roomPin = '0000';

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function () {
    // Extract room PIN from the page if available
    const roomPinElement = document.querySelector('[data-room-pin]');
    if (roomPinElement) {
        roomPin = roomPinElement.getAttribute('data-room-pin');
    }
});

/**
 * Check the room status to see if the game has started
 */
function checkStatus() {
    fetch('/api/rooms/' + roomPin + '/status')
        .then(response => response.json())
        .then(data => {
            if (data.state === 'RUNNING') {
                window.location.href = '/play/game';
            }
        })
        .catch(err => console.error('Error checking room status', err));
}

// Poll every 1 second for faster response
setInterval(checkStatus, 1000);
