$(document).ready(function () {
    // 초기화
    let selectedTags = [];
    let allTags = [];
    let currentIndex = -1;
    let autocompleteData = [];

    // 더미 스니펫 데이터 (코드와 텍스트만)
    const dummySnippets = [
        {
            id: 1,
            title: 'JavaScript 배열 맵',
            content: 'const numbers = [1, 2, 3, 4, 5];\nconst doubled = numbers.map(num => num * 2);',
            language: 'javascript',
            type: 'code',
            createdAt: '2023-02-01'
        },
        {
            id: 2,
            title: 'React Hook 예제',
            content: 'const [count, setCount] = useState(0);',
            language: 'jsx',
            type: 'code',
            createdAt: '2023-02-03'
        },
        {
            id: 3,
            title: 'Java Stream API',
            content: 'list.stream().filter(x -> x > 0).collect(Collectors.toList());',
            language: 'java',
            type: 'code',
            createdAt: '2023-02-05'
        },
        {
            id: 4,
            title: 'Spring Boot Controller',
            content: '@RestController\npublic class ApiController { ... }',
            language: 'java',
            type: 'code',
            createdAt: '2023-02-07'
        },
        {
            id: 5,
            title: 'CSS Grid Layout',
            content: '.container { display: grid; grid-template-columns: repeat(3, 1fr); }',
            language: 'css',
            type: 'code',
            createdAt: '2023-02-09'
        },
        {
            id: 6,
            title: 'JavaScript 학습 노트',
            content: '# JavaScript 기초\n\n1. 변수 선언 (let, const, var)\n2. 함수 정의 (function, arrow function)\n3. 조건문 사용법 (if, switch)',
            type: 'text',
            createdAt: '2023-02-11'
        },
        {
            id: 7,
            title: 'Spring 정리',
            content: '스프링 프레임워크 핵심 개념:\n\n- IoC (Inversion of Control)\n- DI (Dependency Injection)\n- AOP (Aspect Oriented Programming)',
            type: 'text',
            createdAt: '2023-02-13'
        }
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

    // 초기 데이터 로드
    loadAllTags();

    // 알림 메시지 표시
    if (message) {
        showAlert(message, 'success');
    }
    if (error) {
        showAlert(error, 'error');
    }

    // 태그 입력 자동완성
    $('#tagInput').on('input', function () {
        const keyword = $(this).val().trim();
        currentIndex = -1;
        if (keyword.length > 0) {
            searchTags(keyword);
        } else {
            $('#autocomplete').hide();
        }
    });

    // 키보드 이벤트
    $('#tagInput').on('keydown', function (e) {
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
    $(document).on('click', '.autocomplete-option', function () {
        const index = $(this).data('index');
        const tag = autocompleteData[index];
        addToSelected(tag);
        clearInput();
    });

    // 태그 클릭시 스니펫 로드
    $(document).on('click', '.tag-badge:not(.selected-tag)', function () {
        const tagId = $(this).data('id');
        const tagName = $(this).text();
        if (tagId) {
            $('.tag-badge').removeClass('active');
            $(this).addClass('active');
            loadSnippets(tagId, tagName);
        }
    });

    // 외부 클릭시 자동완성 닫기
    $(document).on('click', function (e) {
        if (!$(e.target).closest('.tag-input-wrapper').length) {
            $('#autocomplete').hide();
        }
    });

    // 함수들
    function searchTags(keyword) {
        $.ajax({
            url: '/api/tag/search',
            method: 'GET',
            data: {
                query: keyword
                // userId: currentUserId 제거
            },
            success: function (tags) {
                showAutocomplete(tags);
            },
            error: function () {
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
            data: JSON.stringify({
                name: tagName
                // userId: currentUserId 제거
            }),
            success: function (tag) {
                addToSelected(tag);
                loadAllTags();
            },
            error: function (xhr) {
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
            element.append($('<span>').addClass('tag-delete').html('&times;').on('click', function (e) {
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
            url: '/api/tag/my-tags',
            method: 'GET',
            success: function (tags) {
                allTags = tags;
                displayAllTags(tags);
            },
            error: function () {
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
            const tagBadge = $('<span>')
                .addClass('tag-badge')
                .data('id', tag.tagId)
                .html(tag.name + ' <span class="tag-delete-inline" data-tag-id="' + tag.tagId + '" data-tag-name="' + tag.name + '">×</span>');

            container.append(tagBadge);
        });
    }

    // 인라인 삭제 버튼 클릭 이벤트
    $(document).on('click', '.tag-delete-inline', function(e) {
        e.stopPropagation();
        const tagId = $(this).data('tag-id');
        const tagName = $(this).data('tag-name');
        showDeleteModal(tagId, tagName);
    });

    function loadSnippets(tagId, tagName) {
        console.log('Loading snippets for tagId:', tagId, 'tagName:', tagName);

        $('#snippetsSection').show();
        $('#snippetsTitle').text('📝 "' + tagName + '" 태그의 스니펫 목록');
        $('#snippetsGrid').html('<div class="loading-text">스니펫을 불러오는 중...</div>');

        $.ajax({
            url: '/api/tag/' + tagId + '/snippets',
            method: 'GET',
            success: function (snippets) {
                console.log('Received snippets:', snippets);
                showSnippets(snippets, tagName);
            },
            error: function (xhr, status, error) {
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
});

// 세션 관련 함수들 - 전역 스코프에 정의
function showAlert(message, type) {
    const alert = document.createElement('div');
    alert.className = `alert alert-${type}`;
    alert.textContent = message;
    document.body.appendChild(alert);

    // 애니메이션 효과로 표시
    setTimeout(() => alert.classList.add('show'), 100);

    // 3초 후 자동 제거
    setTimeout(() => {
        alert.classList.remove('show');
        setTimeout(() => alert.remove(), 300);
    }, 3000);
}

// 세션 정보 토글 함수
function toggleSessionInfo() {
    const sessionInfo = document.getElementById('sessionInfo');
    if (sessionInfo.style.display === 'none' || sessionInfo.style.display === '') {
        sessionInfo.style.display = 'block';
        loadSessionData();
    } else {
        sessionInfo.style.display = 'none';
    }
}

// 세션 데이터 로드 함수
async function loadSessionData() {
    try {
        // 세션 ID 가져오기 (쿠키에서)
        const cookies = document.cookie.split(';');
        let sessionId = '쿠키에서 세션ID를 찾을 수 없음';

        for (let cookie of cookies) {
            const [name, value] = cookie.trim().split('=');
            if (name === 'JSESSIONID' || name === 'connect.sid') {
                sessionId = value;
                break;
            }
        }
        document.getElementById('sessionId').textContent = sessionId;

        // 서버 세션 데이터 확인 API 호출
        try {
            const response = await fetch('/tag/check-session');
            if (response.ok) {
                const sessionData = await response.json();
                document.getElementById('serverSessionData').innerHTML =
                    `userId: ${sessionData.userId || '없음'}, ` +
                    `sessionId: ${sessionData.sessionId || '없음'}`;
            } else {
                document.getElementById('serverSessionData').textContent =
                    'API 응답 오류 - 서버에서 확인하세요';
            }
        } catch (error) {
            document.getElementById('serverSessionData').textContent =
                `세션 확인 API 없음 - ${error.message}`;
            console.log('세션 확인 API 호출 실패:', error);
        }
    } catch (error) {
        console.error('세션 정보 로드 중 오류:', error);
        document.getElementById('serverSessionData').textContent = '오류 발생';
    }
}

function displayAllTags(tags) {
    const container = $('#allTags');
    container.empty();
    if (tags.length === 0) {
        container.html('<div class="empty-message">등록된 태그가 없습니다</div>');
        return;
    }
    tags.forEach(tag => {
        const tagContainer = $('<div>').addClass('tag-badge-container');
        const tagBadge = $('<span>')
            .addClass('tag-badge')
            .text(tag.name)
            .data('id', tag.tagId);

        const deleteBtn = $('<button>')
            .addClass('tag-delete-btn')
            .html('&times;')
            .data('tag-id', tag.tagId)
            .data('tag-name', tag.name)
            .on('click', function(e) {
                e.stopPropagation();
                showDeleteModal(tag.tagId, tag.name);
            });

        tagContainer.append(tagBadge).append(deleteBtn);
        container.append(tagContainer);
    });
}

// 삭제 모달 표시
function showDeleteModal(tagId, tagName) {
    $('#deleteTagName').text(tagName);
    $('#deleteModal').show();

    // 확인 버튼에 이벤트 바인딩
    $('#confirmDelete').off('click').on('click', function() {
        deleteTag(tagId, tagName);
        $('#deleteModal').hide();
    });
}

// 태그 삭제 함수
function deleteTag(tagId, tagName) {
    $.ajax({
        url: '/api/tag/' + tagId,
        method: 'DELETE',
        success: function() {
            showAlert('"' + tagName + '" 태그가 삭제되었습니다.', 'success');
            // 확실히 함수가 존재하는지 체크
            if (typeof loadAllTags === 'function') {
                loadAllTags();
            } else {
                console.error('loadAllTags 함수를 찾을 수 없습니다');
                // 페이지 새로고침으로 대체
                location.reload();
            }
        },
        error: function(xhr) {
            if (xhr.status === 403) {
                showAlert('태그 삭제 권한이 없습니다.', 'error');
            } else {
                showAlert('태그 삭제 중 오류가 발생했습니다.', 'error');
            }
        }
    });
}

// 모달 닫기 이벤트
$(document).ready(function() {
    $('#cancelDelete, .modal-close').on('click', function() {
        $('#deleteModal').hide();
    });

    // 모달 외부 클릭시 닫기
    $('#deleteModal').on('click', function(e) {
        if (e.target === this) {
            $(this).hide();
        }
    });
});