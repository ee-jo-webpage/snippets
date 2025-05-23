package kr.or.kosa.snippets.user.loginLog;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface LoginLogMapper {
    void insertLoginLog(LoginLog log);
    void insertLoginLogs(List<LoginLog> logs); // 배치 저장

    void deleteOldLogs(@Param("cutoff") LocalDateTime cutoff);
}