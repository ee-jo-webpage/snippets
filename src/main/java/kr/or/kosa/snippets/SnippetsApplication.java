package kr.or.kosa.snippets;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("kr.or.kosa.snippets")
@EnableScheduling
public class SnippetsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnippetsApplication.class, args);
    }
}