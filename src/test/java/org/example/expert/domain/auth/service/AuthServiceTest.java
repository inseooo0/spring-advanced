package org.example.expert.domain.auth.service;

import io.jsonwebtoken.Claims;
import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    JwtUtil jwtUtil;
    AuthService authService;

    @BeforeEach
    public void beforeEach() {
        authService = new AuthService(userRepository, passwordEncoder, jwtUtil);
    }

    @Test
    public void signup_email_not_exist() {
        // given
        SignupRequest signupRequest = new SignupRequest("", "0000", "Admin");

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            authService.signup(signupRequest);
        });

        // then
        assertEquals("이메일 값이 없습니다.", exception.getMessage());
    }

    @Test
    public void signup_with_same_email() {
        // given
        String sameEmail = "user@email.com";
        SignupRequest signupRequest = new SignupRequest(sameEmail, "0000", "Admin");
        given(userRepository.existsByEmail(sameEmail)).willReturn(true);

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            authService.signup(signupRequest);
        });

        // then
        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
    }

    @Test
    public void 정상_가입() {
        // given
        long userId = 1;
        String email = "user@email.com";
        SignupRequest signupRequest = new SignupRequest(email, "0000", "Admin");
        given(userRepository.existsByEmail(email)).willReturn(false);

        User user = new User(email, "0000", UserRole.ADMIN);
        ReflectionTestUtils.setField(user, "id", userId);
        given(userRepository.save(any())).willReturn(user);

        // when
        SignupResponse signupResponse = authService.signup(signupRequest);

        // then
        String bearerToken = signupResponse.getBearerToken();

        Claims claims = jwtUtil.extractClaims(jwtUtil.substringToken(bearerToken));
        assertEquals(Long.parseLong(claims.getSubject()), userId);
    }

    @Test
    public void 가입되지_않은_유저_로그인() {
        // given
        SigninRequest signinRequest = new SigninRequest("user@email.com", "0000");
        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.empty());

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            authService.signin(signinRequest);
        });

        // then
        assertEquals("가입되지 않은 유저입니다.", exception.getMessage());
    }

    @Test
    public void sign_in_wrong_password() {
        // given
        long userId = 1;
        String wrongPassword = "0000";
        String realPassword = "1111";
        SigninRequest signinRequest = new SigninRequest("user@email.com", wrongPassword);

        User user = new User("user@email.com", passwordEncoder.encode(realPassword), UserRole.ADMIN);
        ReflectionTestUtils.setField(user, "id", userId);
        given(userRepository.findByEmail(signinRequest.getEmail())).willReturn(Optional.of(user));

        // when
        AuthException exception = assertThrows(AuthException.class, () -> {
            authService.signin(signinRequest);
        });

        // then
        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());
    }

    @Test
    public void 정상_로그인() {
        // given
        long userId = 1;
        String email = "user@email.com";
        SigninRequest signinRequest = new SigninRequest(email, "0000");

        User user = new User(email, passwordEncoder.encode("0000"), UserRole.ADMIN);
        ReflectionTestUtils.setField(user, "id", userId);
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // when
        SigninResponse signinResponse = authService.signin(signinRequest);

        // then
        String bearerToken = signinResponse.getBearerToken();
        Claims claims = jwtUtil.extractClaims(jwtUtil.substringToken(bearerToken));

        assertEquals(Long.parseLong(claims.getSubject()), userId);
    }
}