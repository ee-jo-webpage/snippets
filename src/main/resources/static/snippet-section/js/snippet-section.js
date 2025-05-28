$(document).ready(function() {
    // 더미 스니펫 데이터
    const dummySnippets = [
        {
            id: 1,
            title: 'JavaScript 배열 맵',
            content: 'const numbers = [1, 2, 3, 4, 5];\nconst doubled = numbers.map(num => num * 2);\nconsole.log(doubled); // [2, 4, 6, 8, 10]',
            language: 'javascript',
            type: 'CODE',
            createdAt: '2023-02-01',
            hexCode: '#3b82f6',
            colorName: 'Blue'
        },
        {
            id: 2,
            title: 'React Hook 예제',
            content: 'const [count, setCount] = useState(0);\n\nconst increment = () => {\n  setCount(count + 1);\n};',
            language: 'jsx',
            type: 'CODE',
            createdAt: '2023-02-03',
            hexCode: '#10b981',
            colorName: 'Green'
        },
        {
            id: 3,
            title: 'JavaScript 학습 노트',
            content: '# JavaScript 기초\n\n1. 변수 선언 (let, const, var)\n2. 함수 정의 (function, arrow function)\n3. 조건문 사용법 (if, switch)\n4. 반복문 (for, while)\n5. 배열과 객체 조작',
            type: 'TEXT',
            createdAt: '2023-02-11',
            hexCode: '#06b6d4',
            colorName: 'Cyan'
        }
    ];

    // 스니펫 카드 클릭 이벤트
    $(document).on('click', '.snippet-card', function() {
        const snippetId = $(this).data('id') || 1;
        const snippet = dummySnippets.find(s => s.id === snippetId) || dummySnippets[0];
        showSnippetDetailModal(snippet);
    });

    // 기존 Fragment 모달을 활용하는 함수
    function showSnippetDetailModal(snippet) {
        const modal = $('#snippetDetailModal');

        // 모달 제목 업데이트
        modal.find('.modal-header h3').text(snippet.title || '스니펫 상세보기');

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
            if (snippet.language) {
                contentDiv.append(`<div class="language-badge">${snippet.language}</div>`);
            }
            contentDiv.append(`<pre class="code-block">${snippet.content || 'No Code'}</pre>`);
        } else {
            contentDiv.append(`<div class="text-content">${snippet.content || 'No Content'}</div>`);
        }

        // 모달 표시
        modal.show();
    }

    // 모달 닫기 이벤트들
    $(document).on('click', '#snippetDetailModal .modal-close', function() {
        $('#snippetDetailModal').hide();
    });

    $(document).on('click', '#snippetDetailModal .btn-secondary', function() {
        $('#snippetDetailModal').hide();
    });

    $(document).on('click', '#snippetDetailModal', function(e) {
        if (e.target === this) {
            $(this).hide();
        }
    });

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
});