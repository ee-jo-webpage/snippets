package kr.or.kosa.snippets.user.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {
    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;


    // HTML 메일 공통 메서드
    public void sendHtmlMail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("이메일 전송 실패", e);
            throw new RuntimeException("이메일 전송 중 오류 발생");
        }
    }
    //  인증 코드 메일
    public void sendVerificationCode(String to, String code) {
        String subject = "[App] 이메일 인증 코드 안내";
        String content = String.format("""
            <h1>이메일 인증</h1>
            <p>아래 코드를 복사하여 인증창에 입력하세요.</p>
            <h2 style='color:blue;'>%s</h2>
            """, code);
        sendHtmlMail(to, subject, content);
    }

    public void saveVerificationCode(String email, String code, long timeoutInMinutes) {
        redisTemplate.opsForValue().set(getKey(email), code, timeoutInMinutes, TimeUnit.MINUTES);
    }
    private String getKey(String email) {
        return "email_code:" + email;
    }
    public boolean verifyCode(String email, String inputCode) {
        String savedCode = redisTemplate.opsForValue().get(getKey(email));
        return inputCode != null && inputCode.equals(savedCode);
    }

    public void deleteCode(String email) {
        redisTemplate.delete(getKey(email));
    }
}