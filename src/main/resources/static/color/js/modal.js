// 모달 관련 기능

// 색상 추가 모달 열기
function openAddModal() {
    if (!checkLoginStatus()) return;

    // 폼 초기화
    const addForm = document.getElementById('addColorForm');
    if (addForm) {
        addForm.reset();
    }

    document.getElementById('addHexCode').value = '#FF0000';
    document.getElementById('addColorPicker').value = '#FF0000';
    updateAddPreview();

    const modal = document.getElementById('addModal');
    modal.style.display = 'block';
    setTimeout(() => modal.classList.add('active'), 10);
}

// 색상 추가 모달 닫기
function closeAddModal() {
    const modal = document.getElementById('addModal');
    modal.classList.remove('active');
    setTimeout(() => modal.style.display = 'none', 300);
}

// 색상 수정 모달 열기
function openEditModal(buttonElement) {
    if (!checkLoginStatus()) return;

    const colorId = buttonElement.getAttribute('data-color-id');
    const name = buttonElement.getAttribute('data-name');
    const hexCode = buttonElement.getAttribute('data-hex-code');

    document.getElementById('editColorId').value = colorId;
    document.getElementById('editColorName').value = name;
    document.getElementById('editHexCode').value = hexCode;
    document.getElementById('editColorPicker').value = hexCode;
    updateEditPreview();

    const modal = document.getElementById('editModal');
    modal.style.display = 'block';
    setTimeout(() => modal.classList.add('active'), 10);
}

// 색상 수정 모달 닫기
function closeModal() {
    const modal = document.getElementById('editModal');
    modal.classList.remove('active');
    setTimeout(() => modal.style.display = 'none', 300);
}

// 색상 선택기에서 hex 코드 업데이트 (추가 모달)
function updateAddHexFromPicker() {
    const picker = document.getElementById('addColorPicker');
    const hexInput = document.getElementById('addHexCode');
    const preview = document.getElementById('addColorPreview');

    hexInput.value = picker.value;
    preview.style.backgroundColor = picker.value;
    preview.textContent = picker.value;
}

// 미리보기 업데이트 (추가 모달)
function updateAddPreview() {
    const hexCode = document.getElementById('addHexCode').value;
    const preview = document.getElementById('addColorPreview');

    if (isValidHexCode(hexCode)) {
        preview.style.backgroundColor = hexCode;
        preview.textContent = hexCode;
        document.getElementById('addColorPicker').value = hexCode;
    }
}

// 색상 선택기 업데이트 (수정 모달)
function updateEditHexFromPicker() {
    const picker = document.getElementById('editColorPicker');
    const hexInput = document.getElementById('editHexCode');
    const preview = document.getElementById('editColorPreview');

    hexInput.value = picker.value;
    preview.style.backgroundColor = picker.value;
    preview.textContent = picker.value;
}

// 미리보기 업데이트 (수정 모달)
function updateEditPreview() {
    const hexCode = document.getElementById('editHexCode').value;
    const preview = document.getElementById('editColorPreview');

    if (isValidHexCode(hexCode)) {
        preview.style.backgroundColor = hexCode;
        preview.textContent = hexCode;
        document.getElementById('editColorPicker').value = hexCode;
    }
}

// 모달 외부 클릭 시 닫기
function handleModalOutsideClick(event) {
    const addModal = document.getElementById('addModal');
    const editModal = document.getElementById('editModal');

    if (event.target === addModal) {
        closeAddModal();
    }
    if (event.target === editModal) {
        closeModal();
    }
}

// ESC 키로 모달 닫기
function handleEscapeKey(event) {
    if (event.key === 'Escape') {
        closeModal();
        closeAddModal();
    }
}