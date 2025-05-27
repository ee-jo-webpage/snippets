// 스니펫 카드 및 모달 처리 스크립트

// 더미 스니펫 데이터 (북마크 스타일과 동일하게 색상 정보 포함)
const dummySnippets = [
    {
        id: 1,
        title: 'JavaScript 배열 맵',
        content: 'const numbers = [1, 2, 3, 4, 5];\nconst doubled = numbers.map(num => num * 2);',
        language: 'javascript',
        type: 'CODE',
        createdAt: '2023-02-01',
        hexCode: '#3b82f6',
        colorName: 'Blue'
    },
    {
        id: 2,
        title: 'React Hook 예제',
        content: 'const [count, setCount] = useState(0);',
        language: 'jsx',
        type: 'CODE',
        createdAt: '2023-02-03',
        hexCode: '#10b981',
        colorName: 'Green'
    },
    {
        id: 3,
        title: 'Java Stream API',
        content: 'list.stream().filter(x -> x > 0).collect(Collectors.toList());',
        language: 'java',
        type: 'CODE',
        createdAt: '2023-02-05',
        hexCode: '#f59e0b',
        colorName: 'Orange'
    },
    {
        id: 4,
        title: 'Spring Boot Controller',
        content: '@RestController\npublic class ApiController { ... }',
        language: 'java',
        type: 'CODE',
        createdAt: '2023-02-07',
        hexCode: '#ef4444',
        colorName: 'Red'
    },
    {
        id: 5,
        title: 'CSS Grid Layout',
        content: '.container { display: grid; grid-template-columns: repeat(3, 1fr); }',
        language: 'css',
        type: 'CODE',
        createdAt: '2023-02-09',
        hexCode: '#8b5cf6',
        colorName: 'Purple'
    },
    {
        id: 6,
        title: 'JavaScript 학습 노트',
        content: '# JavaScript 기초\n\n1. 변수 선언 (let, const, var)\n2. 함수 정의 (function, arrow function)\n3. 조건문 사용법 (if, switch)',
        type: 'TEXT',
        createdAt: '2023-02-11',
        hexCode: '#06b6d4',
        colorName: 'Cyan'
    },
    {
        id: 7,
        title: 'Spring 정리',
        content: '스프링 프레임워크 핵심 개념:\n\n- IoC (Inversion of Control)\n- DI (Dependency Injection)\n- AOP (Aspect Oriented Programming)',
        type: 'TEXT',
        createdAt: '2023-02-13',
        hexCode: '#ec4899',
        colorName: 'Pink'
    }
];

// 태그별 스니펫 매핑 (테스트용)
const tagSnippetMap = {
    1: [1, 2, 6], // JavaScript
    2: [2],       // React
    3: [3, 4, 7], // Java
    4: [4, 7],    // Spring
    5: [1, 2, 5], // Frontend
    6: [3, 4, 7], // Backend
};

/**
 * 스니펫 목록 표시
 */
function displaySnippets(snippets, tagName) {
    const container = $('#snippetsGrid');
    $('#snippetsTitle').text('📝 "' + tagName + '" 태그의 스니펫 (' + (snippets ? snippets.length : 0) + '개)');
    container.empty();

    if (!snippets || snippets.length === 0) {
        container.html('<div class="empty-state"><div class="emoji">📝</div><p>이 태그에 해당하는 스니펫이 없습니다.</p></div>');
        return;
    }

    snippets.forEach(snippet => {
        const card = createSnippetCard(snippet);
        container.append(card);
    });
}

/**
 * 북마크 스타일과 동일한 스니펫 카드 생성 함수 (미리보기용)
 */
function createSnippetCard(snippet) {
    const card = $('<div>').addClass('snippet-card bookmark-card');

    // 색상이 있는 경우 클래스 추가
    if (snippet.hexCode) {
        card.addClass('has-color');
        card.css('border-left', '5px solid ' + snippet.hexCode);
    }

    // 카드 클릭 이벤트 - 상세보기 모달 표시
    card.on('click', function() {
        showSnippetDetailModal(snippet);
    });

    // 색상 인디케이터 - 상단 우측
    let colorIndicator = null;
    if (snippet.hexCode && snippet.colorName) {
        colorIndicator = $('<div>')
            .addClass('color-indicator')
            .css('background-color', snippet.hexCode)
            .append($('<span>').addClass('color-name').text(snippet.colorName));
    }

    // 메타 정보 (간단히)
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

    // 스니펫 제목 (미리보기)
    const title = $('<h3>')
        .addClass('bookmark-title')
        .text(snippet.title || '제목 없음');

    // 스니펫 콘텐츠 미리보기 (짧게)
    const contentPreview = $('<div>').addClass('snippet-content-preview');

    if (snippet.content) {
        // 내용을 100자로 제한
        const previewText = snippet.content.length > 100
            ? snippet.content.substring(0, 100) + '...'
            : snippet.content;

        contentPreview.text(previewText);
    } else {
        contentPreview.text('내용 없음');
    }

    // 더보기 표시
    const moreIndicator = $('<div>')
        .addClass('more-indicator')
        .text('클릭하여 자세히 보기 →');

    // 카드 구성
    if (colorIndicator) {
        card.append(colorIndicator);
    }
    card.append(metaContainer);
    card.append(title);
    card.append(contentPreview);
    card.append(moreIndicator);

    return card;
}

