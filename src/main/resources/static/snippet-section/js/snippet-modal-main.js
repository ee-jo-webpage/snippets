// snippet-modal-main.js - 스니펫 모달 메인 기능

$(document).ready(function() {

    // 스니펫 카드 클릭 이벤트
    $(document).on('click', '.snippet-card', function() {
        const snippetData = $(this).data('snippet');
        const snippetId = $(this).data('snippet-id') || $(this).data('id');

        console.log('클릭된 카드 - ID:', snippetId, 'Data:', snippetData);

        if (snippetData) {
            SnippetModal.show(snippetData);
        } else {
            console.error('스니펫 데이터를 찾을 수 없습니다.');
            alert('스니펫 정보를 찾을 수 없습니다.');
        }
    });

    // 모달 닫기 이벤트들
    $(document).on('click', '#snippetDetailModal .modal-close', function() {
        SnippetModal.hide();
    });

    $(document).on('click', '#snippetDetailModal .btn-secondary', function() {
        SnippetModal.hide();
    });

    $(document).on('click', '#snippetDetailModal', function(e) {
        if (e.target === this) {
            SnippetModal.hide();
        }
    });

    // ESC 키로 모달 닫기
    $(document).on('keydown', function(e) {
        if (e.key === 'Escape' && $('#snippetDetailModal').is(':visible')) {
            SnippetModal.hide();
        }
    });

    // 스니펫 섹션 닫기 버튼
    $(document).on('click', '#closeSnippetsBtn', function() {
        $('#snippetSectionContainer').empty().hide();
        $('.tag-card').removeClass('active');
    });
});

// 스니펫 모달 네임스페이스
const SnippetModal = {
    show: function(snippet) {
        const modal = $('#snippetDetailModal');
        const snippetId = snippet.id || snippet.snippetId;

        // 스니펫 ID를 모달에 저장
        modal.data('current-snippet-id', snippetId);
        modal.data('current-snippet-data', snippet);

        // 모달 제목 업데이트
        modal.find('.modal-header h3').text(snippet.title || '스니펫 상세보기');

        // 메타 정보 및 콘텐츠 업데이트
        this.updateContent(modal, snippet);

        // 태그 정보 로드
        SnippetTags.load(snippetId);

        // 북마크 상태 로드
        SnippetBookmark.loadStatus(snippetId);

        // 모달 표시
        modal.show();
    },

    hide: function() {
        $('#snippetDetailModal').hide();
    },

    updateContent: function(modal, snippet) {
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
    }
};