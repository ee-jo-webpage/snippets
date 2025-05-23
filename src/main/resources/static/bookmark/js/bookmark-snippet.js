// 세션에서 사용자 ID 가져오기 (서버에서 전달)
let currentUserId = /*[[${session.userId}]]*/ null;

$(document).ready(function() {
    // 페이지 로드 시 모든 스니펫의 북마크 상태 확인
    loadBookmarkStates();

    // 검색 및 필터링 기능
    const searchInput = $('#searchInput');
    const typeFilter = $('#typeFilter');

    function filterSnippets() {
        const searchTerm = searchInput.val().toLowerCase();
        const selectedType = typeFilter.val();

        $('.snippet-card').each(function() {
            const card = $(this);
            const title = card.find('.snippet-title').text().toLowerCase();
            const memo = card.data('memo') ? card.data('memo').toString().toLowerCase() : '';
            const snippetId = card.find('.snippet-id').text().toLowerCase();
            const type = card.data('type') || '';

            const matchesSearch = !searchTerm ||
                title.includes(searchTerm) ||
                memo.includes(searchTerm) ||
                snippetId.includes(searchTerm);

            const matchesType = !selectedType || type === selectedType;

            if (matchesSearch && matchesType) {
                card.show();
            } else {
                card.hide();
            }
        });

        updateNoResultsMessage();
    }

    function updateNoResultsMessage() {
        const visibleCards = $('.snippet-card:visible').length;
        $('#noResults').remove();
        if (visibleCards === 0 && $('.snippet-card').length > 0) {
            $('#snippetsContainer').append(
                '<div id="noResults" class="empty-state">' +
                '<h3>🔍 검색 결과가 없습니다</h3>' +
                '<p>다른 검색어나 필터를 시도해보세요.</p>' +
                '</div>'
            );
        }
    }

    // 이벤트 리스너
    searchInput.on('input', filterSnippets);
    typeFilter.on('change', filterSnippets);
});

// 모든 스니펫의 북마크 상태 로드
function loadBookmarkStates() {
    if (!currentUserId) return;

    $('.btn-bookmark').each(function() {
        const button = $(this);
        const snippetId = button.data('snippet-id');
        checkBookmarkStatus(snippetId, button);
    });
}

// 개별 북마크 상태 확인
function checkBookmarkStatus(snippetId, button) {
    $.get(`/api/bookmarks/check/${snippetId}`)
        .done(function(response) {
            updateBookmarkButton(button, response.isBookmarked);
        })
        .fail(function() {
            console.warn(`북마크 상태 확인 실패: ${snippetId}`);
        });
}

// 북마크 토글
function toggleBookmark(element) {
    if (!currentUserId) {
        showToast('로그인이 필요합니다.', 'error');
        return;
    }

    const button = $(element);
    const snippetId = button.data('snippet-id');
    const isCurrentlyBookmarked = button.hasClass('bookmarked');

    // 버튼 비활성화 및 로딩 표시
    button.prop('disabled', true);
    const originalContent = button.html();
    button.html('<span class="loading-spinner"></span> 처리중...');

    const url = isCurrentlyBookmarked ?
        `/api/bookmarks/${snippetId}` :
        '/api/bookmarks';
    const method = isCurrentlyBookmarked ? 'DELETE' : 'POST';
    const data = isCurrentlyBookmarked ? null : { snippetId: snippetId };

    $.ajax({
        url: url,
        method: method,
        contentType: 'application/json',
        data: data ? JSON.stringify(data) : null,
        success: function(response) {
            if (response.success) {
                updateBookmarkButton(button, !isCurrentlyBookmarked);
                showToast(response.message);
            } else {
                showToast(response.message, 'error');
            }
        },
        error: function(xhr) {
            const errorMessage = xhr.responseJSON?.message || '오류가 발생했습니다.';
            showToast(errorMessage, 'error');
        },
        complete: function() {
            // 버튼 복원
            button.prop('disabled', false);
            if (!button.hasClass('bookmarked') && !button.hasClass('processing')) {
                button.html(originalContent);
            }
        }
    });
}

// 북마크 버튼 상태 업데이트
function updateBookmarkButton(button, isBookmarked) {
    const icon = button.find('.bookmark-icon');
    const text = button.find('.bookmark-text');

    if (isBookmarked) {
        button.addClass('bookmarked');
        icon.text('⭐');
        text.text('북마크됨');
        button.removeClass('processing');
    } else {
        button.removeClass('bookmarked');
        icon.text('☆');
        text.text('북마크');
        button.removeClass('processing');
    }
}

// 토스트 메시지 표시
function showToast(message, type = 'success') {
    const toast = $('#toast');
    toast.removeClass('error').text(message);

    if (type === 'error') {
        toast.addClass('error');
    }

    toast.fadeIn(300);
    setTimeout(function() {
        toast.fadeOut(300);
    }, 3000);
}