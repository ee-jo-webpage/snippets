package kr.or.kosa.snippets.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PreDestroy;
import redis.embedded.RedisServer;

import java.io.IOException;

@Configuration
public class EmbeddedRedisConfig {

    private RedisServer redisServer;

    @Bean
    public RedisServer redisServer() throws IOException {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("mac")) {
            System.out.println("💡 macOS 환경입니다. Embedded Redis는 실행하지 않습니다.");
            return null; // or throw new UnsupportedOperationException()
        }

        redisServer = new RedisServer(6379); // 기본 포트 6379
        redisServer.start();
        return redisServer;
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }
}
