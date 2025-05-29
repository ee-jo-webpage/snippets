$(document).ready(function() {
    if (typeof userId !== 'undefined' && userId != null) {
        loadBookmarkedSnippets();
    }
});

function loadBookmarkedSnippets() {
    $('#bookmarkLoadingState').show();

    $.ajax({
        url: '/api/bookmarks/snippets',
        method: 'GET',
        success: function(response) {
            $('#bookmarkLoadingState').hide();

            if (response.success && response.snippets && response.snippets.length > 0) {
                // 스니펫 섹션 HTML 생성
                const sectionHtml = `
                    <div class="snippets-section">
                        <div class="section-header">
                            <h2>📚 내 북마크 (${response.count}개)</h2>
                        </div>
                        <div id="snippetsGrid" class="snippets-grid">
                            <!-- 스니펫 카드들이 동적으로 추가됨 -->
                        </div>
                    </div>
                `;

                $('#snippetSectionContainer').html(sectionHtml);

                // 스니펫 카드들 생성
                displayBookmarkedSnippets(response.snippets);

            } else {
                // 빈 상태 표시
                showEmptyBookmarkState();
            }
        },
        error: function(xhr) {
            $('#bookmarkLoadingState').hide();
            console.error('북마크 목록 로드 실패:', xhr.responseText);
            Utils.showAlert('북마크 목록을 불러오는데 실패했습니다.', 'error');
        }
    });
}

function displayBookmarkedSnippets(bookmarks) {
    const container = $('#snippetsGrid');
    container.empty();

    bookmarks.forEach((bookmark, index) => {
        const card = createBookmarkSnippetCard(bookmark, index);
        container.append(card);
    });
}

function createBookmarkSnippetCard(bookmark, index) {
    const card = $('<div>').addClass('snippet-card bookmark-card');

    // 스니펫 데이터 설정
    card.data('snippet-id', bookmark.snippetId);
    card.data('id', bookmark.snippetId);
    card.data('snippet', {
        id: bookmark.snippetId,
        snippetId: bookmark.snippetId,
        title: bookmark.memo || '제목 없음',
        content: bookmark.content,
        type: bookmark.type ? bookmark.type.name : 'Unknown',
        language: bookmark.language,
        createdAt: bookmark.createdAt,
        hexCode: bookmark.hexCode,
        colorName: bookmark.name,
        memo: bookmark.memo,
        sourceUrl: bookmark.sourceUrl,
        imageUrl: bookmark.imageUrl,
        altText: bookmark.altText
    });

    // 애니메이션 지연
    card.css('animation-delay', (index * 0.1) + 's');

    // 색상 처리
    if (bookmark.hexCode) {
        card.addClass('has-color');
        card.css('border-left', '5px solid ' + bookmark.hexCode);

        const colorIndicator = $('<div>')
            .addClass('color-indicator')
            .css('background-color', bookmark.hexCode)
            .append($('<span>').addClass('color-name').text(bookmark.name || 'Color'));
        card.append(colorIndicator);
    }

    // 북마크 제거 버튼
    const removeBtn = $('<button>')
        .addClass('bookmark-remove-btn')
        .attr('title', '북마크 제거')
        .html('<i class="fas fa-bookmark"></i>')
        .on('click', function(e) {
            e.stopPropagation();
            removeBookmarkDynamic(bookmark.snippetId, card);
        });
    card.append(removeBtn);

    // 메타 정보
    const metaContainer = $('<div>').addClass('snippet-meta');
    if (bookmark.type) {
        const typeClass = bookmark.type.name === 'CODE' ? 'type-code' :
            bookmark.type.name === 'TEXT' ? 'type-text' : 'type-img';
        metaContainer.append(
            $('<span>')
                .addClass('meta-item ' + typeClass)
                .text(bookmark.type.name)
        );
    }
    if (bookmark.language) {
        metaContainer.append($('<span>').addClass('meta-item').text(bookmark.language));
    }
    // metaContainer.append($('<span>').addClass('meta-item').text('#' + bookmark.snippetId));
    card.append(metaContainer);

    // 제목
    const title = $('<h3>')
        .addClass('bookmark-title')
        .text(bookmark.memo || (bookmark.content ? bookmark.content.substring(0, 50) + '...' : '제목 없음'));
    card.append(title);

    // 콘텐츠 미리보기
    const contentPreview = $('<div>').addClass('snippet-content-preview');
    if (bookmark.content) {
        const previewText = bookmark.content.length > 50
            ? bookmark.content.substring(0, 50) + '...'
            : bookmark.content;
        contentPreview.text(previewText);
    } else {
        contentPreview.text('내용 없음');
    }
    card.append(contentPreview);

    // 더보기 인디케이터
    const moreIndicator = $('<div>')
        .addClass('more-indicator')
        .text('클릭하여 자세히 보기 →');
    card.append(moreIndicator);

    return card;
}

function removeBookmarkDynamic(snippetId, cardElement) {
    if (!confirm('정말로 이 북마크를 제거하시겠습니까?')) {
        return;
    }

    $.ajax({
        url: '/api/bookmarks/remove',
        method: 'DELETE',
        data: { snippetId: snippetId },
        success: function(response) {
            if (response.success) {
                Utils.showAlert(response.message, 'success');
                cardElement.fadeOut(300, function() {
                    $(this).remove();
                    // 북마크가 모두 제거되었는지 확인
                    if ($('.snippet-card').length === 0) {
                        showEmptyBookmarkState();
                    }
                });
            } else {
                Utils.showAlert(response.message || '북마크 제거에 실패했습니다.', 'error');
            }
        },
        error: function(xhr) {
            console.error('북마크 제거 실패:', xhr.responseText);
            Utils.showAlert('북마크 제거 중 오류가 발생했습니다.', 'error');
        }
    });
}

function showEmptyBookmarkState() {
    const emptyHtml = `
        <div class="empty-state">
            <div class="emoji">🌟</div>
            <p>아직 북마크한 스니펫이 없습니다</p>
            <p>마음에 드는 스니펫을 북마크하여 나중에 쉽게 찾아보세요!</p>
            <a href="/snippets" class="btn btn-primary">
                <i class="fas fa-search"></i> 스니펫 둘러보기
            </a>
        </div>
    `;
    $('#snippetSectionContainer').html(emptyHtml);
}