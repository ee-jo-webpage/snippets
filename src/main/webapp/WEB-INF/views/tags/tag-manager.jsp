<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>태그 관리</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: Arial, sans-serif; background: #f5f7fa; }
        .container { max-width: 1200px; margin: 0 auto; padding: 20px; }
        h1 { text-align: center; color: #2c3e50; margin-bottom: 30px; }
        .card { background: white; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); padding: 25px; margin-bottom: 25px; }
        .card-title { font-size: 1.2em; font-weight: bold; margin-bottom: 15px; color: #34495e; }

        /* 태그 입력 */
        .tag-input-wrapper { position: relative; max-width: 500px; }
        .tag-input-field { width: 100%; padding: 12px 15px; border: 2px solid #ddd; border-radius: 8px; font-size: 16px; outline: none; }
        .tag-input-field:focus { border-color: #3498db; }

        /* 자동완성 */
        .autocomplete-dropdown { position: absolute; top: 100%; left: 0; right: 0; background: white; border: 1px solid #ddd; border-top: none;
            border-radius: 0 0 8px 8px; max-height: 200px; overflow-y: auto; z-index: 1000; display: none; }
        .autocomplete-option { padding: 10px 15px; cursor: pointer; }
        .autocomplete-option:hover, .autocomplete-option.highlight { background: #f1f2f6; }

        /* 태그 스타일 */
        .tag-list { display: flex; flex-wrap: wrap; gap: 10px; min-height: 50px; align-items: flex-start; }
        .tag-badge { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 8px 16px;
            border-radius: 20px; cursor: pointer; font-size: 14px; transition: all 0.3s; user-select: none; }
        .tag-badge:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.15); }
        .tag-badge.active { background: linear-gradient(135deg, #27ae60 0%, #2ecc71 100%); }
        .tag-delete { margin-left: 8px; font-weight: bold; opacity: 0.8; }
        .tag-delete:hover { opacity: 1; }

        /* 스니펫 표시 */
        .snippets-section { display: none; }
        .snippets-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(400px, 1fr)); gap: 20px; }
        .snippet-card { background: white; border: 1px solid #e0e0e0; border-radius: 8px; padding: 20px; transition: transform 0.2s; }
        .snippet-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.1); }
        .snippet-header { font-weight: bold; font-size: 1.1em; margin-bottom: 10px; color: #2c3e50; }

        .snippet-meta { display: flex; gap: 10px; margin-bottom: 15px; flex-wrap: wrap; }
        .snippet-meta-item { background: #f8f9fa; color: #666; padding: 4px 8px; border-radius: 4px; font-size: 12px; }
        .snippet-language { background: #e8f5e9; color: #27ae60; }
        .snippet-type { background: #fff3e0; color: #f57c00; }
        .snippet-date { background: #f3e5f5; color: #8e24aa; }

        .snippet-body { background: #f8f9fa; padding: 15px; border-radius: 6px; font-family: monospace;
            font-size: 14px; white-space: pre-wrap; max-height: 200px; overflow-y: auto; border-left: 3px solid #3498db; }

        /* 텍스트 타입 스니펫 */
        .snippet-body.text-type { font-family: inherit; border-left-color: #27ae60; }

        .empty-message { text-align: center; color: #999; font-style: italic; padding: 20px; }
        .loading-text { text-align: center; color: #666; padding: 20px; }
        .help-message { background: #e3f2fd; color: #1565c0; padding: 12px; border-radius: 6px; margin-bottom: 15px; font-size: 14px; }

        /* 반응형 */
        @media (max-width: 768px) {
            .snippets-grid { grid-template-columns: 1fr; }
            .container { padding: 15px; }
        }
    </style>
</head>
<body>
<div class="container">
    <h1>🏷️ 태그 관리</h1>

    <div class="card">
        <div class="card-title">태그 추가</div>
        <div class="help-message">💡 태그 이름을 입력하고 엔터를 눌러 새 태그를 생성하거나 기존 태그를 선택하세요.</div>
        <div class="tag-input-wrapper">
            <input type="text" id="tagInput" class="tag-input-field" placeholder="태그 이름을 입력하세요...">
            <div id="autocomplete" class="autocomplete-dropdown"></div>
        </div>
    </div>

    <div class="card">
        <div class="card-title">선택된 태그</div>
        <div id="selectedTags" class="tag-list">
            <div class="empty-message">선택된 태그가 없습니다</div>
        </div>
    </div>

    <div class="card">
        <div class="card-title">전체 태그 목록</div>
        <div class="help-message">💡 태그를 클릭하면 해당 태그를 가진 스니펫 목록을 볼 수 있습니다.</div>
        <div id="allTags" class="tag-list">
            <div class="loading-text">태그를 불러오는 중...</div>
        </div>
    </div>

    <div id="snippetsSection" class="card snippets-section">
        <div class="card-title" id="snippetsTitle">📝 스니펫 목록</div>
        <div id="snippetsGrid" class="snippets-grid"></div>
    </div>
</div>

<script>
    $(document).ready(function() {
        let selectedTags = [];
        let allTags = [];
        let currentIndex = -1;
        let autocompleteData = [];

        // 더미 스니펫 데이터 (코드와 텍스트만)
        const dummySnippets = [
            { id: 1, title: 'JavaScript 배열 맵', content: 'const numbers = [1, 2, 3, 4, 5];\nconst doubled = numbers.map(num => num * 2);', language: 'javascript', type: 'code', createdAt: '2023-02-01' },
            { id: 2, title: 'React Hook 예제', content: 'const [count, setCount] = useState(0);', language: 'jsx', type: 'code', createdAt: '2023-02-03' },
            { id: 3, title: 'Java Stream API', content: 'list.stream().filter(x -> x > 0).collect(Collectors.toList());', language: 'java', type: 'code', createdAt: '2023-02-05' },
            { id: 4, title: 'Spring Boot Controller', content: '@RestController\npublic class ApiController { ... }', language: 'java', type: 'code', createdAt: '2023-02-07' },
            { id: 5, title: 'CSS Grid Layout', content: '.container { display: grid; grid-template-columns: repeat(3, 1fr); }', language: 'css', type: 'code', createdAt: '2023-02-09' },
            { id: 6, title: 'JavaScript 학습 노트', content: '# JavaScript 기초\n\n1. 변수 선언 (let, const, var)\n2. 함수 정의 (function, arrow function)\n3. 조건문 사용법 (if, switch)', type: 'text', createdAt: '2023-02-11' },
            { id: 7, title: 'Spring 정리', content: '스프링 프레임워크 핵심 개념:\n\n- IoC (Inversion of Control)\n- DI (Dependency Injection)\n- AOP (Aspect Oriented Programming)', type: 'text', createdAt: '2023-02-13' }
        ];

        // 태그별 스니펫 매핑
        const tagSnippetMap = {
            1: [1, 2, 6], // JavaScript
            2: [2],       // React
            3: [3, 4, 7], // Java
            4: [4, 7],    // Spring
            5: [1, 2, 5], // Frontend
            6: [3, 4, 7], // Backend
        };

        loadAllTags();

        // 태그 입력 자동완성
        $('#tagInput').on('input', function() {
            const keyword = $(this).val().trim();
            currentIndex = -1;
            if (keyword.length > 0) {
                searchTags(keyword);
            } else {
                $('#autocomplete').hide();
            }
        });

        // 키보드 이벤트
        $('#tagInput').on('keydown', function(e) {
            const dropdown = $('#autocomplete');
            const options = dropdown.find('.autocomplete-option');

            if (e.key === 'ArrowDown') {
                e.preventDefault();
                currentIndex = Math.min(currentIndex + 1, options.length - 1);
                updateHighlight(options);
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                currentIndex = Math.max(currentIndex - 1, 0);
                updateHighlight(options);
            } else if (e.key === 'Enter') {
                e.preventDefault();
                if (currentIndex >= 0 && options.length > 0) {
                    const tag = autocompleteData[currentIndex];
                    addToSelected(tag);
                } else {
                    const tagName = $(this).val().trim();
                    if (tagName) createNewTag(tagName);
                }
                clearInput();
            } else if (e.key === 'Escape') {
                dropdown.hide();
            }
        });

        // 자동완성 클릭
        $(document).on('click', '.autocomplete-option', function() {
            const index = $(this).data('index');
            const tag = autocompleteData[index];
            addToSelected(tag);
            clearInput();
        });

        // 태그 클릭시 스니펫 로드
        $(document).on('click', '.tag-badge:not(.selected-tag)', function() {
            const tagId = $(this).data('id');
            const tagName = $(this).text();
            if (tagId) {
                $('.tag-badge').removeClass('active');
                $(this).addClass('active');
                loadSnippets(tagId, tagName);
            }
        });

        // 함수들
        function searchTags(keyword) {
            $.ajax({
                url: '/api/tag/search',
                method: 'GET',
                data: { query: keyword },
                success: function(tags) {
                    showAutocomplete(tags);
                },
                error: function() {
                    console.error('태그 검색 오류');
                }
            });
        }

        function showAutocomplete(tags) {
            autocompleteData = tags;
            const dropdown = $('#autocomplete');
            if (tags.length > 0) {
                dropdown.empty();
                tags.forEach((tag, index) => {
                    dropdown.append($('<div>').addClass('autocomplete-option').text(tag.name).data('index', index));
                });
                dropdown.show();
            } else {
                dropdown.hide();
            }
        }

        function updateHighlight(options) {
            options.removeClass('highlight');
            if (currentIndex >= 0) {
                options.eq(currentIndex).addClass('highlight');
            }
        }

        function addToSelected(tag) {
            const exists = selectedTags.some(t => t.tagId === tag.tagId);
            if (!exists) {
                selectedTags.push(tag);
                displaySelected();
            }
        }

        function createNewTag(tagName) {
            $.ajax({
                url: '/api/tag',
                method: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({ name: tagName }),
                success: function(tag) {
                    addToSelected(tag);
                    loadAllTags();
                },
                error: function(xhr) {
                    if (xhr.status === 409) {
                        const existingTag = xhr.responseJSON;
                        if (existingTag) {
                            addToSelected(existingTag);
                        }
                    } else {
                        alert('태그 생성 중 오류가 발생했습니다.');
                    }
                }
            });
        }

        function clearInput() {
            $('#tagInput').val('');
            $('#autocomplete').hide();
            currentIndex = -1;
        }

        function displaySelected() {
            const container = $('#selectedTags');
            container.empty();
            if (selectedTags.length === 0) {
                container.html('<div class="empty-message">선택된 태그가 없습니다</div>');
                return;
            }
            selectedTags.forEach((tag, index) => {
                const element = $('<span>').addClass('tag-badge selected-tag').data('id', tag.tagId);
                element.append($('<span>').text(tag.name));
                element.append($('<span>').addClass('tag-delete').html('&times;').on('click', function(e) {
                    e.stopPropagation();
                    selectedTags.splice(index, 1);
                    displaySelected();
                    // 선택된 태그가 없으면 스니펫 목록 숨기기
                    if (selectedTags.length === 0) {
                        $('#snippetsSection').hide();
                    }
                }));
                container.append(element);
            });
        }

        function loadAllTags() {
            $.ajax({
                url: '/api/tag',
                method: 'GET',
                success: function(tags) {
                    allTags = tags;
                    displayAllTags(tags);
                },
                error: function() {
                    console.error('태그 목록 로딩 오류');
                    $('#allTags').html('<div class="empty-message">태그 목록을 불러오는데 실패했습니다</div>');
                }
            });
        }

        function displayAllTags(tags) {
            const container = $('#allTags');
            container.empty();
            if (tags.length === 0) {
                container.html('<div class="empty-message">등록된 태그가 없습니다</div>');
                return;
            }
            tags.forEach(tag => {
                container.append($('<span>').addClass('tag-badge').text(tag.name).data('id', tag.tagId));
            });
        }

        function loadSnippets(tagId, tagName) {
            console.log('Loading snippets for tagId:', tagId, 'tagName:', tagName);

            $('#snippetsSection').show();
            $('#snippetsTitle').text('📝 "' + tagName + '" 태그의 스니펫 목록');
            $('#snippetsGrid').html('<div class="loading-text">스니펫을 불러오는 중...</div>');

            $.ajax({
                url: '/api/tag/' + tagId + '/snippets',
                method: 'GET',
                success: function(snippets) {
                    console.log('Received snippets:', snippets);
                    showSnippets(snippets, tagName);
                },
                error: function(xhr, status, error) {
                    console.error('API Error:', xhr.status, error);
                    console.log('Using dummy data for tagId:', tagId);

                    // 더미 데이터 사용
                    const snippetIds = tagSnippetMap[tagId] || [];
                    const snippets = dummySnippets.filter(s => snippetIds.includes(s.id));
                    showSnippets(snippets, tagName);
                }
            });
        }

        function showSnippets(snippets, tagName) {
            const container = $('#snippetsGrid');
            $('#snippetsTitle').text('📝 "' + tagName + '" 태그의 스니펫 (' + snippets.length + '개)');
            container.empty();

            if (snippets.length === 0) {
                container.html('<div class="empty-message">이 태그에 해당하는 스니펫이 없습니다</div>');
                return;
            }

            snippets.forEach(snippet => {
                const card = $('<div>').addClass('snippet-card');

                // 제목
                card.append($('<div>').addClass('snippet-header').text(snippet.title || snippet.memo || '제목 없음'));

                // 메타 정보
                const metaContainer = $('<div>').addClass('snippet-meta');

                if (snippet.language) {
                    metaContainer.append($('<span>').addClass('snippet-meta-item snippet-language').text(snippet.language));
                }

                if (snippet.type) {
                    metaContainer.append($('<span>').addClass('snippet-meta-item snippet-type').text(snippet.type));
                }

                if (snippet.createdAt) {
                    metaContainer.append($('<span>').addClass('snippet-meta-item snippet-date').text(snippet.createdAt));
                }

                if (metaContainer.children().length > 0) {
                    card.append(metaContainer);
                }

                // 내용 (코드 또는 텍스트)
                const contentDiv = $('<div>')
                    .addClass('snippet-body')
                    .text(snippet.content || '내용 없음');

                // 텍스트 타입인 경우 다른 스타일 적용
                if (snippet.type === 'text') {
                    contentDiv.addClass('text-type');
                }

                card.append(contentDiv);
                container.append(card);
            });
        }

        // 외부 클릭시 자동완성 닫기
        $(document).on('click', function(e) {
            if (!$(e.target).closest('.tag-input-wrapper').length) {
                $('#autocomplete').hide();
            }
        });
    });
</script>
</body>
</html>