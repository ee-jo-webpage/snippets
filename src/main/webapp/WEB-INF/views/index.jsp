<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>테스트 사용자 선택</title>
    <style>
        .user-card {
            display: inline-block;
            margin: 20px;
            padding: 20px;
            border: 1px solid #ddd;
            border-radius: 10px;
            cursor: pointer;
            transition: background-color 0.2s;
        }
        .user-card:hover {
            background-color: #f0f0f0;
        }
        .current-user {
            background-color: #e3f2fd;
            border-color: #2196f3;
        }
    </style>
</head>
<body>
<h1>테스트용 사용자 선택</h1>
<p>현재 세션 사용자 ID: <strong>${sessionScope.userId}</strong></p>

<div>
    <h3>사용자 전환</h3>

    <a href="/switch-user/1" class="user-card ${sessionScope.userId == 1 ? 'current-user' : ''}">
        <div>
            <h4>사용자 1</h4>
            <p>관리자</p>
        </div>
    </a>

    <a href="/switch-user/2" class="user-card ${sessionScope.userId == 2 ? 'current-user' : ''}">
        <div>
            <h4>사용자 2</h4>
            <p>일반 사용자</p>
        </div>
    </a>

    <a href="/switch-user/3" class="user-card ${sessionScope.userId == 3 ? 'current-user' : ''}">
        <div>
            <h4>사용자 3</h4>
            <p>테스터</p>
        </div>
    </a>
</div>

<div style="margin-top: 40px;">
    <a href="/color/all-colors" class="btn btn-primary">색상 관리 페이지로 이동</a>
</div>
</body>
</html>