package kr.or.kosa.snippets.user.blockIp;

import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BlockedIpLogMapper {
    void insertBlockedIpLog(BlockedIpLog log);

    List<BlockedIpLog> findAllBlockedIpLogs();

    List<BlockedIpLog> findAllStillBlocked(LocalDateTime now);
}