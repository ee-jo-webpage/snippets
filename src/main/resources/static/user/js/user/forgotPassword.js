document.getElementById("resetForm").addEventListener("submit", async e => {
    e.preventDefault();
    const email = e.target.email.value;

    const res = await fetch("/api/forgot-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email })
    });

    if (res.ok) {
        const msg = await res.text();
        alert(msg);
        location.href = "/login";
    } else {
        const error = await res.text();
        alert("오류: " + error);
    }
});