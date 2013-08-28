package com.redinfo.daq.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.redinfo.daq.R;

public class SystemSetting extends Activity implements OnClickListener {
	private static final int CUSTOMER_INTENT_REQ_CODE = 0x966;
	private RelativeLayout checkproductInfolayout = null;
	private RelativeLayout funcsetting = null;
	private RelativeLayout pisetting = null;
	private RelativeLayout customersetting = null;
	private ToggleButton tgBtn = null;
	private ToggleButton customerBtn = null;
	private TextView customerTv = null;
	private Editor edit = null;
	private SharedPreferences sharedpreferences;
	private String name = "";
	private String abbr = "";
	private String code = "-1";
	private boolean hascustomer = false;

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (!hascustomer) {
			edit.putBoolean("customersetting", false);

		} else {
			edit.putBoolean("customersetting", true);
		}
		edit.commit();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.system_activity);
		sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
		edit = sharedpreferences.edit();
		checkproductInfolayout = (RelativeLayout) findViewById(R.id.checkproductLayout);
		checkproductInfolayout.setOnClickListener(this);
		funcsetting = (RelativeLayout) findViewById(R.id.FuncLayout);
		funcsetting.setOnClickListener(this);
		pisetting = (RelativeLayout) findViewById(R.id.PurchaseWareHouseInlayout);
		pisetting.setOnClickListener(this);
		customersetting = (RelativeLayout) findViewById(R.id.Customerlayout);
		customersetting.setOnClickListener(this);
		customerTv = (TextView) findViewById(R.id.custoemrTv);

		customerBtn = (ToggleButton) findViewById(R.id.customer_setting_Btn);
		customerBtn.setClickable(false);
		if (sharedpreferences.getBoolean("customersetting", false)) {
			customerBtn.setChecked(true);
			customersetting.setVisibility(View.VISIBLE);
			customerTv.setText(sharedpreferences.getString("name", "选择购买入库客户"));
			hascustomer = true;
		} else {
			customerBtn.setChecked(false);
			customersetting.setVisibility(View.GONE);
		}
		tgBtn = (ToggleButton) findViewById(R.id.toggleBtn);
		tgBtn.setClickable(false);
		if (sharedpreferences.getBoolean("checkproductInfo", true)) {
			tgBtn.setChecked(true);
		} else {
			tgBtn.setChecked(false);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (!sharedpreferences.getBoolean("PurchaseWareHouseIn", false)) {
			pisetting.setVisibility(View.GONE);
		} else {
			pisetting.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.checkproductLayout:
			if (tgBtn.isChecked()) {
				tgBtn.setChecked(false);
				edit.putBoolean("checkproductInfo", false);
			} else {
				tgBtn.setChecked(true);
				edit.putBoolean("checkproductInfo", true);
			}
			edit.commit();
			break;
		case R.id.PurchaseWareHouseInlayout:
			if (customerBtn.isChecked()) {
				customerBtn.setChecked(false);
				edit.putBoolean("customersetting", false);
				customersetting.setVisibility(View.GONE);
			} else {
				customerBtn.setChecked(true);
				edit.putBoolean("customersetting", true);
				customersetting.setVisibility(View.VISIBLE);
				customerTv.setText(getString(R.string.select_ic_customer));
			}
			edit.commit();
			break;
		case R.id.FuncLayout:
			intent.setClass(SystemSetting.this, SettingActivity.class);
			startActivity(intent);
			break;
		case R.id.Customerlayout:
			intent.setClass(SystemSetting.this, Loading.class);
			this.startActivityForResult(intent, CUSTOMER_INTENT_REQ_CODE);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (requestCode == CUSTOMER_INTENT_REQ_CODE) {

			if (resultCode == RESULT_OK) {
				name = intent.getStringExtra("data_name");
				abbr = intent.getStringExtra("data_abbr");
				code = intent.getStringExtra("data_code");
				customerTv.setText(name);
				edit.putString("name", name);
				edit.putString("abbr", abbr);
				edit.putString("code", code);
				edit.commit();
				hascustomer = true;
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, getString(R.string.select_no_customer),
						Toast.LENGTH_SHORT).show();
			}
		}
	}
}
