package com.xxl.sso.core.filter;

import com.xxl.sso.core.conf.Conf;
import com.xxl.sso.core.login.SsoWebLoginHelper;
import com.xxl.sso.core.path.impl.AntPathMatcher;
import com.xxl.sso.core.user.XxlSsoUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * web sso filter
 *
 * @author xuxueli 2018-04-03
 */
public class XxlSsoWebFilter extends HttpServlet implements Filter {
    private static Logger logger = LoggerFactory.getLogger(XxlSsoWebFilter.class);

    private static final AntPathMatcher antPathMatcher = new AntPathMatcher();

    private String ssoServer;
    private String logoutPath;
    private String excludedPaths;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        /**
         * 获取filter的初始化参数
         */
        ssoServer = filterConfig.getInitParameter(Conf.SSO_SERVER);
        logoutPath = filterConfig.getInitParameter(Conf.SSO_LOGOUT_PATH);
        excludedPaths = filterConfig.getInitParameter(Conf.SSO_EXCLUDED_PATHS);

        logger.info("XxlSsoWebFilter init.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // make url
        String servletPath = req.getServletPath();

        /**
         * 跳过非校验地址
         */
        // excluded path check
        if (excludedPaths!=null && excludedPaths.trim().length()>0) {
            for (String excludedPath:excludedPaths.split(",")) {
                String uriPattern = excludedPath.trim();

                // 支持ANT表达式
                if (antPathMatcher.match(uriPattern, servletPath)) {
                    // excluded path, allow
                    chain.doFilter(request, response);
                    return;
                }

            }
        }

        /**
         * 退出地址
          */
        // logout path check
        if (logoutPath!=null
                && logoutPath.trim().length()>0
                && logoutPath.equals(servletPath)) {

            // remove cookie
            SsoWebLoginHelper.removeSessionIdByCookie(req, res);

            // redirect logout
            String logoutPageUrl = ssoServer.concat(Conf.SSO_LOGOUT);
            res.sendRedirect(logoutPageUrl);

            return;
        }

        // valid login user, cookie + redirect
        XxlSsoUser xxlUser = SsoWebLoginHelper.loginCheck(req, res);

        /**
         * 未登录
         */
        // valid login fail
        if (xxlUser == null) {

            /**
             * 如果需要返回的是json格式
             * 则返回json
             */
            String header = req.getHeader("content-type");
            boolean isJson=  header!=null && header.contains("json");
            if (isJson) {

                // json msg
                res.setContentType("application/json;charset=utf-8");
                res.getWriter().println("{\"code\":"+Conf.SSO_LOGIN_FAIL_RESULT.getCode()+", \"msg\":\""+ Conf.SSO_LOGIN_FAIL_RESULT.getMsg() +"\"}");
                return;
            } else {

                /**
                 * 否则
                 * 重定向到登陆页
                 */

                // total link
                String link = req.getRequestURL().toString();

                // redirect logout
                String loginPageUrl = ssoServer.concat(Conf.SSO_LOGIN)
                        + "?" + Conf.REDIRECT_URL + "=" + link;

                res.sendRedirect(loginPageUrl);
                return;
            }

        }

        /**
         * 如果是登陆用户
         * 则向request中写入用户信息
         * 方便后续处理
         */
        // ser sso user
        request.setAttribute(Conf.SSO_USER, xxlUser);


        // already login, allow
        chain.doFilter(request, response);
        return;
    }

}
