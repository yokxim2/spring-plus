package org.example.expert.config;

import java.io.IOException;

import org.example.expert.domain.auth.security.UserDetailsServiceImpl;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final UserDetailsServiceImpl userDetailsService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws
		ServletException,
		IOException {
		String url = request.getRequestURI();

		String tokenValue = request.getHeader("Authorization");

		if (StringUtils.hasText(tokenValue)) {
			tokenValue = jwtUtil.substringToken(tokenValue);

			if (!jwtUtil.validateToken(tokenValue)) {
				log.error("토큰 에러");
				return;
			}

			Claims claims = jwtUtil.extractClaims(tokenValue);
			try {
				setAuthentication(claims.get("email", String.class));

				// 관리자 권한에 대한 권한 체크
				// @hasRole
				if (url.startsWith("/admin")) {
					UserRole userRole = UserRole.valueOf(claims.get("userRole", String.class));

					// ADMIN 권한이 아닌 경우 403 반환
					if (!userRole.equals(UserRole.ADMIN)) {
						response.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자 권한이 없습니다.");
						return;
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage());
				return;
			}
		}

		chain.doFilter(request, response);
	}

	// 인증 처리
	public void setAuthentication(String email) {
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		Authentication authentication = createAuthentication(email);
		context.setAuthentication(authentication);

		SecurityContextHolder.setContext(context);
	}

	// 인증 객체 생성
	private Authentication createAuthentication(String email) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(email);
		return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	}
}
