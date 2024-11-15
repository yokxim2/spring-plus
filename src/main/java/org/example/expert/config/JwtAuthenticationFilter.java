package org.example.expert.config;

import java.io.IOException;

import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.security.UserDetailsImpl;
import org.example.expert.domain.auth.service.AuthService;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	// UsernamePasswordAuthenticationFilter는 기본적으로 세션 방식을 지원한다.
	// 현재 JWT 토큰 방식을 사용할 예정이므로 커스텀해서 사용한다.

	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.jwtUtil = jwtUtil;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		setFilterProcessesUrl("/auth/signin");
	}

	// 로그인 시도
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws
		AuthenticationException {
		try {
			SigninRequest requestDto = new ObjectMapper().readValue(request.getInputStream(), SigninRequest.class);

			User user = userRepository.findByUsername(requestDto.getEmail()).orElseThrow(
				() -> new InvalidRequestException("가입되지 않은 유저입니다."));
			passwordEncoder.matches(requestDto.getPassword(), user.getPassword());
			return new UsernamePasswordAuthenticationToken(user, user.getPassword(), null);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
		Authentication authResult) {
		Long userId = ((User)authResult.getPrincipal()).getId();
		String username = ((User)authResult.getPrincipal()).getUsername();
		String nickname = ((User)authResult.getPrincipal()).getNickname();
		UserRole role = ((User)authResult.getPrincipal()).getUserRole();

		String token = jwtUtil.createToken(userId, username, nickname, role);

		// 로그인 성공
		// 로그 남기기, 토큰 반환하기
		log.info("로그인 성공 - UserId: {}, Username: {}, Nickname: {}, Role: {}", userId, username, nickname, role);
		response.addHeader("Authorization", token);
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException failed) throws IOException, ServletException {
		log.info("로그인 실패");
		response.setStatus(401);
	}
}
