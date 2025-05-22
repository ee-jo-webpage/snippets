package kr.or.kosa.snippets.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import kr.or.kosa.snippets.user.model.UserDTO;
import kr.or.kosa.snippets.user.model.UserUpdateDTO;
import kr.or.kosa.snippets.user.model.Users;
import kr.or.kosa.snippets.user.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserRestController {
    private final AuthService authService;
    private final UserService userService;
    private final UserRecoveryService userRecoveryService;
    private final MailService mailService;


    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO dto, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "errors", result.getFieldErrors().stream()
                            .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage))
            ));
        }

        try {
            authService.register(dto);
            return ResponseEntity.ok(Map.of("message", "회원가입이 완료되었습니다."));
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();

            String targetField = "email";
            if (message.contains("인증을 완료하지 않은")) {
                targetField = "email";
            } else if (message.contains("탈퇴 처리된")) {
                targetField = "email";
            } else if (message.contains("비밀번호")) {
                targetField = "confirmPassword";
            }
            return ResponseEntity.badRequest().body(
                    Map.of("errors", Map.of(targetField, message))
            );
        }
    }


    /**
     * 회원 가입시 인증 API
     */
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> payload) {
        try {
            authService.verifyEmailCode(payload.get("email"), payload.get("code"));
            return ResponseEntity.ok("인증 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserUpdateDTO dto, BindingResult result, Principal principal) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "errors", result.getFieldErrors().stream()
                            .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage))
            ));
        }

        userService.updateUserInfo(principal.getName(), dto);
        return ResponseEntity.ok("회원 정보가 수정되었습니다.");
    }

//    @PostMapping("/delete")
//    public ResponseEntity<?> deleteAccount(@RequestBody Map<String, String> payload,
//                                           Principal principal,
//                                           HttpServletRequest request) {
//        try {
//            String email = principal.getName();
//            String reason = payload.get("reason");
//            userRecoveryService.deactivateUser(email, reason);
//            request.logout(); // 세션 초기화
//            return ResponseEntity.ok(Map.of("message", "계정이 비활성화되었습니다."));
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body(Map.of("error", "탈퇴 처리 중 오류 발생"));
//        }
//    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteAccount(@RequestBody Map<String, String> payload,
                                           Principal principal,
                                           HttpServletRequest request) {
        try {
            String email = principal.getName();
            String reason = payload.get("reason");

            Users user = userService.findByEmail(email);

            // 구글 로그인 유저라면 연동 해제 시도
            if ("GOOGLE".equalsIgnoreCase(user.getLoginType())) {
                String accessToken = (String) request.getSession().getAttribute("oauth2AccessToken");
                if (accessToken != null) {
                    userService.revokeGoogleAccessToken(accessToken);
                }
            }

            userRecoveryService.deactivateUser(email, reason);
            request.logout(); // 세션 초기화
            return ResponseEntity.ok(Map.of("message", "계정이 비활성화되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "탈퇴 처리 중 오류 발생"));
        }
    }


    @PostMapping("/restore")
    public ResponseEntity<?> restore(@RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            userRecoveryService.restoreAccount(email);
            return ResponseEntity.ok(Map.of("message", "계정이 복구되었습니다. 로그인해주세요."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/recover-account")
    public ResponseEntity<?> recoverAccount(@RequestBody Map<String, String> payload) {
        try {
            userRecoveryService.recoverAccount(payload.get("email"));
            return ResponseEntity.ok(Map.of("message", "계정이 복구되었습니다. 로그인해주세요."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reactive-request")
    public ResponseEntity<?> requestReactivation(@RequestBody Map<String, String> request,
                                                 HttpServletRequest httpRequest) {
        String email = request.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "이메일이 누락되었습니다."));
        }

        Users user = userRecoveryService.findDeletedUserByEmail(email);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "존재하지 않는 계정입니다."));
        }

        // 현재 요청에서 도메인 추출
        String requestUrl = httpRequest.getRequestURL().toString(); // ex: http://localhost:8080/api/reactive-request
        String baseUrl = requestUrl.replace(httpRequest.getRequestURI(), ""); // http://localhost:8080

        String token = UUID.randomUUID().toString();
        mailService.saveRecoveryToken(email, token, 30);
        String link = baseUrl + "/reactivate?email=" + email + "&token=" + token;

        mailService.reActiveAccount(email, link);

        return ResponseEntity.ok(Map.of("message", "복구 메일이 전송되었습니다."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> sendTempPassword(@RequestBody Map<String, String> payload) {
        try {
            userRecoveryService.sendTemporaryPassword(payload.get("email"));
            return ResponseEntity.ok("임시 비밀번호가 이메일로 전송되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> payload, Principal principal) {
        String email = principal.getName();
        String current = payload.get("currentPassword");
        String newPw = payload.get("newPassword");

        try {
            userService.changePassword(email, current, newPw);
            return ResponseEntity.ok(Map.of("message", "비밀번호가 변경되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/session-user")
    public ResponseEntity<?> getSessionUser(@AuthenticationPrincipal CustomUserDetails details) {
        if (details == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok().build();
    }

}