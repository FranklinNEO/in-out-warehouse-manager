package com.redinfo.daq.app;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.redinfo.daq.R;
import com.redinfo.daq.data.CodeDBHelper;
import com.redinfo.daq.ui.CorpOrderConfirmEditCancel;
import com.redinfo.daq.ui.CorpOrderEditCancel;
import com.redinfo.daq.util.ExportXML;

public class ActionActivity extends Activity implements OnClickListener {

	private static final int CUSTOMER_INTENT_REQ_CODE = 0x966;
	// private static final int MENU_LOGOUT = Menu.FIRST;
	// private static final int MENU_ORDERLIST = Menu.FIRST + 1;
	private static final int MENU_ORDERLIST = Menu.FIRST;

	public final static String URL = "/data/data/com.redinfo.daq/databases";
	public final static String DB_FILE_NAME = "info.db";
	SQLiteDatabase db = null;

	public CodeDBHelper m_db = null;
	private Button select = null;
	private Button next = null;
	private TextView infoTv = null;
	private SharedPreferences sharedpreferences;
	private String Func;
	private String name = "";
	private String abbr = "";
	private String code = "-1";
	private CorpOrderEditCancel coedit = null;
	private CorpOrderConfirmEditCancel cocedit = null;
	private String co = null;
	private String coc = null;
	private String createTime = null;
	// private String[] FuncTxt = { getString(R.string.produce_ware_house_in),
	// getString(R.string.return_ware_house_in),
	// getString(R.string.purchase_ware_house_in),
	// getString(R.string.allocate_ware_house_in),
	// getString(R.string.sales_ware_house_out),
	// getString(R.string.destory_ware_house_out),
	// getString(R.string.check_ware_house_out),
	// getString(R.string.return_ware_house_out),
	// getString(R.string.allocate_ware_house_out) };
	private Integer[] FuncTxt = { R.string.produce_ware_house_in,
			R.string.purchase_ware_house_in, R.string.allocate_ware_house_in,
			R.string.return_ware_house_in, R.string.sales_ware_house_out,
			R.string.return_ware_house_out, R.string.allocate_ware_house_out,
			R.string.check_ware_house_out, R.string.destory_ware_house_out };
	private int flag = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.action_activity);
		sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
		MyApplication.getInstance().addActivity(this);
		m_db = CodeDBHelper.getInstance(ActionActivity.this);
		infoTv = (TextView) findViewById(R.id.select_info_tv);
		select = (Button) findViewById(R.id.SelectBtn);
		select.setOnClickListener(this);
		next = (Button) findViewById(R.id.next);
		next.setOnClickListener(this);
		TextView title = (TextView) findViewById(R.id.func_title);
		coedit = (CorpOrderEditCancel) findViewById(R.id.CorpOrderEdit);
		cocedit = (CorpOrderConfirmEditCancel) findViewById(R.id.CorpOrderConfirmEdit);
		Drawable drawable = getResources().getDrawable(
				R.drawable.icon_more_default);
		drawable.setBounds(0, 0, drawable.getMinimumWidth(),
				drawable.getMinimumHeight());
		select.setCompoundDrawables(null, null, drawable, null);
		next.setCompoundDrawables(null, null, drawable, null);
		FileInputStream inStream = null;
		ByteArrayOutputStream outStream = null;
		try {
			inStream = this.openFileInput("funcInfo.txt");
			outStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length = -1;
			while ((length = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, length);
			}
			String content = outStream.toString();
			if (content != null) {
				Func = content.trim();
				flag = Integer.parseInt(Func);
			}
			outStream.close();
			inStream.close();
		} catch (IOException ex) {
		}
		// if (flag == 100) {
		// title.setText(getString(R.string.title_bar_name));
		// } else {
		// title.setText(getString(FuncTxt[flag]));
		// }

		switch (flag) {
		case 100:
			title.setText(getString(R.string.title_bar_name));
			break;
		case 0:
			title.setText(getString(FuncTxt[flag]));
			select.setVisibility(View.VISIBLE);
			break;
		case 1:
			title.setText(getString(FuncTxt[flag]));
			select.setVisibility(View.VISIBLE);
			if (sharedpreferences.getBoolean("customersetting", false)) {
				if (sharedpreferences.getString("name", null) != "") {
					name = sharedpreferences.getString("name", null);
					abbr = sharedpreferences.getString("abbr", null);
					code = sharedpreferences.getString("code", "-1");
					select.setText(sharedpreferences.getString("name", null));
					select.setClickable(false);
				}
			}
			break;
		case 2:

			title.setText(getString(FuncTxt[flag]));
			select.setVisibility(View.VISIBLE);
			break;
		case 3:
			title.setText(getString(FuncTxt[flag]));
			select.setVisibility(View.VISIBLE);

			break;
		case 4:
			title.setText(getString(FuncTxt[flag]));
			select.setVisibility(View.VISIBLE);
			break;
		case 5:
			title.setText(getString(FuncTxt[flag]));
			select.setVisibility(View.VISIBLE);
			break;
		case 6:
			title.setText(getString(FuncTxt[flag]));
			select.setVisibility(View.VISIBLE);
			break;
		case 7:

			title.setText(getString(FuncTxt[flag]));
			select.setVisibility(View.GONE);
			infoTv.setVisibility(View.GONE);
			break;
		case 8:

			title.setText(getString(FuncTxt[flag]));
			select.setVisibility(View.GONE);
			infoTv.setVisibility(View.GONE);
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.SelectBtn:
			co = coedit.getString().trim();
			coc = cocedit.getString().trim();
			if ((co.equals("")) || (coc.equals(""))) {
				Toast.makeText(ActionActivity.this,
						getString(R.string.fill_the_order_and_confirm),
						Toast.LENGTH_SHORT).show();
			} else {
				if (co.equals(coc)) {
					Intent intent = new Intent();
					intent.setClass(ActionActivity.this, Loading.class);
					this.startActivityForResult(intent,
							CUSTOMER_INTENT_REQ_CODE);
				} else {
					Toast.makeText(ActionActivity.this,
							getString(R.string.order_different),
							Toast.LENGTH_SHORT).show();
				}
			}

			break;
		case R.id.next:
			co = coedit.getString().trim();
			coc = cocedit.getString().trim();
			if ((co.equals("")) || (coc.equals(""))) {
				Toast.makeText(ActionActivity.this,
						getString(R.string.please_complete_order),
						Toast.LENGTH_SHORT).show();
			} else {
				if (co.equals(coc)) {
					if ((flag == 7) || (flag == 8)) {
						SimpleDateFormat df = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss");
						createTime = df.format(new java.util.Date());

						insertOrder(coedit.getString().trim(), flag, code,
								createTime);
					} else if (code != "-1") {

						SimpleDateFormat df = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss");
						createTime = df.format(new java.util.Date());

						insertOrder(coedit.getString().trim(), flag, code,
								createTime);
					} else {
						Toast.makeText(ActionActivity.this,
								getString(R.string.please_complete_order),
								Toast.LENGTH_SHORT).show();
					}

				} else {
					Toast.makeText(ActionActivity.this,
							getString(R.string.order_different),
							Toast.LENGTH_SHORT).show();
				}
			}
			break;
		default:
			break;
		}
	}

	private void insertOrder(String OrderID, int flag2, String code2,
			String createTime2) {
		// TODO Auto-generated method stub
		File file = new File(URL, DB_FILE_NAME);
		db = SQLiteDatabase.openOrCreateDatabase(file, null);
		String sql = "SELECT * FROM order_data WHERE CorpOrderID='" + OrderID
				+ "';";
		Cursor cur = db.rawQuery(sql, null);
		if (cur != null && cur.moveToFirst()) {
			Toast.makeText(ActionActivity.this,
					getString(R.string.order_exist), Toast.LENGTH_SHORT).show();
		} else {

			m_db.insert_order(CodeDBHelper.ORDER_TABLE_NAME, orderType[flag2],
					OrderID, code2, 0, createTime2);
			Intent submmitIntent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString("order_Id", OrderID);
			bundle.putString("order_createTime", createTime);
			bundle.putString("customer_Name", name);
			bundle.putString("customer_Abbr", abbr);
			bundle.putString("customer_Code", code);
			Log.d("code", code);
			// bundle.putString("continueOrder", "0");
			submmitIntent.putExtras(bundle);
			submmitIntent.setClass(ActionActivity.this, SubmmitCode.class);
			startActivity(submmitIntent);
			ActionActivity.this.finish();
		}
	}

	private String orderType[] = { "IA", "IC", "ID", "IB", "OA", "OB", "OD",
			"OF", "OE" };

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// MenuInflater menuInflater = getMenuInflater();
		// menuInflater.inflate(R.menu.action, menu);
		// MenuItem orderlist = menu.findItem(R.id.action_order_list);
		// MenuItem logout = menu.findItem(R.id.action_logout);
		// MenuItemCompat.setShowAsAction(orderlist,
		// MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
		// MenuItemCompat.setShowAsAction(logout,
		// MenuItemCompat.SHOW_AS_ACTION_WITH_TEXT);
		// F menu.add(0, MENU_LOGOUT, 0, getString(R.string.logout_account));
		menu.add(0, MENU_ORDERLIST, 0, getString(R.string.order_list));
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// case MENU_LOGOUT:
		// Dialog dialog = null;
		// CustomDialog.Builder customBuilder = new CustomDialog.Builder(
		// ActionActivity.this);
		// customBuilder
		// .setTitle(getString(R.string.attention))
		// .setMessage(getString(R.string.confirm_to_logout))
		// .setNegativeButton(getString(R.string.cancel),
		// new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog,
		// int which) {
		// dialog.dismiss();
		// }
		// })
		// .setPositiveButton(getString(R.string.logout_button),
		// new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog,
		// int which) {
		// try {
		// FileOutputStream outStream = ActionActivity.this
		// .openFileOutput("userInfo.txt",
		// Context.MODE_PRIVATE);
		// String content = "" + "," + "" + ","
		// + "";
		// outStream.write(content.getBytes());
		// outStream.close();
		// } catch (IOException ex) {
		// }
		// Intent intent = new Intent(
		// getApplication(),
		// LoginActivity.class);
		// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		// startActivity(intent);
		// ActionActivity.this.finish();
		// dialog.dismiss();
		// }
		// });
		// dialog = customBuilder.create();
		// dialog.show();
		// return true;
		case MENU_ORDERLIST:
			Intent intent = new Intent();
			intent.setClass(ActionActivity.this, ExportXML.class);
			startActivity(intent);
			this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
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
				select.setText(name);

			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, getString(R.string.select_no_customer),
						Toast.LENGTH_SHORT).show();
			}
		}
	}
}
