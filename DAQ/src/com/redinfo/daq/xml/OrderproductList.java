package com.redinfo.daq.xml;

public class OrderproductList {
	private String productName;
	private String packUnit;
	private String packageSpec;
	private String spec;
	private String type;
	private String pkgRatio;
	private int num1;
	private int num2;
	private int num3;
	private int sum;

	public OrderproductList() {
		super();
	}

	public OrderproductList(String productName, String packUnit,
			String packageSpec, String spec, String type, String pkgRatio,
			int num1, int num2, int num3, int sum) {
		super();
		this.productName = productName;
		this.packUnit = packUnit;
		this.packageSpec = packageSpec;
		this.spec = spec;
		this.type = type;
		this.pkgRatio = pkgRatio;
		this.num1 = num1;
		this.num2 = num2;
		this.num3 = num3;
		this.sum = sum;
	}

	public String getproductName() {
		return this.productName;
	}

	public void setproductName(String productName) {
		this.productName = productName;
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

	public String getpkgRatio() {
		return this.pkgRatio;
	}

	public void setpkgRatio(String pkgRatio) {
		this.pkgRatio = pkgRatio;
	}

	public int getnum1() {
		return this.num1;
	}

	public void setnum1(int num1) {
		this.num1 = num1;
	}

	public int getnum2() {
		return this.num2;
	}

	public void setnum2(int num2) {
		this.num2 = num2;
	}

	public int getnum3() {
		return this.num3;
	}

	public void setnum3(int num3) {
		this.num3 = num3;
	}

	public int getsum() {
		return this.sum;
	}

	public void setsum(int sum) {
		this.sum = sum;
	}
}
