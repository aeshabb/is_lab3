let stompClient = null;

function connect() {
    const socket = new SockJS(getContextPath() + '/ws');
    stompClient = Stomp.over(socket);
    
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/organizations', function (message) {
            const notification = JSON.parse(message.body);
            handleNotification(notification);
        });
    }, function(error) {
        console.error('STOMP error: ' + error);
        // Переподключение через 5 секунд
        setTimeout(connect, 5000);
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    console.log("Disconnected");
}

function handleNotification(notification) {
    console.log('Received notification:', notification);
    
    const action = notification.action;
    const orgId = notification.organizationId;
    
    if (action === 'created' || action === 'updated' || action === 'deleted') {
        // Перезагрузить страницу для обновления данных
        reloadPage();
    }
}

function reloadPage() {
    // Сохраняем текущие параметры URL
    const currentUrl = window.location.href;
    
    // Перезагружаем страницу без добавления в историю
    window.location.replace(currentUrl);
}

function getContextPath() {
    return window.location.pathname.substring(0, window.location.pathname.indexOf("/", 2));
}

// Автоматическое подключение при загрузке страницы
window.addEventListener('load', function() {
    connect();
});

// Отключение при закрытии страницы
window.addEventListener('beforeunload', function() {
    disconnect();
});
