package com.redinfo.daq.app;

import android.app.Application;
import android.content.res.Configuration;

public class DaqApplication extends Application {
	private String ActorID = "";
	private String createTime = "";

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	public String getActorId() {
		return ActorID;
	}

	public void setActorId(String uid) {
		this.ActorID = uid;
	}

	public String getcreateTime() {
		return createTime;
	}

	public void setcreateTime(String createTime) {
		this.createTime = createTime;
	}
}
