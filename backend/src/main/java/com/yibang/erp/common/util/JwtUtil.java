package com.yibang.erp.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 
 * @author yibang-erp
 * @since 2024-01-14
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 生成JWT Token
     * 
     * @param username 用户名
     * @param userId 用户ID
     * @param companyId 公司ID
     * @return JWT Token
     */
    public String generateToken(String username, Long userId, Long companyId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("companyId", companyId);
        
        return createToken(claims, username);
    }

    /**
     * 创建Token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从Token中获取公司ID
     */
    public Long getCompanyIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("companyId", Long.class);
    }

    /**
     * 从Token中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 检查Token是否过期
     */
    public Boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 验证Token
     */
    public Boolean validateToken(String token, String username) {
        try {
            final String tokenUsername = getUsernameFromToken(token);
            return (username.equals(tokenUsername) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从Token中获取指定声明
     */
    private <T> T getClaimFromToken(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从Token中获取所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
