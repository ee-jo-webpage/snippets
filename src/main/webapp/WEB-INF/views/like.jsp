<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Like Feature</title>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <style>
        .like-btn {
            background: none;
            border: none;
            font-size: 24px;
            cursor: pointer;
            transition: all 0.3s ease;
        }

        .like-btn.liked {
            color: #ff6b6b;
            transform: scale(1.1);
        }

        .like-btn:not(.liked) {
            color: #gray;
        }

        .like-section {
            display: flex;
            align-items: center;
            gap: 8px;
            margin: 10px 0;
        }

        .like-count {
            font-weight: bold;
            color: #333;
        }
    </style>
</head>
<body>
<!-- 하트 버튼과 좋아요 수 표시 -->
<div id="likeSection" class="like-section">
    <button id="likeButton" class="like-btn">❤️</button>
    <span id="likeCount" class="like-count">${likeCount}</span>
    <span>좋아요</span>
</div>

<script>
    alert("Alert 테스트!");
    console.log("Console 테스트!");
    let snippetId = 1;
    console.log("하드코딩된 snippetId:", snippetId);
    $(document).ready(function() {
        console.log("=== JavaScript 시작 ===");
        console.log("jQuery 로드됨:", typeof $ !== 'undefined');

        let snippetId = ${snippetId != null ? snippetId : 1};
        console.log("현재 snippetId:", snippetId);
        let isLiked = false;  // 좋아요 상태

        const likeButton = $('#likeButton');
        const likeCount = $('#likeCount');

        console.log("현재 snippetId:", snippetId);

        // 페이지 로드 시 좋아요 상태 확인
        checkLikeStatus();

        // 좋아요 상태 확인 함수
        function checkLikeStatus() {
            $.ajax({
                url: '/api/likes/status',  // 절대 경로 사용
                type: 'GET',
                data: { snippetId: snippetId },
                success: function(response) {
                    if (response.success) {
                        isLiked = response.isLiked;
                        likeCount.text(response.likeCount);
                        updateLikeButton();
                    }
                },
                error: function(xhr, status, error) {
                    console.error('좋아요 상태 확인 실패:', error);
                }
            });
        }

        // 좋아요 버튼 상태 업데이트
        function updateLikeButton() {
            if (isLiked) {
                likeButton.addClass('liked');
            } else {
                likeButton.removeClass('liked');
            }
        }

        // 하트 버튼 클릭 이벤트
        likeButton.on('click', function() {
            console.log("버튼 클릭 - snippetId:", snippetId);

            // 중복 클릭 방지
            likeButton.prop('disabled', true);

            if (isLiked) {
                // 좋아요 취소
                $.ajax({
                    url: '/api/likes/remove',  // 절대 경로 사용
                    type: 'POST',
                    data: { snippetId: snippetId },
                    success: function(response) {
                        if (response.success) {
                            isLiked = false;
                            likeCount.text(response.likeCount);
                            updateLikeButton();
                            console.log('좋아요 취소 성공:', response.message);
                        } else {
                            alert('좋아요 취소 실패: ' + response.message);
                        }
                    },
                    error: function(xhr, status, error) {
                        console.error('좋아요 취소 실패:', error);
                        alert('좋아요 취소 중 오류가 발생했습니다.');
                    },
                    complete: function() {
                        likeButton.prop('disabled', false);
                    }
                });
            } else {
                // 좋아요 추가
                $.ajax({
                    url: '/api/likes/add',  // 절대 경로 사용
                    type: 'POST',
                    data: { snippetId: snippetId },
                    beforeSend: function() {
                        console.log("좋아요 추가 요청 - snippetId:", snippetId);
                    },
                    success: function(response) {
                        if (response.success) {
                            isLiked = true;
                            likeCount.text(response.likeCount);
                            updateLikeButton();
                            console.log('좋아요 추가 성공:', response.message);
                        } else {
                            alert('좋아요 추가 실패: ' + response.message);
                        }
                    },
                    error: function(xhr, status, error) {
                        console.error('좋아요 추가 실패:', error);
                        alert('좋아요 추가 중 오류가 발생했습니다.');
                    },
                    complete: function() {
                        likeButton.prop('disabled', false);
                    }
                });
            }
        });
    });
</script>
</body>
</html>