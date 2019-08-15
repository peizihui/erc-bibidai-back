package com.theforceprotocol.bbd.web;

import com.theforceprotocol.bbd.domain.entity.User;
import com.theforceprotocol.bbd.service.UserService;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    private final UserService userService;

    public AuthInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Optional.ofNullable(request.getSession(false))
                .map(session -> session.getAttribute("user"))
                .map(User.class::cast)
                .map(user -> userService.findByCountryCodeAndPhone(user.getCountryCode(), user.getPhone()))
                .ifPresent(ContextHolder::set);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        ContextHolder.remove();
    }
}
