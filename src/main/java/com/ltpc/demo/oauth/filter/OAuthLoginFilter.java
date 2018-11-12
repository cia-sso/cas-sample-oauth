package com.ltpc.demo.oauth.filter;

import com.alibaba.fastjson.JSONObject;
import com.ltpc.demo.Exception.BusinessException;
import com.ltpc.demo.oauth.model.AccessToken;
import com.ltpc.demo.oauth.model.SSOUserInfo;
import com.ltpc.demo.oauth.util.HttpCustomUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: liutong
 * Date: 2018/6/6
 * Time: 下午3:34
 * Description:
 **/
public class OAuthLoginFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthLoginFilter.class);

    public static final String AUTH_CODE = "authCode";

    public static final String ACCESS_CODE = "accessCode";

    public static final String UID = "uid";

    public static final String LOGOUT_URL = "logoutApi";

    public static final String CHARSET_UTF8 = "UTF-8";

    public static final String AUTHORIZE_URL = "/oauth2.0/authorize";

    public static final String TOKEN_URL = "/oauth2.0/accessToken";

    public static final String PROFILE_URL = "/oauth2.0/profile";

    private static String OAUTH_SER_URL;

    private static String CLIENT_ID;

    private static String CLIENT_SECRET;

    private static String SERVER_NAME;

    private static String[] ignor_suffix = {};

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String ignore_res = filterConfig.getInitParameter("ignore_suffix");
        if (StringUtils.isNotBlank(ignore_res)) {
            this.ignor_suffix = filterConfig.getInitParameter("ignore_suffix").split(",");
        }
        String oauthSerUrl = filterConfig.getInitParameter("oauthSerUrl");
        String serverName = filterConfig.getInitParameter("serverName");
        OAUTH_SER_URL = oauthSerUrl;
        SERVER_NAME = serverName;
        CLIENT_ID = filterConfig.getInitParameter("clientId");
        CLIENT_SECRET = filterConfig.getInitParameter("clientSecret");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession();

        String code = request.getParameter("code");
        SSOUserInfo clientUser = null;
        // 若请求code不为空,则直接获取授权
        if (code != null && !"".equals(code)) {
            clientUser = assembleUser(session, code);
        }else {
            auth(req, res);
            return;
        }

        if(clientUser!= null) {
            request.setAttribute("ssoUser",clientUser);
            request.setAttribute("loginName",clientUser.getLoginName());
        }

        // 若无法获取用户信息，则直接登录
        if (StringUtils.isNotBlank(code) && clientUser == null) {
//            auth(req, res);
//            return;
            System.out.println("the client user is null.");
        }
        chain.doFilter(req, response);
    }

    @Override
    public void destroy() {

    }

    /**
     * 封装用户信息
     *
     * @param session
     * @return
     */
    private SSOUserInfo assembleUser(HttpSession session, String code) {
        SSOUserInfo clientUser = null;
        // 根据code获取access_token
        try {
            AccessToken accessToken = getAccessToken(code);
            if (accessToken == null) {
                throw new BusinessException("", "获取accessToken失败");
            }
            clientUser = queryUser(session,accessToken);
        } catch (Exception e) {
            LOGGER.error("登录失败", e);
        }

        return clientUser;
    }

    /**
     * 查询登录信息
     * @param session
     * @param accessToken
     * @return
     */
    private SSOUserInfo queryUser(HttpSession session,AccessToken accessToken){
        SSOUserInfo clientUser = null;
        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", accessToken.getAccessToken());
        Map<String,String> head = new HashMap<>();
        head.put("Content-Type","application/json");
//        String result = HttpCustomUtil.doGetSSL(OAUTH_SER_URL+PROFILE_URL,params,head);
        String result = HttpCustomUtil.doGet(OAUTH_SER_URL+PROFILE_URL,params,head);
        System.out.println("the user profile:"+result);
        if(StringUtils.isBlank(result)){
            return clientUser;
        }
        clientUser = JSONObject.parseObject(result,SSOUserInfo.class);
        return clientUser;
    }

    /**
     * 跳转到认证系统
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void auth(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String uri = request.getRequestURI();
        String queryString =  request.getQueryString() != null ? "?"+request.getQueryString() : "";
        String redirectUri = URLEncoder.encode(SERVER_NAME + uri + queryString,"UTF-8");
        String oauth2_url = OAUTH_SER_URL + AUTHORIZE_URL + "?response_type=code&client_id=" + CLIENT_ID
                + "&redirect_uri="+ redirectUri;
        LOGGER.info("redirect idp:{}",oauth2_url);
        response.sendRedirect(oauth2_url);
        response.flushBuffer();
    }

    /**
     * 获取access_token
     *
     * @return
     * @throws SocketTimeoutException
     * @throws ConnectException
     * @throws Exception
     */
    public AccessToken getAccessToken(String code) throws SocketTimeoutException, ConnectException, Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("?grant_type=authorization_code")
                .append("&client_id=" + URLEncoder.encode(CLIENT_ID, CHARSET_UTF8))
                .append("&client_secret=" + URLEncoder.encode(CLIENT_SECRET, CHARSET_UTF8))
                .append("&code="+code)
                .append("&redirect_uri=" + SERVER_NAME);
        LOGGER.info("get accessToken params:{}",sb);
        String result = HttpCustomUtil.doPostSSL(OAUTH_SER_URL+TOKEN_URL
                +sb.toString(),null, null);
//        String result = HttpCustomUtil.doPostSSL("http://192.168.1.5:8080/uac"+TOKEN_URL
//                +sb.toString(),null, null);
        LOGGER.info("get accessToken result:{}",result);
        JSONObject tokentObj = StringUtils.isBlank(result)?null:JSONObject.parseObject(result);
        if(tokentObj==null || StringUtils.isBlank(tokentObj.getString("access_token"))){
            return null;
        }
        AccessToken token = new AccessToken();
        token.setAccessToken(tokentObj.getString("access_token"));
        token.setRefreshToken(tokentObj.getString("refresh_token"));
        token.setUid(tokentObj.getString("uid"));
        token.setExpiresIn(tokentObj.getLong("expires_in"));
        token.setCreateDate(tokentObj.getLong("createDate"));
        return token;
    }

}
