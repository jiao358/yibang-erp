package com.yibang.erp.common.util;

import cn.hutool.json.JSONObject;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class UserSecurityUtils {

    public static String getCurrentUsername() {
        UserDetails userDetails=((UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        String userName = userDetails.getUsername();
        return userName;
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
