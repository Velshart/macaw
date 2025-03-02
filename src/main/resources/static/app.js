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
        console.error("roomId is not set! Cannot subscribe.");
        alert("roomId is not set! Cannot subscribe.");
        return;
    }

    setConnected(true);

    stompClient.subscribe(`/topic/${roomId}`, (message) => {
        showMessage(JSON.parse(message.body));
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
    if(!recipient) {
        alert('Please fill in missing fields.');
        return;
    }

    roomId = `chat-room-${[currentUser, recipient].sort().join('-')}`;
    alert("Connecting to room: " + roomId);

    stompClient.activate();
}

function disconnect() {
    stompClient.deactivate().then(() => {
        setConnected(false);
        alert("Successfully disconnected from the room.")
    });
}

function sendMessage() {
    const messageContent = $("#message").val().trim();

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

    //showMessage(chatMessage);
    $("#message").val("");
}

function showMessage(message) {
    $("#chatRoom").append(`<div class="p-2 ${message.sender === currentUser ? 'bg-primary text-white' : 'bg-light'} rounded my-1">
        <strong>${message.sender}:</strong> ${message.content}
    </div>`);
}

$(function () {
    $("#connect").click(() => connect());
    $("#disconnect").click(() => disconnect());
    $("#send").click(() => sendMessage());
});