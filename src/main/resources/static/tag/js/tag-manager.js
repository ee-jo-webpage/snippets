$(document).ready(function () {
    // 초기화
    let allTags = [];
    let selectedTagId = null;

    // 더미 스니펫 데이터 (테스트용)
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

    // 태그별 스니펫 매핑 (테스트용)
    const tagSnippetMap = {
        1: [1, 2, 6], // JavaScript
        2: [2],       // React
        3: [3, 4, 7], // Java
        4: [4, 7],    // Spring
        5: [1, 2, 5], // Frontend
        6: [3, 4, 7], // Backend
    };

    // 로그인된 사용자만 태그 관리 기능 초기화
    if (userId != null) {
        // 초기 데이터 로드
        loadAllTags();

        // 태그 검색 입력 이벤트
        $('#tagSearchInput').on('input', function () {
            const keyword = $(this).val().trim();
            if (keyword.length > 0) {
                searchAndFilterTags(keyword);
            } else {
                displayAllTags(allTags);
            }
        });

        // 태그 검색 엔터키 이벤트
        $('#tagSearchInput').on('keydown', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                const tagName = $(this).val().trim();
                if (tagName) {
                    createNewTag(tagName);
                }
            }
        });

        // 태그 추가 버튼 클릭
        $('#addTagBtn').on('click', function () {
            const tagName = $('#tagSearchInput').val().trim();
            if (tagName) {
                createNewTag(tagName);
            } else {
                showAlert('태그명을 입력해주세요.', 'warning');
                $('#tagSearchInput').focus();
            }
        });

        // 태그 카드 클릭 이벤트
        $(document).on('click', '.tag-card', function () {
            const tagId = $(this).data('id');
            const tagName = $(this).find('.tag-name').text();

            if (tagId && tagName) {
                // 이전 선택 해제
                $('.tag-card').removeClass('active');
                // 현재 선택 활성화
                $(this).addClass('active');

                selectedTagId = tagId;
                loadSnippetsForTag(tagId, tagName);
            }
        });

        // 스니펫 섹션 닫기 버튼
        $('#closeSnippetsBtn').on('click', function () {
            $('#snippetsSection').hide();
            $('.tag-card').removeClass('active');
            selectedTagId = null;
        });
    }

    // 알림 메시지 표시
    if (message) {
        showAlert(message, 'success');
    }
    if (error) {
        showAlert(error, 'error');
    }

    // 함수들
    function loadAllTags() {
        console.log('태그 목록을 로드하는 중...');
        $('#tagsContainer').html('<div class="loading-text">태그를 불러오는 중...</div>');

        $.ajax({
            url: '/api/tag/my-tags',
            method: 'GET',
            success: function (tags) {
                console.log('태그 로드 성공:', tags);
                allTags = tags || [];
                displayAllTags(allTags);
                updateTagCount(allTags.length);
            },
            error: function (xhr, status, error) {
                console.error('태그 로드 실패:', xhr.status, error);
                $('#tagsContainer').html('<div class="empty-message">태그 목록을 불러오는데 실패했습니다.</div>');
                updateTagCount(0);
            }
        });
    }

    function displayAllTags(tags) {
        const container = $('#tagsContainer');
        container.empty();

        if (!tags || tags.length === 0) {
            $('#emptyState').show();
            return;
        }

        $('#emptyState').hide();

        tags.forEach(tag => {
            const card = createTagCard(tag);
            container.append(card);
        });
    }

    function createTagCard(tag) {
        const card = $('<div>')
            .addClass('tag-card')
            .data('id', tag.tagId);

        // 태그 이름
        const tagName = $('<span>')
            .addClass('tag-name')
            .text(tag.name);

        // 스니펫 개수 (초기값)
        const snippetCount = $('<span>')
            .addClass('tag-snippet-count')
            .text('0');

        // 삭제 버튼
        const deleteBtn = $('<button>')
            .addClass('tag-delete-btn')
            .html('×')
            .attr('title', '태그 삭제')
            .on('click', function (e) {
                e.stopPropagation();
                showDeleteModal(tag.tagId, tag.name);
            });

        card.append(tagName).append(snippetCount).append(deleteBtn);

        // 스니펫 개수 비동기 로드
        loadTagSnippetCount(tag.tagId, snippetCount);

        return card;
    }

    function loadTagSnippetCount(tagId, countElement) {
        $.ajax({
            url: '/api/tag/' + tagId + '/snippets',
            method: 'GET',
            success: function (snippets) {
                const count = snippets ? snippets.length : 0;
                countElement.text(count);
            },
            error: function () {
                // API 실패시 더미 데이터 사용
                const snippetIds = tagSnippetMap[tagId] || [];
                const count = snippetIds.length;
                countElement.text(count);
            }
        });
    }

    function searchAndFilterTags(keyword) {
        if (!allTags || allTags.length === 0) {
            return;
        }

        const filteredTags = allTags.filter(tag =>
            tag.name.toLowerCase().includes(keyword.toLowerCase())
        );

        displayAllTags(filteredTags);
    }

    function createNewTag(tagName) {
        if (!tagName || tagName.trim() === '') {
            showAlert('태그명을 입력해주세요.', 'warning');
            return;
        }

        console.log('새 태그 생성:', tagName);

        $.ajax({
            url: '/api/tag',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                name: tagName.trim()
            }),
            success: function (response) {
                console.log('태그 생성 성공:', response);
                showAlert('태그가 성공적으로 생성되었습니다.', 'success');
                $('#tagSearchInput').val('');
                loadAllTags(); // 태그 목록 새로고침
            },
            error: function (xhr) {
                console.error('태그 생성 실패:', xhr.status, xhr.responseText);
                if (xhr.status === 409) {
                    showAlert('이미 존재하는 태그입니다.', 'warning');
                } else if (xhr.status === 401) {
                    showAlert('로그인이 필요합니다.', 'error');
                } else {
                    showAlert('태그 생성 중 오류가 발생했습니다.', 'error');
                }
            }
        });
    }

    function loadSnippetsForTag(tagId, tagName) {
        console.log('태그 스니펫 로드:', tagId, tagName);

        $('#snippetsSection').show();
        $('#snippetsTitle').text('📝 "' + tagName + '" 태그의 스니펫 목록');
        $('#snippetsGrid').html('<div class="loading-text">스니펫을 불러오는 중...</div>');

        $.ajax({
            url: '/api/tag/' + tagId + '/snippets',
            method: 'GET',
            success: function (snippets) {
                console.log('스니펫 로드 성공:', snippets);
                displaySnippets(snippets, tagName);
            },
            error: function (xhr, status, error) {
                console.error('스니펫 로드 실패:', xhr.status, error);

                // API 실패시 더미 데이터 사용
                console.log('더미 데이터 사용 - tagId:', tagId);
                const snippetIds = tagSnippetMap[tagId] || [];
                const snippets = dummySnippets.filter(s => snippetIds.includes(s.id));
                displaySnippets(snippets, tagName);
            }
        });
    }

    function displaySnippets(snippets, tagName) {
        const container = $('#snippetsGrid');
        $('#snippetsTitle').text('📝 "' + tagName + '" 태그의 스니펫 (' + (snippets ? snippets.length : 0) + '개)');
        container.empty();

        if (!snippets || snippets.length === 0) {
            container.html('<div class="empty-state"><div class="emoji">📝</div><p>이 태그에 해당하는 스니펫이 없습니다.</p></div>');
            return;
        }

        snippets.forEach(snippet => {
            const card = createSnippetCard(snippet);
            container.append(card);
        });
    }

    function createSnippetCard(snippet) {
        const card = $('<div>').addClass('snippet-card');

        // 제목
        const header = $('<div>')
            .addClass('snippet-header')
            .text(snippet.title || snippet.memo || '제목 없음');

        // 메타 정보
        const metaContainer = $('<div>').addClass('snippet-meta');

        if (snippet.language) {
            metaContainer.append(
                $('<span>')
                    .addClass('snippet-meta-item snippet-language')
                    .text(snippet.language)
            );
        }

        if (snippet.type) {
            metaContainer.append(
                $('<span>')
                    .addClass('snippet-meta-item snippet-type')
                    .text(snippet.type)
            );
        }

        if (snippet.createdAt) {
            const date = new Date(snippet.createdAt).toLocaleDateString('ko-KR');
            metaContainer.append(
                $('<span>')
                    .addClass('snippet-meta-item snippet-date')
                    .text(date)
            );
        }

        // 내용
        const contentDiv = $('<div>')
            .addClass('snippet-body')
            .text(snippet.content || '내용 없음');

        // 텍스트 타입인 경우 다른 스타일 적용
        if (snippet.type === 'text') {
            contentDiv.addClass('text-type');
        }

        card.append(header);
        if (metaContainer.children().length > 0) {
            card.append(metaContainer);
        }
        card.append(contentDiv);

        return card;
    }

    function updateTagCount(count) {
        $('#tagCount').text('총 ' + count + '개의 태그');
    }

    // 삭제 모달 표시
    function showDeleteModal(tagId, tagName) {
        $('#deleteTagName').text(tagName);
        $('#deleteModal').show();

        // 기존 이벤트 제거 후 새로 바인딩
        $('#confirmDelete').off('click').on('click', function () {
            deleteTag(tagId, tagName);
            $('#deleteModal').hide();
        });
    }

    // 태그 삭제 함수
    function deleteTag(tagId, tagName) {
        console.log('태그 삭제:', tagId, tagName);

        $.ajax({
            url: '/api/tag/' + tagId,
            method: 'DELETE',
            success: function () {
                showAlert('"' + tagName + '" 태그가 삭제되었습니다.', 'success');

                // 삭제된 태그가 현재 선택된 태그라면 스니펫 섹션 숨기기
                if (selectedTagId == tagId) {
                    $('#snippetsSection').hide();
                    selectedTagId = null;
                }

                loadAllTags(); // 태그 목록 새로고침
            },
            error: function (xhr) {
                console.error('태그 삭제 실패:', xhr.status, xhr.responseText);
                if (xhr.status === 403) {
                    showAlert('태그 삭제 권한이 없습니다.', 'error');
                } else if (xhr.status === 401) {
                    showAlert('로그인이 필요합니다.', 'error');
                } else {
                    showAlert('태그 삭제 중 오류가 발생했습니다.', 'error');
                }
            }
        });
    }

    // 모달 닫기 이벤트
    $('#cancelDelete, .modal-close').on('click', function () {
        $('#deleteModal').hide();
    });

    // 모달 외부 클릭시 닫기
    $('#deleteModal').on('click', function (e) {
        if (e.target === this) {
            $(this).hide();
        }
    });
});

