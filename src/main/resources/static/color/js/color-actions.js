// 색상 관련 액션 기능

// 색상 삭제
// function deleteColor(buttonElement) {
//     if (!checkLoginStatus()) return;
//
//     const colorId = buttonElement.getAttribute('data-color-id');
//     const name = buttonElement.getAttribute('data-name');
//
//     if (confirm(`정말로 "${name}" 색상을 삭제하시겠습니까?`)) {
//         // 동적으로 폼 생성하여 제출
//         const form = document.createElement('form');
//         form.method = 'POST';
//         form.action = colorDeleteUrl || '/color/delete';
//
//         // Spring에서 DELETE 메서드를 사용하기 위한 히든 필드
//         // const methodInput = document.createElement('input');
//         // methodInput.type = 'hidden';
//         // methodInput.name = '_method';
//         // methodInput.value = 'DELETE';
//         // form.appendChild(methodInput);
//
//         // colorId 파라미터
//         const colorIdInput = document.createElement('input');
//         colorIdInput.type = 'hidden';
//         colorIdInput.name = 'colorId';
//         colorIdInput.value = colorId;
//         form.appendChild(colorIdInput);
//
//         document.body.appendChild(form);
//         form.submit();
//     }
// }

// 색상 복사 (클립보드에 hex 코드 복사)
function copyColorToClipboard(hexCode) {
    if (navigator.clipboard) {
        navigator.clipboard.writeText(hexCode).then(() => {
            showAlert(`색상 코드 ${hexCode}가 클립보드에 복사되었습니다!`, 'success');
        }).catch(err => {
            console.error('클립보드 복사 실패:', err);
            showAlert('클립보드 복사에 실패했습니다.', 'error');
        });
    } else {
        // 구형 브라우저 대응
        const textArea = document.createElement('textarea');
        textArea.value = hexCode;
        document.body.appendChild(textArea);
        textArea.select();
        try {
            document.execCommand('copy');
            showAlert(`색상 코드 ${hexCode}가 클립보드에 복사되었습니다!`, 'success');
        } catch (err) {
            console.error('클립보드 복사 실패:', err);
            showAlert('클립보드 복사에 실패했습니다.', 'error');
        }
        document.body.removeChild(textArea);
    }
}

// 색상 즐겨찾기 토글 (향후 기능)
function toggleColorFavorite(colorId) {
    if (!checkLoginStatus()) return;

    // 향후 즐겨찾기 기능 구현 시 사용
    console.log(`색상 ${colorId} 즐겨찾기 토글`);
}

// 색상 내보내기 (JSON 형태로)
function exportColors() {
    if (!checkLoginStatus()) return;

    const colorCards = document.querySelectorAll('.color-card');
    const colors = Array.from(colorCards).map(card => {
        const nameElement = card.querySelector('.color-name');
        const hexElement = card.querySelector('.color-hex');
        return {
            name: nameElement ? nameElement.textContent : '',
            hexCode: hexElement ? hexElement.textContent : ''
        };
    });

    const dataStr = JSON.stringify(colors, null, 2);
    const dataBlob = new Blob([dataStr], {type: 'application/json'});
    const url = URL.createObjectURL(dataBlob);

    const link = document.createElement('a');
    link.href = url;
    link.download = 'my-colors.json';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);

    showAlert('색상 목록이 JSON 파일로 다운로드되었습니다!', 'success');
}