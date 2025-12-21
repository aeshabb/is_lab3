<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
    <title>Организации</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <!-- WebSocket libraries -->
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/websocket.js"></script>
</head>
<body>
    <div class="container">
        <h1>Управление организациями</h1>
        
        <nav>
            <a href="${pageContext.request.contextPath}/">Главная</a>
            <a href="${pageContext.request.contextPath}/special">Специальные операции</a>
            <a href="${pageContext.request.contextPath}/import">Импорт организаций</a>
        </nav>

        <c:if test="${not empty message}">
            <div class="message success">${message}</div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="message error">${error}</div>
        </c:if>

        <div class="actions">
            <a href="${pageContext.request.contextPath}/create" class="btn btn-primary">Создать организацию</a>
            
            <form method="get" action="${pageContext.request.contextPath}/" class="filter-form">
                <input type="text" name="filter" placeholder="Фильтр по названию (полное совпадение)" value="${filter}">
                <select name="sort">
                    <option value="">Без сортировки</option>
                    <option value="name" ${sort == 'name' ? 'selected' : ''}>Название</option>
                    <option value="type" ${sort == 'type' ? 'selected' : ''}>Тип</option>
                    <option value="creationDate" ${sort == 'creationDate' ? 'selected' : ''}>Дата создания</option>
                    <option value="rating" ${sort == 'rating' ? 'selected' : ''}>Рейтинг</option>
                    <option value="annualTurnover" ${sort == 'annualTurnover' ? 'selected' : ''}>Годовой оборот</option>
                    <option value="employeesCount" ${sort == 'employeesCount' ? 'selected' : ''}>Количество сотрудников</option>
                </select>
                <select name="order">
                    <option value="asc" ${order == 'asc' ? 'selected' : ''}>По возрастанию</option>
                    <option value="desc" ${order == 'desc' ? 'selected' : ''}>По убыванию</option>
                </select>
                <select name="size">
                    <option value="5" ${pageSize == 5 ? 'selected' : ''}>5</option>
                    <option value="10" ${pageSize == 10 ? 'selected' : ''}>10</option>
                    <option value="20" ${pageSize == 20 ? 'selected' : ''}>20</option>
                </select>
                <button type="submit">Применить</button>
            </form>
        </div>

        <table class="data-table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Название</th>
                    <th>Координаты</th>
                    <th>Дата создания</th>
                    <th>Годовой оборот</th>
                    <th>Количество сотрудников</th>
                    <th>Рейтинг</th>
                    <th>Тип</th>
                    <th>Действия</th>
                </tr>
            </thead>
            <tbody>
                <c:choose>
                    <c:when test="${empty organizationsList}">
                        <tr>
                            <td colspan="9" style="text-align: center;">Нет организаций</td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="item" items="${organizationsList}">
                            <tr>
                                <td>${item.id}</td>
                                <td>${item.name}</td>
                                <td>
                                    <c:if test="${item.coordinates != null}">
                                        (${item.coordinates.x}, ${item.coordinates.y})
                                    </c:if>
                                    <c:if test="${item.coordinates == null}">—</c:if>
                                </td>
                                <td>${item.creationDate}</td>
                                <td>${item.annualTurnover}</td>
                                <td>${item.employeesCount}</td>
                                <td>${item.rating}</td>
                                <td>${item.type}</td>
                                <td>
                                    <a href="${pageContext.request.contextPath}/view/${item.id}">Просмотр</a> |
                                    <a href="${pageContext.request.contextPath}/edit/${item.id}">Редактировать</a> |
                                    <form method="post" action="${pageContext.request.contextPath}/delete/${item.id}" 
                                          style="display:inline;" 
                                          onsubmit="return confirm('Вы уверены?');">
                                        <button type="submit" class="btn-link">Удалить</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>

        <div class="pagination">
            <c:if test="${organizations.number > 0}">
                <a href="${pageContext.request.contextPath}/?page=${organizations.number - 1}&size=${pageSize}&sort=${sort}&order=${order}&filter=${filter}">Предыдущая</a>
            </c:if>
            <span>Страница ${organizations.number + 1} из ${organizations.totalPages}</span>
            <c:if test="${organizations.number < organizations.totalPages - 1}">
                <a href="${pageContext.request.contextPath}/?page=${organizations.number + 1}&size=${pageSize}&sort=${sort}&order=${order}&filter=${filter}">Следующая</a>
            </c:if>
        </div>
    </div>
</body>
</html>

