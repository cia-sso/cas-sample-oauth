package com.ltpc.demo.Exception;

/**
 * Created with IntelliJ IDEA.
 * User: liutong
 * Date: 2018/6/6
 * Time: 下午4:33
 * Description:
 **/
public class BusinessException extends GenericException {
    private static final long serialVersionUID = 1L;

    public BusinessException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public BusinessException(String message) {
        super(message);
        this.errorMessage = message;
    }

    public BusinessException(Exception oriEx) {
        super(oriEx);
    }


    public BusinessException(Throwable oriEx) {
        super(oriEx);
    }

    public BusinessException(String message, Exception oriEx) {
        super(message, oriEx);
        this.errorMessage = message;
    }
    public BusinessException(String message, Throwable oriEx) {
        super(message, oriEx);
        this.errorMessage = message;
    }
}
