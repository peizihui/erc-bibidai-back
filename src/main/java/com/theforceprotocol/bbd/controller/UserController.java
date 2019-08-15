package com.theforceprotocol.bbd.controller;


import com.alibaba.fastjson.JSONObject;
import com.theforceprotocol.bbd.domain.dto.LoginOrRegisterBody;
import com.theforceprotocol.bbd.domain.entity.User;
import com.theforceprotocol.bbd.service.UserService;
import com.theforceprotocol.bbd.util.JsonBuilder;
import com.theforceprotocol.bbd.web.ContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public JSONObject loginOrRegister(@Valid @RequestBody LoginOrRegisterBody body, HttpSession httpSession) {
        User user = userService.loginOrRegister(body);
        httpSession.setAttribute("user", user);
        return JsonBuilder.successBuilder(user);
    }

    @PostMapping("/logout")
    public JSONObject logout(HttpSession httpSession) {
        httpSession.invalidate();
        return JsonBuilder.successBuilder(null);
    }

    @GetMapping("/summary")
    public JSONObject findSummary(String account) {
        return JsonBuilder.successBuilder(userService.findSummary(ContextHolder.requiredCurrentUser(), account));
    }

}
