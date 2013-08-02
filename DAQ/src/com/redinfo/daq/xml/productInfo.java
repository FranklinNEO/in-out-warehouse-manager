package com.redinfo.daq.xml;

public class productInfo {

	private String comment;
	private String productName;
	private String productCode;
	private String physicDetailType;
	private String packUnit;
	private String packageSpec;
	private String spec;
	private String type;
	private String authorizedNo;
	private String typeNo;
	private String pkgRatio;
	private String codeLevel;
	private String codeVersion;
	private String resCode;

	public productInfo() {
		super();
	}

	public productInfo(String comment, String productName, String productCode,
			String physicDetailType, String packUnit, String packageSpec,
			String spec, String type, String authorizedNo, String typeNo,
			String pkgRatio, String codeLevel, String codeVersion,
			String resCode) {
		super();
		this.comment = comment;
		this.productName = productName;
		this.productCode = productCode;
		this.physicDetailType = physicDetailType;
		this.packUnit = packUnit;
		this.packageSpec = packageSpec;
		this.spec = spec;
		this.type = type;
		this.authorizedNo = authorizedNo;
		this.typeNo = typeNo;
		this.pkgRatio = pkgRatio;
		this.codeLevel = codeLevel;
		this.codeVersion = codeVersion;
		this.resCode = resCode;
	}

	public String getcomment() {
		return this.comment;
	}

	public void setcomment(String comment) {
		this.comment = comment;
	}

	public String getproductName() {
		return this.productName;
	}

	public void setproductName(String productName) {
		this.productName = productName;
	}

	public String getproductCode() {
		return this.productCode;
	}

	public void setproductCode(String productCode) {
		this.productCode = productCode;
	}

	public String getphysicDetailType() {
		return this.physicDetailType;
	}

	public void setphysicDetailType(String physicDetailType) {
		this.physicDetailType = physicDetailType;
	}

	public String getpackUnit() {
		return this.packUnit;
	}

	public void setpackUnit(String packUnit) {
		this.packUnit = packUnit;
	}

	public String getpackageSpec() {
		return this.packageSpec;
	}

	public void setpackageSpec(String packageSpec) {
		this.packageSpec = packageSpec;
	}

	public String getspec() {
		return this.spec;
	}

	public void setspec(String spec) {
		this.spec = spec;
	}

	public String gettype() {
		return this.type;
	}

	public void settype(String type) {
		this.type = type;
	}

	public String getauthorizedNo() {
		return this.authorizedNo;
	}

	public void setauthorizedNo(String authorizedNo) {
		this.authorizedNo = authorizedNo;
	}

	public String gettypeNo() {
		return this.typeNo;
	}

	public void settypeNo(String typeNo) {
		this.typeNo = typeNo;
	}

	public String getpkgRatio() {
		return this.pkgRatio;
	}

	public void setpkgRatio(String pkgRatio) {
		this.pkgRatio = pkgRatio;
	}

	public String getcodeLevel() {
		return this.codeLevel;
	}

	public void setcodeLevel(String codeLevel) {
		this.codeLevel = codeLevel;
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

}
