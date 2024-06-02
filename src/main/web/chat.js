let ws;
let username;

function login() {
    username = document.getElementById('usernameInput').value;
    if (username) {
        document.getElementById('loginPage').style.display = 'none';
        document.getElementById('chatPage').style.display = 'flex';

        ws = new WebSocket('ws://localhost:8887');

        ws.onopen = () => {
            ws.send(`/login ${username}`);
        };

        ws.onmessage = (event) => {
            const message = JSON.parse(event.data);
            const chatBox = document.getElementById('chatBox');
            const messageElement = document.createElement('div');
            messageElement.textContent = `${message.sender}: ${message.content}`;
            chatBox.appendChild(messageElement);
        };

        ws.onerror = (error) => {
            console.error('WebSocket error:', error);
        };

        ws.onclose = () => {
            console.log('WebSocket connection closed');
        };
    }
}

function sendMessage() {
    const messageInput = document.getElementById('messageInput');
    const message = messageInput.value;
    if (message) {
        ws.send(message);
        messageInput.value = '';
    }
}
