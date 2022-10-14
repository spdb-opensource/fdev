package com.fdev.docmanage.entity;

public class PreviewParameter {
    private String extuserid;
    
    private String expiration;

    public String getAccount_sync() {
		return account_sync;
	}

	public void setAccount_sync(String account_sync) {
		this.account_sync = account_sync;
	}

	public String getWrite() {
		return write;
	}

	public void setWrite(String write) {
		this.write = write;
	}

	private Boolean copyable;

    private Boolean printable;

    private String watermarkText;

    private String watermarkImageUrl;
    
    private String account_sync;
    
    private String write;

    
    public String getExtuserid() {
		return extuserid;
	}

	public void setExtuserid(String extuserid) {
		this.extuserid = extuserid;
	}

	public String getExpiration() {
        return expiration;
    }

    public void setExpiration(String expiration) {
        this.expiration = expiration;
    }

    public Boolean getCopyable() {
        return copyable;
    }

    public void setCopyable(Boolean copyable) {
        this.copyable = copyable;
    }

    public Boolean getPrintable() {
        return printable;
    }

    public void setPrintable(Boolean printable) {
        this.printable = printable;
    }

    public String getWatermarkText() {
        return watermarkText;
    }

    public void setWatermarkText(String watermarkText) {
        this.watermarkText = watermarkText;
    }

    public String getWatermarkImageUrl() {
        return watermarkImageUrl;
    }

    public void setWatermarkImageUrl(String watermarkImageUrl) {
        this.watermarkImageUrl = watermarkImageUrl;
    }
}
