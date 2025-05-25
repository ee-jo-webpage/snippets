package kr.or.kosa.snippets.navigation.controller;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRunner implements CommandLineRunner {

    @Override
    public void run(String... args) {
        System.out.println("\n========================================");
        System.out.println("🚀 스니펫 프로젝트 시작!");
        System.out.println("========================================");
        System.out.println("📌 네비게이션 페이지: http://localhost:8090/navigation");
        System.out.println("========================================\n");
    }
}