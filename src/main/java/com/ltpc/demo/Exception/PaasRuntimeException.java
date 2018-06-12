package com.ltpc.demo.Exception;

/**
 * Created with IntelliJ IDEA.
 * User: liutong
 * Date: 2018/6/6
 * Time: 下午4:32
 * Description:
 **/
public class PaasRuntimeException extends RuntimeException{
    private static final long serialVersionUID = -8886495803406807620L;
    private String errCode;
    private String errDetail;

    public PaasRuntimeException(String errDetail) {
        super(errDetail);
        this.errDetail = errDetail;
    }

    public PaasRuntimeException(String errCode, String errDetail) {
        super(errCode + ":" + errDetail);
        this.errCode = errCode;
        this.errDetail = errDetail;
    }

    public PaasRuntimeException(String errCode, Exception ex) {
        super(errCode, ex);
        this.errCode = errCode;
    }

    public PaasRuntimeException(String errCode, String errDetail, Exception ex) {
        super(errCode + ":" + errDetail, ex);
        this.errCode = errCode;
        this.errDetail = errDetail;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrDetail() {
        return errDetail;
    }

    public void setErrDetail(String errDetail) {
        this.errDetail = errDetail;
    }
}
