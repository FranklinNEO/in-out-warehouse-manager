package com.redinfo.daq.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.redinfo.daq.R;
import com.redinfo.daq.data.CodeDBHelper;
import com.redinfo.daq.util.ExportXML;
import com.redinfo.daq.util.WriteXML;

public class StateChangeActivity extends Activity implements OnClickListener {
	private String name;
	private String abbr;
	private String code;
	private String OrderID;
	private String order;
	private String createTime = null;
	private String codeNum = null;
	private boolean flag = false;
	private int Func = 100;
	private Button export_btn;
	private Button change_state_btn;
	private Button confirm_btn;
	private Button ViewOrder_btn;
	private TextView confirm_tv;
	private TextView title_tv;
	private TextView message_tv;
	private EditText confirm_et;
	private LinearLayout ll1;
	private LinearLayout ll2;
	private LinearLayout ll3;
	private LinearLayout ll4;
	public final static String URL = "/data/data/com.redinfo.daq/databases";
	public final static String DB_FILE_NAME = "info.db";
	SQLiteDatabase db = null;
	public CodeDBHelper m_db = null;
	public String orderType[] = { "IA", "IC", "ID", "IB", "OA", "OB", "OD",
			"OF", "OE" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.export_xml_dialog);
		m_db = CodeDBHelper.getInstance(StateChangeActivity.this);
		Bundle bundle = this.getIntent().getExtras();
		name = bundle.getString("customer_Name");
		abbr = bundle.getString("customer_Abbr");
		code = bundle.getString("customer_Code");
		OrderID = bundle.getString("order_Id");
		createTime = bundle.getString("order_createTime");
		codeNum = bundle.getString("codeNum");
		order = bundle.getString("order");
		Func = Integer.parseInt(bundle.getString("func"));
		if (Integer.parseInt(bundle.getString("flag")) == 1) {
			flag = true;
		} else if (Integer.parseInt(bundle.getString("flag")) == 0) {
			flag = false;
		}
		export_btn = (Button) findViewById(R.id.button1);
		change_state_btn = (Button) findViewById(R.id.button2);
		confirm_btn = (Button) findViewById(R.id.button3);
		ViewOrder_btn = (Button) findViewById(R.id.button4);
		title_tv = (TextView) findViewById(R.id.title);
		title_tv.setText(getString(R.string.danjuhao) + OrderID);
		message_tv = (TextView) findViewById(R.id.message);
		message_tv.setText(name + "\n" + createTime + "\n"
				+ getString(R.string.tiaomazongliang) + codeNum);
		confirm_tv = (TextView) findViewById(R.id.judge_tv);
		confirm_et = (EditText) findViewById(R.id.judge_et);
		confirm_et.setInputType(InputType.TYPE_DATETIME_VARIATION_NORMAL);
		ll1 = (LinearLayout) findViewById(R.id.ll1);
		ll2 = (LinearLayout) findViewById(R.id.ll2);
		ll3 = (LinearLayout) findViewById(R.id.ll3);
		ll4 = (LinearLayout) findViewById(R.id.ll4);
		ll1.setVisibility(View.GONE);
		ll2.setVisibility(View.GONE);
		ll3.setVisibility(View.GONE);
		if (flag) {
			ll4.setVisibility(View.VISIBLE);
		} else {
			ll4.setVisibility(View.GONE);
		}
		export_btn.setOnClickListener(this);
		change_state_btn.setOnClickListener(this);
		confirm_btn.setOnClickListener(this);
		ViewOrder_btn.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.button1:
			File file = new File(URL, DB_FILE_NAME);
			db = SQLiteDatabase
					.openOrCreateDatabase(file,
							null);
			String code_sql = "SELECT * FROM orderCode_data WHERE orderID='"
					+ order + "';";
			Log.d("code_sql", code_sql);
			Cursor cur_code = db.rawQuery(code_sql,
					null);
			ArrayList<String> Code_Result = new ArrayList<String>();
			ArrayList<String> Code_Date = new ArrayList<String>();
			ArrayList<String> Actor_ID = new ArrayList<String>();
			if (cur_code != null
					&& cur_code.moveToFirst()) {
				do {
					Code_Result.add(cur_code.getString(cur_code
							.getColumnIndex("code20")));
					Code_Date.add(cur_code.getString(cur_code
							.getColumnIndex("actDate")));
					Actor_ID.add(cur_code.getString(cur_code
							.getColumnIndex("actor")));
				} while ((cur_code.moveToNext()));
				cur_code.close();
				db.close();
			} else {
				cur_code.close();
				db.close();
			}

