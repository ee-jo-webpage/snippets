package kr.or.kosa.snippets.user.blockIp;

import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BlockedIpLogMapper {
    // block ip 저장
    void insertBlockedIpLog(BlockedIpLog log);
    // 전체 로그 가져오기
    List<BlockedIpLog> findAllBlockedIpLogs();
    // 현재 차단된 기록 가져오기
    List<BlockedIpLog> findAllStillBlocked(LocalDateTime now);
}