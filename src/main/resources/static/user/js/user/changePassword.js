document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("pwForm");

    const validators = {
        newPassword: value => {
            const pwRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[~!@#$%^&*()_+]).{8,20}$/;
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

    form.addEventListener("submit", async function (e) {
        e.preventDefault();

        document.querySelectorAll('.error-msg').forEach(el => el.innerText = '');

        let valid = true;
        for (let field in validators) {
            const msg = validators[field](form[field].value);
            document.getElementById(`${field}Error`).innerText = msg;
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