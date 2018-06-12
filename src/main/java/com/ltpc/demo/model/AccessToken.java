package com.ltpc.demo.model;

/**
 * Created with IntelliJ IDEA.
 * User: liutong
 * Date: 2018/6/6
 * Time: 下午4:06
 * Description:
 **/
public class AccessToken {
    private String accessToken;

    private String refreshToken;

    private String uid;

    private Long expiresIn;

    private Long createDate;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public Long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Long createDate) {
        this.createDate = createDate;
    }
}
