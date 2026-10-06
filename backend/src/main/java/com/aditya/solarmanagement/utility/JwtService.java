package com.aditya.solarmanagement.utility;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.aditya.solarmanagement.models.Employee;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
	private final SecretKey key;
	private final long expirationMs;

	public JwtService(@Value("${app.security.jwt-secret}") String secret,
			@Value("${app.security.jwt-expiration-ms}") long expirationMs) {
		this.key = secret.startsWith("base64:")
				? Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret.substring(7)))
				: Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expirationMs = expirationMs;
	}

	public String issue(Employee employee) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(employee.getEmailAddress())
				.claim("role", employee.getRole().name())
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plusMillis(expirationMs)))
				.signWith(key)
				.compact();
	}

	public String subject(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
	}

	public long expirationSeconds() {
		return expirationMs / 1000;
	}
}
