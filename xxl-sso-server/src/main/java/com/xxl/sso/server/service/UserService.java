package com.xxl.sso.server.service;

import com.xxl.sso.server.core.model.UserInfo;
import com.xxl.sso.server.core.result.ReturnT;

/**
 * 自己定义如何 获取用户
 */
public interface UserService {

    ReturnT<UserInfo> findUser(String username, String password);

}
