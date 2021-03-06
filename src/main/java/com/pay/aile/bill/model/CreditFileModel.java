package com.pay.aile.bill.model;

import java.io.Serializable;
import java.util.Date;

public class CreditFileModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String fileName;
    private Date sentDate;
    private String subject;
    private String mailType;
    private Long emailId;
    private Integer processResult;
    private Integer status;
    private Date updateDate;
    private Date createDate;
    private String email;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getEmailId() {
        return emailId;
    }

    public void setEmailId(Long emailId) {
        this.emailId = emailId;
    }

    public Integer getProcessResult() {
        return processResult;
    }

    public void setProcessResult(Integer processResult) {
        this.processResult = processResult;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMailType() {
        return mailType;
    }

    public void setMailType(String mailType) {
        this.mailType = mailType;
    }

    public Date getSentDate() {
        return sentDate;
    }

    public void setSentDate(Date sentDate) {
        this.sentDate = sentDate;
    }

    @Override
    public String toString() {
        return "CreditFileModel [id=" + id + ", fileName=" + fileName
                + ", sentDate=" + sentDate + ", subject=" + subject
                + ", mailType=" + mailType + ", emailId=" + emailId
                + ", processResult=" + processResult + ", status=" + status
                + ", updateDate=" + updateDate + ", createDate=" + createDate
                + ", email=" + email + "]";
    }

}