			try {
				if (Environment
						.getExternalStorageState()
						.equals(Environment.MEDIA_MOUNTED)) {
						File destDir = new File(
								Environment
										.getExternalStorageDirectory(),
								"/RedInfo/OrderList/");
						if (!destDir.exists()) {
							destDir.mkdirs();
						}
						File ExportFILE = new File(
								destDir, OrderID
										+ ".xml");// 取得sd卡的目录及要保存的文件名
						FileOutputStream outStream = new FileOutputStream(
								ExportFILE);
						new WriteXML().saxToXml(
								outStream,
								Code_Date,
								Code_Result,
								Actor_ID,
								OrderID,
								code,
								Func);
						Toast.makeText(
								StateChangeActivity.this,
								getString(R.string.have_export_to_sd),
								Toast.LENGTH_SHORT)
								.show();
						m_db.update_order(
								"order_data",
								orderType[Func],
								OrderID,
							code,
								1,
								createTime);

				} else {
					Toast.makeText(
							StateChangeActivity.this,
							getString(R.string.sd_card_not_exist),
							Toast.LENGTH_SHORT)
							.show();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		
			
			
			
			
			
			
//			File file = new File(URL, DB_FILE_NAME);
//			db = SQLiteDatabase.openOrCreateDatabase(file, null);
//			String code_sql = "SELECT * FROM orderCode_data WHERE orderID='"
//					+ order + "';";
//			Cursor cur_code = db.rawQuery(code_sql, null);
//			ArrayList<String> Code_Result = new ArrayList<String>();
//			ArrayList<String> Code_Date = new ArrayList<String>();
//			ArrayList<String> Actor_ID = new ArrayList<String>();
//			if (cur_code != null && cur_code.moveToFirst()) {
//				do {
//					Code_Result.add(cur_code.getString(cur_code
//							.getColumnIndex("code20")));
//					Code_Date.add(cur_code.getString(cur_code
//							.getColumnIndex("actDate")));
//					Actor_ID.add(cur_code.getString(cur_code
//							.getColumnIndex("actor")));
//				} while ((cur_code.moveToNext()));
//				cur_code.close();
//				db.close();
//			} else {
//				cur_code.close();
//				db.close();
//			}
//
//			try {
//				if (Environment.getExternalStorageState().equals(
//						Environment.MEDIA_MOUNTED)) {
//					File destDir = new File(
//							Environment.getExternalStorageDirectory(),
//							"/RedInfo/OrderList/");
//					if (!destDir.exists()) {
//						destDir.mkdirs();
//					}
//					File ExportFILE = new File(destDir, OrderID + ".xml");// 取得sd卡的目录及要保存的文件名
//					FileOutputStream outStream = new FileOutputStream(
//							ExportFILE);
//					new WriteXML().saxToXml(outStream, Code_Date, Code_Result,
//							Actor_ID, OrderID, code, Func);
//					Toast.makeText(StateChangeActivity.this,
//							getString(R.string.have_export_to_sd),
//							Toast.LENGTH_SHORT).show();
//					m_db.update_order("order_data", orderType[Func], OrderID,
//							code, 1, createTime);
//
//				} else {
//					Toast.makeText(StateChangeActivity.this,
//							getString(R.string.sd_card_not_exist),
//							Toast.LENGTH_SHORT).show();
//				}
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
			StateChangeActivity.this.finish();
			break;
		case R.id.button2:
			Random rand = new Random();
			int judge = rand.nextInt(8999) + 1000;
			confirm_tv.setText(judge + "");
			ll1.setVisibility(View.VISIBLE);
			ll2.setVisibility(View.VISIBLE);
			ll3.setVisibility(View.VISIBLE);
			break;
		case R.id.button3:
			m_db.update_order("order_data", orderType[Func], OrderID, code, 0,
					createTime);
			if (confirm_et.getText().toString().trim().equals("")) {
				Toast.makeText(StateChangeActivity.this,
						getString(R.string.enter_judge_code),
						Toast.LENGTH_SHORT).show();
			} else if (confirm_et.getText().toString().trim()
					.equals(confirm_tv.getText().toString().trim())) {
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString("order_Id", OrderID);
				bundle.putString("order_createTime", createTime);
				bundle.putString("customer_Name", name);
				bundle.putString("customer_Abbr", abbr);
				bundle.putString("customer_Code", code);
				intent.putExtras(bundle);
				intent.setClass(StateChangeActivity.this, SubmmitCode.class);
				startActivity(intent);
				ExportXML.instance.finish();
				StateChangeActivity.this.finish();
			} else {
				Toast.makeText(StateChangeActivity.this,
						getString(R.string.judge_num_error), Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.button4:
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString("order_Id", OrderID);
			bundle.putString("customer", name);
			intent.putExtras(bundle);
			intent.setClass(StateChangeActivity.this, StatisticsActivity.class);
			StateChangeActivity.this.finish();
			startActivity(intent);
			ExportXML.instance.finish();
			break;
		default:
			break;
		}
	}

}
