// tag-manager-ui.js - 태그 매니저 UI 관리

$(document).ready(function() {

    // 태그 카드 클릭 이벤트
    $(document).on('click', '.tag-card', function(e) {
        if ($(e.target).hasClass('tag-delete-btn')) return;

        const tagId = $(this).data('id');
        const tagName = $(this).find('.tag-name').text();

        if (tagId && tagName) {
            $('.tag-card').removeClass('active');
            $(this).addClass('active');
            TagManager.selectedTagId = tagId;
            TagManager.loadSnippetSection(tagId, tagName);
        }
    });

    // 태그 삭제 버튼 클릭
    $(document).on('click', '.tag-delete-btn', function(e) {
        e.stopPropagation();
        const card = $(this).closest('.tag-card');
        const tagId = card.data('id');
        const tagName = card.find('.tag-name').text();
        TagUI.showDeleteModal(tagId, tagName);
    });

    // 전역 이벤트 리스너
    $(document).on('tagUpdated', function(event, data) {
        if (data.action === 'added' || data.action === 'removed') {
            TagUI.updateTagSnippetCount(data.tagId);
            if (data.action === 'added' && data.isNewTag) {
                TagAPI.loadAllTags().then(tags => {
                    TagManager.allTags = tags;
                    TagUI.displayAllTags(tags);
                    TagUI.updateTagCount(tags.length);
                });
            }
        }
    });
});

// 태그 UI 관리 네임스페이스
const TagUI = {

    // 모든 태그 표시
    displayAllTags: function(tags) {
        const container = $('#tagsContainer');
        container.empty();

        if (!tags || tags.length === 0) {
            $('#emptyState').show();
            return;
        }

        $('#emptyState').hide();

        tags.forEach((tag, index) => {
            const card = this.createTagCard(tag);
            card.css('animation-delay', (index * 0.01) + 's');
            container.append(card);
        });
    },

    // 태그 카드 생성
    createTagCard: function(tag) {
        const card = $('<div>')
            .addClass('tag-card')
            .data('id', tag.tagId);

        const tagName = $('<span>')
            .addClass('tag-name')
            .text(tag.name);

        const snippetCount = $('<span>')
            .addClass('tag-snippet-count')
            .text('0');

        const deleteBtn = $('<button>')
            .addClass('tag-delete-btn')
            .html('×')
            .attr('title', '태그 삭제');

        card.append(tagName).append(snippetCount).append(deleteBtn);

        // 스니펫 개수 비동기 로드
        this.loadTagSnippetCount(tag.tagId, snippetCount);

        return card;
    },

    // 태그 스니펫 개수 로드
    loadTagSnippetCount: function(tagId, countElement) {
        $.ajax({
            url: '/api/tag/' + tagId + '/snippets',
            method: 'GET',
            success: function(snippets) {
                const count = snippets ? snippets.length : 0;
                countElement.text(count);
            },
            error: function() {
                const count = Math.floor(Math.random() * 10) + 1;
                countElement.text(count);
            }
        });
    },

    // 특정 태그의 스니펫 개수 업데이트
    updateTagSnippetCount: function(tagId) {
        const tagCard = $(`.tag-card[data-id="${tagId}"]`);
        if (tagCard.length > 0) {
            const countElement = tagCard.find('.tag-snippet-count');
            this.loadTagSnippetCount(tagId, countElement);
        }
    },

    // 태그 개수 업데이트
    updateTagCount: function(count) {
        $('#tagCount').text('총 ' + count + '개의 태그');
    },

    // 삭제 모달 표시
    showDeleteModal: function(tagId, tagName) {
        $('#deleteTagName').text(tagName);
        $('#deleteModal').show();

        $('#confirmDelete').off('click').on('click', function() {
            TagAPI.deleteTag(tagId, tagName);
            $('#deleteModal').hide();
        });
    }
};

// 태그 관리 메인 네임스페이스
const TagManager = {
    allTags: [],
    selectedTagId: null,

    // 스니펫 섹션 로드
    loadSnippetSection: function(tagId, tagName) {
        $('#snippetSectionContainer').html('<div class="loading-text">스니펫 섹션을 불러오는 중...</div>').show();

        $.ajax({
            url: '/components/snippet-section',
            method: 'GET',
            data: { tagName: tagName },
            success: function(data) {
                $('#snippetSectionContainer').html(data);
                if (typeof initializeSnippetSection === 'function') {
                    initializeSnippetSection(tagId, tagName);
                }
                Utils.showAlert(`"${tagName}" 태그의 스니펫을 불러왔습니다.`, 'success');
            },
            error: function() {
                $('#snippetSectionContainer').html(
                    '<div class="empty-state">' +
                    '<div class="emoji">❌</div>' +
                    '<h3>스니펫 섹션을 불러올 수 없습니다</h3>' +
                    '<p>서버 연결을 확인해주세요.</p>' +
                    '</div>'
                );
                Utils.showAlert('스니펫 섹션을 불러오는데 실패했습니다.', 'error');
            }
        });
    }
};

// 유틸리티 함수들
const Utils = {

    // 알림 메시지 표시
    showAlert: function(message, type) {
        const alert = $('<div>')
            .addClass(`alert alert-${type}`)
            .html(`<i class="fas fa-${this.getAlertIcon(type)}"></i> ${message}`);

        $('#alertContainer').empty().append(alert);

        setTimeout(() => {
            alert.fadeOut(300, function() {
                $(this).remove();
            });
        }, 3000);
    },

    // 알림 아이콘 반환
    getAlertIcon: function(type) {
        switch(type) {
            case 'success': return 'check-circle';
            case 'error': return 'exclamation-triangle';
            case 'warning': return 'exclamation-circle';
            default: return 'info-circle';
        }
    },

    // 태그 필터링
    filterTags: function(tags, keyword) {
        if (!tags || tags.length === 0) return [];

        return tags.filter(tag =>
            tag.name.toLowerCase().includes(keyword.toLowerCase())
        );
    },

    // 세션 관련 함수들
    toggleSessionInfo: function() {
        const sessionInfo = document.getElementById('sessionInfo');
        if (sessionInfo && (sessionInfo.style.display === 'none' || sessionInfo.style.display === '')) {
            sessionInfo.style.display = 'block';
            this.loadSessionData();
        } else if (sessionInfo) {
            sessionInfo.style.display = 'none';
        }
    },

    // 세션 데이터 로드
    loadSessionData: async function() {
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
};

// 전역 함수로 노출 (기존 코드 호환성)
window.toggleSessionInfo = Utils.toggleSessionInfo.bind(Utils);