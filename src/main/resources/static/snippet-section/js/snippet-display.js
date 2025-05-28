// snippet-display.js - 스니펫 화면 표시 (업데이트됨)

$(document).ready(function() {

    // 스니펫 섹션 초기화 함수 (AJAX 로드 후 호출됨)
    window.initializeSnippetSection = function(tagId, tagName) {
        console.log('스니펫 섹션 초기화:', tagId, tagName);

        $('#closeSnippetsBtn').off('click').on('click', function() {
            $('#snippetSectionContainer').empty().hide();
            $('.tag-card').removeClass('active');
        });

        SnippetDisplay.loadForTag(tagId, tagName);
    };
});

// 스니펫 화면 표시 네임스페이스
const SnippetDisplay = {

    // 특정 태그의 스니펫 로드
    loadForTag: function(tagId, tagName) {
        console.log('스니펫 데이터 로드:', tagId, tagName);

        $('#snippetsGrid').html('<div class="loading-text">스니펫을 불러오는 중...</div>');

        // REST API 호출로 변경
        $.ajax({
            url: `/api/snippets/tag/${tagId}`,
            method: 'GET',
            success: function(snippets) {
                console.log('API에서 받은 스니펫 데이터:', snippets);
                SnippetDisplay.display(snippets);
            },
            error: function(xhr, status, error) {
                console.log('API 호출 실패:', xhr.status, error);

                // 401 Unauthorized인 경우
                if (xhr.status === 401) {
                    alert('로그인이 필요합니다.');
                    window.location.href = '/login';
                    return;
                }

                // 다른 오류의 경우 더미 데이터 사용 (개발용)
                console.log('더미 데이터로 대체');
                const dummySnippets = [
                    {
                        id: 1,
                        snippetId: 1,
                        title: 'JavaScript 배열 맵',
                        memo: 'JavaScript 배열 맵 예제',
                        content: 'const numbers = [1, 2, 3, 4, 5];\nconst doubled = numbers.map(num => num * 2);\nconsole.log(doubled);',
                        language: 'javascript',
                        type: 'CODE',
                        hexCode: '#3b82f6',
                        colorName: 'Blue',
                        createdAt: new Date().toISOString()
                    },
                    {
                        id: 2,
                        snippetId: 2,
                        title: '이미지 예제',
                        memo: '샘플 이미지',
                        imageUrl: 'https://via.placeholder.com/300x200/3b82f6/ffffff?text=Sample+Image',
                        altText: '샘플 이미지입니다',
                        type: 'IMG',
                        hexCode: '#10b981',
                        colorName: 'Green',
                        createdAt: new Date().toISOString()
                    }
                ];
                SnippetDisplay.display(dummySnippets);
            }
        });
    },

    // 모든 스니펫 로드 (페이징 지원)
    loadAll: function(page = 1, size = 30) {
        console.log('모든 스니펫 로드:', page, size);

        $('#snippetsGrid').html('<div class="loading-text">스니펫을 불러오는 중...</div>');

        $.ajax({
            url: '/api/snippets',
            method: 'GET',
            data: {
                page: page,
                size: size
            },
            success: function(response) {
                console.log('API 응답:', response);

                if (response.success && response.data) {
                    SnippetDisplay.display(response.data);

                    // 페이징 정보가 있으면 처리
                    if (response.pagination) {
                        SnippetDisplay.displayPagination(response.pagination);
                    }
                } else {
                    SnippetDisplay.displayEmpty('스니펫을 불러올 수 없습니다.');
                }
            },
            error: function(xhr, status, error) {
                console.error('모든 스니펫 로드 실패:', xhr.status, error);

                if (xhr.status === 401) {
                    alert('로그인이 필요합니다.');
                    window.location.href = '/login';
                } else {
                    SnippetDisplay.displayEmpty('스니펫을 불러오는 중 오류가 발생했습니다.');
                }
            }
        });
    },

    // 스니펫 표시
    display: function(snippets) {
        const container = $('#snippetsGrid');
        container.empty();

        if (!snippets || snippets.length === 0) {
            SnippetDisplay.displayEmpty('이 태그에 해당하는 스니펫이 없습니다.');
            return;
        }

        snippets.forEach(snippet => {
            const card = SnippetDisplay.createCard(snippet);
            container.append(card);
        });
    },

    // 빈 상태 표시
    displayEmpty: function(message = '스니펫이 없습니다.') {
        const container = $('#snippetsGrid');
        container.html(`
            <div class="empty-state">
                <div class="emoji">📝</div>
                <p>${message}</p>
            </div>
        `);
    },

    // 페이징 표시 (필요시 구현)
    displayPagination: function(pagination) {
        console.log('페이징 정보:', pagination);
        // 페이징 UI 구현 (필요시)
    },

    // 스니펫 카드 생성
    createCard: function(snippet) {
        const card = $('<div>').addClass('snippet-card bookmark-card');

        const snippetId = snippet.snippetId || snippet.id;
        card.data('id', snippetId);
        card.data('snippet-id', snippetId);
        card.data('snippet', snippet);

        console.log('스니펫 카드 생성:', {
            snippetId: snippetId,
            title: snippet.title || snippet.memo,
            type: snippet.type,
            data: snippet
        });

        // 색상 처리
        if (snippet.hexCode) {
            card.addClass('has-color');
            card.css('border-left', '5px solid ' + snippet.hexCode);

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
        const contentPreview = SnippetDisplay.createContentPreview(snippet);

        // 더보기 인디케이터
        const moreIndicator = $('<div>')
            .addClass('more-indicator')
            .text('클릭하여 자세히 보기 →');

        // 카드 조립
        card.append(metaContainer);
        card.append(title);
        card.append(contentPreview);
        card.append(moreIndicator);

        return card;
    },

    // 콘텐츠 미리보기 생성
    createContentPreview: function(snippet) {
        const contentPreview = $('<div>').addClass('snippet-content-preview');

        if (snippet.type === 'IMG' || snippet.type === 'IMAGE') {
            // 이미지 스니펫인 경우
            contentPreview.addClass('image-preview');

            if (snippet.imageUrl) {
                console.log('이미지 URL:', snippet.imageUrl);

                const img = $('<img>')
                    .addClass('snippet-preview-image')
                    .attr('src', snippet.imageUrl)
                    .attr('alt', snippet.altText || snippet.memo || '이미지')
                    .on('error', function() {
                        console.error('이미지 로드 실패:', snippet.imageUrl);
                        $(this).replaceWith('<div class="image-error">🖼️ 이미지를 불러올 수 없습니다</div>');
                    })
                    .on('load', function() {
                        console.log('이미지 로드 완료:', snippet.imageUrl);
                    });

                contentPreview.append(img);

                // 이미지 설명 표시
                const displayText = snippet.altText || snippet.memo;
                if (displayText) {
                    contentPreview.append(
                        $('<div>').addClass('image-preview-info').text(displayText)
                    );
                }
            } else {
                contentPreview.append('<div class="image-error">🖼️ 이미지 URL이 없습니다</div>');
                console.warn('이미지 URL이 없는 이미지 스니펫:', snippet);
            }

        } else {
            // 텍스트/코드 스니펫인 경우
            if (snippet.content) {
                const previewText = snippet.content.length > 100
                    ? snippet.content.substring(0, 100) + '...'
                    : snippet.content;
                contentPreview.text(previewText);
            } else {
                contentPreview.text('내용 없음');
            }
        }

        return contentPreview;
    }
};