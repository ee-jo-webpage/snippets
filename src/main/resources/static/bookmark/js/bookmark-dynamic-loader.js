// 통합된 북마크 페이지 스크립트
$(document).ready(function() {
    console.log('북마크 페이지 초기화');
    console.log('사용자 ID:', userId);

    // SnippetModal이 로드되지 않은 경우를 대비한 체크
    if (typeof SnippetModal === 'undefined') {
        console.error('SnippetModal이 로드되지 않았습니다.');
    }

    // 사용자가 로그인된 경우 북마크 로드
    if (typeof userId !== 'undefined' && userId != null) {
        loadBookmarkedSnippets();
    }

    // 스니펫 카드 클릭 이벤트 (위임 방식)
    $(document).on('click', '.snippet-card', function(e) {
        // 북마크 제거 버튼 클릭은 제외
        if ($(e.target).closest('.bookmark-remove-btn').length > 0) {
            return;
        }

        const snippetId = $(this).data('snippet-id');
        console.log('스니펫 카드 클릭:', snippetId);

        if (!snippetId) {
            console.error('스니펫 ID를 찾을 수 없습니다.');
            return;
        }

        // 북마크용 모달 열기 (API 호출 포함)
        openBookmarkModal(snippetId);
    });
});

// API를 통해 북마크된 스니펫 목록 로드
function loadBookmarkedSnippets() {
    $('#bookmarkLoadingState').show();

    $.ajax({
        url: '/api/bookmarks/snippets',
        method: 'GET',
        success: function(response) {
            console.log('북마크 API 응답:', response);
            $('#bookmarkLoadingState').hide();

            if (response.success && response.snippets && response.snippets.length > 0) {
                displayBookmarkSection(response.snippets, response.count);
            } else {
                showEmptyBookmarkState();
            }
        },
        error: function(xhr) {
            $('#bookmarkLoadingState').hide();
            console.error('북마크 목록 로드 실패:', xhr.responseText);
            if (typeof Utils !== 'undefined') {
                Utils.showAlert('북마크 목록을 불러오는데 실패했습니다.', 'error');
            }
        }
    });
}

// 북마크 섹션 표시
function displayBookmarkSection(snippets, count) {
    const sectionHtml = `
        <div class="snippets-section">
            <div class="section-header">
                <h2>📚 내 북마크 (${count || snippets.length}개)</h2>
            </div>
            <div id="snippetsGrid" class="snippets-grid">
                <!-- 스니펫 카드들이 동적으로 추가됨 -->
            </div>
        </div>
    `;

    $('#snippetSectionContainer').html(sectionHtml);
    displayBookmarkedSnippets(snippets);
}

// 북마크된 스니펫 카드들 생성 및 표시
function displayBookmarkedSnippets(bookmarks) {
    const container = $('#snippetsGrid');
    container.empty();

    // API 응답 구조에 따라 처리
    bookmarks.forEach((bookmarkItem, index) => {
        // 새로운 API 구조 (상세 정보 포함)인지 확인
        let bookmark;
        if (bookmarkItem.snippet) {
            // 새로운 API 구조: { snippet: {...}, tags: [...], ... }
            bookmark = {
                ...bookmarkItem.snippet,
                tags: bookmarkItem.tags,
                ownerNickname: bookmarkItem.ownerNickname,
                snippetContent: bookmarkItem.snippetContent,
                isLiked: bookmarkItem.isLiked
            };
        } else {
            // 기존 구조
            bookmark = bookmarkItem;
        }

        const card = createBookmarkSnippetCard(bookmark, index);
        container.append(card);
    });
}

