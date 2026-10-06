package com.aditya.solarmanagement.utility;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.aditya.solarmanagement.service.EmployeeDetailsService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
	private static final String PASSWORD_CHANGE_PATH = "/api/v1/auth/change-password";
	private final JwtService jwtService;
	private final EmployeeDetailsService employeeDetailsService;
	private final ApiResponseWriter apiResponseWriter;

	public JwtAuthenticationFilter(JwtService jwtService, EmployeeDetailsService employeeDetailsService,
			ApiResponseWriter apiResponseWriter) {
		this.jwtService = jwtService;
		this.employeeDetailsService = employeeDetailsService;
		this.apiResponseWriter = apiResponseWriter;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String authorization = request.getHeader("Authorization");
		if (authorization != null && authorization.startsWith("Bearer ")
				&& SecurityContextHolder.getContext().getAuthentication() == null) {
			try {
				String emailAddress = jwtService.subject(authorization.substring(7));
				var user = employeeDetailsService.loadUserByUsername(emailAddress);
				if (user.mustChangePassword() && !PASSWORD_CHANGE_PATH.equals(request.getRequestURI())) {
					logger.info("Blocked request until temporary password is changed");
					apiResponseWriter.write(response, HttpStatus.FORBIDDEN,
							"Change your temporary password to continue");
					return;
				}
				var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (JwtException | IllegalArgumentException | UsernameNotFoundException exception) {
				logger.warn("Rejected invalid or expired bearer token for {} {}", request.getMethod(),
						request.getRequestURI());
				apiResponseWriter.write(response, HttpStatus.UNAUTHORIZED, "Invalid or expired access token");
				return;
			}
		}
		chain.doFilter(request, response);
	}
}