// 전역 함수들
function showAlert(message, type) {
    const alert = document.createElement('div');
    alert.className = `alert alert-${type}`;
    alert.textContent = message;

    // 아이콘 추가
    const icon = document.createElement('i');
    if (type === 'success') {
        icon.className = 'fas fa-check-circle';
    } else if (type === 'error') {
        icon.className = 'fas fa-exclamation-triangle';
    } else if (type === 'warning') {
        icon.className = 'fas fa-exclamation-circle';
    }

    alert.insertBefore(icon, alert.firstChild);
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
    if (sessionInfo && (sessionInfo.style.display === 'none' || sessionInfo.style.display === '')) {
        sessionInfo.style.display = 'block';
        loadSessionData();
    } else if (sessionInfo) {
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

        const sessionIdElement = document.getElementById('sessionId');
        if (sessionIdElement) {
            sessionIdElement.textContent = sessionId;
        }

        // 서버 세션 데이터 확인 API 호출
        try {
            const response = await fetch('/tag/check-session');
            if (response.ok) {
                const sessionData = await response.json();
                const serverSessionElement = document.getElementById('serverSessionData');
                if (serverSessionElement) {
                    serverSessionElement.innerHTML =
                        `userId: ${sessionData.userId || '없음'}, ` +
                        `sessionId: ${sessionData.sessionId || '없음'}`;
                }
            } else {
                const serverSessionElement = document.getElementById('serverSessionData');
                if (serverSessionElement) {
                    serverSessionElement.textContent = 'API 응답 오류 - 서버에서 확인하세요';
                }
            }
        } catch (error) {
            const serverSessionElement = document.getElementById('serverSessionData');
            if (serverSessionElement) {
                serverSessionElement.textContent = `세션 확인 API 없음 - ${error.message}`;
            }
            console.log('세션 확인 API 호출 실패:', error);
        }
    } catch (error) {
        console.error('세션 정보 로드 중 오류:', error);
        const serverSessionElement = document.getElementById('serverSessionData');
        if (serverSessionElement) {
            serverSessionElement.textContent = '오류 발생';
        }
    }
}