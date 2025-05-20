package kr.or.kosa.snippets.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kr.or.kosa.snippets.user.model.UserDTO;
import kr.or.kosa.snippets.user.model.UserUpdateDTO;
import kr.or.kosa.snippets.user.service.AuthService;
import kr.or.kosa.snippets.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserRestController {
    private final AuthService authService;
    private final UserService userService;


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



}