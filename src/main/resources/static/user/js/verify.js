const params = new URLSearchParams(location.search);
const email = params.get("email");
document.getElementById("email").value = email;

const form = document.getElementById("verifyForm");
form.addEventListener("submit", async function (e) {
    e.preventDefault();

    const data = {
        email: email,
        code: form.code.value
    };

    const response = await fetch("/api/verify-code", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    });

    const resultDiv = document.getElementById("result");
    if (response.ok) {
        resultDiv.textContent = "인증 성공! 로그인 페이지로 이동합니다...";
        resultDiv.className = "success";
        setTimeout(() => location.href = "/login", 2000);
    } else {
        const error = await response.text();
        resultDiv.textContent = "실패: " + error;
        resultDiv.className = "";
    }
});

document.getElementById("resendBtn").addEventListener("click", async () => {
    const response = await fetch("/api/resend-code", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email })
    });

    const resultDiv = document.getElementById("result");
    if (response.ok) {
        resultDiv.textContent = "인증 코드가 재전송되었습니다.";
        resultDiv.className = "success";
    } else {
        const error = await response.text();
        resultDiv.textContent = "재전송 실패: " + error;
        resultDiv.className = "";
    }
});