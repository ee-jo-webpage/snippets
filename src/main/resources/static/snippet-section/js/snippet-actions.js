// snippet-actions.js - 스니펫 수정/삭제/북마크 기능

$(document).ready(function() {

    // 수정 버튼 클릭 이벤트
    $(document).on('click', '#editSnippetBtn', SnippetActions.edit);

    // 삭제 버튼 클릭 이벤트
    $(document).on('click', '#deleteSnippetBtn', SnippetActions.delete);

    // 북마크 토글 이벤트
    $(document).on('click', '#bookmarkToggleBtn', SnippetBookmark.toggle);
});

// 스니펫 액션 네임스페이스
const SnippetActions = {

    // 스니펫 수정
    edit: function() {
        const modal = $('#snippetDetailModal');
        const snippetId = modal.data('current-snippet-id');

        if (!snippetId) {
            alert('스니펫 정보를 찾을 수 없습니다.');
            return;
        }

        // 스니펫 타입 추출
        const snippetData = modal.data('current-snippet-data');
        let snippetType = SnippetActions.extractType(snippetData, modal);

        if (!snippetType) {
            const userChoice = confirm('스니펫 타입을 확인할 수 없습니다.\n코드 스니펫으로 처리하시겠습니까?');
            if (userChoice) {
                snippetType = 'CODE';
            } else {
                return;
            }
        }

        SnippetActions.loadEditForm(modal, snippetId, snippetType);
    },

    // 스니펫 타입 추출
    extractType: function(snippetData, modal) {
        let snippetType = null;

        // 데이터에서 타입 추출
        if (snippetData) {
            snippetType = snippetData.type || snippetData.snippetType || snippetData.TYPE;
        }

        // UI에서 타입 추출
        if (!snippetType) {
            const metaDiv = modal.find('.snippet-detail-meta');
            const typeText = metaDiv.find('div:contains("타입:")').text();
            const match = typeText.match(/타입:\s*(\w+)/);
            if (match) {
                snippetType = match[1];
            }
        }

        return snippetType;
    },

    // 수정 폼 로드
    loadEditForm: function(modal, snippetId, snippetType) {
        const editBtn = $('#editSnippetBtn');
        const originalText = editBtn.html();
        editBtn.prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> 로딩 중...');

        let editUrl = '';
        switch(snippetType.toUpperCase()) {
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

        $.ajax({
            url: editUrl,
            method: 'GET',
            success: function(html) {
                modal.hide();
                SnippetActions.showEditModal(html, snippetId);
            },
            error: function() {
                alert('수정 폼을 불러오는 중 오류가 발생했습니다.');
                editBtn.prop('disabled', false).html(originalText);
            }
        });
    },

    // 수정 모달 표시
    showEditModal: function(formHtml, snippetId) {
        $('#editSnippetModal').remove();

        const editModalHtml = `
            <div id="editSnippetModal" class="modal" style="display: block;">
                <div class="modal-content">
                    <span class="close edit-modal-close">&times;</span>
                    <div id="editModalContent">${formHtml}</div>
                </div>
            </div>
        `;

        $('body').append(editModalHtml);
        SnippetActions.setupEditModalEvents(snippetId);
    },

    // 수정 모달 이벤트 설정
    setupEditModalEvents: function(snippetId) {
        const editModal = $('#editSnippetModal');
        const form = editModal.find('form');

        if (form.length) {
            form.on('submit', function(e) {
                e.preventDefault();
                SnippetActions.submitEditForm(form, editModal);
            });
        }

        // 모달 닫기 이벤트들
        editModal.find('.close, .edit-modal-close').on('click', function() {
            if (confirm('수정을 취소하시겠습니까?')) {
                editModal.remove();
            }
        });

        editModal.on('click', function(e) {
            if (e.target === this && confirm('수정을 취소하시겠습니까?')) {
                editModal.remove();
            }
        });
    },

    // 수정 폼 제출
    submitEditForm: function(form, editModal) {
        const formData = new FormData(form[0]);
        const submitBtn = form.find('button[type="submit"]');
        const originalText = submitBtn.html();

        submitBtn.prop('disabled', true).html('저장 중...');

        $.ajax({
            url: form.attr('action'),
            method: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            success: function() {
                alert('스니펫이 성공적으로 수정되었습니다.');
                editModal.remove();
                setTimeout(() => window.location.reload(), 300);
            },
            error: function(xhr) {
                let errorMessage = '스니펫 수정에 실패했습니다.';
                if (xhr.status === 400) errorMessage = '입력값에 오류가 있습니다.';
                else if (xhr.status === 403) errorMessage = '권한이 없습니다.';
                else if (xhr.status === 404) errorMessage = '스니펫을 찾을 수 없습니다.';

                alert(errorMessage);
                submitBtn.prop('disabled', false).html(originalText);
            }
        });
    },

    // 스니펫 삭제
    delete: function() {
        const modal = $('#snippetDetailModal');
        const snippetId = modal.data('current-snippet-id');

        if (!snippetId) {
            alert('스니펫 정보를 찾을 수 없습니다.');
            return;
        }

        if (!confirm('정말로 이 스니펫을 삭제하시겠습니까?\n삭제된 스니펫은 복구할 수 없습니다.')) {
            return;
        }

        const deleteBtn = $('#deleteSnippetBtn');
        const originalText = deleteBtn.html();
        deleteBtn.prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> 삭제 중...');

        // 태그 제거 후 스니펫 삭제
        SnippetTags.removeAll(snippetId)
            .then(() => SnippetActions.deleteSnippet(snippetId))
            .then(() => {
                alert('스니펫이 삭제되었습니다.');
                modal.hide();
                SnippetActions.removeSnippetFromUI(snippetId);
                setTimeout(() => window.location.reload(), 300);
            })
            .catch((error) => {
                console.error('스니펫 삭제 실패:', error);
                alert('스니펫 삭제에 실패했습니다.');
            })
            .finally(() => {
                deleteBtn.prop('disabled', false).html(originalText);
            });
    },

    // 스니펫 삭제 요청
    deleteSnippet: function(snippetId) {
        return new Promise((resolve, reject) => {
            $.ajax({
                url: `/snippets/delete/${snippetId}`,
                method: 'POST',
                success: resolve,
                error: function(xhr) {
                    reject({
                        status: xhr.status,
                        message: xhr.responseText
                    });
                }
            });
        });
    },

    // UI에서 스니펫 제거
    removeSnippetFromUI: function(snippetId) {
        $(`.snippet-card[data-snippet-id="${snippetId}"], .snippet-card[data-id="${snippetId}"]`)
            .fadeOut(300, function() {
                $(this).remove();
                if ($('.snippet-card').length === 0) {
                    $('#snippetsGrid').html(`
                        <div class="empty-state">
                            <div class="emoji">📝</div>
                            <p>스니펫이 없습니다.</p>
                        </div>
                    `);
                }
            });
    }
};

// 북마크 기능 네임스페이스
const SnippetBookmark = {

    // 북마크 상태 로드
    loadStatus: function(snippetId) {
        $.ajax({
            url: `/api/bookmarks/check/${snippetId}`,
            method: 'GET',
            success: function(response) {
                if (response.success) {
                    SnippetBookmark.updateButton(response.isBookmarked);
                }
            },
            error: function() {
                SnippetBookmark.updateButton(false);
            }
        });
    },

    // 북마크 버튼 상태 업데이트
    updateButton: function(isBookmarked) {
        const bookmarkBtn = $('#bookmarkToggleBtn');
        if (!bookmarkBtn.length) return;

        bookmarkBtn.prop('disabled', false);

        if (isBookmarked) {
            bookmarkBtn.removeClass('btn-bookmark').addClass('btn-bookmark bookmarked');
            bookmarkBtn.html('<i class="fas fa-bookmark"></i> <span class="bookmark-text">북마크됨</span>');
        } else {
            bookmarkBtn.removeClass('bookmarked').addClass('btn-bookmark');
            bookmarkBtn.html('<i class="far fa-bookmark"></i> <span class="bookmark-text">북마크</span>');
        }
    },

    // 북마크 토글
    toggle: function() {
        const modal = $('#snippetDetailModal');
        const snippetId = modal.data('current-snippet-id');

        if (!snippetId) {
            alert('스니펫 정보를 찾을 수 없습니다.');
            return;
        }

        const bookmarkBtn = $('#bookmarkToggleBtn');
        const isCurrentlyBookmarked = bookmarkBtn.hasClass('bookmarked');

        bookmarkBtn.prop('disabled', true).html('<i class="fas fa-spinner fa-spin"></i> 처리 중...');

        $.ajax({
            url: '/api/bookmarks/toggle',
            method: 'POST',
            data: { snippetId: snippetId },
            success: function(response) {
                if (response.success) {
                    SnippetBookmark.updateButton(response.bookmarked);
                    SnippetBookmark.showFeedback(response.message, 'success');
                } else {
                    alert(response.message || '북마크 처리에 실패했습니다.');
                    SnippetBookmark.updateButton(isCurrentlyBookmarked);
                }
            },
            error: function(xhr) {
                let errorMessage = '북마크 처리 중 오류가 발생했습니다.';
                if (xhr.status === 401) errorMessage = '로그인이 필요합니다.';
                else if (xhr.status === 403) errorMessage = '권한이 없습니다.';

                alert(errorMessage);
                SnippetBookmark.updateButton(isCurrentlyBookmarked);
            },
            complete: function() {
                bookmarkBtn.prop('disabled', false);
            }
        });
    },

    // 북마크 피드백 표시
    showFeedback: function(message, type = 'success') {
        const feedbackClass = type === 'success' ? 'bookmark-feedback-success' : 'bookmark-feedback-error';
        const feedback = $(`<div class="bookmark-feedback ${feedbackClass}">${message}</div>`);

        const modal = $('#snippetDetailModal');
        modal.find('.modal-body').append(feedback);

        setTimeout(() => {
            feedback.fadeOut(300, function() {
                $(this).remove();
            });
        }, 3000);
    }
};