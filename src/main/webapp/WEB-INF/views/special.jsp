<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Специальные операции</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <!-- WebSocket libraries -->
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/websocket.js"></script>
</head>
<body>
    <div class="container">
        <h1>Специальные операции</h1>
        
        <nav>
            <a href="${pageContext.request.contextPath}/">Главная</a>
            <a href="${pageContext.request.contextPath}/special">Специальные операции</a>
        </nav>

        <c:if test="${not empty message}">
            <div class="message success">${message}</div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="message error">${error}</div>
        </c:if>

        <div class="special-operations">
            <section class="operation-section">
                <h2>Удалить все объекты, значение поля rating которого эквивалентно заданному</h2>
                <form method="post" action="${pageContext.request.contextPath}/special/delete-by-rating">
                    <input type="number" name="rating" step="0.01" placeholder="Рейтинг" required>
                    <button type="submit" class="btn btn-danger">Удалить</button>
                </form>
            </section>

            <section class="operation-section">
                <h2>Вернуть массив объектов, значение поля name которых содержит заданную подстроку</h2>
                <form method="post" action="${pageContext.request.contextPath}/special/find-by-name-substring">
                    <input type="text" name="nameSubstring" placeholder="Подстрока в названии" required>
                    <button type="submit" class="btn btn-primary">Найти</button>
                </form>
            </section>

            <section class="operation-section">
                <h2>Вернуть массив уникальных значений поля postalAddress по всем объектам</h2>
                <form method="get" action="${pageContext.request.contextPath}/special/get-unique-postal-addresses">
                    <button type="submit" class="btn btn-primary">Получить уникальные адреса</button>
                </form>
            </section>

            <section class="operation-section">
                <h2>Добавить нового сотрудника в организацию с указанным id</h2>
                <form method="post" action="${pageContext.request.contextPath}/special/add-employee">
                    <input type="number" name="organizationId" placeholder="ID организации" required>
                    <button type="submit" class="btn btn-primary">Добавить сотрудника</button>
                </form>
            </section>

            <section class="operation-section">
                <h2>Реализовать поглощение одной организацией другой</h2>
                <form method="post" action="${pageContext.request.contextPath}/special/merge-organizations">
                    <input type="number" name="targetOrganizationId" placeholder="ID целевой организации (поглощающей)" required>
                    <input type="number" name="sourceOrganizationId" placeholder="ID поглощаемой организации" required>
                    <button type="submit" class="btn btn-primary">Поглотить</button>
                </form>
            </section>
        </div>

        <c:if test="${not empty result}">
            <div class="results">
                <h2>Результаты поиска<c:if test="${not empty searchValue}"> (по подстроке: "${searchValue}")</c:if>:</h2>
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Название</th>
                            <th>Рейтинг</th>
                            <th>Тип</th>
                            <th>Годовой оборот</th>
                            <th>Количество сотрудников</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="item" items="${result}">
                            <tr>
                                <td>${item.id}</td>
                                <td>${item.name}</td>
                                <td>${item.rating}</td>
                                <td>${item.type}</td>
                                <td>${item.annualTurnover}</td>
                                <td>${item.employeesCount}</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:if>

        <c:if test="${not empty addresses}">
            <div class="results">
                <h2>Уникальные почтовые адреса:</h2>
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Улица</th>
                            <th>Почтовый индекс</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="address" items="${addresses}">
                            <tr>
                                <td>${address.id}</td>
                                <td>${address.street}</td>
                                <td>${address.zipCode}</td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:if>
    </div>
</body>
</html>
