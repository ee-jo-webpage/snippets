$(document).ready(function() {
    // 초기화
    let allTags = [];
    let selectedTagId = null;

    // 로그인된 사용자만 태그 관리 기능 초기화
    if (typeof userId !== 'undefined' && userId != null) {
        // 초기 데이터 로드
        loadAllTags();

        // 태그 검색 입력 이벤트
        $('#tagSearchInput').on('input', function() {
            const keyword = $(this).val().trim();
            if (keyword.length > 0) {
                searchAndFilterTags(keyword);
            } else {
                displayAllTags(allTags);
            }
        });

        // 태그 검색 엔터키 이벤트
        $('#tagSearchInput').on('keydown', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                const tagName = $(this).val().trim();
                if (tagName) {
                    createNewTag(tagName);
                }
            }
        });

        // 태그 추가 버튼 클릭
        $('#addTagBtn').on('click', function() {
            const tagName = $('#tagSearchInput').val().trim();
            if (tagName) {
                createNewTag(tagName);
            } else {
                showAlert('태그명을 입력해주세요.', 'warning');
                $('#tagSearchInput').focus();
            }
        });

        // 태그 카드 클릭 이벤트 (AJAX 로딩 방식)
        $(document).on('click', '.tag-card', function(e) {
            // 삭제 버튼 클릭시 이벤트 전파 방지
            if ($(e.target).hasClass('tag-delete-btn')) {
                return;
            }

            const tagId = $(this).data('id');
            const tagName = $(this).find('.tag-name').text();

            if (tagId && tagName) {
                // 이전 선택 해제
                $('.tag-card').removeClass('active');
                // 현재 선택 활성화
                $(this).addClass('active');

                selectedTagId = tagId;
                // AJAX로 스니펫 섹션 로드
                loadSnippetSection(tagId, tagName);
            }
        });

        // 태그 삭제 버튼 클릭
        $(document).on('click', '.tag-delete-btn', function(e) {
            e.stopPropagation();
            const card = $(this).closest('.tag-card');
            const tagId = card.data('id');
            const tagName = card.find('.tag-name').text();
            showDeleteModal(tagId, tagName);
        });
    }

    // 알림 메시지 표시 (서버에서 전달된 메시지)
    if (typeof message !== 'undefined' && message) {
        showAlert(message, 'success');
    }
    if (typeof error !== 'undefined' && error) {
        showAlert(error, 'error');
    }

    // AJAX로 스니펫 섹션 로드하는 함수
    function loadSnippetSection(tagId, tagName) {
        console.log('스니펫 섹션 로드:', tagId, tagName);

        $('#snippetSectionContainer').html('<div class="loading-text">스니펫 섹션을 불러오는 중...</div>').show();

        $.ajax({
            url: '/components/snippet-section',  // 수정됨
            method: 'GET',
            data: { tagName: tagName },  // 추가됨
            success: function(data) {
                $('#snippetSectionContainer').html(data);
                if (typeof initializeSnippetSection === 'function') {
                    initializeSnippetSection(tagId, tagName);
                }
                showAlert(`"${tagName}" 태그의 스니펫을 불러왔습니다.`, 'success');
            },
            error: function(xhr, status, error) {
                console.error('스니펫 섹션 로드 실패:', error);
                $('#snippetSectionContainer').html(
                    '<div class="empty-state">' +
                    '<div class="emoji">❌</div>' +
                    '<h3>스니펫 섹션을 불러올 수 없습니다</h3>' +
                    '<p>서버 연결을 확인해주세요.</p>' +
                    '</div>'
                );
                showAlert('스니펫 섹션을 불러오는데 실패했습니다.', 'error');
            }
        });
    }

    // 태그 관리 함수들
    function loadAllTags() {
        console.log('태그 목록을 로드하는 중...');
        $('#tagsContainer').html('<div class="loading-text">태그를 불러오는 중...</div>');

        $.ajax({
            url: '/api/tag/my-tags',
            method: 'GET',
            success: function(tags) {
                console.log('태그 로드 성공:', tags);
                allTags = tags || [];
                displayAllTags(allTags);
                updateTagCount(allTags.length);
            },
            error: function(xhr, status, error) {
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

        tags.forEach((tag, index) => {
            const card = createTagCard(tag);
            // 애니메이션 지연 효과
            card.css('animation-delay', (index * 0.1) + 's');
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
            .attr('title', '태그 삭제');

        card.append(tagName).append(snippetCount).append(deleteBtn);

        // 스니펫 개수 비동기 로드
        loadTagSnippetCount(tag.tagId, snippetCount);

        return card;
    }

    function loadTagSnippetCount(tagId, countElement) {
        $.ajax({
            url: '/api/tag/' + tagId + '/snippets',
            method: 'GET',
            success: function(snippets) {
                const count = snippets ? snippets.length : 0;
                countElement.text(count);
            },
            error: function() {
                // API 실패시 랜덤 숫자 표시
                const count = Math.floor(Math.random() * 10) + 1;
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
            success: function(response) {
                console.log('태그 생성 성공:', response);
                showAlert('태그가 성공적으로 생성되었습니다.', 'success');
                $('#tagSearchInput').val('');
                loadAllTags(); // 태그 목록 새로고침
            },
            error: function(xhr) {
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

    function updateTagCount(count) {
        $('#tagCount').text('총 ' + count + '개의 태그');
    }

    // 삭제 모달 표시
    function showDeleteModal(tagId, tagName) {
        $('#deleteTagName').text(tagName);
        $('#deleteModal').show();

        // 기존 이벤트 제거 후 새로 바인딩
        $('#confirmDelete').off('click').on('click', function() {
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
            success: function() {
                showAlert('"' + tagName + '" 태그가 삭제되었습니다.', 'success');

                // 삭제된 태그가 현재 선택된 태그라면 스니펫 섹션 숨기기
                if (selectedTagId == tagId) {
                    selectedTagId = null;
                    $('#snippetSectionContainer').empty().hide();
                }

                loadAllTags(); // 태그 목록 새로고침
            },
            error: function(xhr) {
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
    $('#cancelDelete, .modal-close').on('click', function() {
        $('#deleteModal').hide();
    });

    // 모달 외부 클릭시 닫기
    $('#deleteModal').on('click', function(e) {
        if (e.target === this) {
            $(this).hide();
        }
    });

    // ESC 키로 모달 닫기
    $(document).on('keydown', function(e) {
        if (e.key === 'Escape' && $('#deleteModal').is(':visible')) {
            $('#deleteModal').hide();
        }
    });
});

// 전역 함수들
function showAlert(message, type) {
    const alert = $('<div>')
        .addClass(`alert alert-${type}`)
        .html(`<i class="fas fa-${getAlertIcon(type)}"></i> ${message}`);

    // 기존 알림 제거 후 새 알림 추가
    $('#alertContainer').empty().append(alert);

    // 3초 후 자동 제거
    setTimeout(() => {
        alert.fadeOut(300, function() {
            $(this).remove();
        });
    }, 3000);
}

function getAlertIcon(type) {
    switch(type) {
        case 'success': return 'check-circle';
        case 'error': return 'exclamation-triangle';
        case 'warning': return 'exclamation-circle';
        default: return 'info-circle';
    }
}

// 스니펫 섹션 초기화 함수 (AJAX 로드 후 호출됨)
// 스니펫 섹션 초기화 함수 (AJAX 로드 후 호출됨)
function initializeSnippetSection(tagId, tagName) {
    console.log('스니펫 섹션 초기화:', tagId, tagName);

    // 스니펫 섹션의 닫기 버튼 이벤트
    $('#closeSnippetsBtn').off('click').on('click', function() {
        $('#snippetSectionContainer').empty().hide();
        $('.tag-card').removeClass('active');
    });

    // 실제 스니펫 데이터 로드 추가
    loadSnippetsForTag(tagId, tagName);
}

// 세션 관련 함수들 (기존 코드에서 필요한 경우)
function toggleSessionInfo() {
    const sessionInfo = document.getElementById('sessionInfo');
    if (sessionInfo && (sessionInfo.style.display === 'none' || sessionInfo.style.display === '')) {
        sessionInfo.style.display = 'block';
        loadSessionData();
    } else if (sessionInfo) {
        sessionInfo.style.display = 'none';
    }
}

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

// 스니펫 데이터 로드 함수
function loadSnippetsForTag(tagId, tagName) {
    console.log('스니펫 데이터 로드:', tagId, tagName);

    // 스니펫 그리드에 로딩 표시
    $('#snippetsGrid').html('<div class="loading-text">스니펫을 불러오는 중...</div>');

    // 실제 API 호출
    $.ajax({
        url: '/api/tag/' + tagId + '/snippets',
        method: 'GET',
        success: function(snippets) {
            displaySnippets(snippets);
        },
        error: function() {
            // API 실패시 더미 데이터로 테스트
            console.log('API 실패, 더미 데이터 사용');
            const dummySnippets = [
                {
                    id: 1,
                    title: 'JavaScript 배열 맵',
                    content: 'const numbers = [1, 2, 3, 4, 5];\\nconst doubled = numbers.map(num => num * 2);',
                    language: 'javascript',
                    type: 'CODE',
                    hexCode: '#3b82f6',
                    colorName: 'Blue'
                }
            ];
            displaySnippets(dummySnippets);
        }
    });
}

// 스니펫 표시 함수
function displaySnippets(snippets) {
    const container = $('#snippetsGrid');
    container.empty();

    if (!snippets || snippets.length === 0) {
        container.html(
            '<div class="empty-state">' +
            '<div class="emoji">📝</div>' +
            '<p>이 태그에 해당하는 스니펫이 없습니다.</p>' +
            '</div>'
        );
        return;
    }

    snippets.forEach(snippet => {
        const card = createSnippetCard(snippet);
        container.append(card);
    });
}

// 스니펫 카드 생성 함수
function createSnippetCard(snippet) {
    const card = $('<div>').addClass('snippet-card bookmark-card');

    // 색상이 있는 경우
    if (snippet.hexCode) {
        card.addClass('has-color');
        card.css('border-left', '5px solid ' + snippet.hexCode);

        // 색상 인디케이터
        const colorIndicator = $('<div>')
            .addClass('color-indicator')
            .css('background-color', snippet.hexCode)
            .append($('<span>').addClass('color-name').text(snippet.colorName || 'Color'));
        card.append(colorIndicator);
    }

    // 메타 정보
    const metaContainer = $('<div>').addClass('snippet-meta');

    if (snippet.type) {
        const typeClass = snippet.type === 'CODE' ? 'type-code' :
            snippet.type === 'TEXT' ? 'type-text' : 'type-img';
        metaContainer.append(
            $('<span>')
                .addClass('meta-item ' + typeClass)
                .text(snippet.type)
        );
    }

    if (snippet.language) {
        metaContainer.append(
            $('<span>')
                .addClass('meta-item')
                .text(snippet.language)
        );
    }

    // 제목
    const title = $('<h3>')
        .addClass('bookmark-title')
        .text(snippet.title || snippet.memo || '제목 없음');

    // 콘텐츠 미리보기
    const contentPreview = $('<div>').addClass('snippet-content-preview');
    if (snippet.content) {
        const previewText = snippet.content.length > 100
            ? snippet.content.substring(0, 100) + '...'
            : snippet.content;
        contentPreview.text(previewText);
    } else {
        contentPreview.text('내용 없음');
    }

    // 더보기 인디케이터
    const moreIndicator = $('<div>')
        .addClass('more-indicator')
        .text('클릭하여 자세히 보기 →');

    // 카드 조립
    card.append(metaContainer);
    card.append(title);
    card.append(contentPreview);
    card.append(moreIndicator);

    // 클릭 이벤트 추가
    card.data('snippet', snippet);

    return card;
}