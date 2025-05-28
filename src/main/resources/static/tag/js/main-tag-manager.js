// main-tag-manager.js - 메인 초기화 및 이벤트 바인딩

$(document).ready(function() {

    // 로그인된 사용자만 태그 관리 기능 초기화
    if (typeof userId !== 'undefined' && userId != null) {
        initializeTagManager();
    }

    // 초기 메시지 표시
    showInitialMessages();

    // 모달 이벤트 바인딩
    bindModalEvents();

    // 태그 관리 초기화
    function initializeTagManager() {
        // 초기 데이터 로드
        TagAPI.loadAllTags().then(tags => {
            TagManager.allTags = tags;
            TagUI.displayAllTags(tags);
            TagUI.updateTagCount(tags.length);
        });

        // 이벤트 바인딩
        bindTagEvents();
    }

    // 태그 관련 이벤트 바인딩
    function bindTagEvents() {
        // 태그 검색 입력 이벤트
        $('#tagSearchInput').on('input', function() {
            const keyword = $(this).val().trim();
            if (keyword.length > 0) {
                const filteredTags = Utils.filterTags(TagManager.allTags, keyword);
                TagUI.displayAllTags(filteredTags);
            } else {
                TagUI.displayAllTags(TagManager.allTags);
            }
        });

        // 태그 검색 엔터키 이벤트
        $('#tagSearchInput').on('keydown', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                const tagName = $(this).val().trim();
                if (tagName) {
                    createNewTag(tagName);
                }
            }
        });

        // 태그 추가 버튼 클릭
        $('#addTagBtn').on('click', function() {
            const tagName = $('#tagSearchInput').val().trim();
            if (tagName) {
                createNewTag(tagName);
            } else {
                Utils.showAlert('태그명을 입력해주세요.', 'warning');
                $('#tagSearchInput').focus();
            }
        });
    }

    // 새 태그 생성
    function createNewTag(tagName) {
        TagAPI.createTag(tagName)
            .then(() => {
                $('#tagSearchInput').val('');
                return TagAPI.loadAllTags();
            })
            .then(tags => {
                TagManager.allTags = tags;
                TagUI.displayAllTags(tags);
                TagUI.updateTagCount(tags.length);
            })
            .catch(error => {
                console.error('태그 생성 실패:', error);
            });
    }

    // 모달 이벤트 바인딩
    function bindModalEvents() {
        // 삭제 모달 닫기
        $('#cancelDelete, .modal-close').on('click', function() {
            $('#deleteModal').hide();
        });

        // 모달 외부 클릭시 닫기
        $('#deleteModal').on('click', function(e) {
            if (e.target === this) {
                $(this).hide();
            }
        });

        // ESC 키로 모달 닫기
        $(document).on('keydown', function(e) {
            if (e.key === 'Escape' && $('#deleteModal').is(':visible')) {
                $('#deleteModal').hide();
            }
        });
    }

    // 초기 메시지 표시
    function showInitialMessages() {
        if (typeof message !== 'undefined' && message) {
            Utils.showAlert(message, 'success');
        }
        if (typeof error !== 'undefined' && error) {
            Utils.showAlert(error, 'error');
        }
    }
});