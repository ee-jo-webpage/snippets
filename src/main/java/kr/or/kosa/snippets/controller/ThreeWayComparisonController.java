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
public class ThreeWayComparisonController {

    @Autowired
    private SnippetService snippetService;

    @Autowired
    private LikeService likeService;

    /**
     * 3가지 방식 전체 성능 비교
     * 1. 기존 방식 (인덱스 + 저장된 like_count 컬럼)
     * 2. 뷰 방식 (실시간 계산)
     * 3. 상위 100개 뷰 방식
     */
    @GetMapping("/three-way-comparison")
    public Map<String, Object> compareThreeWays(
            @RequestParam(value = "iterations", defaultValue = "10") int iterations,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {

        Map<String, Object> results = new HashMap<>();

        // ===== 최신순 조회 비교 =====
        Map<String, Long> recentComparison = new HashMap<>();

        // 1. 기존 방식 - 최신순
        long recentOriginal = measureTime("기존_최신순", iterations, () ->
                snippetService.getAllPublicSnippets(limit));
        recentComparison.put("기존_방식", recentOriginal);

        // 2. 뷰 방식 - 최신순 (created_at으로 정렬)
        long recentView = measureTime("뷰_최신순", iterations, () ->
                snippetService.getRecentSnippetsFromView(limit));
        recentComparison.put("뷰_방식", recentView);


        // ===== 인기순 조회 비교 =====
        Map<String, Long> popularComparison = new HashMap<>();

        // 1. 기존 방식 - 인기순 (저장된 like_count 사용)
        long popularOriginal = measureTime("기존_인기순", iterations, () ->
                snippetService.getPopularSnippetsJavaSorted(limit));
        popularComparison.put("기존_방식_저장된값", popularOriginal);

        // 2. 뷰 방식 - 인기순 (실시간 계산)
        long popularView = measureTime("뷰_인기순", iterations, () ->
                snippetService.getPopularSnippetsFromView(limit));
        popularComparison.put("뷰_방식_실시간", popularView);

        // 3. 상위 100개 뷰 방식
        long popularTop100 = measureTime("상위100_뷰", iterations, () ->
                snippetService.getTop100PopularSnippets(limit));
        popularComparison.put("상위100개_뷰", popularTop100);


        // ===== 좋아요 관련 기능 비교 =====
        Map<String, Long> likeComparison = new HashMap<>();

        // 1. 좋아요 상태 확인 (기존 방식 - 인덱스 활용)
        long likeStatusOriginal = measureTime("좋아요상태_기존", iterations, () -> {
            List<Boolean> statuses = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                statuses.add(likeService.isLiked(i));
            }
            return statuses;
        });
        likeComparison.put("좋아요_상태_확인", likeStatusOriginal);

        // 2. 좋아요 수 조회 (기존 방식 - 실시간 계산하지만 인덱스 활용)
        long likeCountOriginal = measureTime("좋아요수_기존", iterations, () -> {
            List<Long> counts = new ArrayList<>();
            for (int i = 1; i <= 10; i++) {
                counts.add(likeService.getLikesCount(i));
            }
            return counts;
        });
        likeComparison.put("좋아요_수_조회", likeCountOriginal);


        // ===== 전체 성능 점수 계산 =====
        Map<String, Object> performanceScore = calculatePerformanceScore(
                recentComparison, popularComparison, likeComparison);


        // ===== 메모리 사용량 비교 =====
        Map<String, Object> memoryUsage = checkMemoryUsage();


        // 결과 종합
        results.put("최신순_조회_비교", recentComparison);
        results.put("인기순_조회_비교", popularComparison);
        results.put("좋아요_기능_비교", likeComparison);
        results.put("성능_점수", performanceScore);
        results.put("메모리_사용량", memoryUsage);
        results.put("테스트_설정", Map.of(
                "반복_횟수", iterations,
                "조회_개수", limit,
                "테스트_시간", System.currentTimeMillis()
        ));

        return results;
    }

    /**
     * 개별 기능별 상세 비교
     */
    @GetMapping("/detailed-comparison")
    public Map<String, Object> detailedComparison(
            @RequestParam(value = "feature", defaultValue = "popular") String feature,
            @RequestParam(value = "iterations", defaultValue = "20") int iterations,
            @RequestParam(value = "dataSize", defaultValue = "20") int dataSize) {

        Map<String, Object> results = new HashMap<>();

        if ("popular".equals(feature)) {
            // 인기순 조회 상세 비교
            results = comparePopularQueryMethods(iterations, dataSize);
        } else if ("recent".equals(feature)) {
            // 최신순 조회 상세 비교
            results = compareRecentQueryMethods(iterations, dataSize);
        } else if ("like".equals(feature)) {
            // 좋아요 기능 상세 비교
            results = compareLikeFunctions(iterations);
        }

        return results;
    }

