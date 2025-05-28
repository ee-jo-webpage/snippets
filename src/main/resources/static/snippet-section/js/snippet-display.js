// snippet-display.js - 스니펫 화면 표시

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

        $.ajax({
            url: '/api/tag/' + tagId + '/snippets',
            method: 'GET',
            success: function(snippets) {
                SnippetDisplay.display(snippets);
            },
            error: function() {
                console.log('API 실패, 더미 데이터 사용');
                const dummySnippets = [
                    {
                        id: 1,
                        title: 'JavaScript 배열 맵',
                        content: 'const numbers = [1, 2, 3, 4, 5];\\nconst doubled = numbers.map(num => num * 2);',
                        language: 'javascript',
                        type: 'CODE',
                        hexCode: '#3b82f6',
                        colorName: 'Blue'
                    }
                ];
                SnippetDisplay.display(dummySnippets);
            }
        });
    },

    // 스니펫 표시
    display: function(snippets) {
        const container = $('#snippetsGrid');
        container.empty();

        if (!snippets || snippets.length === 0) {
            container.html(
                '<div class="empty-state">' +
                '<div class="emoji">📝</div>' +
                '<p>이 태그에 해당하는 스니펫이 없습니다.</p>' +
                '</div>'
            );
            return;
        }

        snippets.forEach(snippet => {
            const card = SnippetDisplay.createCard(snippet);
            container.append(card);
        });
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
        const contentPreview = $('<div>').addClass('snippet-content-preview');
        if (snippet.content) {
            const previewText = snippet.content.length > 100
                ? snippet.content.substring(0, 100) + '...'
                : snippet.content;
            contentPreview.text(previewText);
        } else {
            contentPreview.text('내용 없음');
        }

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
    }
};