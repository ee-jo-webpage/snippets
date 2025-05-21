<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>스니펫 태그 관리</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Arial', 'Malgun Gothic', sans-serif;
            background-color: #f5f7fa;
            color: #333;
        }

        .container {
            max-width: 1400px;
            margin: 0 auto;
            padding: 20px;
        }

        h1 {
            color: #2c3e50;
            margin-bottom: 30px;
            text-align: center;
            font-size: 2.5em;
        }

        .search-bar {
            background: white;
            border-radius: 10px;
            padding: 20px;
            margin-bottom: 20px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        }

        .search-input {
            width: 100%;
            padding: 12px 20px;
            border: 2px solid #e0e6ed;
            border-radius: 8px;
            font-size: 16px;
            outline: none;
        }

        .search-input:focus {
            border-color: #3498db;
        }

        .snippets-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(500px, 1fr));
            gap: 20px;
        }

        .snippet-card {
            background: white;
            border-radius: 12px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            padding: 20px;
            transition: transform 0.2s ease;
        }

        .snippet-card:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
        }

        .snippet-header {
            display: flex;
            justify-content: space-between;
            align-items: flex-start;
            margin-bottom: 15px;
        }

        .snippet-id {
            background: #3498db;
            color: white;
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: bold;
        }

        .snippet-title {
            font-size: 18px;
            font-weight: bold;
            color: #2c3e50;
            margin-bottom: 10px;
        }

        .snippet-meta {
            display: flex;
            gap: 10px;
            margin-bottom: 15px;
        }

        .snippet-language,
        .snippet-type,
        .snippet-date {
            font-size: 12px;
            padding: 4px 10px;
            border-radius: 4px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .snippet-language {
            background: #e8f5e9;
            color: #27ae60;
        }

        .snippet-type {
            background: #fff3e0;
            color: #f57c00;
        }

        .snippet-date {
            background: #f3e5f5;
            color: #8e24aa;
        }

        .snippet-content {
            background: #f8f9fa;
            border: 1px solid #e9ecef;
            border-radius: 6px;
            padding: 15px;
            font-family: 'Consolas', 'Monaco', monospace;
            font-size: 13px;
            line-height: 1.5;
            white-space: pre-wrap;
            overflow-x: auto;
            max-height: 150px;
            overflow-y: auto;
            margin-bottom: 15px;
        }

        /* 태그 관리 영역 */
        .tag-management {
            border-top: 1px solid #e9ecef;
            padding-top: 15px;
        }

        .current-tags {
            margin-bottom: 15px;
        }

        .current-tags-label {
            font-size: 14px;
            color: #666;
            margin-bottom: 8px;
        }

        .tag-container {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            margin-bottom: 15px;
        }

        .tag-item {
            display: inline-flex;
            align-items: center;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 6px 12px;
            border-radius: 16px;
            font-size: 12px;
            font-weight: 500;
        }

        .tag-remove {
            margin-left: 8px;
            cursor: pointer;
            opacity: 0.8;
            font-weight: bold;
        }

        .tag-remove:hover {
            opacity: 1;
        }

        .tag-input-container {
            position: relative;
        }

        .tag-input {
            width: 100%;
            padding: 10px 15px;
            border: 2px solid #e0e6ed;
            border-radius: 8px;
            font-size: 14px;
            outline: none;
        }

        .tag-input:focus {
            border-color: #3498db;
        }

        .autocomplete-list {
            position: absolute;
            top: 100%;
            left: 0;
            right: 0;
            background: white;
            border: 1px solid #e0e6ed;
            border-top: none;
            border-radius: 0 0 8px 8px;
            max-height: 150px;
            overflow-y: auto;
            z-index: 1000;
            display: none;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }

        .autocomplete-item {
            padding: 10px 15px;
            cursor: pointer;
            border-bottom: 1px solid #f8f9fa;
            transition: background-color 0.2s;
        }

        .autocomplete-item:hover,
        .autocomplete-item.selected {
            background: #f1f2f6;
        }

        .autocomplete-item:last-child {
            border-bottom: none;
        }

        .no-tags {
            color: #999;
            font-style: italic;
            font-size: 14px;
        }

        .loading {
            text-align: center;
            padding: 40px;
            color: #666;
        }

        .error {
            background: #ffebee;
            color: #c62828;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 20px;
        }

        /* 반응형 */
        @media (max-width: 768px) {
            .snippets-grid {
                grid-template-columns: 1fr;
            }

            .snippet-header {
                flex-direction: column;
                gap: 10px;
            }
        }

        /* 플로팅 알림 */
        .toast {
            position: fixed;
            top: 20px;
            right: 20px;
            background: #27ae60;
            color: white;
            padding: 12px 20px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            display: none;
            z-index: 2000;
        }

        .toast.error {
            background: #e74c3c;
        }
    </style>
</head>
<body>
<div class="container">
    <h1>📝 스니펫 태그 관리</h1>

    <div class="search-bar">
        <input type="text"
               id="snippetSearch"
               class="search-input"
               placeholder="스니펫 검색 (제목, 내용, 태그...)">
    </div>

    <div id="errorMessage" class="error" style="display: none;"></div>

    <div id="snippetsContainer" class="snippets-grid">
        <div class="loading">스니펫을 불러오는 중...</div>
    </div>
</div>

<!-- 플로팅 알림 -->
<div id="toast" class="toast"></div>

<script type="text/javascript">

    $(document).ready(function () {
        // 전역 변수 선언
        let allSnippets = [];
        let allTags = [];
        let snippetTags = {}; // snippetId -> tags array

        // 페이지 로드시 데이터 가져오기
        loadInitialData();

        // 검색 기능
        $('#snippetSearch').on('input', function () {
            const keyword = $(this).val().toLowerCase();
            filterSnippets(keyword);
        });

        // 태그 입력 이벤트 (동적으로 생성되는 요소용)
        $(document).on('input', '.tag-input', function () {
            const input = $(this);
            const keyword = input.val().trim();
            const autocompleteList = input.siblings('.autocomplete-list');

            if (keyword.length > 0) {
                const filteredTags = allTags.filter(tag =>
                    tag.name.toLowerCase().includes(keyword.toLowerCase())
                );
                showAutocomplete(autocompleteList, filteredTags);
            } else {
                autocompleteList.hide();
            }
        });

        // 태그 입력 키보드 이벤트
        $(document).on('keydown', '.tag-input', function (e) {
            const input = $(this);
            const autocompleteList = input.siblings('.autocomplete-list');
            const items = autocompleteList.find('.autocomplete-item');
            const selectedItem = items.filter('.selected');

            if (e.key === 'ArrowDown') {
                e.preventDefault();
                if (selectedItem.length === 0) {
                    items.first().addClass('selected');
                } else {
                    selectedItem.removeClass('selected');
                    const next = selectedItem.next();
                    if (next.length > 0) {
                        next.addClass('selected');
                    } else {
                        items.first().addClass('selected');
                    }
                }
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                if (selectedItem.length === 0) {
                    items.last().addClass('selected');
                } else {
                    selectedItem.removeClass('selected');
                    const prev = selectedItem.prev();
                    if (prev.length > 0) {
                        prev.addClass('selected');
                    } else {
                        items.last().addClass('selected');
                    }
                }
            } else if (e.key === 'Enter') {
                e.preventDefault();
                if (selectedItem.length > 0) {
                    const tagName = selectedItem.text();
                    addTagToSnippet(input, tagName);
                } else {
                    const tagName = input.val().trim();
                    if (tagName) {
                        addTagToSnippet(input, tagName);
                    }
                }
            } else if (e.key === 'Escape') {
                autocompleteList.hide();
            }
        });

        // 자동완성 클릭
        $(document).on('click', '.autocomplete-item', function () {
            const tagName = $(this).text();
            const input = $(this).closest('.tag-input-container').find('.tag-input');
            addTagToSnippet(input, tagName);
        });

        // 태그 제거
        $(document).on('click', '.tag-remove', function (e) {
            e.preventDefault();
            const tagElement = $(this).closest('.tag-item');
            const tagName = tagElement.text().replace('×', '').trim();
            const snippetId = $(this).closest('.snippet-card').data('snippet-id');

            removeTagFromSnippet(snippetId, tagName, tagElement);
        });

        // 함수들
        function loadInitialData() {
            // 모든 태그 로드
            $.ajax({
                url: '/api/tag',
                method: 'GET',
                success: function (tags) {
                    console.log('모든 태그 로드 성공:', tags);
                    allTags = tags || [];
                },
                error: function (xhr) {
                    console.error('태그 목록 로드 실패:', xhr.status);
                    allTags = [];
                    showError('태그 목록을 불러오는데 실패했습니다.');
                },
                complete: function () {
                    // 태그 로드 후 스니펫 로드
                    loadSnippets();
                }
            });
        }

        function loadSnippets() {
            console.log('===== 스니펫 로드 시작 =====');

            $.ajax({
                url: '/api/snippet',
                method: 'GET',
                success: function(snippets) {
                    console.log('스니펫 API 호출 성공');

                    // 응답 데이터 구조 확인
                    console.log('응답 데이터 타입:', typeof snippets);
                    console.log('응답이 배열인가?', Array.isArray(snippets));

                    // 응답에서 배열 데이터 추출
                    if (!Array.isArray(snippets)) {
                        console.log('응답이 배열이 아님. 변환 시도...');
                        if (snippets && typeof snippets === 'object') {
                            if (snippets.content && Array.isArray(snippets.content)) {
                                snippets = snippets.content;
                            }
                        }
                    }

                    // 데이터 샘플 확인
                    if (Array.isArray(snippets) && snippets.length > 0) {
                        console.log('첫 번째 스니펫 구조:', snippets[0]);
                    }

                    // 필드 정규화 및 유효한 스니펫 필터링
                    allSnippets = [];
                    if (Array.isArray(snippets)) {
                        // 간단히 원본 데이터를 사용
                        allSnippets = snippets.filter(snippet => snippet !== null && snippet !== undefined);

                        // 디버깅을 위해 첫 몇 개 스니펫의 ID 출력
                        allSnippets.slice(0, 3).forEach((snippet, index) => {
                            console.log(`스니펫 ${index}의 ID(snippetId): ${snippet.snippetId}`);
                        });
                    }

                    console.log('처리된 스니펫 수:', allSnippets.length);

                    // 필터링 후에도 스니펫이 없으면 더미 데이터 사용
                    if (allSnippets.length === 0) {
                        console.log('스니펫이 없어 더미 데이터를 사용합니다.');
                        allSnippets = getDummySnippets();
                    }

                    // 태그 로드 시작
                    loadSnippetTags();
                },
                error: function(xhr, status, error) {
                    console.error('스니펫 로드 실패:', error);
                    console.log('에러 응답:', xhr.responseText);

                    console.log('더미 데이터 사용');
                    allSnippets = getDummySnippets();

                    // 태그 로드 시작
                    loadSnippetTags();
                }
            });
        }

        // 태그 로드 함수
        function loadSnippetTags(focusSnippetId) {
            console.log("모든 스니펫 태그 로드 시작");
            if (!allSnippets || allSnippets.length === 0) {
                console.log('로드할 스니펫이 없습니다.');
                displaySnippets([], focusSnippetId);
                return;
            }

            console.log('스니펫 태그 로드 시작 - 총 스니펫 수:', allSnippets.length);

            let loaded = 0;
            const totalSnippets = allSnippets.length;

            // 모든 스니펫의 태그 초기화
            allSnippets.forEach(snippet => {
                const snippetId = snippet.snippetId;
                console.log(`스니펫 태그 초기화, ID: ${snippetId}`);
                snippetTags[snippetId] = [];
            });

            // 태그 로드
            allSnippets.forEach((snippet, index) => {
                const snippetId = snippet.snippetId;
                console.log(`스니펫 ${index}의 태그 로드, ID: ${snippetId}`);

                if (!snippetId) {
                    console.error(`스니펫 ${index}에 유효한 ID가 없습니다:`, snippet);
                    loaded++;
                    checkAllLoaded();
                    return;
                }

                const apiUrl = '/api/tag/snippets/' + snippetId;
                console.log('태그 API URL:', apiUrl);

                $.ajax({
                    url: apiUrl,
                    method: 'GET',
                    success: function(tags) {
                        console.log('스니펫 ID - '+snippetId+'의 태그 로드 성공:', tags);
                        if (Array.isArray(tags)) {
                            snippetTags[snippetId] = tags;
                        } else {
                            console.warn(`스니펫 ID ${snippetId}의 태그 응답이 배열이 아님:`, tags);
                            snippetTags[snippetId] = [];
                        }
                    },
                    error: function(xhr, status, error) {
                        console.error(`스니펫 ID ${snippetId}의 태그 로드 실패:`, {
                            status: xhr.status,
                            error: error
                        });
                        snippetTags[snippetId] = [];
                    },
                    complete: function() {
                        loaded++;
                        console.log(`태그 로드 진행 상황: ${loaded}/${totalSnippets}`);
                        checkAllLoaded();
                    }
                });
            });

            // 모든 태그 로드 완료 확인
            function checkAllLoaded() {
                if (loaded >= totalSnippets) {
                    console.log('모든 스니펫의 태그 로드 완료');
                    displaySnippets(allSnippets, focusSnippetId);
                }
            }

            // 안전장치
            setTimeout(function() {
                if (loaded < totalSnippets) {
                    console.warn(`태그 로드 타임아웃: ${loaded}/${totalSnippets} 완료`);
                    displaySnippets(allSnippets, focusSnippetId);
                }
            }, 5000);
        }

        // 더미 스니펫 데이터
        function getDummySnippets() {
            const dummySnippets = [
                {
                    snippetId: 1,
                    title: 'JavaScript 배열 맵',
                    content: 'const numbers = [1, 2, 3, 4, 5];\nconst doubled = numbers.map(num => num * 2);',
                    language: 'javascript',
                    type: 'code',
                    createdAt: '2023-02-01',
                    memo: 'JavaScript 배열 맵 함수 예제'
                },
                {
                    snippetId: 2,
                    title: 'React Hook 예제',
                    content: 'import React, { useState } from "react";\n\nfunction Counter() {\n  const [count, setCount] = useState(0);\n  return <div>{count}</div>;\n}',
                    language: 'jsx',
                    type: 'code',
                    createdAt: '2023-02-03',
                    memo: 'React useState 훅 사용법'
                },
                {
                    snippetId: 3,
                    title: 'Date 포맷팅',
                    content: 'function formatDate(date) {\n  return date.toISOString().split("T")[0];\n}',
                    language: 'javascript',
                    type: 'code',
                    createdAt: '2023-02-05',
                    memo: '날짜 포맷팅 유틸리티 함수'
                }
            ];

            console.log('더미 스니펫 생성:', dummySnippets.length);
            return dummySnippets;
        }

        // 스니펫 표시 함수
        function displaySnippets(snippets, focusSnippetId) {
            const container = $('#snippetsContainer');
            container.empty();

            if (snippets.length === 0) {
                container.html('<div class="loading">표시할 스니펫이 없습니다.</div>');
                return;
            }

            snippets.forEach(snippet => {
                const snippetCard = createSnippetCard(snippet);
                container.append(snippetCard);
            });

            // 포커스 설정 (지정된 스니펫 ID가 있는 경우)
            if (focusSnippetId) {
                console.log(`지정된 스니펫 ID ${focusSnippetId}로 포커스 이동 시도`);

                // 약간의 지연을 두고 포커스 설정 (DOM이 완전히 렌더링된 후)
                setTimeout(function() {
                    const card = $(`.snippet-card[data-snippet-id="${focusSnippetId}"]`);
                    if (card.length > 0) {
                        console.log(`스니펫 ID ${focusSnippetId}의 카드를 찾음`);

                        // 입력 필드에 포커스
                        const inputField = card.find('.tag-input');
                        if (inputField.length > 0) {
                            // 카드가 화면에 보이도록 스크롤
                            $('html, body').animate({
                                scrollTop: card.offset().top - 100 // 상단에서 100px 위치에 스크롤
                            }, 300);

                            // 포커스 설정
                            inputField.focus();
                            console.log(`스니펫 ID ${focusSnippetId}의 태그 입력 필드에 포커스 설정 완료`);
                        } else {
                            console.warn(`스니펫 ID ${focusSnippetId}의 태그 입력 필드를 찾을 수 없음`);
                        }
                    } else {
                        console.warn(`스니펫 ID ${focusSnippetId}의 카드를 찾을 수 없음`);
                    }
                }, 300);
            }
        }

        // 스니펫 카드 생성 함수
        function createSnippetCard(snippet) {
            const snippetId = snippet.snippetId;
            const tags = snippetTags[snippetId] || [];

            // HTML 문자열 생성
            let cardHtml = '<div class="snippet-card" data-snippet-id="' + snippetId + '">';
            cardHtml += '<div class="snippet-header">';
            cardHtml += '<div class="snippet-snippetId">ID: ' + snippetId + '</div>';
            cardHtml += '</div>';
            cardHtml += '<div class="snippet-title">' + (snippet.title || snippet.memo || '제목 없음') + '</div>';
            cardHtml += '<div class="snippet-meta">';

            if (snippet.language) {
                cardHtml += '<span class="snippet-language">' + snippet.language + '</span>';
            }
            if (snippet.type) {
                cardHtml += '<span class="snippet-type">' + snippet.type + '</span>';
            }
            if (snippet.createdAt) {
                cardHtml += '<span class="snippet-date">' + snippet.createdAt + '</span>';
            }

            cardHtml += '</div>';
            cardHtml += '<div class="snippet-content">' + (snippet.content || '내용 없음') + '</div>';
            cardHtml += '<div class="tag-management">';
            cardHtml += '<div class="current-tags">';
            cardHtml += '<div class="current-tags-label">현재 태그:</div>';
            cardHtml += '<div class="tag-container" data-snippet-id="' + snippetId + '">';

            if (tags.length === 0) {
                cardHtml += '<span class="no-tags">태그가 없습니다</span>';
            }

            cardHtml += '</div>';
            cardHtml += '</div>';
            cardHtml += '<div class="tag-input-container">';
            cardHtml += '<input type="text" class="tag-input" placeholder="태그 이름을 입력하고 엔터를 누르세요..." data-snippet-id="' + snippetId + '">';
            cardHtml += '<div class="autocomplete-list"></div>';
            cardHtml += '</div>';
            cardHtml += '</div>';
            cardHtml += '</div>';

            const card = $(cardHtml);

            // 태그 표시
            const tagContainer = card.find('.tag-container');
            if (tags.length > 0) {
                tagContainer.empty();
                tags.forEach(tag => {
                    const tagElement = $('<span class="tag-item">' + tag.name + '<span class="tag-remove">&times;</span></span>');
                    tagContainer.append(tagElement);
                });
            }

            return card;
        }

        // 자동완성 표시 함수
        function showAutocomplete(autocompleteList, tags) {
            autocompleteList.empty();

            if (tags.length > 0) {
                tags.forEach(tag => {
                    const item = $('<div class="autocomplete-item">' + tag.name + '</div>');
                    autocompleteList.append(item);
                });
                autocompleteList.show();
            } else {
                autocompleteList.hide();
            }
        }

        // 태그 추가 함수
        function addTagToSnippet(input, tagName) {
            const snippetId = input.data('snippet-id');
            console.log(`태그 추가 시도: 스니펫 ID=${snippetId}, 태그명=${tagName}`);

            if (!snippetId) {
                console.error('유효하지 않은 스니펫 ID:', snippetId);
                showToast('스니펫 ID를 찾을 수 없습니다.', 'error');
                return;
            }

            if (!tagName || tagName.trim() === '') {
                showToast('태그 이름을 입력하세요.', 'error');
                return;
            }

            const currentTags = snippetTags[snippetId] || [];
            console.log(`현재 태그 목록 (스니펫 ${snippetId}):`, currentTags);

            const alreadyExists = currentTags.some(tag =>
                tag.name.toLowerCase() === tagName.toLowerCase()
            );

            if (alreadyExists) {
                showToast('이미 추가된 태그입니다.', 'error');
                input.val('');
                input.siblings('.autocomplete-list').hide();
                return;
            }

            console.log('태그 생성 API 호출: '+ tagName);
            $.ajax({
                url: '/api/tag',
                method: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({name: tagName}),
                success: function (tag) {
                    console.log(`태그 생성 성공:`, tag);
                    addTagToSnippetAPI(snippetId, tag);
                },
                error: function (xhr, status, error) {
                    console.error(`태그 생성 실패:`, {
                        status: xhr.status,
                        statusText: xhr.statusText,
                        error: error,
                        response: xhr.responseText
                    });

                    if (xhr.status === 409) {
                        console.log('태그가 이미 존재함. 기존 태그 사용:', xhr.responseJSON);
                        const existingTag = xhr.responseJSON;
                        addTagToSnippetAPI(snippetId, existingTag);
                    } else {
                        showToast('태그 생성 중 오류가 발생했습니다.', 'error');
                    }
                }
            });

            input.val('');
            input.siblings('.autocomplete-list').hide();
        }

        // 스니펫에 태그 추가 API 호출 함수
        function addTagToSnippetAPI(snippetId, tag) {
            if (!tag || !tag.tagId) {
                console.error('유효하지 않은 태그:', tag);
                showToast('유효하지 않은 태그입니다.', 'error');
                return;
            }

            const apiUrl = '/api/tag/snippet/' + snippetId + '/tags/' + tag.tagId;
            console.log(`태그 추가 API 호출: ${apiUrl}`, {snippetId, tagId: tag.tagId});

            $.ajax({
                url: apiUrl,
                method: 'POST',
                success: function (response) {
                    console.log(`태그 추가 성공:`, response);
                    showToast('태그가 추가되었습니다. 스니펫을 다시 로드합니다...');
                    reloadAllSnippets(snippetId);
                },
                error: function (xhr, status, error) {
                    console.error(`태그 추가 실패:`, {
                        status: xhr.status,
                        statusText: xhr.statusText,
                        error: error,
                        response: xhr.responseText,
                        apiUrl: apiUrl
                    });
                    showToast('태그 추가 중 오류가 발생했습니다.', 'error');
                }
            });
        }

        // 태그 제거 함수
        function removeTagFromSnippet(snippetId, tagName, tagElement) {
            const tag = (snippetTags[snippetId] || []).find(t => t.name === tagName);
            if (!tag) {
                showToast('태그를 찾을 수 없습니다.', 'error');
                return;
            }

            const apiUrl = '/api/tag/snippets/' + snippetId + '/tags/' + tag.tagId;
            console.log(`태그 제거 API 호출: ${apiUrl}`, {snippetId, tagId: tag.tagId});

            $.ajax({
                url: apiUrl,
                method: 'DELETE',
                success: function () {
                    showToast('태그가 제거되었습니다. 스니펫을 다시 로드합니다...');
                    reloadAllSnippets(snippetId);
                },
                error: function (xhr, status, error) {
                    console.error(`태그 제거 실패:`, {
                        status: xhr.status,
                        error: error
                    });
                    showToast('태그 제거 중 오류가 발생했습니다.', 'error');
                }
            });
        }

        // 스니펫 태그 UI 업데이트 함수
        function updateSnippetTagsUI(snippetId) {
            const card = $(`.snippet-card[data-snippet-id="${snippetId}"]`);
            const tagContainer = card.find('.tag-container');
            const tags = snippetTags[snippetId] || [];

            tagContainer.empty();

            if (tags.length === 0) {
                tagContainer.html('<span class="no-tags">태그가 없습니다</span>');
            } else {
                tags.forEach(tag => {
                    const tagElement = $('<span class="tag-item">' + tag.name + '<span class="tag-remove">&times;</span></span>');
                    tagContainer.append(tagElement);
                });
            }
        }

        // 스니펫 다시 로드 함수
        function reloadAllSnippets(focusSnippetId) {
            console.log('===== 모든 스니펫 다시 로드 시작 =====');
            console.log('로드 후 포커스할 스니펫 ID:', focusSnippetId);

            $('#snippetsContainer').html('<div class="loading">스니펫을 다시 불러오는 중...</div>');

            $.ajax({
                url: '/api/snippet',
                method: 'GET',
                success: function(snippets) {
                    console.log('스니펫 API 호출 성공, 스니펫 수:', snippets.length);
                    allSnippets = snippets;
                    snippetTags = {};
                    loadSnippetTags(focusSnippetId);
                },
                error: function(xhr, status, error) {
                    console.error('스니펫 다시 로드 실패:', error);
                    showToast('스니펫을 다시 로드하는데 실패했습니다.', 'error');
                    displaySnippets(allSnippets, focusSnippetId);
                }
            });
        }

        // 스니펫 필터링 함수
        function filterSnippets(keyword) {
            const filteredSnippets = allSnippets.filter(snippet => {
                const searchableText = [
                    snippet.title || '',
                    snippet.content || '',
                    snippet.memo || '',
                    snippet.language || '',
                    snippet.type || ''
                ].join(' ').toLowerCase();

                // 태그도 검색에 포함
                const tags = snippetTags[snippet.snippetId] || [];
                const tagNames = tags.map(tag => tag.name).join(' ').toLowerCase();

                return searchableText.includes(keyword) || tagNames.includes(keyword);
            });

            displaySnippets(filteredSnippets);
        }

        // 토스트 메시지 표시 함수
        function showToast(message, type) {
            type = type || 'success';
            const toast = $('#toast');
            toast.removeClass('error').text(message);

            if (type === 'error') {
                toast.addClass('error');
            }

            toast.fadeIn(300);
            setTimeout(function () {
                toast.fadeOut(300);
            }, 3000);
        }

        // 에러 메시지 표시 함수
        function showError(message) {
            $('#errorMessage').text(message).show();
        }

        // 외부 클릭시 자동완성 닫기
        $(document).on('click', function (e) {
            if (!$(e.target).closest('.tag-input-container').length) {
                $('.autocomplete-list').hide();
            }
        });
    });

</script>
</body>
</html>