package com.ltpc.demo.oauth.model;

/**
 * Created with IntelliJ IDEA.
 * User: liutong
 * Date: 2018/6/6
 * Time: 下午4:05
 * Description:
 **/
public class SSOUserInfo {
    private Long subCustomerId;
    private String subCustomerame;
    private String loginName;
    private String email;
    private String telephone;
    private Long customerId;
    private Long accountId;
    private Long departmentId;

    public Long getSubCustomerId() {
        return subCustomerId;
    }

    public void setSubCustomerId(Long subCustomerId) {
        this.subCustomerId = subCustomerId;
    }

    public String getSubCustomerame() {
        return subCustomerame;
    }

    public void setSubCustomerame(String subCustomerame) {
        this.subCustomerame = subCustomerame;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
}
