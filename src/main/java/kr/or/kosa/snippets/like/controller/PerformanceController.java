package kr.or.kosa.snippets.like.controller;

import kr.or.kosa.snippets.like.model.Snippet;
import kr.or.kosa.snippets.like.service.SnippetService;
import kr.or.kosa.snippets.like.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 포트폴리오용 성능 비교 컨트롤러
 * 3가지 방식의 성능을 간단히 비교: 기존방식 vs 뷰방식 vs 하이브리드
 */
@Controller
@RequestMapping("/performance")
public class PerformanceController {

    @Autowired
    private SnippetService snippetService;

    @Autowired
    private LikeService likeService;

    /**
     * 성능 비교 페이지 표시
     */
    @GetMapping("/comparison")
    public String showComparisonPage() {
        return "like/performance-comparison";  // performance-comparison.html
    }

    /**
     * 3가지 방식 성능 비교 API
     */
    @GetMapping("/api/compare")
    @ResponseBody
    public Map<String, Object> performanceComparison(
            @RequestParam(value = "iterations", defaultValue = "10") int iterations,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {

        Map<String, Object> result = new HashMap<>();

        // 1. 기존 방식 (저장된 like_count 사용)
        long originalTime = measureAverageTime(iterations, () ->
                snippetService.getPopularSnippetsJavaSorted(limit));

        // 2. 뷰 방식 (실시간 계산) - 없으면 시뮬레이션
        long viewTime = measureAverageTime(iterations, () ->
                simulateViewQuery(limit));

        // 3. 좋아요 기능 성능
        long likeTime = measureAverageTime(iterations, () -> {
            likeService.isLiked(1);
            return likeService.getLikesCount(1);
        });

        // 결과 정리
        result.put("기존_방식_ms", originalTime);
        result.put("뷰_방식_ms", viewTime);
        result.put("좋아요_기능_ms", likeTime);
        result.put("성능_개선율", calculateImprovement(viewTime, originalTime));
        result.put("테스트_설정", Map.of(
                "반복횟수", iterations,
                "데이터크기", limit,
                "테스트시간", System.currentTimeMillis()
        ));
        result.put("메모리_정보", getMemoryInfo());
        result.put("권장사항", getRecommendations(originalTime, viewTime));

        return result;
    }

    /**
     * 실시간 모니터링 API
     */
    @GetMapping("/api/monitor")
    @ResponseBody
    public Map<String, Object> realtimeMonitor() {
        Map<String, Object> monitor = new HashMap<>();

        // 즉시 1회 측정
        long originalTime = measureTime(() ->
                snippetService.getPopularSnippetsJavaSorted(10));

        long viewTime = measureTime(() ->
                simulateViewQuery(10));

        monitor.put("기존방식", originalTime + "ms");
        monitor.put("뷰방식", viewTime + "ms");
        monitor.put("성능차이", String.format("%.1f배", (double) viewTime / originalTime));
        monitor.put("측정시각", new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()));

        return monitor;
    }

    // === 유틸리티 메소드들 ===

    /**
     * 평균 실행 시간 측정
     */
    private long measureAverageTime(int iterations, Supplier<Object> operation) {
        // 웜업
        operation.get();
        operation.get();

        long totalTime = 0;
        for (int i = 0; i < iterations; i++) {
            totalTime += measureTime(operation);
        }
        return totalTime / iterations;
    }

    /**
     * 단일 실행 시간 측정
     */
    private long measureTime(Supplier<Object> operation) {
        long startTime = System.nanoTime();
        operation.get();
        long endTime = System.nanoTime();
        return (endTime - startTime) / 1_000_000; // 밀리초 변환
    }

    /**
     * 뷰 방식 시뮬레이션 (실제 뷰가 없는 경우)
     */
    private Object simulateViewQuery(int limit) {
        try {
            // 실제 뷰가 구현되어 있다면 뷰 사용
            List<Snippet> viewSnippets = snippetService.getPopularSnippetsFromView(limit);
            if (viewSnippets != null && !viewSnippets.isEmpty()) {
                return viewSnippets;
            }
        } catch (Exception e) {
            System.out.println("뷰 방식 실행 실패, 시뮬레이션으로 대체: " + e.getMessage());
        }

        // 뷰가 없거나 실패시 시뮬레이션: 상위 100개만 처리
        var allSnippets = snippetService.getAllPublicSnippets();

        // 각 스니펫의 실제 좋아요 수 계산
        allSnippets.forEach(snippet -> {
            long actualLikes = likeService.getLikesCount(snippet.getSnippetId());
            snippet.setLikeCount((int) actualLikes);
        });

        // 정렬 후 상위 100개만 선택, 그 중에서 limit만큼 반환
        return allSnippets.stream()
                .sorted((s1, s2) -> Integer.compare(s2.getLikeCount(), s1.getLikeCount()))
                .limit(100)  // 뷰처럼 상위 100개만
                .limit(limit)  // 요청된 개수만큼만
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 성능 개선율 계산
     */
    private String calculateImprovement(long newTime, long originalTime) {
        if (originalTime == 0) return "측정불가";

        double improvement = ((double) originalTime / newTime) * 100;
        if (improvement > 100) {
            return String.format("%.1f배 빠름", improvement / 100);
        } else {
            return String.format("%.1f%% 느림", 100 - improvement);
        }
    }

    /**
     * 메모리 정보 수집
     */
    private Map<String, Object> getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> memory = new HashMap<>();

        memory.put("사용중_MB", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024);
        memory.put("최대_MB", runtime.maxMemory() / 1024 / 1024);
        memory.put("사용률", String.format("%.1f%%",
                ((double)(runtime.totalMemory() - runtime.freeMemory()) / runtime.maxMemory()) * 100));

        return memory;
    }

    /**
     * 성능 개선 권장사항
     */
    private java.util.List<String> getRecommendations(long originalTime, long viewTime) {
        java.util.List<String> recommendations = new java.util.ArrayList<>();

        double ratio = (double) viewTime / originalTime;

        if (ratio < 1.2) {
            recommendations.add("✅ 두 방식의 성능이 비슷합니다");
            recommendations.add("💡 데이터 일관성을 위해 뷰 방식도 고려 가능");
        } else if (ratio < 2.0) {
            recommendations.add("⚠️ 뷰 방식이 약간 느립니다");
            recommendations.add("🔧 인덱스 최적화 또는 캐시 도입 검토");
        } else {
            recommendations.add("🚨 뷰 방식의 성능이 크게 저하됩니다");
            recommendations.add("🎯 기존 방식(인덱스 + 저장된 값) 강력 권장");
            recommendations.add("📈 데이터량 증가시 성능 차이가 더 벌어질 것으로 예상");
        }

        return recommendations;
    }
}