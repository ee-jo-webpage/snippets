package kr.or.kosa.snippets.user.blockIp;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class BlockedIpLogApiController {

    private final BlockedIpLogMapper blockedIpLogMapper;

    @GetMapping("/blocked-ips")
    public List<BlockedIpLog> getBlockedIps() {
        return blockedIpLogMapper.findAllBlockedIpLogs();
    }
}