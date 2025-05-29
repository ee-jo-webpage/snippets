document.querySelector('form').addEventListener('submit', function(e) {
    const button = this.querySelector('.btn-primary');
    button.classList.add('loading');
    button.innerHTML = '';
});

// 입력 필드 애니메이션
document.querySelectorAll('input').forEach(input => {
    input.addEventListener('focus', function() {
        this.parentElement.style.transform = 'scale(1.02)';
    });

    input.addEventListener('blur', function() {
        this.parentElement.style.transform = 'scale(1)';
    });
});

// 키보드 네비게이션 개선
document.addEventListener('keydown', function(e) {
    if (e.key === 'Enter') {
        const activeElement = document.activeElement;
        if (activeElement.tagName === 'INPUT') {
            const form = activeElement.closest('form');
            const inputs = Array.from(form.querySelectorAll('input[required]'));
            const currentIndex = inputs.indexOf(activeElement);

            if (currentIndex < inputs.length - 1) {
                e.preventDefault();
                inputs[currentIndex + 1].focus();
            }
        }
    }
});