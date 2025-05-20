package kr.or.kosa.snippets.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("kr.or.kosa.snippets.like.mapper")
public class MyBatisConfig {
    // MyBatis 매퍼 스캔 설정
    // kr.or.kosa.snippets.mapper 패키지의 모든 매퍼 인터페이스를 자동으로 찾아서 빈으로 등록
}