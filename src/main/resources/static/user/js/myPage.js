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

document.getElementById("deleteBtn").addEventListener("click", async () => {
    let reason = "";

    // 1차: 드롭다운 선택
    const { value: selected } = await Swal.fire({
        title: "탈퇴 사유를 선택해주세요",
        html: `
    <style>
        .swal2-select-enhanced {
            width: 100%;
            padding: 0.75rem 1rem;
            font-size: 1rem;
            border-radius: 12px;
            border: 1px solid #ccc;
            appearance: none;
            background-color: #fff;
        }
        .swal2-select-enhanced:focus {
            outline: none;
            border-color: #3b82f6;
            box-shadow: 0 0 0 3px rgba(59,130,246,0.1);
        }
    </style>
    <select id="customReason" class="swal2-select-enhanced">
        <option value="" disabled selected>사유를 선택해주세요</option>
        <option value="이용이 불편함">이용이 불편함</option>
        <option value="자주 사용하지 않음">자주 사용하지 않음</option>
        <option value="개인정보 우려">개인정보 우려</option>
        <option value="기타">기타 (직접 입력)</option>
    </select>
    `,
        preConfirm: () => {
            const selected = document.getElementById('customReason').value;
            if (!selected) {
                Swal.showValidationMessage("사유를 선택해주세요");
            }
            return selected;
        },
        showCancelButton: true,
        confirmButtonText: "다음",
        cancelButtonText: "취소"
    });


    if (!selected) return;

    // 기타 선택 시 추가 입력
    if (selected === "기타") {
        const { value: etc } = await Swal.fire({
            title: "기타 사유를 입력해주세요",
            input: "text",
            inputPlaceholder: "ex) 자주 끊기고 느려요...",
            showCancelButton: true,
            customClass: {
                input: 'swal2-custom-input'
            },
            inputValidator: (value) => {
                if (!value || value.trim() === "") return "기타 사유를 입력해주세요.";
            }
        });


        if (!etc) return;
        reason = etc.trim();
    } else {
        reason = selected;
    }

    // 서버에 탈퇴 요청 보내기
    const res = await fetch("/api/delete", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ reason })
    });
    const result = await res.json();

    if (res.ok) {
        await Swal.fire("탈퇴 완료", result.message || "탈퇴가 정상 처리되었습니다.", "success");
        location.href = "/";
    } else {
        await Swal.fire("오류", "탈퇴 처리 중 문제가 발생했습니다.", "error");
    }
});