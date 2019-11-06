package com.ysten.ystenreport.bean;

public class ReportBean {
	private int id;
	private String type;
	private String detail;
	private String filename;

	private boolean isReported;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public boolean isReported() {
		return isReported;
	}
	public void setReported(boolean isReported) {
		this.isReported = isReported;
	}
}
