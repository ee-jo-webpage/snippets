$(document).ready(function () {
    const searchInput = $('#searchInput');
    const languageFilter = $('#languageFilter');
    const typeFilter = $('#typeFilter');
    const bookmarksContainer = $('#bookmarksContainer');

    // 검색 및 필터링 기능
    function filterBookmarks() {
        const searchTerm = searchInput.val().toLowerCase();
        const selectedLanguage = languageFilter.val();
        const selectedType = typeFilter.val();

        $('.bookmark-card').each(function () {
            const card = $(this);
            const title = card.find('.bookmark-title').text().toLowerCase();
            const content = card.find('.snippet-content').text().toLowerCase();
            const tags = card.find('.tag-item').map(function () {
                return $(this).text().toLowerCase();
            }).get().join(' ');

            const language = card.data('language') || '';
            const type = card.data('type') || '';

            const matchesSearch = !searchTerm ||
                title.includes(searchTerm) ||
                content.includes(searchTerm) ||
                tags.includes(searchTerm);

            const matchesLanguage = !selectedLanguage || language === selectedLanguage;
            const matchesType = !selectedType || type === selectedType;

            if (matchesSearch && matchesLanguage && matchesType) {
                card.show();
            } else {
                card.hide();
            }
        });

        // 검색 결과가 없을 때 메시지 표시
        const visibleCards = $('.bookmark-card:visible').length;
        if (visibleCards === 0 && $('.bookmark-card').length > 0) {
            if ($('#noResults').length === 0) {
                bookmarksContainer.append(
                    '<div id="noResults" class="empty-state">' +
                    '<h3>🔍 검색 결과가 없습니다</h3>' +
                    '<p>다른 검색어나 필터를 시도해보세요.</p>' +
                    '</div>'
                );
            }
        } else {
            $('#noResults').remove();
        }
    }

    // 이벤트 리스너
    searchInput.on('input', filterBookmarks);
    languageFilter.on('change', filterBookmarks);
    typeFilter.on('change', filterBookmarks);

    // 언어 필터 옵션 동적 생성
    const languages = new Set();
    $('.bookmark-card').each(function () {
        const language = $(this).data('language');
        if (language) {
            languages.add(language);
        }
    });

    languages.forEach(language => {
        if ($(`#languageFilter option[value="${language}"]`).length === 0) {
            languageFilter.append(`<option value="${language}">${language}</option>`);
        }
    });
});

// 북마크 제거 함수
function removeBookmark(snippetId) {
    if (!confirm('이 북마크를 제거하시겠습니까?')) {
        return;
    }

    $.ajax({
        url: /*[[@{/bookmarks/remove}]]*/ '/bookmarks/remove',
        method: 'DELETE',
        data: {snippetId: snippetId},
        success: function (response) {
            alert('북마크가 제거되었습니다.');
            location.reload(); // 페이지 새로고침
        },
        error: function (xhr) {
            alert('북마크 제거 중 오류가 발생했습니다.');
            console.error('Error:', xhr.responseText);
        }
    });
}

// 툴팁 기능 (선택사항)
$(document).ready(function () {
    $('[title]').tooltip();
});