$(document).ready(function() {
    // 로그인한 사용자만 북마크 상태 확인
    var userId = /*[[${userId}]]*/ null;
    if (userId) {
        checkAllBookmarkStatus();
    }
});

// 모든 스니펫의 북마크 상태 확인
function checkAllBookmarkStatus() {
    $('.bookmark-btn').each(function() {
        const button = $(this);
        const snippetId = button.data('snippet-id');
        checkBookmarkStatus(snippetId, button);
    });
}

// 개별 스니펫의 북마크 상태 확인
function checkBookmarkStatus(snippetId, buttonElement) {
    $.ajax({
        url: '/api/bookmarks/check',
        method: 'GET',
        data: { snippetId: snippetId },
        success: function(response) {
            if (response.success && response.bookmarked) {
                updateBookmarkButton(buttonElement, true);
            }
        },
        error: function(xhr) {
            console.log('북마크 상태 확인 실패:', xhr.responseText);
        }
    });
}

// 북마크 토글
function toggleBookmark(buttonElement) {
    const button = $(buttonElement);
    const snippetId = button.data('snippet-id');

    // 세션 userId 체크
    var userId = /*[[${userId}]]*/ null;
    if (!userId) {
        showAlert('세션에 사용자 정보가 없습니다. 새로고침 후 다시 시도해주세요.', 'error');
        return;
    }

    // 로딩 상태
    button.addClass('loading');

    $.ajax({
        url: '/api/bookmarks/toggle',
        method: 'POST',
        data: { snippetId: snippetId },
        success: function(response) {
            button.removeClass('loading');

            if (response.success) {
                updateBookmarkButton(button, response.bookmarked);
                showAlert(response.message, 'success');
            } else {
                showAlert(response.message, 'error');
            }
        },
        error: function(xhr) {
            button.removeClass('loading');
            console.error('북마크 토글 실패:', xhr.responseText);
            showAlert('북마크 처리 중 오류가 발생했습니다.', 'error');
        }
    });
}

// 북마크 버튼 상태 업데이트
function updateBookmarkButton(button, isBookmarked) {
    const icon = button.find('.bookmark-icon');
    const text = button.find('.bookmark-text');

    if (isBookmarked) {
        button.addClass('bookmarked');
        icon.text('★');
        text.text('북마크됨');
    } else {
        button.removeClass('bookmarked');
        icon.text('☆');
        text.text('북마크');
    }
}

// 알림 메시지 표시
function showAlert(message, type) {
    const alertDiv = $('#alertMessage');
    alertDiv.removeClass('alert-success alert-error alert-warning')
        .addClass('alert alert-' + type)
        .text(message)
        .show();

    // 3초 후 자동 숨김
    setTimeout(function() {
        alertDiv.fadeOut();
    }, 3000);
}