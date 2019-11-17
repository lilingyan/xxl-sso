package com.xxl.sso.core.store;

import com.xxl.sso.core.user.XxlSsoUser;

/**
 * make client sessionId
 *
 *      client: cookie = [userid#version]
 *      server: redis
 *                  key = [userid]
 *                  value = user (user.version, valid this)
 *
 * //   group         The same group shares the login status, Different groups will not interact
 *
 * sessionId和userId的相互解析
 * 这里是把userId直接拼接存在sessionId中，也可以加密，或者本地映射等
 *
 * @author xuxueli 2018-11-15 15:45:08
 */

public class SsoSessionIdHelper {


    /**
     * make client sessionId
     *
     * sessionId是由用户id和随机生成的版本号合并
     *
     * @param xxlSsoUser
     * @return
     */
    public static String makeSessionId(XxlSsoUser xxlSsoUser){
        String sessionId = xxlSsoUser.getUserid().concat("_").concat(xxlSsoUser.getVersion());
        return sessionId;
    }

    /**
     * parse storeKey from sessionId
     *
     * 从sessionId中解析出用户id
     *
     * @param sessionId
     * @return
     */
    public static String parseStoreKey(String sessionId) {
        if (sessionId!=null && sessionId.indexOf("_")>-1) {
            String[] sessionIdArr = sessionId.split("_");
            if (sessionIdArr.length==2
                    && sessionIdArr[0]!=null
                    && sessionIdArr[0].trim().length()>0) {
                String userId = sessionIdArr[0].trim();
                return userId;
            }
        }
        return null;
    }

    /**
     * parse version from sessionId
     *
     * 解析版本号
     *
     * @param sessionId
     * @return
     */
    public static String parseVersion(String sessionId) {
        if (sessionId!=null && sessionId.indexOf("_")>-1) {
            String[] sessionIdArr = sessionId.split("_");
            if (sessionIdArr.length==2
                    && sessionIdArr[1]!=null
                    && sessionIdArr[1].trim().length()>0) {
                String version = sessionIdArr[1].trim();
                return version;
            }
        }
        return null;
    }

}
