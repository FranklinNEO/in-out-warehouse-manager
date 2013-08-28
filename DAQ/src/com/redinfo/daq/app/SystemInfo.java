package com.redinfo.daq.app;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.redinfo.daq.R;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class SystemInfo extends Activity {

	private String verson;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		String pkName = this.getPackageName();
		try {
			verson = this.getPackageManager().getPackageInfo(pkName, 0).versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TextView link = (TextView) findViewById(R.id.linkTv);
		link.setText(Html
				.fromHtml("<a href=\"http://www.redinfo.com\">http://www.redinfo.com</a>"));
		link.setMovementMethod(LinkMovementMethod.getInstance());
		TextView tvVerson = (TextView) findViewById(R.id.vesion);
		tvVerson.setText("V" + verson.substring(0,3));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		int year = Integer.parseInt(sdf.format(new Date()));
		TextView copyTv = (TextView) findViewById(R.id.copyright);
		copyTv.setText("Copyright © " + (year - 1) + "-" + year + " RedInfo.");

	}

}
