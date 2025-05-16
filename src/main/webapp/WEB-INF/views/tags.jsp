<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>태그 관리</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Arial', sans-serif;
            background-color: #f5f5f5;
            padding: 20px;
        }

        .container {
            max-width: 800px;
            margin: 0 auto;
            background-color: white;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            padding: 30px;
        }

        h1 {
            text-align: center;
            color: #333;
            margin-bottom: 30px;
        }

        .alert {
            padding: 10px;
            margin-bottom: 20px;
            border-radius: 4px;
        }

        .alert-success {
            background-color: #dff0d8;
            border: 1px solid #d6e9c6;
            color: #3c763d;
        }

        .alert-error {
            background-color: #f2dede;
            border: 1px solid #ebccd1;
            color: #a94442;
        }

        .tag-form {
            background-color: #f9f9f9;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 30px;
        }

        .form-group {
            display: flex;
            gap: 10px;
            align-items: center;
        }

        .form-control {
            flex: 1;
            padding: 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 16px;
        }

        .form-control:focus {
            outline: none;
            border-color: #007bff;
            box-shadow: 0 0 0 2px rgba(0,123,255,0.25);
        }

        .btn {
            padding: 10px 15px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            text-decoration: none;
            font-size: 14px;
            transition: background-color 0.3s;
        }

        .btn-primary {
            background-color: #007bff;
            color: white;
        }

        .btn-primary:hover {
            background-color: #0056b3;
        }

        .btn-warning {
            background-color: #ffc107;
            color: #212529;
        }

        .btn-warning:hover {
            background-color: #e0a800;
        }

        .btn-danger {
            background-color: #dc3545;
            color: white;
        }

        .btn-danger:hover {
            background-color: #c82333;
        }

        .btn-sm {
            padding: 5px 10px;
            font-size: 12px;
        }

        .tag-list {
            min-height: 200px;
        }

        .tag-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 15px;
            border: 1px solid #e0e0e0;
            border-radius: 8px;
            margin-bottom: 10px;
            background-color: white;
        }

        .tag-item:hover {
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }

        .tag-name {
            font-size: 18px;
            font-weight: 500;
            color: #333;
        }

        .tag-actions {
            display: flex;
            gap: 10px;
        }

        .empty-state {
            text-align: center;
            color: #666;
            font-style: italic;
            padding: 40px;
        }

        .tag-stats {
            margin-top: 30px;
            padding: 15px;
            background-color: #f8f9fa;
            border-radius: 8px;
            text-align: center;
            color: #666;
        }

        /* 수정 폼 스타일 */
        .edit-form {
            display: flex;
            gap: 10px;
            width: 100%;
        }

        .edit-form .form-control {
            flex: 1;
        }
    </style>
</head>
<body>
<div class="container">
    <h1>태그 관리</h1>

    <!-- 메시지 표시 -->
    <c:if test="${not empty message}">
        <div class="alert alert-success">${message}</div>
    </c:if>

    <c:if test="${not empty error}">
        <div class="alert alert-error">${error}</div>
    </c:if>

    <!-- 태그 추가 폼 -->
    <div class="tag-form">
        <form action="${pageContext.request.contextPath}/api/tags" method="post">
            <div class="form-group">
                <input type="text" name="name" class="form-control"
                       placeholder="새 태그 이름을 입력하세요" required>
                <button type="submit" class="btn btn-primary">태그 추가</button>
            </div>
        </form>
    </div>

    <!-- 태그 목록 -->
    <div class="tag-list">
        <c:choose>
            <c:when test="${not empty tags}">
                <c:forEach var="tag" items="${tags}">
                    <div class="tag-item">
                        <c:choose>
                            <c:when test="${param.editId == tag.tagId}">
                                <!-- 수정 모드 -->
                                <form action="${pageContext.request.contextPath}/api/tags/${tag.tagId}"
                                      method="post" class="edit-form">
                                    <input type="hidden" name="_method" value="PUT">
                                    <input type="text" name="name" value="${tag.name}"
                                           class="form-control" required>
                                    <button type="submit" class="btn btn-primary btn-sm">저장</button>
                                    <a href="${pageContext.request.contextPath}/tags"
                                       class="btn btn-warning btn-sm">취소</a>
                                </form>
                            </c:when>
                            <c:otherwise>
                                <!-- 일반 표시 모드 -->
                                <span class="tag-name">#${tag.name}</span>
                                <div class="tag-actions">
                                    <a href="${pageContext.request.contextPath}/tags?editId=${tag.tagId}"
                                       class="btn btn-warning btn-sm">수정</a>
                                    <form action="${pageContext.request.contextPath}/api/tags/${tag.tagId}"
                                          method="post" style="display: inline;"
                                          onsubmit="return confirm('정말 삭제하시겠습니까?')">
                                        <input type="hidden" name="_method" value="DELETE">
                                        <button type="submit" class="btn btn-danger btn-sm">삭제</button>
                                    </form>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="empty-state">
                    태그가 없습니다. 새 태그를 추가해보세요!
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- 태그 통계 -->
    <div class="tag-stats">
        <p>총 <strong>${tags.size()}</strong>개의 태그</p>
    </div>
</div>

<script>
    // 폼 제출 시 로딩 상태 처리
    document.addEventListener('DOMContentLoaded', function() {
        const forms = document.querySelectorAll('form');
        forms.forEach(form => {
            form.addEventListener('submit', function() {
                const submitBtn = form.querySelector('button[type="submit"]');
                if (submitBtn) {
                    submitBtn.disabled = true;
                    submitBtn.textContent = '처리중...';
                }
            });
        });
    });
</script>
</body>
</html>