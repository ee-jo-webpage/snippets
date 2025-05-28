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
                return;
            }

            redisServer = new RedisServer(6379);
            redisServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null && redisServer.isActive()) {
            redisServer.stop();
        }
    }
}