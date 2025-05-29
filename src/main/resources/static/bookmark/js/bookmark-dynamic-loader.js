// 통합된 북마크 페이지 스크립트
$(document).ready(function() {
    console.log('북마크 페이지 초기화');
    console.log('사용자 ID:', userId);

    // 사용자가 로그인된 경우 북마크 로드
    if (typeof userId !== 'undefined' && userId != null) {
        loadBookmarkedSnippets();
    }

    // 북마크 페이지에서는 기존 SnippetModal 이벤트를 오버라이드
    $(document).off('click', '.snippet-card'); // 기존 이벤트 제거

    // 북마크 전용 스니펫 카드 클릭 이벤트
    $(document).on('click', '.snippet-card', function(e) {
        // 북마크 제거 버튼 클릭은 제외
        if ($(e.target).closest('.bookmark-remove-btn').length > 0) {
            return;
        }

        const snippetId = $(this).data('snippet-id');
        console.log('북마크 스니펫 카드 클릭:', snippetId);

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

    // 기본 색상 적용 (로딩 중에도 색상 표시)
    if (basicSnippet.hexCode) {
        const modalContent = modal.find('.modal-content');
        modalContent.css({
            'border-left': `6px solid ${basicSnippet.hexCode}`,
            'box-shadow': `0 4px 20px rgba(0,0,0,0.1), -2px 0 0 0 ${basicSnippet.hexCode}`
        });

        const modalHeader = modal.find('.modal-header');
        modalHeader.css({
            'border-bottom': `2px solid ${basicSnippet.hexCode}`,
            'background': `linear-gradient(90deg, ${basicSnippet.hexCode}15 0%, transparent 100%)`
        });
    }

    // 로딩 상태 표시
    const contentDiv = modal.find('.snippet-detail-content');
    contentDiv.html(`
        <div class="loading-spinner" style="text-align: center; padding: 40px;">
            <i class="fas fa-spinner fa-spin" style="font-size: 24px; color: ${basicSnippet.hexCode || '#666'};"></i>
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

    // 북마크 페이지 전용 모달 업데이트
    updateBookmarkModalContent(modal, snippet);

    // 태그 정보 업데이트 (안전하게 처리)
    updateSnippetTags(snippet.tags);
}

// 북마크 전용 모달 콘텐츠 업데이트
function updateBookmarkModalContent(modal, snippet) {
    // 모달에 색상 테두리 적용
    const modalContent = modal.find('.modal-content');
    if (snippet.hexCode) {
        modalContent.css({
            'border-left': `6px solid ${snippet.hexCode}`,
            'box-shadow': `0 4px 20px rgba(0,0,0,0.1), -2px 0 0 0 ${snippet.hexCode}`
        });

        // 모달 헤더에도 색상 포인트 추가
        const modalHeader = modal.find('.modal-header');
        modalHeader.css({
            'border-bottom': `2px solid ${snippet.hexCode}`,
            'background': `linear-gradient(90deg, ${snippet.hexCode}15 0%, transparent 100%)`
        });
    } else {
        // 색상이 없는 경우 기본 스타일로 리셋
        modalContent.css({
            'border-left': '',
            'box-shadow': '0 4px 20px rgba(0,0,0,0.1)'
        });

        const modalHeader = modal.find('.modal-header');
        modalHeader.css({
            'border-bottom': '1px solid #dee2e6',
            'background': ''
        });
    }

    // 메타 정보 업데이트
    const metaDiv = modal.find('.snippet-detail-meta');
    metaDiv.empty();

    if (snippet.type) {
        metaDiv.append(`<div class="meta-row"><strong>타입:</strong> ${snippet.type}</div>`);
    }
    if (snippet.language) {
        metaDiv.append(`<div class="meta-row"><strong>언어:</strong> ${snippet.language}</div>`);
    }
    if (snippet.createdAt) {
        const date = new Date(snippet.createdAt).toLocaleDateString('ko-KR');
        metaDiv.append(`<div class="meta-row"><strong>생성일:</strong> ${date}</div>`);
    }
    if (snippet.ownerNickname) {
        metaDiv.append(`<div class="meta-row"><strong>작성자:</strong> ${snippet.ownerNickname}</div>`);
    }
    // 출처를 작성자 아래에 추가
    if (snippet.sourceUrl) {
        metaDiv.append(`
            <div class="meta-row">
                <strong>출처:</strong> 
                <a href="${snippet.sourceUrl}" target="_blank" style="
                    color: ${snippet.hexCode || '#007bff'}; 
                    text-decoration: none;
                    word-break: break-all;
                " onmouseover="this.style.textDecoration='underline'" 
                   onmouseout="this.style.textDecoration='none'">
                    ${snippet.sourceUrl}
                </a>
            </div>
        `);
    }
    if (snippet.hexCode && snippet.colorName) {
        const colorDiv = $('<div class="meta-row color-meta"><strong>색상:</strong> </div>');
        colorDiv.append(`
            <span class="color-preview" style="
                background-color: ${snippet.hexCode}; 
                width: 24px; 
                height: 24px; 
                display: inline-block; 
                margin-right: 8px; 
                border-radius: 4px; 
                border: 2px solid ${snippet.hexCode}40;
                box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                vertical-align: middle;
            "></span> 
            <span style="vertical-align: middle;">${snippet.colorName}</span>
        `);
        metaDiv.append(colorDiv);
    }

    // 콘텐츠 업데이트
    const contentDiv = modal.find('.snippet-detail-content');
    contentDiv.empty();

    const contentHtml = renderBookmarkSnippetContent(snippet);
    contentDiv.html(contentHtml);

    // 메모를 태그 섹션 위에 추가
    addMemoSection(modal, snippet);
}

// 북마크 스니펫 콘텐츠 렌더링
function renderBookmarkSnippetContent(snippet) {
    const snippetContent = snippet.snippetContent || {};

    if (snippet.type === 'IMG' || snippet.type === 'IMAGE') {
        const imageUrl = snippetContent.imageUrl || snippet.imageUrl;
        const altText = snippetContent.altText || snippet.altText || snippet.memo || '이미지';

        if (!imageUrl) {
            return `
                <div class="bookmark-modal-content image-content">
                    <div class="image-error">🖼️ 이미지 URL이 설정되지 않았습니다</div>
                </div>
            `;
        }

        return `
            <div class="bookmark-modal-content image-content">
                <div class="image-container">
                    <img src="${imageUrl}" 
                         alt="${escapeHtml(altText)}" 
                         class="modal-image"
                         onclick="showImageZoom('${imageUrl}')"
                         style="max-width: 100%; height: auto; border-radius: 8px;"
                         onerror="this.parentNode.innerHTML='<div class=\\'image-error\\'>🖼️ 이미지를 불러올 수 없습니다</div>'">
                    <div class="image-actions" style="margin-top: 10px;">
                        <button class="image-zoom-btn" onclick="showImageZoom('${imageUrl}')" 
                                style="margin-right: 10px; padding: 8px 12px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer;">
                            <i class="fas fa-search-plus"></i> 확대
                        </button>
                        <button class="image-download-btn" onclick="downloadImage('${imageUrl}', '${escapeHtml(altText)}')"
                                style="padding: 8px 12px; background: #28a745; color: white; border: none; border-radius: 4px; cursor: pointer;">
                            <i class="fas fa-download"></i> 다운로드
                        </button>
                    </div>
                </div>
                ${altText && altText !== snippet.memo ? `<div class="image-info" style="margin-top: 15px;"><strong>설명:</strong> ${escapeHtml(altText)}</div>` : ''}
            </div>
        `;

    } else if (snippet.type === 'CODE') {
        const language = snippetContent.language || snippet.language || 'text';
        const content = snippetContent.content || snippet.content || '코드 내용이 없습니다.';

        return `
            <div class="bookmark-modal-content code-content">
                <div class="code-container" style="position: relative;">
                    <pre class="code-block" style="background-color : black; padding: 15px; border-radius: 8px; overflow-x: auto; margin: 0;"><code class="language-${language}">${escapeHtml(content)}</code></pre>
                </div>
            </div>
        `;

    } else {
        // TEXT 타입 또는 기타
        const content = snippetContent.content || snippet.content || '텍스트 내용이 없습니다.';

        return `
            <div class="bookmark-modal-content text-content">
                <div class="text-container" style="position: relative;">
                    <div class="text-content-body" style="background: #f8f9fa; padding: 15px; border-radius: 8px; line-height: 1.6;">${escapeHtml(content).replace(/\n/g, '<br>')}</div>
                </div>
            </div>
        `;
    }
}

// 메모 섹션 추가 (태그 위에 배치)
function addMemoSection(modal, snippet) {
    const tagsSection = modal.find('.snippet-tags-section');

    // 기존 메모 섹션 제거
    modal.find('.snippet-memo-section').remove();

    if (snippet.memo && snippet.memo.trim()) {
        const memoSection = $(`
            <div class="snippet-memo-section" style="margin-bottom: 20px;">
                <div class="memo-container" style="
                    background: #fffbf0;
                    border: 2px solid ${snippet.hexCode || '#ffd700'};
                    border-radius: 8px;
                    padding: 15px;
                    position: relative;
                    box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                ">
                    <div class="memo-header" style="
                        display: flex;
                        align-items: center;
                        margin-bottom: 10px;
                        color: ${snippet.hexCode || '#d4a017'};
                        font-weight: bold;
                    ">
                        <i class="fas fa-sticky-note" style="
                            margin-right: 8px;
                            font-size: 18px;
                        "></i>
                        <span>메모</span>
                    </div>
                    <div class="memo-content" style="
                        color: #444;
                        line-height: 1.6;
                        font-size: 14px;
                        white-space: pre-wrap;
                        word-wrap: break-word;
                    ">${escapeHtml(snippet.memo)}</div>
                    <div class="memo-corner" style="
                        position: absolute;
                        top: -2px;
                        right: -2px;
                        width: 0;
                        height: 0;
                        border-left: 20px solid transparent;
                        border-top: 20px solid ${snippet.hexCode || '#ffd700'};
                    "></div>
                </div>
            </div>
        `);

        // 태그 섹션 바로 위에 메모 섹션 추가
        if (tagsSection.length > 0) {
            tagsSection.before(memoSection);
        } else {
            // 태그 섹션이 없으면 모달 바디 끝에 추가
            modal.find('.modal-body').append(memoSection);
        }
    }
}

// 태그 정보 업데이트 (안전하게 처리)
function updateSnippetTags(tags) {
    const tagsDisplay = $('#snippetTagsDisplay');
    if (tagsDisplay.length === 0) return;

    tagsDisplay.empty();

    if (tags && tags.length > 0) {
        tags.forEach(function(tag) {
            const tagElement = $(`
                <div class="tag-badge" data-tag-id="${tag.tagId}">
                    <span>${tag.name}</span>
                    <span class="tag-remove" title="태그 제거">×</span>
                </div>
            `);
            tagsDisplay.append(tagElement);
        });
    } else {
        tagsDisplay.append(`
            <span style="
                color: #6c757d; 
                font-style: italic; 
                padding: 10px;
                display: block;
                text-align: center;
                background: #f8f9fa;
                border-radius: 8px;
            ">
                <i class="fas fa-info-circle" style="margin-right: 6px;"></i>
                태그가 없습니다
            </span>
        `);
    }
}

// 유틸리티 함수들
function showImageZoom(imageUrl) {
    const zoomModal = $(`
        <div id="imageZoomModal" class="modal" style="
            display: flex; 
            position: fixed; 
            z-index: 10000; 
            left: 0; 
            top: 0; 
            width: 100%; 
            height: 100%; 
            background-color: rgba(0,0,0,0.9);
            align-items: center;
            justify-content: center;
        ">
            <div class="modal-content" style="position: relative; max-width: 90%; max-height: 90%;">
                <span class="modal-close" style="
                    position: absolute; 
                    top: -40px; 
                    right: 0; 
                    color: white; 
                    font-size: 40px; 
                    font-weight: bold; 
                    cursor: pointer;
                ">&times;</span>
                <img src="${imageUrl}" style="max-width: 100%; max-height: 100%; object-fit: contain;" alt="확대된 이미지">
            </div>
        </div>
    `);

    $('body').append(zoomModal);

    zoomModal.on('click', function(e) {
        if (e.target === this || $(e.target).hasClass('modal-close')) {
            zoomModal.remove();
        }
    });
}

function downloadImage(imageUrl, filename) {
    try {
        const link = document.createElement('a');
        link.href = imageUrl;
        link.download = filename || 'image';
        link.target = '_blank';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        showToast('이미지 다운로드를 시작합니다.');
    } catch (error) {
        console.error('이미지 다운로드 실패:', error);
        showToast('이미지 다운로드에 실패했습니다.');
    }
}

function copyToClipboard(text, button) {
    if (navigator.clipboard) {
        navigator.clipboard.writeText(text).then(() => {
            showToast('클립보드에 복사되었습니다!');
            $(button).text('복사됨!');
            setTimeout(() => {
                $(button).html('<i class="fas fa-copy"></i> 복사');
            }, 2000);
        }).catch(() => {
            fallbackCopyToClipboard(text, button);
        });
    } else {
        fallbackCopyToClipboard(text, button);
    }
}

function fallbackCopyToClipboard(text, button) {
    const textArea = document.createElement('textarea');
    textArea.value = text;
    document.body.appendChild(textArea);
    textArea.select();
    try {
        document.execCommand('copy');
        showToast('클립보드에 복사되었습니다!');
        $(button).text('복사됨!');
        setTimeout(() => {
            $(button).html('<i class="fas fa-copy"></i> 복사');
        }, 2000);
    } catch (err) {
        showToast('복사에 실패했습니다.');
    }
    document.body.removeChild(textArea);
}

function showToast(message) {
    const toast = $(`
        <div class="toast-message" style="
            position: fixed; 
            top: 20px; 
            right: 20px; 
            background: #333; 
            color: white; 
            padding: 12px 20px; 
            border-radius: 4px; 
            z-index: 10000;
            opacity: 0;
            transition: opacity 0.3s;
        ">${message}</div>
    `);

    $('body').append(toast);

    setTimeout(() => toast.css('opacity', '1'), 10);
    setTimeout(() => {
        toast.css('opacity', '0');
        setTimeout(() => toast.remove(), 300);
    }, 3000);
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

$(document).ready(function() {
    // 스니펫 카드 클릭 이벤트 수정
    $(document).on('click', '.snippet-card', function() {
        // 더미 데이터 대신 실제 스니펫 데이터 사용
        const snippetData = $(this).data('snippet');
        const snippetId = $(this).data('snippet-id') || $(this).data('id');

        console.log('클릭된 카드 - ID:', snippetId, 'Data:', snippetData);

        if (snippetData) {
            showSnippetDetailModal(snippetData);
        } else {
            console.error('스니펫 데이터를 찾을 수 없습니다.');
            alert('스니펫 정보를 찾을 수 없습니다.');
        }
    });

    // 기존 Fragment 모달을 활용하는 함수
    function showSnippetDetailModal(snippet) {
        const modal = $('#snippetDetailModal');

        // 모달 제목 업데이트
        modal.find('.modal-header h3').text(snippet.title || '스니펫 상세보기');

        // 기존 메타 정보 및 콘텐츠 업데이트 (기존 코드)
        updateModalMetaAndContent(modal, snippet);

        // 태그 정보 로드 및 표시
        loadSnippetTags(snippet.id || snippet.snippetId);

        // 모달 표시
        modal.show();
    }

    // 메타 정보 및 콘텐츠 업데이트 함수 분리
    function updateModalMetaAndContent(modal, snippet) {
        // 메타 정보 업데이트
        const metaDiv = modal.find('.snippet-detail-meta');
        metaDiv.empty();

        if (snippet.type) {
            metaDiv.append(`<div class="meta-row"><strong>타입:</strong> ${snippet.type}</div>`);
        }
        if (snippet.language) {
            metaDiv.append(`<div class="meta-row"><strong>언어:</strong> ${snippet.language}</div>`);
        }
        if (snippet.createdAt) {
            const date = new Date(snippet.createdAt).toLocaleDateString('ko-KR');
            metaDiv.append(`<div class="meta-row"><strong>생성일:</strong> ${date}</div>`);
        }
        if (snippet.hexCode && snippet.colorName) {
            const colorDiv = $('<div class="meta-row color-meta"><strong>색상:</strong> </div>');
            colorDiv.append(`<span class="color-box" style="background-color: ${snippet.hexCode};"></span> ${snippet.colorName}`);
            metaDiv.append(colorDiv);
        }

        // 콘텐츠 업데이트
        const contentDiv = modal.find('.snippet-detail-content');
        contentDiv.empty();

        if (snippet.type === 'CODE') {
            // if (snippet.language) {
            //     contentDiv.append(`<div class="language-badge">${snippet.language}</div>`);
            // }
            contentDiv.append(`<pre class="code-block">${snippet.content || 'No Code'}</pre>`);
        } else {
            contentDiv.append(`<div class="text-content">${snippet.content || 'No Content'}</div>`);
        }
    }

    // 스니펫 태그 표시 함수
    function displaySnippetTags(tags, snippetId) {
        const tagsDisplay = $('#snippetTagsDisplay');
        tagsDisplay.empty();

        if (!tags || tags.length === 0) {
            tagsDisplay.html('<div class="empty-tags">태그가 없습니다.</div>');
            return;
        }

        tags.forEach(tag => {
            const tagBadge = $(`
            <div class="tag-badge" data-tag-id="${tag.tagId}">
                <span>${tag.name}</span>
                <span class="tag-remove" title="태그 제거">×</span>
            </div>
        `);

            // 태그 제거 이벤트
            tagBadge.find('.tag-remove').on('click', function() {
                removeTagFromSnippet(snippetId, tag.tagId, tagBadge);
            });

            tagsDisplay.append(tagBadge);
        });
    }

    // 태그 추가 이벤트 리스너
    $(document).on('click', '#addTagsBtn', function() {
        const tagInput = $('#tagInput');
        const tagNames = tagInput.val().trim();

        if (!tagNames) {
            alert('태그를 입력해주세요.');
            tagInput.focus(); // 포커스 다시 주기
            return;
        }

        const snippetId = $('#snippetDetailModal').data('current-snippet-id');

        if (!snippetId) {
            alert('스니펫 정보를 찾을 수 없습니다.');
            return;
        }

        addTagsToSnippet(snippetId, tagNames);
        tagInput.val(''); // 입력창 초기화
        tagInput.focus(); // 입력창에 포커스 유지
    });

    // 태그 추가 함수
    function addTagsToSnippet(snippetId, tagNames) {
        const tagArray = tagNames.split(',').map(name => name.trim()).filter(name => name);

        tagArray.forEach(tagName => {
            // 먼저 태그 생성 (이미 존재하면 409 에러)
            $.ajax({
                url: '/api/tag',
                method: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({ name: tagName }),
                success: function(response) {
                    // 태그 생성 성공시 스니펫에 연결
                    addExistingTagToSnippet(snippetId, response.tag.tagId);
                },
                error: function(xhr) {
                    if (xhr.status === 409) {
                        // 이미 존재하는 태그면 해당 태그 찾아서 연결
                        findAndAddTag(snippetId, tagName);
                    } else {
                        console.error('태그 생성 실패:', xhr.responseText);
                        alert(`태그 "${tagName}" 생성에 실패했습니다.`);
                    }
                }
            });
        });
    }

    // 기존 태그 찾아서 추가
    function findAndAddTag(snippetId, tagName) {
        $.ajax({
            url: '/api/tag/search',
            method: 'GET',
            data: { query: tagName },
            success: function(tags) {
                const matchingTag = tags.find(tag => tag.name === tagName);
                if (matchingTag) {
                    addExistingTagToSnippet(snippetId, matchingTag.tagId);
                }
            }
        });
    }

// 기존 태그를 스니펫에 연결 (이벤트 추가)
    function addExistingTagToSnippet(snippetId, tagId) {
        $.ajax({
            url: `/api/tag/snippet/${snippetId}/tag/${tagId}`,
            method: 'POST',
            success: function() {
                // 태그 목록 새로고침
                loadSnippetTags(snippetId);

                // 전역 이벤트 발생 - 태그 추가됨
                $(document).trigger('tagUpdated', {
                    action: 'added',
                    tagId: tagId,
                    snippetId: snippetId,
                    isNewTag: false
                });
            },
            error: function(xhr) {
                if (xhr.status !== 409) {
                    console.error('태그 연결 실패:', xhr.responseText);
                }
            }
        });
    }

// 태그 추가 함수 수정 (새 태그 생성 시 이벤트 발생)
    function addTagsToSnippet(snippetId, tagNames) {
        const tagArray = tagNames.split(',').map(name => name.trim()).filter(name => name);

        tagArray.forEach(tagName => {
            $.ajax({
                url: '/api/tag',
                method: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({ name: tagName }),
                success: function(response) {
                    // 새 태그 생성 성공시 스니펫에 연결
                    $.ajax({
                        url: `/api/tag/snippet/${snippetId}/tag/${response.tag.tagId}`,
                        method: 'POST',
                        success: function() {
                            loadSnippetTags(snippetId);

                            // 전역 이벤트 발생 - 새 태그 생성됨
                            $(document).trigger('tagUpdated', {
                                action: 'added',
                                tagId: response.tag.tagId,
                                snippetId: snippetId,
                                isNewTag: true,
                                tagName: tagName
                            });
                        }
                    });
                },
                error: function(xhr) {
                    if (xhr.status === 409) {
                        findAndAddTag(snippetId, tagName);
                    } else {
                        console.error('태그 생성 실패:', xhr.responseText);
                        alert(`태그 "${tagName}" 생성에 실패했습니다.`);
                    }
                }
            });
        });
    }

// 태그 제거 함수 수정 (이벤트 추가)
    function removeTagFromSnippet(snippetId, tagId, tagElement) {
        if (!confirm('이 태그를 제거하시겠습니까?')) {
            return;
        }

        $.ajax({
            url: `/api/tag/snippet/${snippetId}/tag/${tagId}`,
            method: 'DELETE',
            success: function() {
                tagElement.fadeOut(300, function() {
                    $(this).remove();
                    if ($('#snippetTagsDisplay .tag-badge').length === 0) {
                        $('#snippetTagsDisplay').html('<div class="empty-tags">태그가 없습니다.</div>');
                    }
                });

                // 전역 이벤트 발생 - 태그 제거됨
                $(document).trigger('tagUpdated', {
                    action: 'removed',
                    tagId: tagId,
                    snippetId: snippetId
                });
            },
            error: function(xhr) {
                console.error('태그 제거 실패:', xhr.responseText);
                alert('태그 제거에 실패했습니다.');
            }
        });
    }

// 태그 제거 함수
    function removeTagFromSnippet(snippetId, tagId, tagElement) {
        if (!confirm('이 태그를 제거하시겠습니까?')) {
            return;
        }

        $.ajax({
            url: `/api/tag/snippet/${snippetId}/tag/${tagId}`,
            method: 'DELETE',
            success: function() {
                tagElement.fadeOut(300, function() {
                    $(this).remove();
                    // 태그가 모두 제거되었으면 빈 상태 표시
                    if ($('#snippetTagsDisplay .tag-badge').length === 0) {
                        $('#snippetTagsDisplay').html('<div class="empty-tags">태그가 없습니다.</div>');
                    }
                });
            },
            error: function(xhr) {
                console.error('태그 제거 실패:', xhr.responseText);
                alert('태그 제거에 실패했습니다.');
            }
        });
    }

// 모달 표시시 스니펫 ID 저장
    function showSnippetDetailModal(snippet) {
        const modal = $('#snippetDetailModal');
        const snippetId = snippet.id || snippet.snippetId;

        // 스니펫 ID를 모달에 저장
        modal.data('current-snippet-id', snippetId);

        // 나머지 기존 코드...
        updateModalMetaAndContent(modal, snippet);
        loadSnippetTags(snippetId);
        modal.show();
    }

    // 스니펫 태그 로드 함수
    function loadSnippetTags(snippetId) {
        console.log('스니펫 태그 로드:', snippetId);

        const tagsDisplay = $('#snippetTagsDisplay');
        tagsDisplay.html('<div class="loading-text">태그를 불러오는 중...</div>');

        $.ajax({
            url: `/api/tag/snippet/${snippetId}`,
            method: 'GET',
            success: function(tags) {
                displaySnippetTags(tags, snippetId);
            },
            error: function(xhr, status, error) {
                console.error('태그 로드 실패:', error);
                tagsDisplay.html('<div class="empty-tags">태그를 불러올 수 없습니다.</div>');
            }
        });
    }

    // 모달 닫기 이벤트들
    $(document).on('click', '#snippetDetailModal .modal-close', function() {
        $('#snippetDetailModal').hide();
    });

    // 태그 입력창 개선
    $(document).on('click', '#snippetDetailModal .btn-secondary', function() {
        $('#snippetDetailModal').hide();
    });

    $(document).on('click', '#snippetDetailModal', function(e) {
        if (e.target === this) {
            $(this).hide();
        }
    });


    // 태그 입력창에서 엔터키 이벤트 추가
    $(document).on('keydown', '#tagInput', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault(); // 폼 제출 방지
            $('#addTagsBtn').trigger('click'); // 태그 추가 버튼 클릭 이벤트 실행
        }
    });

    $(document).on('focus', '#tagInput', function() {
        $(this).attr('placeholder', '태그명 입력 후 Enter 또는 버튼 클릭');
    });

    $(document).on('blur', '#tagInput', function() {
        $(this).attr('placeholder', '태그 추가 (쉼표로 구분)');
    });


    // 태그 추가 후 성공 피드백
    function showTagAddedFeedback(tagName) {
        const feedback = $('<div class="tag-added-feedback">').text(`"${tagName}" 태그가 추가되었습니다!`);
        $('#snippetTagsDisplay').append(feedback);

        setTimeout(() => {
            feedback.fadeOut(300, function() {
                $(this).remove();
            });
        }, 2000);
    }
    // ESC 키로 모달 닫기
    $(document).on('keydown', function(e) {
        if (e.key === 'Escape' && $('#snippetDetailModal').is(':visible')) {
            $('#snippetDetailModal').hide();
        }
    });

    // 스니펫 섹션 닫기 버튼 (동적으로 로드된 후 바인딩됨)
    $(document).on('click', '#closeSnippetsBtn', function() {
        $('#snippetSectionContainer').empty().hide();
        $('.tag-card').removeClass('active');
    });

    // 기존 JavaScript 코드에 추가할 부분

// 수정 버튼 클릭 이벤트
    $(document).on('click', '#editSnippetBtn', function() {
        const modal = $('#snippetDetailModal');
        const snippetId = modal.data('current-snippet-id');

        if (!snippetId) {
            alert('스니펫 정보를 찾을 수 없습니다.');
            return;
        }

        // 현재 스니펫 데이터에서 타입 확인 (여러 방법으로 시도)
        const snippetData = modal.data('current-snippet-data');
        let snippetType = null;

        if (snippetData) {
            // 다양한 형태의 type 필드 확인
            snippetType = snippetData.type || snippetData.snippetType || snippetData.TYPE;
            console.log('스니펫 데이터:', snippetData);
            console.log('추출된 타입:', snippetType);
        }

        // 타입을 찾지 못한 경우 모달의 메타 정보에서 추출 시도
        if (!snippetType) {
            console.log('데이터에서 타입을 찾지 못해 모달 UI에서 추출 시도');

            // 모달의 메타 정보에서 타입 추출
            const metaDiv = modal.find('.snippet-detail-meta');
            const typeText = metaDiv.find('div:contains("타입:")').text();

            if (typeText) {
                // "타입: CODE" 형태에서 "CODE" 부분 추출
                const match = typeText.match(/타입:\s*(\w+)/);
                if (match) {
                    snippetType = match[1];
                    console.log('UI에서 추출한 타입:', snippetType);
                }
            }
        }

        // 여전히 타입을 찾지 못한 경우
        if (!snippetType) {
            // 기본값으로 CODE 설정하거나 사용자에게 선택하게 할 수 있음
            const userChoice = confirm('스니펫 타입을 확인할 수 없습니다.\n코드 스니펫으로 처리하시겠습니까?\n(취소를 누르면 수정을 중단합니다)');
            if (userChoice) {
                snippetType = 'CODE';
            } else {
                return;
            }
        }

        proceedWithEdit(modal, snippetId, snippetType);
    });

// 수정 프로세스 진행 함수
    function proceedWithEdit(modal, snippetId, snippetType) {
        console.log('스니펫 수정 요청:', snippetId, 'Type:', snippetType);

        // 수정 버튼 비활성화
        const editBtn = $('#editSnippetBtn');
        const originalText = editBtn.html();
        editBtn.prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> 로딩 중...');

        // 타입에 따라 적절한 수정 폼 로드
        let editUrl = '';
        switch(snippetType.toUpperCase()) { // 대소문자 구분 없이 처리
            case 'CODE':
                editUrl = `/snippets/edit-form/code/${snippetId}`;
                break;
            case 'IMG':
            case 'IMAGE':
                editUrl = `/snippets/edit-form/img/${snippetId}`;
                break;
            case 'TEXT':
                editUrl = `/snippets/edit-form/text/${snippetId}`;
                break;
            default:
                alert(`지원하지 않는 스니펫 타입입니다: ${snippetType}`);
                editBtn.prop('disabled', false).html(originalText);
                return;
        }

        // AJAX로 수정 폼 로드
        $.ajax({
            url: editUrl,
            method: 'GET',
            success: function(html) {
                // 현재 상세보기 모달 닫기
                modal.hide();

                // 새로운 수정 모달 생성 및 표시
                showEditModal(html, snippetId);

                console.log('수정 모달 표시 완료');
            },
            error: function(xhr, status, error) {
                console.error('수정 폼 로드 실패:', error);
                alert('수정 폼을 불러오는 중 오류가 발생했습니다.');
                editBtn.prop('disabled', false).html(originalText);
            }
        });
    }

// 수정 모달 표시 함수
    function showEditModal(formHtml, snippetId) {
        // 현재 상세보기 모달 완전히 숨기기
        $('#snippetDetailModal').hide();

        // 기존 수정 모달이 있다면 제거
        $('#editSnippetModal').remove();

        // 새로운 수정 모달 HTML 생성 (기존 모달 구조와 동일하게)
        const editModalHtml = `
        <div id="editSnippetModal" class="modal" style="display: block;">
            <div class="modal-content">
                <span class="close edit-modal-close">&times;</span>
                <div id="editModalContent">
                    ${formHtml}
                </div>
            </div>
        </div>
    `;

        // 모달을 body에 추가
        $('body').append(editModalHtml);

        // 수정 모달 이벤트 설정
        setupEditModalEvents(snippetId);
    }

// 수정 모달 이벤트 설정
    function setupEditModalEvents(snippetId) {
        const editModal = $('#editSnippetModal');
        const form = editModal.find('form');

        // 폼이 있으면 제출 이벤트를 AJAX로 처리
        if (form.length) {
            // 폼 제출 이벤트를 AJAX로 처리
            form.on('submit', function(e) {
                e.preventDefault();

                const formData = new FormData(this);
                const submitBtn = form.find('button[type="submit"]');
                const originalText = submitBtn.html();

                // 버튼 비활성화
                submitBtn.prop('disabled', true).html('저장 중...');

                // AJAX로 폼 제출
                $.ajax({
                    url: form.attr('action'),
                    method: 'POST',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function(response) {
                        alert('스니펫이 성공적으로 수정되었습니다.');

                        // 수정 모달 닫기
                        editModal.remove();

                        // 페이지 새로고침으로 변경사항 반영
                        setTimeout(() => {
                            window.location.reload();
                        }, 300);
                    },
                    error: function(xhr, status, error) {
                        console.error('스니펫 수정 실패:', error);

                        let errorMessage = '스니펫 수정에 실패했습니다.';
                        if (xhr.status === 400) {
                            errorMessage = '입력값에 오류가 있습니다. 다시 확인해주세요.';
                        } else if (xhr.status === 403) {
                            errorMessage = '스니펫을 수정할 권한이 없습니다.';
                        } else if (xhr.status === 404) {
                            errorMessage = '해당 스니펫을 찾을 수 없습니다.';
                        }

                        alert(errorMessage);
                        submitBtn.prop('disabled', false).html(originalText);
                    }
                });
            });

            // 취소 버튼이 있다면 이벤트 추가
            const cancelBtn = form.find('button:contains("취소")');
            if (cancelBtn.length === 0) {
                // 취소 버튼이 없다면 추가
                const newCancelBtn = $('<button type="button" class="cancel-btn">취소</button>');
                form.append(newCancelBtn);

                newCancelBtn.on('click', function() {
                    if (confirm('수정을 취소하시겠습니까? 변경사항이 저장되지 않습니다.')) {
                        editModal.remove();
                    }
                });
            }
        }

        // 모달 닫기 이벤트들
        editModal.find('.close, .edit-modal-close').on('click', function() {
            if (confirm('수정을 취소하시겠습니까? 변경사항이 저장되지 않습니다.')) {
                editModal.remove();
            }
        });

        // 모달 외부 클릭시 닫기
        editModal.on('click', function(e) {
            if (e.target === this) {
                if (confirm('수정을 취소하시겠습니까? 변경사항이 저장되지 않습니다.')) {
                    editModal.remove();
                }
            }
        });

        // ESC 키로 모달 닫기
        $(document).one('keydown.editModal', function(e) {
            if (e.key === 'Escape') {
                if (confirm('수정을 취소하시겠습니까? 변경사항이 저장되지 않습니다.')) {
                    editModal.remove();
                    $(document).off('keydown.editModal');
                }
            }
        });
    }

// 모달 푸터를 원래 상태로 복원
    function resetModalFooter(modal) {
        const modalFooter = modal.find('.modal-footer');
        modalFooter.html(`
        <div class="modal-footer-left">
            <button id="editSnippetBtn" class="btn btn-warning">
                <i class="fas fa-edit"></i> 수정
            </button>
            <button id="deleteSnippetBtn" class="btn btn-danger">
                <i class="fas fa-trash"></i> 삭제
            </button>
        </div>
        <div class="modal-footer-right">
            <button class="btn btn-secondary">
                <i class="fas fa-times"></i> 닫기
            </button>
        </div>
    `);
    }


    // 삭제 버튼 클릭 이벤트
    $(document).on('click', '#deleteSnippetBtn', function() {
        const modal = $('#snippetDetailModal');
        const snippetId = modal.data('current-snippet-id');

        if (!snippetId) {
            alert('스니펫 정보를 찾을 수 없습니다.');
            return;
        }

        // 삭제 확인 다이얼로그
        if (!confirm('정말로 이 스니펫을 삭제하시겠습니까?\n삭제된 스니펫은 복구할 수 없습니다.')) {
            return;
        }

        // 삭제 버튼 비활성화 (중복 클릭 방지)
        const deleteBtn = $('#deleteSnippetBtn');
        const originalText = deleteBtn.html();
        deleteBtn.prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> 삭제 중...');

        console.log('스니펫 삭제 프로세스 시작:', snippetId);

        // 1단계: 스니펫과 연결된 모든 태그 제거
        removeAllTagsFromSnippet(snippetId)
            .then(() => {
                console.log('태그 제거 완료, 스니펫 삭제 진행');
                // 2단계: 스니펫 삭제
                return deleteSnippetRequest(snippetId);
            })
            .then(() => {
                // 삭제 성공
                alert('스니펫이 삭제되었습니다.');
                modal.hide();

                // 현재 표시된 스니펫 카드를 DOM에서 제거
                $(`.snippet-card[data-snippet-id="${snippetId}"], .snippet-card[data-id="${snippetId}"]`).fadeOut(300, function() {
                    $(this).remove();

                    // 스니펫이 모두 제거되었는지 확인
                    if ($('.snippet-card').length === 0) {
                        $('#snippetsGrid').html(`
                        <div class="empty-state">
                            <div class="emoji">📝</div>
                            <p>스니펫이 없습니다.</p>
                            <p>새로운 스니펫을 추가해보세요!</p>
                        </div>
                    `);
                    }
                });

                // 전역 이벤트 발생
                $(document).trigger('snippetDeleted', {
                    snippetId: snippetId
                });

                // 페이지 새로고침으로 태그 매니저 업데이트
                setTimeout(() => {
                    window.location.reload();
                }, 300); // 1초 후 새로고침 (사용자가 성공 메시지를 볼 시간 제공)
            })
            .catch((error) => {
                console.error('스니펫 삭제 프로세스 실패:', error);

                let errorMessage = '스니펫 삭제에 실패했습니다.';
                if (error.step === 'tag-removal') {
                    errorMessage = '태그 제거 중 오류가 발생했습니다. 다시 시도해주세요.';
                } else if (error.step === 'snippet-deletion') {
                    if (error.status === 404) {
                        errorMessage = '해당 스니펫을 찾을 수 없습니다.';
                    } else if (error.status === 403) {
                        errorMessage = '스니펫을 삭제할 권한이 없습니다.';
                    } else if (error.status === 500) {
                        errorMessage = '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
                    }
                }

                alert(errorMessage);
            })
            .finally(() => {
                // 버튼 상태 복원
                deleteBtn.prop('disabled', false).html(originalText);
            });
    });

// 스니펫과 연결된 모든 태그 제거 함수
    function removeAllTagsFromSnippet(snippetId) {
        return new Promise((resolve, reject) => {
            // 먼저 스니펫에 연결된 태그 목록 조회
            $.ajax({
                url: `/api/tag/snippet/${snippetId}`,
                method: 'GET',
                success: function(tags) {
                    if (!tags || tags.length === 0) {
                        console.log('제거할 태그가 없습니다.');
                        resolve();
                        return;
                    }

                    console.log(`${tags.length}개의 태그 제거 시작:`, tags.map(t => t.name));

                    // 모든 태그 제거 요청을 병렬로 처리
                    const removePromises = tags.map(tag => {
                        return new Promise((tagResolve, tagReject) => {
                            $.ajax({
                                url: `/api/tag/snippet/${snippetId}/tag/${tag.tagId}`,
                                method: 'DELETE',
                                success: function() {
                                    console.log(`태그 "${tag.name}" 제거 완료`);
                                    tagResolve();
                                },
                                error: function(xhr) {
                                    console.warn(`태그 "${tag.name}" 제거 실패:`, xhr.responseText);
                                    // 개별 태그 제거 실패는 무시하고 계속 진행
                                    tagResolve();
                                }
                            });
                        });
                    });

                    // 모든 태그 제거 완료 대기
                    Promise.all(removePromises)
                        .then(() => {
                            console.log('모든 태그 제거 완료');
                            resolve();
                        })
                        .catch((error) => {
                            console.error('태그 제거 중 오류:', error);
                            // 태그 제거 실패해도 스니펫 삭제는 진행
                            resolve();
                        });
                },
                error: function(xhr) {
                    console.warn('태그 목록 조회 실패:', xhr.responseText);
                    // 태그 목록 조회 실패해도 스니펫 삭제는 진행
                    resolve();
                }
            });
        });
    }

// 스니펫 삭제 요청 함수
    function deleteSnippetRequest(snippetId) {
        return new Promise((resolve, reject) => {
            $.ajax({
                url: `/snippets/delete/${snippetId}`,
                method: 'POST',
                success: function(response) {
                    console.log('스니펫 삭제 완료');
                    resolve(response);
                },
                error: function(xhr, status, error) {
                    console.error('스니펫 삭제 실패:', xhr.responseText || error);
                    reject({
                        step: 'snippet-deletion',
                        status: xhr.status,
                        message: xhr.responseText || error
                    });
                }
            });
        });
    }


    // 북마크 상태 로드
    function loadBookmarkStatus(snippetId) {
        console.log('북마크 상태 확인:', snippetId);

        $.ajax({
            url: `/api/bookmarks/check/${snippetId}`,
            method: 'GET',
            success: function(response) {
                if (response.success) {
                    updateBookmarkButton(response.isBookmarked);
                }
            },
            error: function(xhr) {
                console.warn('북마크 상태 확인 실패:', xhr.responseText);
                // 로그인하지 않은 경우에도 버튼은 표시
                updateBookmarkButton(false);
            }
        });
    }

// 북마크 버튼 상태 업데이트
    function updateBookmarkButton(isBookmarked) {
        const bookmarkBtn = $('#bookmarkToggleBtn');

        if (!bookmarkBtn.length) {
            console.warn('북마크 버튼을 찾을 수 없습니다.');
            return;
        }

        const bookmarkText = bookmarkBtn.find('.bookmark-text');
        const bookmarkIcon = bookmarkBtn.find('i');

        // 버튼 활성화
        bookmarkBtn.prop('disabled', false);

        if (isBookmarked) {
            bookmarkBtn.removeClass('btn-bookmark').addClass('btn-bookmark bookmarked');
            bookmarkBtn.html('<i class="fas fa-bookmark"></i> <span class="bookmark-text">북마크됨</span>');
            console.log('북마크 버튼 상태: 북마크됨');
        } else {
            bookmarkBtn.removeClass('bookmarked').addClass('btn-bookmark');
            bookmarkBtn.html('<i class="far fa-bookmark"></i> <span class="bookmark-text">북마크</span>');
            console.log('북마크 버튼 상태: 북마크 안됨');
        }
    }


// 북마크 토글 이벤트
    $(document).on('click', '#bookmarkToggleBtn', function() {
        const modal = $('#snippetDetailModal');
        const snippetId = modal.data('current-snippet-id');

        if (!snippetId) {
            alert('스니펫 정보를 찾을 수 없습니다.');
            return;
        }

        const bookmarkBtn = $(this);
        const isCurrentlyBookmarked = bookmarkBtn.hasClass('bookmarked');

        // 버튼 비활성화
        bookmarkBtn.prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> 처리 중...');

        console.log('북마크 토글 요청:', snippetId);

        $.ajax({
            url: '/api/bookmarks/toggle',
            method: 'POST',
            data: { snippetId: snippetId },
            success: function(response) {
                console.log('북마크 토글 응답:', response);

                if (response.success) {
                    // 북마크 상태 업데이트
                    updateBookmarkButton(response.bookmarked);

                    // 성공 메시지 표시
                    showBookmarkFeedback(response.message, 'success');

                    console.log('북마크 토글 성공:', response.bookmarked);
                } else {
                    console.warn('북마크 토글 실패:', response);
                    alert(response.message || '북마크 처리에 실패했습니다.');
                    // 원래 상태로 복원
                    updateBookmarkButton(isCurrentlyBookmarked);
                }
            },
            error: function(xhr) {
                console.error('북마크 토글 에러:', xhr.status, xhr.responseText);

                let errorMessage = '북마크 처리 중 오류가 발생했습니다.';
                if (xhr.status === 401) {
                    errorMessage = '로그인이 필요합니다.';
                } else if (xhr.status === 403) {
                    errorMessage = '권한이 없습니다.';
                }

                alert(errorMessage);
                // 원래 상태로 복원
                updateBookmarkButton(isCurrentlyBookmarked);
            },
            complete: function() {
                // 버튼 활성화 (complete에서 처리하여 success/error 모두에서 실행)
                bookmarkBtn.prop('disabled', false);
                console.log('북마크 토글 요청 완료');
            }
        });
    });


// 북마크 피드백 메시지 표시
    function showBookmarkFeedback(message, type = 'success') {
        const feedbackClass = type === 'success' ? 'bookmark-feedback-success' : 'bookmark-feedback-error';
        const feedback = $(`<div class="bookmark-feedback ${feedbackClass}">${message}</div>`);

        // 모달 하단에 피드백 표시
        const modal = $('#snippetDetailModal');
        modal.find('.modal-body').append(feedback);

        // 3초 후 제거
        setTimeout(() => {
            feedback.fadeOut(300, function() {
                $(this).remove();
            });
        }, 3000);
    }
});

// HTML 이스케이프 함수
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}