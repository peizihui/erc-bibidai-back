package com.theforceprotocol.bbd.service;

import com.theforceprotocol.bbd.domain.dto.LoginOrRegisterBody;
import com.theforceprotocol.bbd.domain.dto.Summary;
import com.theforceprotocol.bbd.domain.entity.User;

public interface UserService {
    User findByCountryCodeAndPhone(Integer countryCode, String phone);

    User loginOrRegister(LoginOrRegisterBody body);

    Summary findSummary(User user, String account);
}
