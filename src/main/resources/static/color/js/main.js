// 메인 초기화 스크립트

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
    // 메시지 표시
    if (message) {
        showAlert(message, 'success');
    }

    if (error) {
        showAlert(error, 'error');
    }

    // 이벤트 리스너 등록
    initializeEventListeners();
});

// 이벤트 리스너 초기화
function initializeEventListeners() {
    // hex 코드 입력 시 실시간 업데이트 (추가 모달)
    const addHexInput = document.getElementById('addHexCode');
    if (addHexInput) {
        addHexInput.addEventListener('input', updateAddPreview);
    }

    // hex 코드 입력 시 실시간 업데이트 (수정 모달)
    const editHexInput = document.getElementById('editHexCode');
    if (editHexInput) {
        editHexInput.addEventListener('input', updateEditPreview);
    }

    // 모달 외부 클릭 시 닫기
    window.addEventListener('click', handleModalOutsideClick);

    // ESC 키로 모달 닫기
    document.addEventListener('keydown', handleEscapeKey);
}