    /**
     * 실시간 성능 모니터링 (WebSocket용)
     */
    @GetMapping("/realtime-monitoring")
    public Map<String, Object> realtimePerformanceMonitoring() {
        Map<String, Object> monitoring = new HashMap<>();

        // 각 방식의 실시간 응답 시간 측정 (1회)
        long originalTime = measureTime("실시간_기존방식", 1, () ->
                snippetService.getPopularSnippetsJavaSorted(10));

        long viewTime = measureTime("실시간_뷰방식", 1, () ->
                snippetService.getPopularSnippetsFromView(10));

        monitoring.put("기존_방식_ms", originalTime);
        monitoring.put("뷰_방식_ms", viewTime);
        monitoring.put("성능_차이_배수", (double) viewTime / originalTime);
        monitoring.put("측정_시각", System.currentTimeMillis());

        return monitoring;
    }

    // ===== 유틸리티 메서드들 =====

    private <T> long measureTime(String operationName, int iterations,
                                 java.util.function.Supplier<T> operation) {
        // 웜업
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

        long avgTimeMs = totalTime / iterations / 1_000_000;
        System.out.println("[성능측정] " + operationName + ": " + avgTimeMs + "ms");
        return avgTimeMs;
    }

    private Map<String, Object> calculatePerformanceScore(
            Map<String, Long> recent, Map<String, Long> popular, Map<String, Long> like) {

        Map<String, Object> score = new HashMap<>();

        // 기존 방식 점수 (기준: 100점)
        score.put("기존_방식_점수", 100);

        // 뷰 방식 점수 계산 (기존 방식 대비)
        long recentOriginal = recent.get("기존_방식");
        long recentView = recent.get("뷰_방식");
        double recentRatio = (double) recentOriginal / recentView;

        long popularOriginal = popular.get("기존_방식_저장된값");
        long popularView = popular.get("뷰_방식_실시간");
        double popularRatio = (double) popularOriginal / popularView;

        // 전체 평균 점수
        double viewScore = (recentRatio + popularRatio) / 2 * 100;
        score.put("뷰_방식_점수", Math.round(viewScore));

        // 성능 개선 권장사항
        List<String> recommendations = new ArrayList<>();
        if (viewScore < 50) {
            recommendations.add("뷰 방식은 현재 데이터량에서 성능이 크게 떨어집니다");
            recommendations.add("기존 방식(인덱스 + 저장된 값) 사용을 강력히 권장합니다");
        } else if (viewScore < 80) {
            recommendations.add("뷰 방식도 사용 가능하지만 기존 방식이 더 효율적입니다");
            recommendations.add("데이터량 증가를 고려하여 기존 방식 권장합니다");
        } else {
            recommendations.add("두 방식 모두 비슷한 성능을 보입니다");
            recommendations.add("데이터 일관성을 위해 뷰 방식도 고려해볼 수 있습니다");
        }

        score.put("권장사항", recommendations);
        return score;
    }

    private Map<String, Object> checkMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();

        Map<String, Object> memory = new HashMap<>();
        memory.put("최대_메모리_MB", runtime.maxMemory() / 1024 / 1024);
        memory.put("전체_메모리_MB", runtime.totalMemory() / 1024 / 1024);
        memory.put("사용_메모리_MB", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024);
        memory.put("가용_메모리_MB", runtime.freeMemory() / 1024 / 1024);

        return memory;
    }

    private Map<String, Object> comparePopularQueryMethods(int iterations, int dataSize) {
        Map<String, Object> comparison = new HashMap<>();

        // 다양한 크기별 테스트
        int[] testSizes = {10, 20, 50, 100};

        for (int size : testSizes) {
            if (size > dataSize) continue;

            Map<String, Long> sizeResults = new HashMap<>();

            sizeResults.put("기존방식", measureTime("기존_" + size, iterations, () ->
                    snippetService.getPopularSnippetsJavaSorted(size)));

            sizeResults.put("뷰방식", measureTime("뷰_" + size, iterations, () ->
                    snippetService.getPopularSnippetsFromView(size)));

            sizeResults.put("상위100뷰", measureTime("상위100_" + size, iterations, () ->
                    snippetService.getTop100PopularSnippets(size)));

            comparison.put("크기_" + size, sizeResults);
        }

        return comparison;
    }

    private Map<String, Object> compareRecentQueryMethods(int iterations, int dataSize) {
        Map<String, Object> comparison = new HashMap<>();

        comparison.put("기존_방식", measureTime("기존_최신순", iterations, () ->
                snippetService.getAllPublicSnippets(dataSize)));

        comparison.put("뷰_방식", measureTime("뷰_최신순", iterations, () ->
                snippetService.getRecentSnippetsFromView(dataSize)));

        return comparison;
    }

    private Map<String, Object> compareLikeFunctions(int iterations) {
        Map<String, Object> comparison = new HashMap<>();

        // 좋아요 상태 확인
        comparison.put("좋아요_상태_확인", measureTime("좋아요상태", iterations, () -> {
            List<Boolean> statuses = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                statuses.add(likeService.isLiked(i));
            }
            return statuses;
        }));

        // 좋아요 수 조회
        comparison.put("좋아요_수_조회", measureTime("좋아요수", iterations, () -> {
            List<Long> counts = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                counts.add(likeService.getLikesCount(i));
            }
            return counts;
        }));

        return comparison;
    }
}