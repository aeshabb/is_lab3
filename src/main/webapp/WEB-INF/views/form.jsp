<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html>
<head>
    <title>${organization.id == null ? 'Создать' : 'Редактировать'} организацию</title>
    <link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/style.css">
    <!-- WebSocket libraries -->
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/websocket.js"></script>
</head>
<body>
    <div class="container">
        <h1>${organization.id == null ? 'Создать' : 'Редактировать'} организацию</h1>
        
        <nav>
            <a href="${pageContext.request.contextPath}/">Главная</a>
            <a href="${pageContext.request.contextPath}/special">Специальные операции</a>
        </nav>

        <c:if test="${not empty error}">
            <div class="message error">${error}</div>
        </c:if>

        <form:form method="post" modelAttribute="organization" action="${pageContext.request.contextPath}${organization.id == null ? '/create' : '/edit/' += organization.id}" accept-charset="UTF-8">
            <form:hidden path="id"/>
            <div class="form-group">
                <label>Название *</label>
                <form:input path="name" required="true"/>
                <form:errors path="name" cssClass="error"/>
            </div>

            <fieldset>
                <legend>Координаты *</legend>
                <c:if test="${not empty existingCoordinates}">
                    <div class="form-group">
                        <label>Или выберите существующие координаты:</label>
                        <select id="selectCoordinates" onchange="fillCoordinates(this.value)">
                            <option value="">-- Создать новые --</option>
                            <c:forEach var="coord" items="${existingCoordinates}">
                                <option value="${coord.id}" 
                                        data-x="${coord.x}" 
                                        data-y="${coord.y}">
                                    (${coord.x}, ${coord.y}) - ID: ${coord.id}
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                </c:if>
                <c:if test="${empty organization.coordinates}">
                    <jsp:useBean id="coordinates" class="org.itmo.lab3.model.Coordinates" scope="request"/>
                    <c:set target="${organization}" property="coordinates" value="${coordinates}"/>
                </c:if>
                <form:hidden path="coordinates.id" id="coordId"/>
                <div class="form-group">
                    <label>X *</label>
                    <form:input path="coordinates.x" type="number" id="coordX" required="true"/>
                    <form:errors path="coordinates.x" cssClass="error"/>
                </div>
                <div class="form-group">
                    <label>Y * (макс 323)</label>
                    <form:input path="coordinates.y" type="number" step="0.01" max="323" id="coordY" required="true"/>
                    <form:errors path="coordinates.y" cssClass="error"/>
                </div>
            </fieldset>

            <fieldset>
                <legend>Почтовый адрес *</legend>
                <c:if test="${not empty existingAddresses}">
                    <div class="form-group">
                        <label>Или выберите существующий адрес:</label>
                        <select id="selectPostalAddress" onchange="fillPostalAddress(this.value)">
                            <option value="">-- Создать новый --</option>
                            <c:forEach var="addr" items="${existingAddresses}">
                                <option value="${addr.id}" 
                                        data-street="${addr.street}" 
                                        data-zip="${addr.zipCode}">
                                    ${addr.street}, ${addr.zipCode} - ID: ${addr.id}
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                </c:if>
                <c:if test="${empty organization.postalAddress}">
                    <jsp:useBean id="postalAddress" class="org.itmo.lab3.model.Address" scope="request"/>
                    <c:set target="${organization}" property="postalAddress" value="${postalAddress}"/>
                </c:if>
                <div class="form-group">
                    <label>Улица * (макс 180 символов)</label>
                    <form:input path="postalAddress.street" maxlength="180" id="postalStreet" required="true"/>
                    <form:errors path="postalAddress.street" cssClass="error"/>
                </div>
                <div class="form-group">
                    <label>Почтовый индекс * (мин 7 символов)</label>
                    <form:input path="postalAddress.zipCode" minlength="7" id="postalZip" required="true"/>
                    <form:errors path="postalAddress.zipCode" cssClass="error"/>
                </div>
            </fieldset>

            <fieldset>
                <legend>Официальный адрес (необязательно)</legend>
                <c:if test="${not empty existingAddresses}">
                    <div class="form-group">
                        <label>Или выберите существующий адрес:</label>
                        <select id="selectOfficialAddress" onchange="fillOfficialAddress(this.value)">
                            <option value="">-- Создать новый --</option>
                            <c:forEach var="addr" items="${existingAddresses}">
                                <option value="${addr.id}" 
                                        data-street="${addr.street}" 
                                        data-zip="${addr.zipCode}">
                                    ${addr.street}, ${addr.zipCode} - ID: ${addr.id}
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                </c:if>
                <div class="form-group">
                    <label>Улица (макс 180 символов)</label>
                    <form:input path="officialAddress.street" maxlength="180" id="officialStreet"/>
                    <form:errors path="officialAddress.street" cssClass="error"/>
                    <small>Оставьте пустым, если не нужно указывать официальный адрес</small>
                </div>
                <div class="form-group">
                    <label>Почтовый индекс (мин 7 символов, если указан)</label>
                    <form:input path="officialAddress.zipCode" id="officialZip"/>
                    <form:errors path="officialAddress.zipCode" cssClass="error"/>
                    <small>Оставьте пустым, если не нужно указывать официальный адрес</small>
                </div>
            </fieldset>

            <div class="form-group">
                <label>Годовой оборот *</label>
                <form:input path="annualTurnover" type="number" min="1" required="true"/>
                <form:errors path="annualTurnover" cssClass="error"/>
            </div>

            <div class="form-group">
                <label>Количество сотрудников *</label>
                <form:input path="employeesCount" type="number" min="1" required="true"/>
                <form:errors path="employeesCount" cssClass="error"/>
            </div>

            <div class="form-group">
                <label>Рейтинг *</label>
                <form:input path="rating" type="number" step="0.01" min="0.01" required="true"/>
                <form:errors path="rating" cssClass="error"/>
            </div>

            <div class="form-group">
                <label>Тип организации *</label>
                <form:select path="type" required="true">
                    <form:option value="">Выберите тип</form:option>
                    <form:option value="COMMERCIAL">Коммерческая</form:option>
                    <form:option value="PUBLIC">Публичная</form:option>
                    <form:option value="OPEN_JOINT_STOCK_COMPANY">Открытое акционерное общество</form:option>
                </form:select>
                <form:errors path="type" cssClass="error"/>
            </div>

            <div class="form-actions">
                <button type="submit" class="btn btn-primary">Сохранить</button>
                <a href="${pageContext.request.contextPath}/" class="btn btn-secondary">Отмена</a>
            </div>
        </form:form>
    </div>

    <script>
        function fillCoordinates(optionValue) {
            if (optionValue) {
                const option = document.getElementById('selectCoordinates').options[document.getElementById('selectCoordinates').selectedIndex];
                document.getElementById('coordId').value = optionValue;
                document.getElementById('coordX').value = option.getAttribute('data-x');
                document.getElementById('coordY').value = option.getAttribute('data-y');
            } else {
                document.getElementById('coordId').value = '';
            }
        }

        function fillPostalAddress(optionValue) {
            if (optionValue) {
                const option = document.getElementById('selectPostalAddress').options[document.getElementById('selectPostalAddress').selectedIndex];
                document.getElementById('postalStreet').value = option.getAttribute('data-street');
                document.getElementById('postalZip').value = option.getAttribute('data-zip');
            }
        }

        function fillOfficialAddress(optionValue) {
            if (optionValue) {
                const option = document.getElementById('selectOfficialAddress').options[document.getElementById('selectOfficialAddress').selectedIndex];
                document.getElementById('officialStreet').value = option.getAttribute('data-street');
                document.getElementById('officialZip').value = option.getAttribute('data-zip');
            }
        }
    </script>
</body>
</html>

