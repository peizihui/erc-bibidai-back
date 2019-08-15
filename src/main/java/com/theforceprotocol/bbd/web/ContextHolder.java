package com.theforceprotocol.bbd.web;

import com.theforceprotocol.bbd.domain.entity.User;
import com.theforceprotocol.bbd.exception.BusinessException;
import com.theforceprotocol.bbd.util.Errors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ContextHolder {
    private static final ThreadLocal<User> CONTEXT = new ThreadLocal<>();

    static void set(User user) {
        CONTEXT.set(user);
    }

    public static User get() {
        return CONTEXT.get();
    }

    public static User currentUser() {
        return get();
    }

    public static User requiredCurrentUser() {
        User user = get();
        if (user != null) {
            return user;
        }
        throw new BusinessException(Errors.LOGIN_REQUIRED);
    }

    static void remove() {
        CONTEXT.remove();
    }
}
