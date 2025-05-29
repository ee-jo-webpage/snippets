package kr.or.kosa.snippets.user.blockIp;

import org.springframework.security.core.AuthenticationException;

public class IpBlockException extends AuthenticationException {
    public IpBlockException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public IpBlockException(String msg) {
        super(msg);
    }
}