$(document).ready(function() {
    // 고유한 ID로 요소 선택
    const searchInput = $('#bookmarkSearchInput');
    const typeFilter = $('#bookmarkTypeFilter');

    // 검색 및 필터링 기능
    function filterBookmarks() {
        const searchTerm = searchInput.val().toLowerCase();
        const selectedType = typeFilter.val();

        $('.bookmark-card').each(function() {
            const card = $(this);
            const title = card.find('.bookmark-title').text().toLowerCase();
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

        // 검색 결과가 없을 때 메시지 표시
        const visibleCards = $('.bookmark-card:visible').length;
        $('#bookmarkNoResults').remove();
        if (visibleCards === 0 && $('.bookmark-card').length > 0) {
            $('#bookmarksContainer').append(
                '<div id="bookmarkNoResults" class="empty-state">' +
                '<h3>🔍 검색 결과가 없습니다</h3>' +
                '<p>다른 검색어나 필터를 시도해보세요.</p>' +
                '</div>'
            );
        }
    }

    // 이벤트 리스너
    searchInput.on('input', filterBookmarks);
    typeFilter.on('change', filterBookmarks);
});

// 북마크 제거 함수 - REST API 사용으로 수정
function removeBookmark(snippetId) {
    if (!confirm('이 북마크를 제거하시겠습니까?')) {
        return;
    }

    $.ajax({
        url: '/api/bookmarks/remove',
        method: 'DELETE',
        data: { snippetId: snippetId },
        success: function(response) {
            if (response.success) {
                alert(response.message);
                location.reload();
            } else {
                alert(response.message);
            }
        },
        error: function(xhr) {
            alert('북마크 제거 중 오류가 발생했습니다.');
            console.error('Error:', xhr.responseText);
        }
    });
}