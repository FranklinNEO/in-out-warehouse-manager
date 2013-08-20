package com.redinfo.daq.app;

public class DateSetting {
	private int startYear = -1;
	private int startMonth = -1;
	private int startDay = -1;
	private int finishYear = -1;
	private int finishMonth = -1;
	private int finishDay = -1;

	public DateSetting() {
		super();
	}

	public DateSetting(int startYear, int startMonth, int startDay,
			int finishYear, int finishMonth, int finishDay) {
		super();
		this.startYear = startYear;
		this.startMonth = startMonth;
		this.startDay = startDay;
		this.finishYear = finishYear;
		this.finishMonth = finishMonth;
		this.finishDay = finishDay;
	}

	public int getstartYear() {
		return this.startYear;
	}

	public void setstartYear(int startYear) {
		this.startYear = startYear;
	}

	public int getstartMonth() {
		return this.startMonth;
	}

	public void setstartMonth(int startMonth) {
		this.startMonth = startMonth;
	}

	public int getstartDay() {
		return this.startDay;
	}

	public void setstartDay(int startDay) {
		this.startDay = startDay;
	}

	public int getfinishYear() {
		return this.finishYear;
	}

	public void setfinishYear(int finishYear) {
		this.finishYear = finishYear;
	}

	public int getfinishMonth() {
		return this.finishMonth;
	}

	public void setfinishMonth(int finishMonth) {
		this.finishMonth = finishMonth;
	}

	public int getfinishDay() {
		return this.finishDay;
	}

	public void setfinishDay(int finishDay) {
		this.finishDay = finishDay;
	}

}
