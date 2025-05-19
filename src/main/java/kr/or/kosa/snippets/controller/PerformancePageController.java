package kr.or.kosa.snippets.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PerformancePageController {

    /**
     * 현재 시스템 성능 테스트 페이지
     */
    @GetMapping("/performance-test")
    public String showPerformanceTestPage() {
        return "performance-test"; // performance-test.html
    }

    /**
     * 3가지 방식 최종 비교 페이지
     */
    @GetMapping("/three-way-comparison")
    public String showThreeWayComparisonPage() {
        return "three-way-comparison"; // three-way-comparison.html
    }

    /**
     * 메인 페이지에서 성능 테스트 링크
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/snippets";
    }
}