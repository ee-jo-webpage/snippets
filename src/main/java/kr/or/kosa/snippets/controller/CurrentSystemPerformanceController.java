// 파일 위치: src/main/java/kr/or/kosa/snippets/controller/CurrentSystemPerformanceController.java

package kr.or.kosa.snippets.controller;

import kr.or.kosa.snippets.service.SnippetService;
import kr.or.kosa.snippets.service.LikeService;
import kr.or.kosa.snippets.model.Snippet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/performance")
public class CurrentSystemPerformanceController {

    @Autowired
    private SnippetService snippetService;

    @Autowired
    private LikeService likeService;

    /**
     * 현재 시스템(뷰 미사용)의 성능 테스트
     */
    @GetMapping("/current-system")
    public Map<String, Object> testCurrentSystemPerformance(
            @RequestParam(value = "iterations", defaultValue = "10") int iterations,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {

        Map<String, Object> results = new HashMap<>();
        Map<String, Long> testResults = new HashMap<>();

        // 1. 최신순 조회 성능 (created_at 기준)
        long recentTime = measureQueryTime("최신순 조회", iterations, () -> {
            return snippetService.getAllPublicSnippets(limit);
        });
        testResults.put("최신순_조회", recentTime);

        // 2. 인기순 조회 성능 (like_count 컬럼 기준)
        long popularTime = measureQueryTime("인기순 조회", iterations, () -> {
            return snippetService.getPopularSnippetsJavaSorted(limit);
        });
        testResults.put("인기순_조회", popularTime);

        // 3. 특정 사용자 스니펫 조회
        long userSnippetsTime = measureQueryTime("사용자 스니펫 조회", iterations, () -> {
            return snippetService.getSnippetsByUserId(2);
        });
        testResults.put("사용자_스니펫_조회", userSnippetsTime);

        // 4. 좋아요 상태 확인 (여러 스니펫)
        long likeStatusTime = measureQueryTime("좋아요 상태 확인", iterations, () -> {
            List<Boolean> statuses = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                statuses.add(likeService.isLiked(i));
            }
            return statuses;
        });
        testResults.put("좋아요_상태_확인", likeStatusTime);

        // 5. 좋아요 수 조회 (여러 스니펫)
        long likeCountTime = measureQueryTime("좋아요 수 조회", iterations, () -> {
            List<Long> counts = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                counts.add(likeService.getLikesCount(i));
            }
            return counts;
        });
        testResults.put("좋아요_수_조회", likeCountTime);

        // 6. 스니펫 상세 조회
        long detailTime = measureQueryTime("스니펫 상세 조회", iterations, () -> {
            return snippetService.getSnippetDetailById(1);
        });
        testResults.put("스니펫_상세_조회", detailTime);

        results.put("testResults", testResults);
        results.put("iterations", iterations);
        results.put("limit", limit);
        results.put("timestamp", System.currentTimeMillis());
        results.put("systemInfo", getSystemInfo());

        return results;
    }

    /**
     * 좋아요 추가/삭제 성능 테스트
     */
    @PostMapping("/like-operations")
    public Map<String, Object> testLikeOperations(
            @RequestParam(value = "iterations", defaultValue = "5") int iterations) {

        Map<String, Object> results = new HashMap<>();

        // 좋아요 추가 성능
        long addLikeTime = measureQueryTime("좋아요 추가", iterations, () -> {
            try {
                likeService.addLike(100); // 테스트용 스니펫 ID
                return true;
            } catch (Exception e) {
                return false;
            }
        });

        // 좋아요 삭제 성능
        long removeLikeTime = measureQueryTime("좋아요 삭제", iterations, () -> {
            try {
                likeService.removeLike(100); // 테스트용 스니펫 ID
                return true;
            } catch (Exception e) {
                return false;
            }
        });

        results.put("좋아요_추가_시간", addLikeTime);
        results.put("좋아요_삭제_시간", removeLikeTime);
        results.put("iterations", iterations);

        return results;
    }

    /**
     * 대량 데이터 처리 성능 테스트
     */
    @GetMapping("/bulk-operations")
    public Map<String, Object> testBulkOperations() {
        Map<String, Object> results = new HashMap<>();

        // 전체 스니펫 조회
        long allSnippetsTime = measureQueryTime("전체 스니펫 조회", 3, () -> {
            return snippetService.getAllPublicSnippets();
        });

        // 페이징 처리 조회
        long pagedTime = measureQueryTime("페이징 조회", 5, () -> {
            return snippetService.getAllPublicSnippetsPaged(0, 10);
        });

        results.put("전체_스니펫_조회", allSnippetsTime);
        results.put("페이징_조회", pagedTime);

        return results;
    }

    /**
     * 쿼리 실행 시간 측정 유틸리티
     */
    private <T> long measureQueryTime(String operationName, int iterations, java.util.function.Supplier<T> operation) {
        // 웜업 (JVM 최적화)
        for (int i = 0; i < 2; i++) {
            operation.get();
        }

        long totalTime = 0;
        for (int i = 0; i < iterations; i++) {
            long startTime = System.nanoTime();
            operation.get();
            long endTime = System.nanoTime();
            totalTime += (endTime - startTime);
        }

        long avgTimeMs = totalTime / iterations / 1_000_000; // 나노초를 밀리초로
        System.out.println(operationName + " 평균 실행 시간: " + avgTimeMs + "ms");

        return avgTimeMs;
    }

    /**
     * 시스템 정보 수집
     */
    private Map<String, Object> getSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();

        Runtime runtime = Runtime.getRuntime();
        systemInfo.put("사용가능_메모리_MB", runtime.maxMemory() / 1024 / 1024);
        systemInfo.put("사용중_메모리_MB", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024);
        systemInfo.put("프로세서_수", runtime.availableProcessors());
        systemInfo.put("Java_버전", System.getProperty("java.version"));

        return systemInfo;
    }
}