const form = document.getElementById("updateForm");

const validators = {
    nickname: value => {
        if (!value) return "닉네임은 필수입니다.";
        return (value.length < 2 || value.length > 20)
            ? "닉네임은 2자 이상 20자 이하로 입력해주세요." : "";
    }
};

function validateField(fieldName) {
    const field = form[fieldName];
    const errorSpan = document.getElementById(`${fieldName}Error`);
    const message = validators[fieldName](field.value);
    if (errorSpan) {
        errorSpan.innerText = message;
    } else {
        console.warn("error span not found for", fieldName);
    }
    return message === "";
}

// 에러 메시지 span 생성 및 유효성 검사 연결
["nickname"].forEach(fieldName => {
    const input = form[fieldName];
    const span = document.createElement("span");
    span.id = `${fieldName}Error`;
    span.className = "error-msg";
    input.parentElement.appendChild(span);

    input.addEventListener("focusout", () => {
        validateField(fieldName);
    });
});

form.addEventListener("submit", async function (e) {
    e.preventDefault();

    let valid = true;
    for (let field in validators) {
        if (!validateField(field)) {
            if (valid) form[field].focus();
            valid = false;
        }
    }
    if (!valid) return;

    const data = {
        email: form.email.value,
        nickname: form.nickname.value,
    };

    const res = await fetch("/api/update", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    });

    const msg = await res.text();
    if (res.ok) {
        await Swal.fire({
            icon: "success",
            title: "정보 수정 완료",
            text: msg || "회원 정보가 성공적으로 수정되었습니다."
        });
        location.href = "/";
    } else {
        await Swal.fire({
            icon: "error",
            title: "수정 실패",
            text: msg || "알 수 없는 오류가 발생했습니다."
        });
    }

});