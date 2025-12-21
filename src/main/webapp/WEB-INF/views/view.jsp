<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
    <title>Просмотр организации</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <!-- WebSocket libraries -->
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/websocket.js"></script>
</head>
<body>
    <div class="container">
        <h1>Информация об организации</h1>
        
        <nav>
            <a href="${pageContext.request.contextPath}/">Главная</a>
            <a href="${pageContext.request.contextPath}/special">Специальные операции</a>
        </nav>

        <div class="organization-details">
            <h2>${organization.name}</h2>
            
            <dl>
                <dt>ID:</dt>
                <dd>${organization.id}</dd>
                
                <dt>Координаты:</dt>
                <dd>X: ${organization.coordinates.x}, Y: ${organization.coordinates.y}</dd>
                
                <dt>Дата создания:</dt>
                <dd>${organization.creationDate}</dd>
                
                <dt>Годовой оборот:</dt>
                <dd>${organization.annualTurnover}</dd>
                
                <dt>Количество сотрудников:</dt>
                <dd>${organization.employeesCount}</dd>
                
                <dt>Рейтинг:</dt>
                <dd>${organization.rating}</dd>
                
                <dt>Тип:</dt>
                <dd>${organization.type}</dd>
                
                <dt>Почтовый адрес:</dt>
                <dd>${organization.postalAddress.street}, ${organization.postalAddress.zipCode}</dd>
                
                <c:if test="${not empty organization.officialAddress}">
                    <dt>Официальный адрес:</dt>
                    <dd>${organization.officialAddress.street}, ${organization.officialAddress.zipCode}</dd>
                </c:if>
            </dl>

            <h3>Связанные объекты</h3>
            <h4>Координаты (ID: ${organization.coordinates.id})</h4>
            <dl>
                <dt>X:</dt>
                <dd>${organization.coordinates.x}</dd>
                <dt>Y:</dt>
                <dd>${organization.coordinates.y}</dd>
            </dl>

            <h4>Почтовый адрес (ID: ${organization.postalAddress.id})</h4>
            <dl>
                <dt>Улица:</dt>
                <dd>${organization.postalAddress.street}</dd>
                <dt>Почтовый индекс:</dt>
                <dd>${organization.postalAddress.zipCode}</dd>
            </dl>

            <c:if test="${not empty organization.officialAddress}">
                <h4>Официальный адрес (ID: ${organization.officialAddress.id})</h4>
                <dl>
                    <dt>Улица:</dt>
                    <dd>${organization.officialAddress.street}</dd>
                    <dt>Почтовый индекс:</dt>
                    <dd>${organization.officialAddress.zipCode}</dd>
                </dl>
            </c:if>

            <div class="actions">
                <a href="${pageContext.request.contextPath}/edit/${organization.id}" class="btn btn-primary">Редактировать</a>
                <a href="${pageContext.request.contextPath}/" class="btn btn-secondary">Назад</a>
            </div>
        </div>
    </div>
</body>
</html>

