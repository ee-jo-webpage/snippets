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


    public void saveRecoveryToken(String email, String token, long timeoutInMinutes) {
        String key = "recover:" + email;
        redisTemplate.opsForValue().set(key, token, timeoutInMinutes, TimeUnit.MINUTES);
    }

    /**
     * 계정 복구 발송 이메일
     */
    public void reActiveAccount(String to, String link) {
        String subject = "[App] 휴면 계정 활성화 알림 안내";
        //<img src="https://upload.wikimedia.org/wikipedia/commons/0/08/Netflix_2015_logo.svg" alt="logo" style="height:40px">
        String content = String.format("""
                <div style="max-width:600px;margin:auto;font-family:'Segoe UI',sans-serif;border:1px solid #ddd;border-radius:10px;overflow:hidden;">
                     <div style="background:#e50914;padding:20px;text-align:center;">
                         <p style="height:40px color:white">SNIPPET</p>
                     </div>
                     <div style="padding:30px;background:#fff;">
                         <h2 style="color:#333;">계정 복구 요청</h2>
                         <p style="font-size:16px;color:#444;">
                             안녕하세요. 요청하신 계정 복구를 위해 아래 버튼을 클릭해주세요.
                         </p>
                         <div style="margin:30px 0;text-align:center;">
                             <a href="%s" style="display:inline-block;padding:14px 28px;background-color:#e50914;color:white;text-decoration:none;border-radius:5px;font-weight:bold;">
                                 계정 복구하기
                             </a>
                         </div>
                         <p style="font-size:14px;color:#777;">만약 복구 요청을 하지 않으셨다면 이 메일을 무시해주세요.</p>
                         <p style="font-size:12px;color:#aaa;">© 2025 Your App</p>
                     </div>
                 </div>
                
                """, link);
        sendHtmlMail(to, subject, content);
    }
    public boolean verifyRecoveryToken(String email, String token) {
        String key = "recover:" + email;
        String saved = redisTemplate.opsForValue().get(key);
        return token.equals(saved);
    }

    public void deleteRecoveryToken(String email) {
        redisTemplate.delete("recover:" + email);
    }

}