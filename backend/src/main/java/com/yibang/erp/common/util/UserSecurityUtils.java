package com.yibang.erp.common.util;

import cn.hutool.json.JSONObject;
import com.yibang.erp.domain.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class UserSecurityUtils {


    public static String getCurrentUsername() {
        UserDetails userDetails=((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        String userName = userDetails.getUsername();
        return userName;
    }

    public static Long getCurrentUserId() {
        try {
            // 从JWT token中解析用户ID
            String username = getCurrentUsername();
            UserService userService = SpringUtils.getBean(UserService.class);

            return  userService.getUserByUsername(username).getId();



            // 这里需要根据实际的用户ID获取方式来实现
            // 暂时返回默认值，实际项目中应该从数据库或JWT中获取
        } catch (Exception e) {
            return null; // 默认用户ID
        }
    }

    public static String getCurrentUserRoles(){
        UserDetails userDetails=((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return new JSONObject(userDetails.getAuthorities().stream().map(x-> x.getAuthority()).toList()).toString();
    }



    public static boolean  isSales(){
        boolean isSales=  SecurityContextHolder.getContext().getAuthentication().getAuthorities().
                stream().anyMatch(x->x.getAuthority().equals("ROLE_SALES"));

        return isSales;
    }

    public static boolean  isSupplier(){
        boolean isSupplier=  SecurityContextHolder.getContext().getAuthentication().getAuthorities().
                stream().anyMatch(x->x.getAuthority().equals("ROLE_SUPPLIER_ADMIN")) || SecurityContextHolder.getContext().getAuthentication().getAuthorities().
                stream().anyMatch(x->x.getAuthority().equals("ROLE_SUPPLIER_OPERATOR"));

        return isSupplier;
    }

    public static boolean isAdmin(){
        boolean isAdmin=  SecurityContextHolder.getContext().getAuthentication().getAuthorities().
                stream().anyMatch(x->x.getAuthority().equals("ROLE_SYSTEM_ADMIN"));
        return isAdmin;
    }




}
