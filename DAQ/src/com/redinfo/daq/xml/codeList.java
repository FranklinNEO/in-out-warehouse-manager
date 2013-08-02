package com.redinfo.daq.xml;

public class codeList {
	private String codeVersion;
	private String resCode;
	private int orderID;
	private int num;

	public codeList() {
		super();
	}

	public codeList(String codeVersion, String resCode, int orderID, int num) {
		this.codeVersion = codeVersion;
		this.resCode = resCode;
		this.orderID = orderID;
		this.num = num;
	}

	public String getcodeVersion() {
		return this.codeVersion;
	}

	public void setcodeVersion(String codeVersion) {
		this.codeVersion = codeVersion;
	}

	public String getresCode() {
		return this.resCode;
	}

	public void setresCode(String resCode) {
		this.resCode = resCode;
	}

	public int getorderID() {
		return this.orderID;
	}

	public void setorderID(int orderID) {
		this.orderID = orderID;
	}

	public int getnum() {
		return this.num;
	}

	public void setnum(int num) {
		this.num = num;
	}
}
