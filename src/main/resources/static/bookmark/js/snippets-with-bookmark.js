// 페이지 로드 시 북마크 상태 확인
$(document).ready(function() {
    const bookmarkedIds = /*[[${bookmarkedSnippetIds}]]*/ [];
    console.log('북마크된 스니펫 ID:', bookmarkedIds);
});

function toggleBookmark(button) {
    const snippetId = button.getAttribute('data-snippet-id');

    $.ajax({
        url: '/api/bookmarks/toggle',
        method: 'POST',
        data: { snippetId: snippetId },
        success: function(response) {
            if (response.success) {
                if (response.bookmarked) {
                    button.classList.add('bookmarked');
                    button.querySelector('.bookmark-icon').textContent = '★';
                    button.querySelector('.bookmark-text').textContent = '북마크됨';
                } else {
                    button.classList.remove('bookmarked');
                    button.querySelector('.bookmark-icon').textContent = '☆';
                    button.querySelector('.bookmark-text').textContent = '북마크';
                }
                showAlert(response.message, 'success');
            } else {
                showAlert(response.message, 'error');
            }
        },
        error: function(xhr) {
            if (xhr.status === 401) {
                showAlert('로그인이 필요합니다.', 'error');
            } else {
                showAlert('북마크 처리 중 오류가 발생했습니다.', 'error');
            }
        }
    });
}

function showAlert(message, type) {
    const alertDiv = document.getElementById('alertMessage');
    alertDiv.textContent = message;
    alertDiv.className = 'alert-' + type;
    alertDiv.style.display = 'block';

    setTimeout(function() {
        alertDiv.style.display = 'none';
    }, 3000);
}

