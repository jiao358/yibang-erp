package com.yibang.erp.infrastructure.repository;

import com.yibang.erp.domain.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

/**
 * 用户Redis缓存Repository
 * 基于Redis对UserRepository进行缓存封装，防止数据库击穿
 * 
 * @author yibang-erp
 * @since 2024-01-14
 */
@Slf4j
@Repository
public class UserRedisRepository {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    // Redis键前缀
    private static final String USER_KEY_PREFIX = "user:";
    private static final String USER_BY_USERNAME_KEY_PREFIX = "user:username:";
    private static final String USER_BY_EMAIL_KEY_PREFIX = "user:email:";
    private static final String USER_BY_PHONE_KEY_PREFIX = "user:phone:";
    private static final String USER_BY_COMPANY_KEY_PREFIX = "user:company:";
    private static final String USER_BY_ROLE_KEY_PREFIX = "user:role:";
    
    // 缓存过期时间：5分钟
    private static final Duration CACHE_EXPIRE_TIME = Duration.ofMinutes(5);

    /**
     * 根据ID查询用户（带缓存）
     */
    public User selectById(Long id) {
        if (id == null) {
            return null;
        }
        
        String cacheKey = USER_KEY_PREFIX + id;
        
        try {
            // 先从缓存获取
            User cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);
            if (cachedUser != null) {
                log.debug("从缓存获取用户: id={}", id);
                return cachedUser;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: id={}", id);
            User user = userRepository.selectById(id);
            
            if (user != null) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, user, CACHE_EXPIRE_TIME);
                log.debug("用户数据已缓存: id={}", id);
            }
            