// 개별 북마크 카드 생성
function createBookmarkSnippetCard(bookmark, index) {
    const card = $('<div>').addClass('snippet-card bookmark-card');

    // 스니펫 데이터 설정
    const snippetData = {
        id: bookmark.snippetId,
        snippetId: bookmark.snippetId,
        title: bookmark.memo || bookmark.title || '제목 없음',
        content: bookmark.content,
        type: bookmark.type ? (bookmark.type.name || bookmark.type) : 'Unknown',
        language: bookmark.language,
        createdAt: bookmark.createdAt,
        hexCode: bookmark.hexCode,
        colorName: bookmark.name,
        memo: bookmark.memo,
        sourceUrl: bookmark.sourceUrl,
        imageUrl: bookmark.imageUrl,
        altText: bookmark.altText,
        // 추가 상세 정보
        tags: bookmark.tags,
        ownerNickname: bookmark.ownerNickname,
        snippetContent: bookmark.snippetContent,
        isLiked: bookmark.isLiked
    };

    card.data('snippet-id', bookmark.snippetId);
    card.data('id', bookmark.snippetId);
    card.data('snippet', snippetData);

    // 애니메이션 지연
    card.css('animation-delay', (index * 0.1) + 's');

    // 색상 처리
    if (bookmark.hexCode) {
        card.addClass('has-color');
        card.css('border', '5px solid ' + bookmark.hexCode);

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
    const typeName = bookmark.type ? (bookmark.type.name || bookmark.type) : 'Unknown';

    if (typeName) {
        const typeClass = typeName === 'CODE' ? 'type-code' :
            typeName === 'TEXT' ? 'type-text' : 'type-img';
        metaContainer.append(
            $('<span>')
                .addClass('meta-item ' + typeClass)
                .text(typeName)
        );
    }
    if (bookmark.language) {
        metaContainer.append($('<span>').addClass('meta-item').text(bookmark.language));
    }
    card.append(metaContainer);

    // 제목
    const title = $('<h3>')
        .addClass('bookmark-title')
        .text(bookmark.memo || bookmark.title ||
            (bookmark.content ? bookmark.content.substring(0, 50) + '...' : '제목 없음'));
    card.append(title);

    // 콘텐츠 미리보기
    const contentPreview = $('<div>').addClass('snippet-content-preview');

    // 타입별 미리보기 생성
    if (typeName === 'IMG') {
        if (bookmark.imageUrl) {
            const imgPreview = $('<div>').addClass('image-preview-container')
                .append($('<img>')
                    .attr('src', bookmark.imageUrl)
                    .attr('alt', bookmark.altText || '이미지')
                    .addClass('preview-image')
                    .on('error', function() {
                        $(this).parent().html('<div class="image-error">🖼️ 이미지 미리보기 없음</div>');
                    }));
            contentPreview.append(imgPreview);
        } else {
            contentPreview.html('<div class="image-error">🖼️ 이미지 URL 없음</div>');
        }
    } else if (typeName === 'CODE') {
        const codeContent = bookmark.content || '';
        const preview = codeContent.length > 100 ? codeContent.substring(0, 100) + '...' : codeContent;
        contentPreview.append($('<div>').addClass('code-preview')
            .append($('<div>').addClass('language-badge').text(bookmark.language || 'text'))
            .append($('<pre>').append($('<code>').text(preview))));
    } else {
        const textContent = bookmark.content || bookmark.memo || '';
        const preview = textContent.length > 150 ? textContent.substring(0, 150) + '...' : textContent;
        contentPreview.text(preview || '내용 없음');
    }

    card.append(contentPreview);

    // 더보기 인디케이터
    const moreIndicator = $('<div>')
        .addClass('more-indicator')
        .text('클릭하여 자세히 보기 →');
    card.append(moreIndicator);

    return card;
}

// 북마크 모달 열기 (API 호출 포함)
function openBookmarkModal(snippetId) {
    console.log('북마크 모달 열기:', snippetId);

    // 카드에서 기본 데이터 가져오기
    const cardElement = $(`.snippet-card[data-snippet-id="${snippetId}"]`);
    const basicSnippet = cardElement.data('snippet') || { id: snippetId, snippetId: snippetId, title: '제목 없음' };

    // 모달 표시
    const modal = $('#snippetDetailModal');
    modal.find('.modal-header h3').text(basicSnippet.title || '스니펫 상세보기');

    // 로딩 상태 표시
    const contentDiv = modal.find('.snippet-detail-content');
    contentDiv.html(`
        <div class="loading-spinner" style="text-align: center; padding: 40px;">
            <i class="fas fa-spinner fa-spin" style="font-size: 24px; color: #666;"></i>
            <p style="margin-top: 10px; color: #666;">북마크 상세 정보를 불러오는 중...</p>
        </div>
    `);

    modal.show();

    // 이미 상세 정보가 있는 경우 (새로운 API 응답에서 온 경우)
    if (basicSnippet.snippetContent) {
        console.log('캐시된 상세 정보 사용');
        updateModalWithDetailedInfo(modal, basicSnippet);
        return;
    }

    // API 호출하여 상세 정보 로드
    fetch(`/api/bookmarks/snippet/${snippetId}/detail`)
        .then(response => response.json())
        .then(data => {
            console.log('API 응답:', data);

            if (data.success) {
                // API에서 가져온 상세 정보로 업데이트
                const detailedSnippet = {
                    ...basicSnippet,
                    ...data.data.snippet,
                    snippetContent: data.data.snippetContent,
                    tags: data.data.tags,
                    isLiked: data.data.isLiked,
                    ownerNickname: data.data.ownerNickname,
                    actualLikeCount: data.data.actualLikeCount,
                    isBookmarked: true
                };

                updateModalWithDetailedInfo(modal, detailedSnippet);
            } else {
                console.error('API 호출 실패:', data.message);
                updateModalWithDetailedInfo(modal, basicSnippet);
            }
        })
        .catch(error => {
            console.error('API 호출 오류:', error);
            updateModalWithDetailedInfo(modal, basicSnippet);
        });
}

// 모달 상세 정보 업데이트
function updateModalWithDetailedInfo(modal, snippet) {
    console.log('모달 업데이트:', snippet);

    // SnippetModal이 있는 경우 사용
    if (typeof SnippetModal !== 'undefined' && SnippetModal.updateContent) {
        SnippetModal.updateContent(modal, snippet);
    } else {
        // 대체 구현
        displayBasicContent(modal, snippet);
    }

    // 태그 정보 업데이트
    if (typeof SnippetTags !== 'undefined' && snippet.tags) {
        SnippetTags.updateTags(snippet.tags);
    }
}

// 기본 콘텐츠 표시 (SnippetModal이 없는 경우 대체)
function displayBasicContent(modal, snippet) {
    const contentDiv = modal.find('.snippet-detail-content');
    const snippetContent = snippet.snippetContent || {};

    let contentHtml = '';

    if (snippet.type === 'IMG' || snippet.type === 'IMAGE') {
        const imageUrl = snippetContent.imageUrl || snippet.imageUrl;
        if (imageUrl) {
            contentHtml = `
                <div class="image-content">
                    <img src="${imageUrl}" alt="${snippet.altText || '이미지'}" style="max-width: 100%; height: auto;">
                    ${snippet.altText ? `<p><strong>설명:</strong> ${escapeHtml(snippet.altText)}</p>` : ''}
                </div>
            `;
        } else {
            contentHtml = '<div class="error">이미지 URL이 없습니다.</div>';
        }
    } else if (snippet.type === 'CODE') {
        const content = snippetContent.content || snippet.content || '';
        const language = snippetContent.language || snippet.language || 'text';
        contentHtml = `
            <div class="code-content">
                <div class="language-badge">${language}</div>
                <pre><code>${escapeHtml(content)}</code></pre>
            </div>
        `;
    } else {
        const content = snippetContent.content || snippet.content || '';
        contentHtml = `
            <div class="text-content">
                <div>${escapeHtml(content).replace(/\n/g, '<br>')}</div>
            </div>
        `;
    }

    contentDiv.html(contentHtml);
}

// 북마크 제거 (동적)
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
                if (typeof Utils !== 'undefined') {
                    Utils.showAlert(response.message, 'success');
                }
                cardElement.fadeOut(300, function() {
                    $(this).remove();
                    // 북마크가 모두 제거되었는지 확인
                    if ($('.snippet-card').length === 0) {
                        showEmptyBookmarkState();
                    }
                });
            } else {
                if (typeof Utils !== 'undefined') {
                    Utils.showAlert(response.message || '북마크 제거에 실패했습니다.', 'error');
                }
            }
        },
        error: function(xhr) {
            console.error('북마크 제거 실패:', xhr.responseText);
            if (typeof Utils !== 'undefined') {
                Utils.showAlert('북마크 제거 중 오류가 발생했습니다.', 'error');
            }
        }
    });
}

// 빈 북마크 상태 표시
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

// HTML 이스케이프 함수
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
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