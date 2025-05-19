const form = document.getElementById('registerForm');
const submitButton = form.querySelector("button[type='submit']");
const messageDiv = document.getElementById('message');

// 버튼 상태 통합 관리 함수
function setSubmitState(isLoading, label = "처리 중...") {
    submitButton.disabled = isLoading;
    submitButton.textContent = isLoading ? label : "가입하기";
}

const validators = {
    email: value => {
        if (!value) return "이메일은 필수입니다.";
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(value) ? "" : "올바른 이메일 형식이 아닙니다.";
    },
    nickname: value => {
        if (!value) return "닉네임은 필수입니다.";
        return (value.length < 2 || value.length > 20)
            ? "닉네임은 2자 이상 20자 이하로 입력해주세요." : "";
    },
    password: value => {
        if (!value) return "비밀번호는 필수입니다.";
        const pwRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[~!@#$%^&*()_+]).{8,20}$/;
        return pwRegex.test(value) ? "" : "비밀번호는 영문, 숫자, 특수문자를 포함한 8~20자여야 합니다.";
    },
    confirmPassword: value => {
        const pw = form.password.value;
        return value === pw ? "" : "비밀번호와 일치하지 않습니다.";
    }
};

function validateField(fieldName) {
    const field = form[fieldName];
    const message = validators[fieldName](field.value);
    const errorSpan = document.getElementById(`${fieldName}Error`);
    errorSpan.innerText = message;
    return message === "";
}

["email", "nickname", "password", "confirmPassword"].forEach(fieldName => {
    form[fieldName].addEventListener('focusout', () => {
        validateField(fieldName);
    });
});

form.addEventListener('submit', async function (e) {
    e.preventDefault();

    // 초기화
    document.querySelectorAll('.error-msg').forEach(e => e.innerText = '');
    messageDiv.innerText = '';
    messageDiv.className = '';

    setSubmitState(true); // 처리 중...

    // 1. 프론트 유효성 검사
    let valid = true;
    for (let field in validators) {
        const isValid = validateField(field);
        if (!isValid && valid) {
            form[field].focus();
            valid = false;
        }
    }
    if (!valid) {
        setSubmitState(false); // 유효성 실패 시 복구
        return;
    }

    // 2. 서버 요청
    const data = {
        email: form.email.value,
        nickname: form.nickname.value,
        password: form.password.value,
        confirmPassword: form.confirmPassword.value
    };

    try {
        const response = await fetch('/api/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        let result;
        try {
            result = await response.json();
        } catch (e) {
            console.error(" JSON 파싱 실패", e);
            alert("서버 오류가 발생했습니다.");
            const text = await response.text();
            console.error("서버 응답 텍스트:", text);
            setSubmitState(false);
            return;
        }

        if (response.ok) {
            await Swal.fire({
                title: '회원가입 성공!',
                text: result.message || '로그인 페이지로 이동합니다.',
                icon: 'success',
                confirmButtonText: '확인'
            });
            // location.href = `/verify?email=${encodeURIComponent(data.email)}`;
        } else {
            const errors = result.errors;
            if (errors) {
                let focused = false;
                for (let field in errors) {
                    const msg = errors[field];
                    const span = document.getElementById(`${field}Error`);
                    if (span) span.innerText = msg;

                    if (msg.includes("탈퇴 처리된")) {
                        await Swal.fire({
                            title: "탈퇴한 계정입니다",
                            text: "계정을 복구하시겠습니까?",
                            icon: "warning",
                            showCancelButton: true,
                            confirmButtonText: "복구하기",
                            cancelButtonText: "취소"
                        }).then(async result => {
                            if (result.isConfirmed) {
                                setSubmitState(true, "복구 요청 중...");
                                // const res = await fetch("/api/reactive-request", {
                                //     method: "POST",
                                //     headers: { "Content-Type": "application/json" },
                                //     body: JSON.stringify({ email: form.email.value })
                                // });
                                const result = await res.json();

                                if (res.ok) {
                                    await Swal.fire("복구 메일 전송", result.message || "메일을 확인해주세요.", "success");
                                    location.href = "/login";
                                } else {
                                    await Swal.fire("복구 실패", result.error || "알 수 없는 오류가 발생했습니다.", "error");
                                    setSubmitState(false);
                                }
                            } else {
                                setSubmitState(false); // 복구 취소 시
                            }
                        });
                        return;
                    }

                    if (!focused) {
                        form[field].focus();
                        focused = true;
                    }
                }
            } else {
                messageDiv.className = 'error';
                messageDiv.innerText = result.message || "알 수 없는 오류가 발생했습니다.";
            }
        }
    } catch (e) {
        alert("서버 오류가 발생했습니다.");
    } finally {
        setSubmitState(false);
    }
});