            return user;
            
        } catch (Exception e) {
            log.error("查询用户失败: id={}", id, e);
            // 缓存异常时直接查询数据库
            return userRepository.selectById(id);
        }
    }

    /**
     * 根据用户名查询用户（带缓存）
     */
    public User findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        
        String cacheKey = USER_BY_USERNAME_KEY_PREFIX + username;
        
        try {
            // 先从缓存获取
            User cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);
            if (cachedUser != null) {
                log.debug("从缓存获取用户: username={}", username);
                return cachedUser;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: username={}", username);
            User user = userRepository.findByUsername(username);
            
            if (user != null) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, user, CACHE_EXPIRE_TIME);
                // 同时缓存用户ID映射
                redisTemplate.opsForValue().set(USER_KEY_PREFIX + user.getId(), user, CACHE_EXPIRE_TIME);
                log.debug("用户数据已缓存: username={}, id={}", username, user.getId());
            }
            
            return user;
            
        } catch (Exception e) {
            log.error("查询用户失败: username={}", username, e);
            // 缓存异常时直接查询数据库
            return userRepository.findByUsername(username);
        }
    }

    /**
     * 根据邮箱查询用户（带缓存）
     */
    public User findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        
        String cacheKey = USER_BY_EMAIL_KEY_PREFIX + email;
        
        try {
            // 先从缓存获取
            User cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);
            if (cachedUser != null) {
                log.debug("从缓存获取用户: email={}", email);
                return cachedUser;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: email={}", email);
            User user = userRepository.findByEmail(email);
            
            if (user != null) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, user, CACHE_EXPIRE_TIME);
                // 同时缓存用户ID映射
                redisTemplate.opsForValue().set(USER_KEY_PREFIX + user.getId(), user, CACHE_EXPIRE_TIME);
                log.debug("用户数据已缓存: email={}, id={}", email, user.getId());
            }
            
            return user;
            
        } catch (Exception e) {
            log.error("查询用户失败: email={}", email, e);
            // 缓存异常时直接查询数据库
            return userRepository.findByEmail(email);
        }
    }

    /**
     * 根据手机号查询用户（带缓存）
     */
    public User findByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        
        String cacheKey = USER_BY_PHONE_KEY_PREFIX + phone;
        
        try {
            // 先从缓存获取
            User cachedUser = (User) redisTemplate.opsForValue().get(cacheKey);
            if (cachedUser != null) {
                log.debug("从缓存获取用户: phone={}", phone);
                return cachedUser;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: phone={}", phone);
            User user = userRepository.findByPhone(phone);
            
            if (user != null) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, user, CACHE_EXPIRE_TIME);
                // 同时缓存用户ID映射
                redisTemplate.opsForValue().set(USER_KEY_PREFIX + user.getId(), user, CACHE_EXPIRE_TIME);
                log.debug("用户数据已缓存: phone={}, id={}", phone, user.getId());
            }
            
            return user;
            
        } catch (Exception e) {
            log.error("查询用户失败: phone={}", phone, e);
            // 缓存异常时直接查询数据库
            return userRepository.findByPhone(phone);
        }
    }

    /**
     * 根据公司ID查询用户列表（带缓存）
     */
    @SuppressWarnings("unchecked")
    public List<User> findByCompanyId(Long companyId) {
        if (companyId == null) {
            return List.of();
        }
        
        String cacheKey = USER_BY_COMPANY_KEY_PREFIX + companyId;
        
        try {
            // 先从缓存获取
            List<User> cachedUsers = (List<User>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedUsers != null) {
                log.debug("从缓存获取用户列表: companyId={}, count={}", companyId, cachedUsers.size());
                return cachedUsers;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: companyId={}", companyId);
            List<User> users = userRepository.findByCompanyId(companyId);
            
            if (users != null && !users.isEmpty()) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, users, CACHE_EXPIRE_TIME);
                // 同时缓存每个用户的详细信息
                for (User user : users) {
                    redisTemplate.opsForValue().set(USER_KEY_PREFIX + user.getId(), user, CACHE_EXPIRE_TIME);
                }
                log.debug("用户列表数据已缓存: companyId={}, count={}", companyId, users.size());
            }
            
            return users != null ? users : List.of();
            
        } catch (Exception e) {
            log.error("查询用户列表失败: companyId={}", companyId, e);
            // 缓存异常时直接查询数据库
            return userRepository.findByCompanyId(companyId);
        }
    }

    /**
     * 根据角色ID查询用户列表（带缓存）
     */
    @SuppressWarnings("unchecked")
    public List<User> findByRoleId(Long roleId) {
        if (roleId == null) {
            return List.of();
        }
        
        String cacheKey = USER_BY_ROLE_KEY_PREFIX + roleId;
        
        try {
            // 先从缓存获取
            List<User> cachedUsers = (List<User>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedUsers != null) {
                log.debug("从缓存获取用户列表: roleId={}, count={}", roleId, cachedUsers.size());
                return cachedUsers;
            }
            
            // 缓存未命中，查询数据库
            log.debug("缓存未命中，查询数据库: roleId={}", roleId);
            List<User> users = userRepository.findByRoleId(roleId);
            
            if (users != null && !users.isEmpty()) {
                // 将结果存入缓存
                redisTemplate.opsForValue().set(cacheKey, users, CACHE_EXPIRE_TIME);
                // 同时缓存每个用户的详细信息
                for (User user : users) {
                    redisTemplate.opsForValue().set(USER_KEY_PREFIX + user.getId(), user, CACHE_EXPIRE_TIME);
                }
                log.debug("用户列表数据已缓存: roleId={}, count={}", roleId, users.size());
            }
            
            return users != null ? users : List.of();
            
        } catch (Exception e) {
            log.error("查询用户列表失败: roleId={}", roleId, e);
            // 缓存异常时直接查询数据库
            return userRepository.findByRoleId(roleId);
        }
    }

    /**
     * 插入用户（同时更新缓存）
     */
    public int insert(User user) {
        try {
            int result = userRepository.insert(user);
            if (result > 0 && user.getId() != null) {
                // 清除相关缓存
                clearUserCache(user);
                log.debug("用户插入成功，已清除相关缓存: id={}", user.getId());
            }
            return result;
        } catch (Exception e) {
            log.error("插入用户失败", e);
            throw e;
        }
    }

    /**
     * 更新用户（同时更新缓存）
     */
    public int updateById(User user) {
        try {
            int result = userRepository.updateById(user);
            if (result > 0) {
                // 清除相关缓存
                clearUserCache(user);
                log.debug("用户更新成功，已清除相关缓存: id={}", user.getId());
            }
            return result;
        } catch (Exception e) {
            log.error("更新用户失败: id={}", user.getId(), e);
            throw e;
        }
    }

    /**
     * 删除用户（同时清除缓存）
     */
    public int deleteById(Long id) {
        try {
            // 先获取用户信息，用于清除缓存
            User user = userRepository.selectById(id);
            int result = userRepository.deleteById(id);
            if (result > 0) {
                // 清除相关缓存
                if (user != null) {
                    clearUserCache(user);
                }
                log.debug("用户删除成功，已清除相关缓存: id={}", id);
            }
            return result;
        } catch (Exception e) {
            log.error("删除用户失败: id={}", id, e);
            throw e;
        }
    }

    /**
     * 清除用户相关缓存
     */
    private void clearUserCache(User user) {
        try {
            if (user == null) {
                return;
            }
            
            // 清除各种键的缓存
            redisTemplate.delete(USER_KEY_PREFIX + user.getId());
            
            if (user.getUsername() != null) {
                redisTemplate.delete(USER_BY_USERNAME_KEY_PREFIX + user.getUsername());
            }
            if (user.getEmail() != null) {
                redisTemplate.delete(USER_BY_EMAIL_KEY_PREFIX + user.getEmail());
            }
            if (user.getPhone() != null) {
                redisTemplate.delete(USER_BY_PHONE_KEY_PREFIX + user.getPhone());
            }
            if (user.getCompanyId() != null) {
                redisTemplate.delete(USER_BY_COMPANY_KEY_PREFIX + user.getCompanyId());
            }
            if (user.getRoleId() != null) {
                redisTemplate.delete(USER_BY_ROLE_KEY_PREFIX + user.getRoleId());
            }
            
            log.debug("用户缓存已清除: id={}", user.getId());
            
        } catch (Exception e) {
            log.error("清除用户缓存失败: id={}", user.getId(), e);
        }
    }

    /**
     * 手动刷新用户缓存
     */
    public void refreshUserCache(Long userId) {
        try {
            User user = userRepository.selectById(userId);
            if (user != null && user.getId() != null) {
                // 清除旧缓存
                clearUserCache(user);
                // 重新缓存
                String cacheKey = USER_KEY_PREFIX + user.getId();
                redisTemplate.opsForValue().set(cacheKey, user, CACHE_EXPIRE_TIME);
                log.info("用户缓存已刷新: id={}", userId);
            }
        } catch (Exception e) {
            log.error("刷新用户缓存失败: id={}", userId, e);
        }
    }

    /**
     * 清除所有用户缓存
     */
    public void clearAllUserCache() {
        try {
            // 获取所有用户相关的键
            String pattern = USER_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            pattern = USER_BY_USERNAME_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            pattern = USER_BY_EMAIL_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            pattern = USER_BY_PHONE_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            pattern = USER_BY_COMPANY_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            pattern = USER_BY_ROLE_KEY_PREFIX + "*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            
            log.info("所有用户缓存已清除");
        } catch (Exception e) {
            log.error("清除所有用户缓存失败", e);
        }
    }
}
