package com.xxl.sso.core.login;

import com.xxl.sso.core.conf.Conf;
import com.xxl.sso.core.store.SsoLoginStore;
import com.xxl.sso.core.store.SsoSessionIdHelper;
import com.xxl.sso.core.user.XxlSsoUser;
import com.xxl.sso.core.util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author xuxueli 2018-04-03
 */
public class SsoWebLoginHelper {

    /**
     * client login
     *
     * 存储用户id到redis中
     * 存储sessionId到前端Cookie中
     *
     * @param response
     * @param sessionId
     * @param ifRemember    true: cookie not expire, false: expire when browser close （server cookie）
     * @param xxlUser
     */
    public static void login(HttpServletResponse response,
                             String sessionId,
                             XxlSsoUser xxlUser,
                             boolean ifRemember) {

        String storeKey = SsoSessionIdHelper.parseStoreKey(sessionId);
        if (storeKey == null) {
            throw new RuntimeException("parseStoreKey Fail, sessionId:" + sessionId);
        }

        SsoLoginStore.put(storeKey, xxlUser);
        CookieUtil.set(response, Conf.SSO_SESSIONID, sessionId, ifRemember);
    }

    /**
     * client logout
     *
     * 删除redis
     * 和删除Cookie
     * 中的值
     *
     * @param request
     * @param response
     */
    public static void logout(HttpServletRequest request,
                              HttpServletResponse response) {

        String cookieSessionId = CookieUtil.getValue(request, Conf.SSO_SESSIONID);
        if (cookieSessionId==null) {
            return;
        }

        String storeKey = SsoSessionIdHelper.parseStoreKey(cookieSessionId);
        if (storeKey != null) {
            SsoLoginStore.remove(storeKey);
        }

        CookieUtil.remove(request, response, Conf.SSO_SESSIONID);
    }



    /**
     * login check
     *
     * @param request
     * @param response
     * @return
     */
    public static XxlSsoUser loginCheck(HttpServletRequest request, HttpServletResponse response){

        String cookieSessionId = CookieUtil.getValue(request, Conf.SSO_SESSIONID);

        /**
         * 如果cookie中有SessionId
         */
        // cookie user
        XxlSsoUser xxlUser = SsoTokenLoginHelper.loginCheck(cookieSessionId);
        if (xxlUser != null) {
            return xxlUser;
        }

        // redirect user

        /**
         * 从request中获取sessionid信息
         */

        /**
         * 删除Cookie中的老sessionId
         */
        // remove old cookie
        SsoWebLoginHelper.removeSessionIdByCookie(request, response);

        /**
         * 获取本地sessionId(自动延期)
         * 并重新设置到到Cookie中
         */
        // set new cookie
        String paramSessionId = request.getParameter(Conf.SSO_SESSIONID);
        xxlUser = SsoTokenLoginHelper.loginCheck(paramSessionId);
        if (xxlUser != null) {
            CookieUtil.set(response, Conf.SSO_SESSIONID, paramSessionId, false);    // expire when browser close （client cookie）
            return xxlUser;
        }

        return null;
    }


    /**
     * client logout, cookie only
     *
     * @param request
     * @param response
     */
    public static void removeSessionIdByCookie(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.remove(request, response, Conf.SSO_SESSIONID);
    }

    /**
     * get sessionid by cookie
     *
     * @param request
     * @return
     */
    public static String getSessionIdByCookie(HttpServletRequest request) {
        String cookieSessionId = CookieUtil.getValue(request, Conf.SSO_SESSIONID);
        return cookieSessionId;
    }


}
