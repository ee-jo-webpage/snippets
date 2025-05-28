// snippet-tags.js - 스니펫 태그 관리

$(document).ready(function() {

    // 태그 추가 버튼 이벤트
    $(document).on('click', '#addTagsBtn', function() {
        const tagInput = $('#tagInput');
        const tagNames = tagInput.val().trim();

        if (!tagNames) {
            alert('태그를 입력해주세요.');
            tagInput.focus();
            return;
        }

        const snippetId = $('#snippetDetailModal').data('current-snippet-id');
        if (!snippetId) {
            alert('스니펫 정보를 찾을 수 없습니다.');
            return;
        }

        SnippetTags.add(snippetId, tagNames);
        tagInput.val('');
        tagInput.focus();
    });

    // 태그 입력창에서 엔터키 이벤트
    $(document).on('keydown', '#tagInput', function(e) {
        if (e.key === 'Enter') {
            e.preventDefault();
            $('#addTagsBtn').trigger('click');
        }
    });

    // 태그 입력창 포커스 이벤트
    $(document).on('focus', '#tagInput', function() {
        $(this).attr('placeholder', '태그명 입력 후 Enter 또는 버튼 클릭');
    });

    $(document).on('blur', '#tagInput', function() {
        $(this).attr('placeholder', '태그 추가 (쉼표로 구분)');
    });
});

// 스니펫 태그 관리 네임스페이스
const SnippetTags = {

    // 스니펫 태그 로드
    load: function(snippetId) {
        console.log('스니펫 태그 로드:', snippetId);

        const tagsDisplay = $('#snippetTagsDisplay');
        tagsDisplay.html('<div class="loading-text">태그를 불러오는 중...</div>');

        $.ajax({
            url: `/api/tag/snippet/${snippetId}`,
            method: 'GET',
            success: function(tags) {
                SnippetTags.display(tags, snippetId);
            },
            error: function(xhr, status, error) {
                console.error('태그 로드 실패:', error);
                tagsDisplay.html('<div class="empty-tags">태그를 불러올 수 없습니다.</div>');
            }
        });
    },

    // 태그 표시
    display: function(tags, snippetId) {
        const tagsDisplay = $('#snippetTagsDisplay');
        tagsDisplay.empty();

        if (!tags || tags.length === 0) {
            tagsDisplay.html('<div class="empty-tags">태그가 없습니다.</div>');
            return;
        }

        tags.forEach(tag => {
            const tagBadge = this.createTagBadge(tag, snippetId);
            tagsDisplay.append(tagBadge);
        });
    },

    // 태그 배지 생성
    createTagBadge: function(tag, snippetId) {
        const tagBadge = $(`
            <div class="tag-badge" data-tag-id="${tag.tagId}">
                <span>${tag.name}</span>
                <span class="tag-remove" title="태그 제거">×</span>
            </div>
        `);

        // 태그 제거 이벤트
        tagBadge.find('.tag-remove').on('click', function() {
            SnippetTags.remove(snippetId, tag.tagId, tagBadge);
        });

        return tagBadge;
    },

    // 태그 추가
    add: function(snippetId, tagNames) {
        const tagArray = tagNames.split(',').map(name => name.trim()).filter(name => name);

        tagArray.forEach(tagName => {
            // 먼저 태그 생성 시도
            $.ajax({
                url: '/api/tag',
                method: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({ name: tagName }),
                success: function(response) {
                    // 새 태그 생성 성공시 스니펫에 연결
                    SnippetTags.connectToSnippet(snippetId, response.tag.tagId, true);
                },
                error: function(xhr) {
                    if (xhr.status === 409) {
                        // 이미 존재하는 태그면 찾아서 연결
                        SnippetTags.findAndConnect(snippetId, tagName);
                    } else {
                        console.error('태그 생성 실패:', xhr.responseText);
                        alert(`태그 "${tagName}" 생성에 실패했습니다.`);
                    }
                }
            });
        });
    },

    // 기존 태그 찾아서 연결
    findAndConnect: function(snippetId, tagName) {
        $.ajax({
            url: '/api/tag/search',
            method: 'GET',
            data: { query: tagName },
            success: function(tags) {
                const matchingTag = tags.find(tag => tag.name === tagName);
                if (matchingTag) {
                    SnippetTags.connectToSnippet(snippetId, matchingTag.tagId, false);
                }
            }
        });
    },

    // 태그를 스니펫에 연결
    connectToSnippet: function(snippetId, tagId, isNewTag = false) {
        $.ajax({
            url: `/api/tag/snippet/${snippetId}/tag/${tagId}`,
            method: 'POST',
            success: function() {
                // 태그 목록 새로고침
                SnippetTags.load(snippetId);

                // 전역 이벤트 발생
                $(document).trigger('tagUpdated', {
                    action: 'added',
                    tagId: tagId,
                    snippetId: snippetId,
                    isNewTag: isNewTag
                });
            },
            error: function(xhr) {
                if (xhr.status !== 409) {
                    console.error('태그 연결 실패:', xhr.responseText);
                }
            }
        });
    },

    // 태그 제거
    remove: function(snippetId, tagId, tagElement) {
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

                // 전역 이벤트 발생
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
    },

    // 모든 태그 제거 (삭제 시 사용)
    removeAll: function(snippetId) {
        return new Promise((resolve, reject) => {
            $.ajax({
                url: `/api/tag/snippet/${snippetId}`,
                method: 'GET',
                success: function(tags) {
                    if (!tags || tags.length === 0) {
                        resolve();
                        return;
                    }

                    const removePromises = tags.map(tag => {
                        return new Promise((tagResolve) => {
                            $.ajax({
                                url: `/api/tag/snippet/${snippetId}/tag/${tag.tagId}`,
                                method: 'DELETE',
                                success: () => tagResolve(),
                                error: () => tagResolve() // 실패해도 계속 진행
                            });
                        });
                    });

                    Promise.all(removePromises).then(resolve);
                },
                error: function() {
                    resolve(); // 조회 실패해도 계속 진행
                }
            });
        });
    }
};