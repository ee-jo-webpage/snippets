// tag-api.js - 태그 API 호출 관리

// 태그 API 네임스페이스
const TagAPI = {

    // 모든 태그 로드
    loadAllTags: function() {
        return new Promise((resolve, reject) => {
            $('#tagsContainer').html('<div class="loading-text">태그를 불러오는 중...</div>');

            $.ajax({
                url: '/api/tag/my-tags',
                method: 'GET',
                success: function(tags) {
                    console.log('태그 로드 성공:', tags);
                    resolve(tags || []);
                },
                error: function(xhr, status, error) {
                    console.error('태그 로드 실패:', xhr.status, error);
                    $('#tagsContainer').html('<div class="empty-message">태그 목록을 불러오는데 실패했습니다.</div>');
                    reject(error);
                }
            });
        });
    },

    // 새 태그 생성
    createTag: function(tagName) {
        return new Promise((resolve, reject) => {
            if (!tagName || tagName.trim() === '') {
                Utils.showAlert('태그명을 입력해주세요.', 'warning');
                reject('태그명이 비어있습니다.');
                return;
            }

            console.log('새 태그 생성:', tagName);

            $.ajax({
                url: '/api/tag',
                method: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({
                    name: tagName.trim()
                }),
                success: function(response) {
                    console.log('태그 생성 성공:', response);
                    Utils.showAlert('태그가 성공적으로 생성되었습니다.', 'success');
                    resolve(response);
                },
                error: function(xhr) {
                    console.error('태그 생성 실패:', xhr.status, xhr.responseText);

                    let errorMessage = '태그 생성 중 오류가 발생했습니다.';
                    if (xhr.status === 409) {
                        errorMessage = '이미 존재하는 태그입니다.';
                    } else if (xhr.status === 401) {
                        errorMessage = '로그인이 필요합니다.';
                    }

                    Utils.showAlert(errorMessage, xhr.status === 409 ? 'warning' : 'error');
                    reject(xhr);
                }
            });
        });
    },

    // 태그 삭제
    deleteTag: function(tagId, tagName) {
        console.log('태그 삭제:', tagId, tagName);

        $.ajax({
            url: '/api/tag/' + tagId,
            method: 'DELETE',
            success: function() {
                Utils.showAlert('"' + tagName + '" 태그가 삭제되었습니다.', 'success');

                // 삭제된 태그가 현재 선택된 태그라면 스니펫 섹션 숨기기
                if (TagManager.selectedTagId == tagId) {
                    TagManager.selectedTagId = null;
                    $('#snippetSectionContainer').empty().hide();
                }

                // 태그 목록 새로고침
                TagAPI.loadAllTags().then(tags => {
                    TagManager.allTags = tags;
                    TagUI.displayAllTags(tags);
                    TagUI.updateTagCount(tags.length);
                });
            },
            error: function(xhr) {
                console.error('태그 삭제 실패:', xhr.status, xhr.responseText);

                let errorMessage = '태그 삭제 중 오류가 발생했습니다.';
                if (xhr.status === 403) {
                    errorMessage = '태그 삭제 권한이 없습니다.';
                } else if (xhr.status === 401) {
                    errorMessage = '로그인이 필요합니다.';
                }

                Utils.showAlert(errorMessage, 'error');
            }
        });
    },

    // 태그 검색
    searchTags: function(keyword) {
        return new Promise((resolve, reject) => {
            if (!keyword || keyword.trim() === '') {
                resolve([]);
                return;
            }

            $.ajax({
                url: '/api/tag/search',
                method: 'GET',
                data: { query: keyword.trim() },
                success: function(tags) {
                    resolve(tags || []);
                },
                error: function(xhr) {
                    console.error('태그 검색 실패:', xhr.responseText);
                    reject(xhr);
                }
            });
        });
    },

    // 특정 태그의 스니펫 조회
    getSnippetsByTag: function(tagId) {
        return new Promise((resolve, reject) => {
            $.ajax({
                url: '/api/tag/' + tagId + '/snippets',
                method: 'GET',
                success: function(snippets) {
                    resolve(snippets || []);
                },
                error: function(xhr) {
                    console.error('태그별 스니펫 조회 실패:', xhr.responseText);
                    reject(xhr);
                }
            });
        });
    }
};

// 스니펫 API 네임스페이스
const SnippetAPI = {

    // 모든 스니펫 조회
    getAllSnippets: function() {
        return new Promise((resolve, reject) => {
            $.ajax({
                url: '/api/snippet',
                method: 'GET',
                success: function(snippets) {
                    resolve(snippets || []);
                },
                error: function(xhr) {
                    console.error('스니펫 조회 실패:', xhr.responseText);
                    reject(xhr);
                }
            });
        });
    }
};