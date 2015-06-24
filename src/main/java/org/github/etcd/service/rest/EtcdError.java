package org.github.etcd.service.rest;

public class EtcdError {
    private Integer errorCode;
    private String message;
    private String cause;
    private Long index;
    public Integer getErrorCode() {
        return errorCode;
    }
    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getCause() {
        return cause;
    }
    public void setCause(String cause) {
        this.cause = cause;
    }
    public Long getIndex() {
        return index;
    }
    public void setIndex(Long index) {
        this.index = index;
    }
    @Override
    public String toString() {
        return "EtcdError [errorCode=" + errorCode + ", message=" + message
                + ", cause=" + cause + ", index=" + index + "]";
    }
}
