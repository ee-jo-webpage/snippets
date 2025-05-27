
document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("pwForm");

    // 정규식: 영문 + 숫자 + 특수문자 + 8~20자
    const pwRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[~!@#$%^&*()_+]).{8,20}$/;

    // 유효성 검사기
    const validators = {
        newPassword: value => {
            return pwRegex.test(value)
                ? ""
                : "비밀번호는 영문, 숫자, 특수문자를 포함한 8~20자여야 합니다.";
        },
        confirmPassword: value => {
            return value === form.newPassword.value
                ? ""
                : "비밀번호가 일치하지 않습니다.";
        }
    };

    // 공통 에러 표시 함수
    function showError(field, message) {
        const span = document.getElementById(`${field}Error`);
        if (span) {
            span.innerText = message;
            span.classList.toggle("show", !!message);
        }
    }

    // 비밀번호 강도 UI
    function checkPasswordStrength(password) {
        const requirements = {
            length: password.length >= 8 && password.length <= 20,
            alpha: /[A-Za-z]/.test(password),
            number: /\d/.test(password),
            special: /[~!@#$%^&*()_+]/.test(password)
        };

        Object.entries(requirements).forEach(([key, passed]) => {
            const el = document.getElementById(`req-${key}`);
            const icon = el?.querySelector('i');
            if (el && icon) {
                el.classList.toggle("met", passed);
                el.classList.toggle("unmet", !passed);
                icon.className = passed ? 'fas fa-check' : 'fas fa-times';
            }
        });

        const score = Object.values(requirements).filter(Boolean).length;
        const fill = document.getElementById("strengthFill");
        const text = document.getElementById("strengthText");

        fill.className = "strength-fill";
        text.className = "strength-text";

        if (score <= 1) {
            fill.classList.add("weak");
            text.classList.add("weak");
            text.textContent = "약함";
        } else if (score === 2) {
            fill.classList.add("fair");
            text.classList.add("fair");
            text.textContent = "보통";
        } else if (score === 3) {
            fill.classList.add("good");
            text.classList.add("good");
            text.textContent = "좋음";
        } else {
            fill.classList.add("strong");
            text.classList.add("strong");
            text.textContent = "매우 강함";
        }
    }

    // 새 비밀번호 입력 시
    form.newPassword.addEventListener("input", e => {
        const password = e.target.value;
        const strengthEl = document.getElementById("passwordStrength");
        strengthEl.classList.toggle("show", password.length > 0);
        checkPasswordStrength(password);
        showError("newPassword", validators.newPassword(password));
    });

    // 비밀번호 확인
    form.confirmPassword.addEventListener("input", e => {
        showError("confirmPassword", validators.confirmPassword(e.target.value));
    });

    // 제출
    form.addEventListener("submit", async function (e) {
        e.preventDefault();

        document.querySelectorAll('.error-msg').forEach(el => {
            el.innerText = "";
            el.classList.remove("show");
        });

        let valid = true;
        for (let field in validators) {
            const msg = validators[field](form[field].value);
            showError(field, msg);
            if (msg && valid) {
                form[field].focus();
                valid = false;
            }
        }
        if (!valid) return;

        const data = {
            currentPassword: form.currentPassword.value,
            newPassword: form.newPassword.value
        };

        const res = await fetch("/api/change-password", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(data)
        });

        const result = await res.json();
        if (res.ok) {
            await Swal.fire("비밀번호 변경 완료", result.message || "비밀번호가 변경되었습니다.", "success");
            location.href = "/";
        } else {
            await Swal.fire("오류", result.error || "비밀번호가 일치하지 않습니다.", "error");
        }
    });
});