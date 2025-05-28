// 유틸리티 함수들

// Thymeleaf 변수들 (HTML에서 전달받음)
const { currentUserId, message, error, colorDeleteUrl } = window.thymeleafVars || {};

// 알림 메시지 표시
function showAlert(message, type) {
    const alert = document.createElement('div');
    alert.className = `alert alert-${type} fixed`;
    alert.textContent = message;
    document.body.appendChild(alert);

    // 애니메이션 효과로 표시
    setTimeout(() => alert.classList.add('show'), 100);

    // 3초 후 자동 제거
    setTimeout(() => {
        alert.classList.remove('show');
        setTimeout(() => alert.remove(), 300);
    }, 3000);
}

// 세션 정보 토글 함수 (디버깅용)
function toggleSessionInfo() {
    const sessionInfo = document.getElementById('sessionInfo');
    if (sessionInfo) {
        if (sessionInfo.style.display === 'none' || sessionInfo.style.display === '') {
            sessionInfo.style.display = 'block';
            loadSessionData();
        } else {
            sessionInfo.style.display = 'none';
        }
    }
}

// 세션 데이터 로드 함수 (디버깅용)
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

        const sessionIdElement = document.getElementById('sessionId');
        if (sessionIdElement) {
            sessionIdElement.textContent = sessionId;
        }

        // 서버 세션 데이터 확인 API 호출
        try {
            const response = await fetch('/color/check-session');
            if (response.ok) {
                const sessionData = await response.json();
                const serverSessionElement = document.getElementById('serverSessionData');
                if (serverSessionElement) {
                    serverSessionElement.innerHTML =
                        `userId: ${sessionData.userId || '없음'}, ` +
                        `sessionId: ${sessionData.sessionId || '없음'}`;
                }
            } else {
                const serverSessionElement = document.getElementById('serverSessionData');
                if (serverSessionElement) {
                    serverSessionElement.textContent = 'API 응답 오류 - 서버에서 확인하세요';
                }
            }
        } catch (error) {
            const serverSessionElement = document.getElementById('serverSessionData');
            if (serverSessionElement) {
                serverSessionElement.textContent = `세션 확인 API 없음 - ${error.message}`;
            }
            console.log('세션 확인 API 호출 실패:', error);
        }
    } catch (error) {
        console.error('세션 정보 로드 중 오류:', error);
        const serverSessionElement = document.getElementById('serverSessionData');
        if (serverSessionElement) {
            serverSessionElement.textContent = '오류 발생';
        }
    }
}

// 로그인 상태 확인
function checkLoginStatus() {
    if (!currentUserId) {
        alert('로그인이 필요한 기능입니다.');
        return false;
    }
    return true;
}

// Hex 색상 코드 유효성 검사
function isValidHexCode(hexCode) {
    return /^#[0-9A-Fa-f]{6}$/i.test(hexCode);
}