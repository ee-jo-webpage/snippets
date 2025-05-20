package kr.or.kosa.snippets.user.service;

import java.util.Random;
import java.util.UUID;

public class CodeGenerator {
    private CodeGenerator() {} // 인스턴스화 방지

    public static String generateCode() {
        return String.valueOf(new Random().nextInt(900_000) + 100_000); // 6자리
    }

    public static String generateTempPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}