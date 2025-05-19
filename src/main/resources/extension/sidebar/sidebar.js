chrome.storage.local.get("highlights", (result) => {
    console.log("highlights 호출!")
});

chrome.storage.onChanged.addListener((changes, area) => {
    if (area === "local" && changes.highlights) {
        console.log("로컬 스토리지 변경 감지!");
    }
});