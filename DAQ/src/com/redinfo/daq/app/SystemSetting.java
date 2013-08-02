package com.redinfo.daq.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.redinfo.daq.R;

public class SystemSetting extends Activity implements OnClickListener {
	private RelativeLayout checkupdate = null;
	private RelativeLayout funcsetting = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.system_activity);
		checkupdate = (RelativeLayout) findViewById(R.id.Loadlayout);
		checkupdate.setOnClickListener(this);
		funcsetting = (RelativeLayout) findViewById(R.id.FuncLayout);
		funcsetting.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.Loadlayout:
			break;
		case R.id.FuncLayout:
			intent.setClass(SystemSetting.this, SettingActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
	}

}
