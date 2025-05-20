package kr.or.kosa.snippets.user.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import redis.embedded.RedisServer;

@Configuration
@ConditionalOnProperty(name = "spring.redis.embedded.enabled", havingValue = "true", matchIfMissing = false)
public class EmbeddedRedisConfig {

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() {
        try {
            String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("mac")) {
                System.out.println("💡 macOS 환경입니다. Embedded Redis는 실행하지 않습니다.");
                return;
            }

            redisServer = new RedisServer(6379);
            redisServer.start();
            System.out.println("💡 Embedded Redis 서버가 시작되었습니다.");
        } catch (Exception e) {
            System.err.println("💥 Embedded Redis 서버 시작 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null && redisServer.isActive()) {
            redisServer.stop();
            System.out.println("💡 Embedded Redis 서버가 중지되었습니다.");
        }
    }
}