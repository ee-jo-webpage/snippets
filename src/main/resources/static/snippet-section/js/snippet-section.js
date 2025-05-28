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
            if (snippet.language) {
                contentDiv.append(`<div class="language-badge">${snippet.language}</div>`);
            }
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
});