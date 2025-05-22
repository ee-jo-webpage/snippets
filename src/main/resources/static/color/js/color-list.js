// 세션 정보 토글 함수
function toggleSessionInfo() {
    const sessionInfo = document.getElementById('sessionInfo');
    if (sessionInfo.style.display === 'none' || sessionInfo.style.display === '') {
        sessionInfo.style.display = 'block';
        loadSessionData();
    } else {
        sessionInfo.style.display = 'none';
    }
}

// 세션 데이터 로드 함수
async function loadSessionData() {
    try {
        // 세션 ID 가져오기 (쿠키에서)
        const cookies = document.cookie.split(';');
        let sessionId = '쿠키에서 세션ID를 찾을 수 없음';

        for (let cookie of cookies) {
            const [name, value] = cookie.trim().split('=');
            if (name === 'JSESSIONID' || name === 'connect.sid') {
                sessionId = value;
                break;
            }
        }
        document.getElementById('sessionId').textContent = sessionId;

        // 서버 세션 데이터 확인 API 호출
        try {
            // URL을 /session-info로 변경
            const response = await fetch('/color/session-info');
            if (response.ok) {
                const sessionData = await response.json();
                console.log('세션 데이터:', sessionData);  // 디버깅 로그 개선
                console.log('userId 값:', sessionData.userId);
                console.log('sessionId 값:', sessionData.sessionId);

                // hasUserId와 sessionExists 속성이 없으니 직접 계산
                const hasUserId = sessionData.userId != null;
                const sessionExists = sessionData.sessionId != null;

                document.getElementById('serverSessionData').innerHTML =
                    `<strong>userId:</strong> ${sessionData.userId || '없음'}<br>` +
                    `<strong>sessionId:</strong> ${sessionData.sessionId || '없음'}<br>` +
                    `<strong>세션 존재:</strong> ${sessionExists ? '예' : '아니오'}<br>` +
                    `<strong>userId 존재:</strong> ${hasUserId ? '예' : '아니오'}`;
            } else {
                document.getElementById('serverSessionData').textContent =
                    `HTTP ${response.status} - 세션 확인 엔드포인트가 없습니다`;
            }
        } catch (error) {
            document.getElementById('serverSessionData').textContent =
                `네트워크 오류: ${error.message}`;
            console.log('세션 확인 API 호출 실패:', error);
        }
    } catch (error) {
        console.error('세션 정보 로드 중 오류:', error);
        document.getElementById('serverSessionData').textContent = '오류 발생';
    }
}

// 모달 열기/닫기 함수
function openAddModal() {
    document.getElementById('addModal').style.display = 'block';
    // 폼 초기화
    document.getElementById('addColorForm').reset();
    document.getElementById('colorPreview').style.backgroundColor = '#FFFFFF';
    document.getElementById('colorPreview').textContent = '미리보기';
}

function openEditModal(buttonElement) {
    const colorId = buttonElement.getAttribute('data-color-id');
    const name = buttonElement.getAttribute('data-name');
    const hexCode = buttonElement.getAttribute('data-hex-code');
    document.getElementById('editModal').style.display = 'block';
    document.getElementById('editColorId').value = colorId;
    document.getElementById('editColorName').value = name;
    document.getElementById('editHexCode').value = hexCode;
    document.getElementById('editColorPicker').value = hexCode;
    document.getElementById('editColorPreview').style.backgroundColor = hexCode;
    document.getElementById('editColorPreview').textContent = hexCode;
}

function closeModal(modalId) {
    document.getElementById(modalId).style.display = 'none';
}

// 모달 외부 클릭 시 닫기
window.onclick = function(event) {
    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    });
}

// 색상 선택기 이벤트
function updateHexFromPicker() {
    const colorPicker = document.getElementById('colorPicker');
    const hexInput = document.getElementById('hexCode');
    const preview = document.getElementById('colorPreview');

    hexInput.value = colorPicker.value;
    preview.style.backgroundColor = colorPicker.value;
    preview.textContent = colorPicker.value;
}

function updateEditHexFromPicker() {
    const colorPicker = document.getElementById('editColorPicker');
    const hexInput = document.getElementById('editHexCode');
    const preview = document.getElementById('editColorPreview');

    hexInput.value = colorPicker.value;
    preview.style.backgroundColor = colorPicker.value;
    preview.textContent = colorPicker.value;
}

// 헥스 코드 입력 시 미리보기 업데이트
document.getElementById('hexCode').addEventListener('input', function() {
    const hexValue = this.value;
    const preview = document.getElementById('colorPreview');
    const colorPicker = document.getElementById('colorPicker');

    if (/^#[0-9A-Fa-f]{6}$/.test(hexValue)) {
        preview.style.backgroundColor = hexValue;
        preview.textContent = hexValue;
        colorPicker.value = hexValue;
    }
});

document.getElementById('editHexCode').addEventListener('input', function() {
    const hexValue = this.value;
    const preview = document.getElementById('editColorPreview');
    const colorPicker = document.getElementById('editColorPicker');

    if (/^#[0-9A-Fa-f]{6}$/.test(hexValue)) {
        preview.style.backgroundColor = hexValue;
        preview.textContent = hexValue;
        colorPicker.value = hexValue;
    }
});

// 삭제 - 동적 폼 생성 후 제출
function deleteColor(buttonElement) {
    const colorId = buttonElement.getAttribute('data-color-id');
    const name = buttonElement.getAttribute('data-name');

    if (confirm(`정말로 "${colorName}" 색상을 삭제하시겠습니까?`)) {
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = /*[[@{/color/delete}]]*/ '/color/delete';

        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'colorId';
        input.value = colorId;
        form.appendChild(input);

        document.body.appendChild(form);
        form.submit();
    }
}
