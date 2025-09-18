package com.yibang.erp.controller.hsf;

import com.yibang.erp.domain.dto.microservice.Result;
import com.yibang.erp.domain.dto.microservice.UserAuthRequest;
import com.yibang.erp.domain.dto.microservice.UserInfoResponse;
import com.yibang.erp.domain.entity.User;
import com.yibang.erp.domain.service.UserService;
import com.yibang.erp.common.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * HSF用户认证服务控制器
 * 提供给yibang-mall系统调用
 *
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@RestController
@RequestMapping("/api/hsf/auth")
public class HsfAuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录验证
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody UserAuthRequest request) {
        try {
            log.info("HSF API调用: 用户登录验证 - {}", request.getUsername());
            
            // 验证用户名密码
            User user = userService.authenticateUser(request.getUsername(), request.getPassword());
            if (user == null) {
                return Result.error("用户名或密码错误");
            }

            // 生成JWT Token
            String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());

            // 构建用户信息响应
            UserInfoResponse userInfo = new UserInfoResponse();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setRealName(user.getRealName());
            userInfo.setEmail(user.getEmail());
            userInfo.setRoleId(user.getRole());
            userInfo.setCompanyId(user.getCompanyId());
            userInfo.setStatus(user.getStatus());
            userInfo.setToken(token);

            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("user", userInfo);
            data.put("token", token);

            return Result.success("登录成功", data);
        } catch (Exception e) {
            log.error("HSF API调用失败: 用户登录验证", e);
            return Result.error("登录验证失败: " + e.getMessage());
        }
    }

    /**
     * 验证Token
     */
    @PostMapping("/verify")
    public Result<UserInfoResponse> verifyToken(@RequestHeader("Authorization") String token) {
        try {
            log.info("HSF API调用: Token验证");
            
            // 移除Bearer前缀
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // 验证Token
            if (!jwtUtil.validateToken(token, null)) {
                return Result.error("Token无效或已过期");
            }

            // 从Token中获取用户信息
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.getUserByUsername(username);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 构建用户信息响应
            UserInfoResponse userInfo = new UserInfoResponse();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setRealName(user.getRealName());
            userInfo.setEmail(user.getEmail());
            userInfo.setRoleId(user.getRole());
            userInfo.setCompanyId(user.getCompanyId());
            userInfo.setStatus(user.getStatus());
            userInfo.setToken(token);

            return Result.success("Token验证成功", userInfo);
        } catch (Exception e) {
            log.error("HSF API调用失败: Token验证", e);
            return Result.error("Token验证失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户ID获取用户信息
     */
    @GetMapping("/user/{userId}")
    public Result<UserInfoResponse> getUserById(@PathVariable Long userId) {
        try {
            log.info("HSF API调用: 根据用户ID获取用户信息 - {}", userId);
            
            User user = userService.getUserById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 构建用户信息响应
            UserInfoResponse userInfo = new UserInfoResponse();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setRealName(user.getRealName());
            userInfo.setEmail(user.getEmail());
            userInfo.setRoleId(user.getRole());
            userInfo.setCompanyId(user.getCompanyId());
            userInfo.setStatus(user.getStatus());

            return Result.success("获取用户信息成功", userInfo);
        } catch (Exception e) {
            log.error("HSF API调用失败: 根据用户ID获取用户信息", e);
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户名获取用户信息
     */
    @GetMapping("/user/username/{username}")
    public Result<UserInfoResponse> getUserByUsername(@PathVariable String username) {
        try {
            log.info("HSF API调用: 根据用户名获取用户信息 - {}", username);
            
            User user = userService.getUserByUsername(username);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 构建用户信息响应
            UserInfoResponse userInfo = new UserInfoResponse();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setRealName(user.getRealName());
            userInfo.setEmail(user.getEmail());
            userInfo.setRoleId(user.getRole());
            userInfo.setCompanyId(user.getCompanyId());
            userInfo.setStatus(user.getStatus());

            return Result.success("获取用户信息成功", userInfo);
        } catch (Exception e) {
            log.error("HSF API调用失败: 根据用户名获取用户信息", e);
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }
}
