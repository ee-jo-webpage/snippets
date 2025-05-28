// package kr.or.kosa.snippets.snippetExt.config;
//
// import org.apache.ibatis.session.ExecutorType;
// import org.apache.ibatis.session.SqlSessionFactory;
// import org.mybatis.spring.SqlSessionTemplate;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
//
// @Configuration
// public class MyBatisConfigs {
//
//     private final SqlSessionFactory sqlSessionFactory;
//
//     public MyBatisConfigs(SqlSessionFactory sqlSessionFactory) {
//         this.sqlSessionFactory = sqlSessionFactory;
//     }
//
//     @Bean(name = "batch")
//     public SqlSessionTemplate batchSqlSessionTemplate() {
//         return new SqlSessionTemplate(sqlSessionFactory, ExecutorType.BATCH);
//     }
// }
