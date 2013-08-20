package com.redinfo.daq.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.redinfo.daq.R;

public class ConfirmActivity extends Activity implements OnClickListener {
	private EditText et = null;
	private Button btn = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.confirm_dialog);
		et = (EditText) findViewById(R.id.confirm_et);
		btn = (Button) findViewById(R.id.button1);
		btn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.button1:
			if ((et.getText().toString().trim()).equals("123456")) {
				Intent intent = new Intent();
				intent.setClass(ConfirmActivity.this, SystemSetting.class);
				startActivity(intent);
				this.finish();
			} else {
				Toast.makeText(ConfirmActivity.this,
						getString(R.string.judge_code_error),
						Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}
	}

}
