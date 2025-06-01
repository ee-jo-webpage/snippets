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
    show: function (snippet) {
        const modal = $('#snippetDetailModal');
        const snippetId = snippet.id || snippet.snippetId;

        // 스니펫 ID를 모달에 저장
        modal.data('current-snippet-id', snippetId);
        modal.data('current-snippet-data', snippet);

        // 모달 제목 업데이트
        modal.find('.modal-header h3').text(snippet.title || '스니펫 상세보기');

        // 로딩 상태 표시
        this.showLoading(modal);

        // 모달 표시
        modal.show();

        // 북마크된 스니펫인지 확인하여 상세 정보 로드
        this.loadSnippetDetail(snippetId, snippet);
    },

    showLoading: function(modal) {
        const contentDiv = modal.find('.snippet-detail-content');
        contentDiv.html(`
            <div class="loading-spinner">
                <i class="fas fa-spinner fa-spin"></i>
                <p>스니펫 정보를 불러오는 중...</p>
            </div>
        `);
    },

    loadSnippetDetail: function(snippetId, basicSnippet) {
        // 일반 스니펫 상세 정보 API 호출
        fetch(`/api/tag/snippet/${snippetId}/detail`)
            .then(response => response.json())
            .then(data => {
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
                        isBookmarked: data.data.isBookmarked
                    };

                    const modal = $('#snippetDetailModal');

                    // 메타 정보 및 콘텐츠 업데이트
                    this.updateContent(modal, detailedSnippet);

                    // 태그 정보 업데이트 - 수정된 부분
                    this.updateTags(data.data.tags || [], snippetId);

                    // 북마크 상태 업데이트
                    this.updateButton(data.data.isBookmarked || false);
                } else {
                    // API 호출 실패 시 기본 데이터로 표시
                    console.warn('북마크 상세 정보 로드 실패:', data.message);
                    this.updateContent($('#snippetDetailModal'), basicSnippet);

                    // 기본 태그/북마크 로드
                    if (typeof SnippetTags !== 'undefined') {
                        SnippetTags.load(snippetId);
                    }
                    if (typeof SnippetBookmark !== 'undefined') {
                        SnippetBookmark.loadStatus(snippetId);
                    }
                }
            })
            .catch(error => {
                console.error('북마크 상세 정보 로드 중 오류:', error);

                // 오류 발생 시 기본 데이터로 표시
                this.updateContent($('#snippetDetailModal'), basicSnippet);

                // 기본 태그/북마크 로드
                if (typeof SnippetTags !== 'undefined') {
                    SnippetTags.load(snippetId);
                }
                if (typeof SnippetBookmark !== 'undefined') {
                    SnippetBookmark.loadStatus(snippetId);
                }
            });
    },

    // 기본 태그 로드 함수 추가
    loadBasicTags: function(snippetId) {
        console.log('기본 태그 로드:', snippetId);

        $.ajax({
            url: `/api/tag/snippet/${snippetId}`,
            method: 'GET',
            success: (tags) => {
                console.log('기본 태그 로드 성공:', tags);
                this.updateTags(tags || [], snippetId);
            },
            error: (xhr) => {
                console.error('기본 태그 로드 실패:', xhr.responseText);
                this.updateTags([], snippetId);
            }
        });
    },

    updateTags: function(tags, snippetId) {
        console.log('태그 업데이트:', tags, snippetId);

        const tagsDisplay = $('#snippetTagsDisplay');
        if (!tagsDisplay.length) {
            console.warn('태그 표시 요소를 찾을 수 없습니다.');
            return;
        }

        tagsDisplay.empty();

        if (!tags || tags.length === 0) {
            tagsDisplay.html('<div class="empty-tags">태그가 없습니다.</div>');
            return;
        }

        tags.forEach(tag => {
            // 태그 데이터 구조 정규화
            const tagData = {
                tagId: tag.tagId || tag.id,
                name: tag.name
            };

            const tagBadge = this.createTagBadge(tagData, snippetId);
            tagsDisplay.append(tagBadge);
        });
    },

    // 태그 배지 생성 함수 추가
    createTagBadge: function(tag, snippetId) {
        const tagBadge = $(`
            <div class="tag-badge" data-tag-id="${tag.tagId}">
                <span>${tag.name}</span>
                <span class="tag-remove" title="태그 제거">×</span>
            </div>
        `);

        // 태그 제거 이벤트
        tagBadge.find('.tag-remove').on('click', () => {
            this.removeTag(snippetId, tag.tagId, tagBadge);
        });

        return tagBadge;
    },

    // 태그 제거 함수 추가
    removeTag: function(snippetId, tagId, tagElement) {
        if (!confirm('이 태그를 제거하시겠습니까?')) {
            return;
        }

        $.ajax({
            url: `/api/tag/snippet/${snippetId}/tag/${tagId}`,
            method: 'DELETE',
            success: () => {
                tagElement.fadeOut(300, function() {
                    $(this).remove();
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
            error: (xhr) => {
                console.error('태그 제거 실패:', xhr.responseText);
                alert('태그 제거에 실패했습니다.');
            }
        });
    },


    hide: function () {
        $('#snippetDetailModal').hide();
    },


    updateContent: function (modal, snippet) {
        // 스니펫 색상이 있으면 모달에 적용
        if (snippet.hexCode) {
            this.applySnippetColor(modal, snippet.hexCode);
        } else {
            this.removeSnippetColor(modal);
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
        if (snippet.hexCode && snippet.colorName) {
            const colorDiv = $('<div class="meta-row color-meta"><strong>색상:</strong> </div>');
            colorDiv.append(`<span class="color-box" style="background-color: ${snippet.hexCode};"></span> ${snippet.colorName}`);
            metaDiv.append(colorDiv);
        }

        // 콘텐츠 업데이트
        const contentDiv = modal.find('.snippet-detail-content');
        contentDiv.empty();

        // 스니펫 타입별 콘텐츠 렌더링
        const contentHtml = this.renderSnippetContent(snippet);
        contentDiv.append(contentHtml);
    },
    // 새로 추가할 함수: 스니펫 색상 적용
    applySnippetColor: function(modal, hexCode) {
        // RGB 변환 함수
        const hexToRgb = (hex) => {
            const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
            return result ? {
                r: parseInt(result[1], 16),
                g: parseInt(result[2], 16),
                b: parseInt(result[3], 16)
            } : null;
        };

        const rgb = hexToRgb(hexCode);

        if (rgb) {
            // 모달 콘텐츠에 border-left 적용
            modal.find('.modal-content').css('border-left', `5px solid ${hexCode}`);

            // 모달 헤더에 스타일 적용
            modal.find('.modal-header').css({
                'border-bottom': `2px solid ${hexCode}`,
                'background': `linear-gradient(90deg, rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, 0.082) 0%, transparent 100%)`
            });

            console.log(`스니펫 색상 적용: ${hexCode}, RGB: ${rgb.r}, ${rgb.g}, ${rgb.b}`);
        }
    },

    // 새로 추가할 함수: 스니펫 색상 제거
    removeSnippetColor: function(modal) {
        // 기본 스타일로 복원
        modal.find('.modal-content').css('border-left', '');
        modal.find('.modal-header').css({
            'border-bottom': '',
            'background': ''
        });

        console.log('스니펫 색상 제거됨');
    },


    renderSnippetContent: function(snippet) {
        const snippetContent = snippet.snippetContent || {};

        if (snippet.type === 'IMG' || snippet.type === 'IMAGE') {
            const imageUrl = snippetContent.imageUrl || snippet.imageUrl;
            const altText = snippetContent.altText || snippet.altText || snippet.memo || '이미지';
            const sourceUrl = snippet.sourceUrl;

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
                             alt="${this.escapeHtml(altText)}" 
                             class="modal-image"
                             onclick="this.showImageZoom('${imageUrl}')"
                             onerror="this.parentNode.innerHTML='<div class=\\'image-error\\'>🖼️ 이미지를 불러올 수 없습니다</div>'">
                        <div class="image-actions">
                            <button class="image-zoom-btn" onclick="SnippetModal.showImageZoom('${imageUrl}')">
                                <i class="fas fa-search-plus"></i> 확대
                            </button>
                            <button class="image-download-btn" onclick="SnippetModal.downloadImage('${imageUrl}', '${this.escapeHtml(altText)}')">
                                <i class="fas fa-download"></i> 다운로드
                            </button>
                        </div>
                    </div>
                    ${altText ? `<div class="image-info"><strong>설명:</strong> ${this.escapeHtml(altText)}</div>` : ''}
                    ${sourceUrl ? `<div class="image-source"><strong>출처:</strong> <a href="${sourceUrl}" target="_blank">${sourceUrl}</a></div>` : ''}
                </div>
            `;

        } else if (snippet.type === 'CODE') {
            const language = snippetContent.language || snippet.language || 'text';
            const content = snippetContent.content || snippet.content || '코드 내용이 없습니다.';

            return `
                <div class="bookmark-modal-content code-content">
                    <div class="language-badge">${language}</div>
                    <div class="code-container">
                        <pre class="code-block"><code class="language-${language}">${this.escapeHtml(content)}</code></pre>
                    </div>
                </div>
            `;

        } else {
            // TEXT 타입 또는 기타
            const content = snippetContent.content || snippet.content || '텍스트 내용이 없습니다.';

            return `
                <div class="bookmark-modal-content text-content">
                    <div class="text-container">
                       
                        <div class="text-content-body">${this.escapeHtml(content).replace(/\n/g, '<br>')}</div>
                    </div>
                </div>
            `;
        }
    },

    copyToClipboard: function(button) {
        const content = $(button).data('content');
        if (navigator.clipboard) {
            navigator.clipboard.writeText(content).then(() => {
                this.showToast('클립보드에 복사되었습니다!');
            }).catch(() => {
                this.fallbackCopyToClipboard(content);
            });
        } else {
            this.fallbackCopyToClipboard(content);
        }
    },

    fallbackCopyToClipboard: function(text) {
        const textArea = document.createElement('textarea');
        textArea.value = text;
        document.body.appendChild(textArea);
        textArea.select();
        try {
            document.execCommand('copy');
            this.showToast('클립보드에 복사되었습니다!');
        } catch (err) {
            this.showToast('복사에 실패했습니다.');
        }
        document.body.removeChild(textArea);
    },

    showImageZoom: function(imageUrl) {
        // 이미지 확대 모달 구현
        const zoomModal = $(`
            <div id="imageZoomModal" class="modal image-zoom-modal" style="display: flex;">
                <div class="modal-content image-zoom-content">
                    <span class="modal-close">&times;</span>
                    <img src="${imageUrl}" class="zoomed-image" alt="확대된 이미지">
                </div>
            </div>
        `);

        $('body').append(zoomModal);

        zoomModal.on('click', function(e) {
            if (e.target === this || $(e.target).hasClass('modal-close')) {
                zoomModal.remove();
            }
        });
    },

    downloadImage: function(imageUrl, filename) {
        try {
            const link = document.createElement('a');
            link.href = imageUrl;
            link.download = filename || 'image';
            link.target = '_blank';
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            this.showToast('이미지 다운로드를 시작합니다.');
        } catch (error) {
            console.error('이미지 다운로드 실패:', error);
            this.showToast('이미지 다운로드에 실패했습니다.');
        }
    },

    showToast: function(message) {
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
    },

    escapeHtml: function(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
};


// 태그 추가 이벤트 리스너
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

    SnippetModal.addTags(snippetId, tagNames);
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

// 태그 추가 함수를 SnippetModal에 추가
SnippetModal.addTags = function(snippetId, tagNames) {
    const tagArray = tagNames.split(',').map(name => name.trim()).filter(name => name);

    tagArray.forEach(tagName => {
        // 먼저 태그 생성 시도
        $.ajax({
            url: '/api/tag',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ name: tagName }),
            success: (response) => {
                // 새 태그 생성 성공시 스니펫에 연결
                this.connectTagToSnippet(snippetId, response.tag.tagId);
            },
            error: (xhr) => {
                if (xhr.status === 409) {
                    // 이미 존재하는 태그면 찾아서 연결
                    this.findAndConnectTag(snippetId, tagName);
                } else {
                    console.error('태그 생성 실패:', xhr.responseText);
                    alert(`태그 "${tagName}" 생성에 실패했습니다.`);
                }
            }
        });
    });
};

// 기존 태그 찾아서 연결
SnippetModal.findAndConnectTag = function(snippetId, tagName) {
    $.ajax({
        url: '/api/tag/search',
        method: 'GET',
        data: { query: tagName },
        success: (tags) => {
            const matchingTag = tags.find(tag => tag.name === tagName);
            if (matchingTag) {
                this.connectTagToSnippet(snippetId, matchingTag.tagId);
            }
        }
    });
};

// 태그를 스니펫에 연결
SnippetModal.connectTagToSnippet = function(snippetId, tagId) {
    $.ajax({
        url: `/api/tag/snippet/${snippetId}/tag/${tagId}`,
        method: 'POST',
        success: () => {
            // 태그 목록 새로고침
            this.loadBasicTags(snippetId);

            // 전역 이벤트 발생
            $(document).trigger('tagUpdated', {
                action: 'added',
                tagId: tagId,
                snippetId: snippetId
            });
        },
        error: (xhr) => {
            if (xhr.status !== 409) {
                console.error('태그 연결 실패:', xhr.responseText);
            }
        }
    });
};