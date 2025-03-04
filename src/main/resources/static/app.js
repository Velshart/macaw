const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8081/ws'
});

let currentUser = "";
let roomId = null;

fetchCurrentUserUsername();

function fetchCurrentUserUsername() {
    fetch('/current-user-username')
        .then(response => response.json())
        .then(data => currentUser = data.username)
        .catch(error => alert("An error occurred while fetching user data: " + error));
}

stompClient.onConnect = () => {

    if (!roomId) {
        alert("Cannot subscribe: roomId is not set.");
        return;
    }

    setConnected(true);

    const joinMessage = {
        sender: currentUser,
        content: `${currentUser} joined the chat.`,
        type: "INFO"
    }

    stompClient.publish({
        destination: `/app/chat/${roomId}`,
        body: JSON.stringify(joinMessage)
    });

    stompClient.subscribe(`/topic/${roomId}`, (message) => {
        const receivedMessage = JSON.parse(message.body);

        if (receivedMessage.type === "INFO") {
            chatInfo(receivedMessage.content);
        } else {
            showMessage(receivedMessage);
        }
    });
    alert('Connected to room ' + roomId);
};
stompClient.onWebSocketError = (error) => {
    alert('WebSocket error: ' + error);
};

stompClient.onStompError = (frame) => {
    alert('STOMP error:' + frame.headers['message'] + frame.body);
};

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    $("#chatRoom").toggleClass("d-none", !connected);
    $("#chatControls").toggleClass("d-none", !connected);
    if (!connected) roomId = null;
}

function connect() {
    const recipient = $("#recipient").val().trim();
    if (!recipient) {
        alert('Please fill in missing fields.');
        return;
    }

    roomId = `chat-room-${[currentUser, recipient].sort().join('-')}`;
    alert("Connecting to room: " + roomId);

    stompClient.activate();
}

function disconnect() {
    const leaveMessage = {
        sender: currentUser,
        content: `${currentUser} left the chat.`,
        type: "INFO"
    }

    stompClient.publish({
        destination: `/app/chat/${roomId}`,
        body: JSON.stringify(leaveMessage)
    });

    stompClient.deactivate().then(() => {
        setConnected(false);
        alert("Successfully disconnected from the room.")
    });
}

function sendMessage() {
    const $messageInput = $("#message");
    const messageContent = $messageInput.val().trim();

    if (!messageContent) {
        alert('Please enter message you want to send.');
        return;
    }

    const chatMessage = {
        sender: currentUser,
        content: messageContent
    };

    stompClient.publish({
        destination: `/app/chat/${roomId}`,
        body: JSON.stringify(chatMessage)
    });
    $messageInput.val("");
}

function showMessage(message) {
    $("#chatRoom").append(`<div class="p-2 ${message.sender === currentUser ? 'bg-primary text-white' : 'bg-light'} rounded my-1">
        <strong>${message.sender}:</strong> ${message.content}
    </div>`);
}

function chatInfo(content) {
    $("#chatRoom").append(`<div class="p-2 text-muted">${content}</div>`);
}

$(function () {
    $("#connect").click(() => connect());
    $("#disconnect").click(() => disconnect());
    $("#send").click(() => sendMessage());
});