/**
 * 스니펫 상세보기 모달 표시 함수
 */
function showSnippetDetailModal(snippet) {
    // 기존 모달 제거
    $('#snippetDetailModal').remove();

    // 모달 생성
    const modal = $('<div>')
        .attr('id', 'snippetDetailModal')
        .addClass('modal snippet-modal')
        .css('display', 'flex');

    const modalContent = $('<div>').addClass('modal-content snippet-modal-content');

    // 모달 헤더
    const modalHeader = $('<div>').addClass('modal-header');
    const modalTitle = $('<h3>').text(snippet.title || '스니펫 상세보기');
    const closeBtn = $('<span>').addClass('modal-close').html('&times;');

    modalHeader.append(modalTitle).append(closeBtn);

    // 모달 바디
    const modalBody = $('<div>').addClass('modal-body snippet-modal-body');

    // 메타 정보
    const metaInfo = $('<div>').addClass('snippet-detail-meta');

    if (snippet.type) {
        metaInfo.append($('<div>').addClass('meta-row').html(`<strong>타입:</strong> ${snippet.type}`));
    }
    if (snippet.language) {
        metaInfo.append($('<div>').addClass('meta-row').html(`<strong>언어:</strong> ${snippet.language}`));
    }
    if (snippet.createdAt) {
        const date = new Date(snippet.createdAt).toLocaleDateString('ko-KR');
        metaInfo.append($('<div>').addClass('meta-row').html(`<strong>생성일:</strong> ${date}`));
    }
    if (snippet.hexCode && snippet.colorName) {
        const colorDiv = $('<div>').addClass('meta-row color-meta');
        const colorBox = $('<span>')
            .addClass('color-box')
            .css('background-color', snippet.hexCode);
        colorDiv.html(`<strong>색상:</strong> `).append(colorBox).append(` ${snippet.colorName}`);
        metaInfo.append(colorDiv);
    }

    // 콘텐츠
    const contentDiv = $('<div>').addClass('snippet-detail-content');

    if (snippet.type === 'CODE') {
        if (snippet.language) {
            contentDiv.append($('<div>').addClass('language-badge').text(snippet.language));
        }
        const codeBlock = $('<pre>').addClass('code-block').text(snippet.content || 'No Code');
        contentDiv.append(codeBlock);
    } else {
        const textContent = $('<div>').addClass('text-content').text(snippet.content || 'No Content');
        contentDiv.append(textContent);
    }

    modalBody.append(metaInfo).append(contentDiv);

    // 모달 푸터
    const modalFooter = $('<div>').addClass('modal-footer');
    const closeModalBtn = $('<button>')
        .addClass('btn btn-secondary')
        .html('<i class="fas fa-times"></i> 닫기');

    modalFooter.append(closeModalBtn);

    // 모달 조립
    modalContent.append(modalHeader).append(modalBody).append(modalFooter);
    modal.append(modalContent);

    // 이벤트 리스너
    closeBtn.on('click', function() {
        modal.remove();
    });

    closeModalBtn.on('click', function() {
        modal.remove();
    });

    // 모달 외부 클릭시 닫기
    modal.on('click', function(e) {
        if (e.target === this) {
            modal.remove();
        }
    });

    // ESC 키로 닫기
    $(document).on('keydown.snippetModal', function(e) {
        if (e.key === 'Escape') {
            modal.remove();
            $(document).off('keydown.snippetModal');
        }
    });

    // 모달을 DOM에 추가
    $('body').append(modal);
}

/**
 * 태그의 스니펫 로드
 */
function loadSnippetsForTag(tagId, tagName) {
    console.log('태그 스니펫 로드:', tagId, tagName);

    $('#snippetsSection').show();
    $('#snippetsTitle').text('📝 "' + tagName + '" 태그의 스니펫 목록');
    $('#snippetsGrid').html('<div class="loading-text">스니펫을 불러오는 중...</div>');

    $.ajax({
        url: '/api/tag/' + tagId + '/snippets',
        method: 'GET',
        success: function (snippets) {
            console.log('스니펫 로드 성공:', snippets);
            displaySnippets(snippets, tagName);
        },
        error: function (xhr, status, error) {
            console.error('스니펫 로드 실패:', xhr.status, error);

            // API 실패시 더미 데이터 사용
            console.log('더미 데이터 사용 - tagId:', tagId);
            const snippetIds = tagSnippetMap[tagId] || [];
            const snippets = dummySnippets.filter(s => snippetIds.includes(s.id));
            displaySnippets(snippets, tagName);
        }
    });
}