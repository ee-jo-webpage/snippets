// const params = new URLSearchParams(location.search);
// const email = params.get("email");
// document.getElementById("email").value = email;
//
// const form = document.getElementById("verifyForm");
// form.addEventListener("submit", async function (e) {
//     e.preventDefault();
//
//     const data = {
//         email: email,
//         code: form.code.value
//     };
//
//     const response = await fetch("/api/verify-code", {
//         method: "POST",
//         headers: {
//             "Content-Type": "application/json"
//         },
//         body: JSON.stringify(data)
//     });
//
//     const resultDiv = document.getElementById("result");
//     if (response.ok) {
//         resultDiv.textContent = "인증 성공! 로그인 페이지로 이동합니다...";
//         resultDiv.className = "success";
//         setTimeout(() => location.href = "/login", 2000);
//     } else {
//         const error = await response.text();
//         resultDiv.textContent = "실패: " + error;
//         resultDiv.className = "";
//     }
// });
//
// document.getElementById("resendBtn").addEventListener("click", async () => {
//     const response = await fetch("/api/resend-code", {
//         method: "POST",
//         headers: { "Content-Type": "application/json" },
//         body: JSON.stringify({ email })
//     });
//
//     const resultDiv = document.getElementById("result");
//     if (response.ok) {
//         resultDiv.textContent = "인증 코드가 재전송되었습니다.";
//         resultDiv.className = "success";
//     } else {
//         const error = await response.text();
//         resultDiv.textContent = "재전송 실패: " + error;
//         resultDiv.className = "";
//     }
// });

document.addEventListener("DOMContentLoaded", function () {

    const params = new URLSearchParams(location.search);
    const email = params.get("email") || "user@example.com";
    document.getElementById("email").value = email;
    const emailDisplay = document.getElementById("emailDisplay");
    if (emailDisplay) emailDisplay.textContent = email;

    // DOM 요소
    const form = document.getElementById("verifyForm");
    const verifyBtn = document.getElementById("verifyBtn");
    const resendBtn = document.getElementById("resendBtn");
    const resultDiv = document.getElementById("result");
    const timerDiv = document.getElementById("timer");
    const codeInput = document.getElementById("codeInput");

    // 타이머 관련 변수
    let resendTimer = 0;
    let timerInterval;
    let isResendCooldown = false;

    // 버튼 로딩 상태 관리
    function setButtonLoading(button, isLoading) {
        if (!button) return;
        if (isLoading) {
            button.classList.add('loading');
            button.disabled = true;
            button.textContent = '';
        } else {
            button.classList.remove('loading');
            button.disabled = false;
            button.textContent = button === verifyBtn ? '인증하기' : '인증 코드 재전송';
        }
    }

    // 결과 메시지 표시
    function showResult(message, type = 'info') {
        resultDiv.textContent = message;
        resultDiv.className = `result ${type} show`;
        setTimeout(() => {
            resultDiv.classList.remove('show');
        }, 5000);
    }

    // 재전송 타이머 시작
    function startResendTimer() {
        resendTimer = 60;
        isResendCooldown = true; // 타이머 시작됨!
        resendBtn.disabled = true;
        timerDiv.textContent = `재전송 가능까지 ${resendTimer}초`;
        timerDiv.classList.remove("warning");

        timerInterval = setInterval(() => {
            resendTimer--;
            timerDiv.textContent = `재전송 가능까지 ${resendTimer}초`;
            if (resendTimer <= 10) {
                timerDiv.classList.add("warning");
            }
            if (resendTimer <= 0) {
                clearInterval(timerInterval);
                isResendCooldown = false; // 타이머 종료
                resendBtn.disabled = false;
                timerDiv.textContent = '';
                timerDiv.classList.remove("warning");
            }
        }, 1000);
    }

    // 인증 코드 입력 제한: 숫자 6자리
    codeInput.addEventListener('input', function (e) {
        let value = e.target.value.replace(/\D/g, '');
        if (value.length > 6) value = value.slice(0, 6);
        e.target.value = value;
    });

    // 키보드 엔터로 인증하기
    codeInput.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') {
            form.dispatchEvent(new Event('submit'));
        }
    });

    // 인증 제출
    form.addEventListener("submit", async function (e) {
        e.preventDefault();
        const code = form.code.value.trim();
        if (code.length !== 6) {
            showResult("6자리 인증 코드를 입력해주세요.", "error");
            return;
        }

        setButtonLoading(verifyBtn, true);

        try {
            const res = await fetch("/api/verify-code", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({email, code})
            });

            if (res.ok) {
                showResult("✅ 인증 성공! 로그인 페이지로 이동합니다...", "success");
                setTimeout(() => location.href = "/login", 2000);
            } else {
                const error = await res.text();
                showResult(`❌ 인증 실패: ${error}`, "error");
            }
        } catch {
            showResult("❌ 네트워크 오류가 발생했습니다.", "error");
        } finally {
            setButtonLoading(verifyBtn, false);
        }
    });

    // 인증 코드 재전송
    resendBtn.addEventListener("click", async () => {
        if (isResendCooldown) {
            showResult("⏳ 잠시 후 다시 시도해주세요.", "info");
            return;
        }
        setButtonLoading(resendBtn, true);

        try {
            const res = await fetch("/api/resend-code", {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({email})
            });

            if (res.ok) {
                showResult("📧 인증 코드가 재전송되었습니다.", "success");
                startResendTimer();
            } else {
                const error = await res.text();
                showResult(`❌ 재전송 실패: ${error}`, "error");
            }
        } catch {
            showResult("❌ 네트워크 오류가 발생했습니다.", "error");
        } finally {
            setButtonLoading(resendBtn, false);
        }
    });

    // 페이지 로드시 타이머 시작
    startResendTimer();
});