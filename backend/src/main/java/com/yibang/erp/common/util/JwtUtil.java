package com.yibang.erp.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.ArrayList;

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
        
        // 根据用户名设置默认角色
        List<String> roles = "admin".equals(username) ? 
            Arrays.asList("ADMIN", "USER") : Arrays.asList("USER");
        claims.put("roles", roles);
        
        return createToken(claims, username);
    }

    /**
     * 生成JWT Token（带自定义角色）
     * 
     * @param username 用户名
     * @param userId 用户ID
     * @param companyId 公司ID
     * @param roles 用户角色列表
     * @return JWT Token
     */
    public String generateToken(String username, Long userId, Long companyId, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("companyId", companyId);
        claims.put("roles", roles);
        
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

    /**
     * 从Token中获取角色列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            Object rolesObj = claims.get("roles");
            System.out.println("JWT工具类 - 从token中提取的roles对象: " + rolesObj);
            System.out.println("JWT工具类 - roles对象的类型: " + (rolesObj != null ? rolesObj.getClass().getName() : "null"));
            
            if (rolesObj instanceof List) {
                List<String> roles = (List<String>) rolesObj;
                System.out.println("JWT工具类 - 成功提取角色列表: " + roles);
                return roles;
            } else if (rolesObj != null) {
                System.out.println("JWT工具类 - roles对象不是List类型，尝试转换");
                // 尝试其他类型的转换
                if (rolesObj instanceof String) {
                    String roleStr = (String) rolesObj;
                    if (roleStr.startsWith("[") && roleStr.endsWith("]")) {
                        // 可能是JSON字符串，尝试解析
                        try {
                            // 简单的字符串解析，移除方括号和引号
                            String[] roleArray = roleStr.substring(1, roleStr.length() - 1)
                                .split(",");
                            List<String> roles = new ArrayList<>();
                            for (String role : roleArray) {
                                roles.add(role.trim().replace("\"", ""));
                            }
                            System.out.println("JWT工具类 - 从字符串解析角色: " + roles);
                            return roles;
                        } catch (Exception e) {
                            System.out.println("JWT工具类 - 字符串解析失败: " + e.getMessage());
                        }
                    }
                }
            }
            System.out.println("JWT工具类 - 无法提取角色信息");
            return null;
        } catch (Exception e) {
            System.out.println("JWT工具类 - 提取角色时发生异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
