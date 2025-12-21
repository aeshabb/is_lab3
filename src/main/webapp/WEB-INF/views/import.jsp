<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <title>Импорт организаций</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/style.css">
    <style>
        .import-container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
        }
        .upload-form {
            background: #f5f5f5;
            padding: 20px;
            border-radius: 5px;
            margin-bottom: 30px;
        }
        .form-group {
            margin-bottom: 15px;
        }
        .form-group label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
        }
        .form-group input[type="text"],
        .form-group input[type="file"] {
            width: 100%;
            padding: 8px;
            border: 1px solid #ddd;
            border-radius: 3px;
        }
        .btn-upload {
            background-color: #4CAF50;
            color: white;
            padding: 10px 20px;
            border: none;
            border-radius: 3px;
            cursor: pointer;
            font-size: 16px;
        }
        .btn-upload:hover {
            background-color: #45a049;
        }
        .alert {
            padding: 15px;
            margin-bottom: 20px;
            border-radius: 3px;
        }
        .alert-success {
            background-color: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }
        .alert-error {
            background-color: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }
        .history-table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }
        .history-table th,
        .history-table td {
            padding: 12px;
            text-align: left;
            border-bottom: 1px solid #ddd;
        }
        .history-table th {
            background-color: #4CAF50;
            color: white;
        }
        .history-table tr:hover {
            background-color: #f5f5f5;
        }
        .status-success {
            color: green;
            font-weight: bold;
        }
        .status-failed {
            color: red;
            font-weight: bold;
        }
        .back-link {
            display: inline-block;
            margin-bottom: 20px;
            color: #4CAF50;
            text-decoration: none;
        }
        .back-link:hover {
            text-decoration: underline;
        }
        .download-link {
            color: #2196F3;
            text-decoration: none;
            font-weight: bold;
        }
        .download-link:hover {
            text-decoration: underline;
        }
        .format-example {
            background: #fff;
            padding: 15px;
            border: 1px solid #ddd;
            border-radius: 3px;
            margin-top: 10px;
            font-family: monospace;
            font-size: 12px;
            overflow-x: auto;
        }
    </style>
</head>
<body>
    <div class="import-container">
        <a href="${pageContext.request.contextPath}/" class="back-link">← Вернуться к списку организаций</a>
        
        <h1>Импорт организаций</h1>

        <c:if test="${not empty success}">
            <div class="alert alert-success">${success}</div>
        </c:if>

        <c:if test="${not empty error}">
            <div class="alert alert-error">${error}</div>
        </c:if>

        <div class="upload-form">
            <h2>Загрузка файла</h2>
            <form action="${pageContext.request.contextPath}/import/upload" method="post" enctype="multipart/form-data">
                <div class="form-group">
                    <label for="username">Имя пользователя:</label>
                    <input type="text" id="username" name="username" required>
                </div>
                <div class="form-group">
                    <label for="file">Выберите JSON файл:</label>
                    <input type="file" id="file" name="file" accept=".json" required>
                </div>
                <button type="submit" class="btn-upload">Загрузить и импортировать</button>
            </form>

            <h3>Формат JSON файла:</h3>
            <div class="format-example">
[
  {
    "name": "ООО Рога и Копыта",
    "coordinates": {
      "x": 100,
      "y": 200.5
    },
    "officialAddress": {
      "street": "Ленина 1",
      "zipCode": "1234567"
    },
    "annualTurnover": 1000000,
    "employeesCount": 50,
    "rating": 4.5,
    "type": "COMMERCIAL",
    "postalAddress": {
      "street": "Пушкина 2",
      "zipCode": "7654321"
    }
  }
]
            </div>
        </div>

        <h2>История импорта</h2>
        <c:choose>
            <c:when test="${empty importHistory}">
                <p>История импорта пуста</p>
            </c:when>
            <c:otherwise>
                <table class="history-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Дата и время</th>
                            <th>Пользователь</th>
                            <th>Статус</th>
                            <th>Импортировано</th>
                            <th>Файл</th>
                            <th>Ошибка</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach items="${importHistory}" var="history">
                            <tr>
                                <td>${history.id}</td>
                                <td>
                                    ${history.timestamp}
                                </td>
                                <td>${history.username}</td>
                                <td class="${history.status == 'SUCCESS' ? 'status-success' : 'status-failed'}">
                                    ${history.status}
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${history.status == 'SUCCESS'}">
                                            ${history.importedCount}
                                        </c:when>
                                        <c:otherwise>
                                            -
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <c:if test="${not empty history.fileObjectName}">
                                        <a href="${pageContext.request.contextPath}/import/download/${history.fileObjectName}" 
                                           class="download-link" title="Скачать файл импорта">📥 Скачать</a>
                                    </c:if>
                                    <c:if test="${empty history.fileObjectName}">
                                        -
                                    </c:if>
                                </td>
                                <td>
                                    <c:if test="${not empty history.errorMessage}">
                                        ${history.errorMessage}
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:otherwise>
        </c:choose>
    </div>
</body>
</html>
