package kr.or.kosa.snippets;

import kr.or.kosa.snippets.user.blockIp.IpLocationService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan(
        basePackages = "kr.or.kosa.snippets",
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = IpLocationService.class)
)

@EnableScheduling
public class SnippetsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnippetsApplication.class, args);
    }
}