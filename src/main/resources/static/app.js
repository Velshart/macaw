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
        .catch(error => console.error("An error occurred while fetching user data: ", error));
}

async function isUserWithGivenUsernameRegistered(username) {
    try {
        const response = await fetch('/all-users-usernames');
        const usernames = await response.json();

        return new Set(usernames).has(username);
    } catch (error) {
        console.error("Error during fetching data: ", error)
        return false;
    }
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

    publishMessage(joinMessage);

    stompClient.subscribe(`/topic/${roomId}`, (message) => {
        const receivedMessage = JSON.parse(message.body);

        if (receivedMessage.type === "INFO") {
            chatInfo(receivedMessage.content);
        } else {
            showMessage(receivedMessage);
        }
    });
};
stompClient.onWebSocketError = (error) => {
    console.error('WebSocket error: ', error);
};

stompClient.onStompError = (frame) => {
    console.error('STOMP error: ', frame.headers['message'] + frame.body);
};

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    $("#message").prop("disabled", !connected);
    $("#chatRoom").toggleClass("d-none", !connected);
    $("#chatControls").toggleClass("d-none", !connected);
    if (!connected) roomId = null;
}

function connect() {
    const recipient = $("#recipient").val().trim();
    if (!recipient) {
        alert('Please fill in missing fields.');
        return;
    } else if (currentUser === recipient) {
        alert("The recipient's name must be different from the user's name.")
        return;
    }
    (async () => {
        const recipientExists = await isUserWithGivenUsernameRegistered(recipient)
        if (recipientExists) {
            roomId = `chat-room-${[currentUser, recipient].sort().join('-')}`;
            stompClient.activate();
        } else {
            alert("Recipient with provided username does not exist.")
        }
    })();
}

function disconnect() {
    const leaveMessage = {
        sender: currentUser,
        content: `${currentUser} left the chat.`,
        type: "INFO"
    }

    publishMessage(leaveMessage);

    stompClient.deactivate().then(() => {
        setConnected(false);
    });

    $("#recipient").val("");
}

function sendMessage() {
    const $messageInput = $("#message");
    const messageContent = $messageInput.val().trim();

    if (!messageContent) {
        return;
    }

    const chatMessage = {
        sender: currentUser,
        content: messageContent,
        type: "CHAT"
    };

    publishMessage(chatMessage);
    $messageInput.val("");
}

function publishMessage(message) {
    stompClient.publish({
        destination: `/app/chat/${roomId}`,
        body: JSON.stringify(message)
    });
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