package com.redinfo.daq.app;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import mexxen.mx5010.barcode.BarcodeEvent;
import mexxen.mx5010.barcode.BarcodeListener;
import mexxen.mx5010.barcode.BarcodeManager;

import com.redinfo.daq.R;
import com.redinfo.daq.barcode.CaptureActivity;
import com.redinfo.daq.barcode.Contents.Type;
import com.redinfo.daq.data.CodeDBHelper;
import com.redinfo.daq.ui.CustomDialog;
import com.redinfo.daq.util.SoundPlayer;
import com.redinfo.daq.util.SoundPlayer.State;
import com.redinfo.daq.util.WriteXML;
import com.redinfo.daq.xml.codeList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SubmmitCode extends Activity implements OnClickListener {

	// This intent string contains the captured data as a string
	// (in the case of MSR this data string contains a concatenation of the
	// track data)
	private static final String DATA_STRING_TAG = "com.motorolasolutions.emdk.datawedge.data_string";
	private static String ourIntentAction = "com.redinfo.daq.submmit.RECVR";
	public final static String URL = "/data/data/com.redinfo.daq/databases";
	private static final String BARCODE_SCANER_INTENT = "com.redinfo.daq.barcode.SCAN";
	public final static String DB_FILE_NAME = "info.db";
	SQLiteDatabase db = null;
	public CodeDBHelper m_db = null;

	private static final int BARCODE_INTENT_REQ_CODE = 0x965;

	// private static final int MENU_LOGOUT = Menu.FIRST;
	private com.redinfo.daq.util.SoundPlayer sp;
	private String name;
	// private String abbr;
	private String code;
	private String OrderID;
	private ListView Code_List = null;
	private ArrayList<codeList> BarcodeList = null;
	private codeList cl = null;
	private ArrayList<String> BarcodeResult = null;
	private ArrayList<String> BarcodeDate = null;
	// private ArrayList<String> SimpleBarCodeList = null;
	// private ArrayList<integer> SimpeBarCodeNum = null;
	private BarcodeManager bm = null;
	private BarcodeListener bl = null;
	private CustomAdapter adapter = null;
	private Button Submmit = null;
	private Button Scanner = null;
	private Button Statistics = null;
	private Button CodeBtn = null;
	private Button CodeAddBtn = null;
	private TextView orderText = null;
	private TextView customerText = null;
	private EditText codeEdit = null;
	private String createTime = null;
	private int order_id;
	private int flag = 100;
	public String orderType[] = { "IA", "IC", "ID", "IB", "OA", "OB", "OD",
			"OF", "OE" };
	private SharedPreferences sharedpreferences;
	private String type = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.submmit);
		// ourIntentAction = getString(R.string.intentAction);
		MyApplication.getInstance().addActivity(this);
		sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
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
				flag = Integer.parseInt(content.trim());
			}
			outStream.close();
			inStream.close();
		} catch (IOException ex) {
		}
		m_db = CodeDBHelper.getInstance(SubmmitCode.this);
		File file = new File(URL, DB_FILE_NAME);
		db = SQLiteDatabase.openOrCreateDatabase(file, null);

		Bundle bundle = this.getIntent().getExtras();
		name = bundle.getString("customer_Name");
		// abbr = bundle.getString("customer_Abbr");
		code = bundle.getString("customer_Code");
		OrderID = bundle.getString("order_Id");
		createTime = bundle.getString("order_createTime");

		// continueOrder=Integer.parseInt(bundle.getString("continueOrder"));
		Code_List = (ListView) findViewById(R.id.code_result);
		BarcodeResult = new ArrayList<String>();
		BarcodeDate = new ArrayList<String>();
		BarcodeList = new ArrayList<codeList>();
		sp = new SoundPlayer();
		sp.addSound(SubmmitCode.this, R.raw.alert, State.refreshed);
		sp.addSound(SubmmitCode.this, R.raw.beep, State.pull);
		codeEdit = (EditText) findViewById(R.id.code_add_et);
		// codeEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
		codeEdit.setInputType(InputType.TYPE_DATETIME_VARIATION_NORMAL);
		CodeAddBtn = (Button) findViewById(R.id.code_add_btn);
		CodeAddBtn.setOnClickListener(this);
		Submmit = (Button) findViewById(R.id.submmit);
		// if(continueOrder==1){
		// }
		Submmit.setOnClickListener(this);
		Scanner = (Button) findViewById(R.id.scan_btn);
		Scanner.setOnClickListener(this);
		Statistics = (Button) findViewById(R.id.Product_Statistics);
		if (sharedpreferences.getBoolean("checkproductInfo", true)) {
			Statistics.setEnabled(true);
		} else {
			Statistics.setEnabled(false);
			Statistics.setTextColor(Color.parseColor("#60000000"));
		}
		Statistics.setOnClickListener(this);
		CodeBtn = (Button) findViewById(R.id.Barcode_List);

		CodeBtn.setOnClickListener(this);
		orderText = (TextView) findViewById(R.id.orderText);
		customerText = (TextView) findViewById(R.id.customerText);
		orderText.setText(OrderID);
		customerText.setText(name);
		UpdateCodeList();

		// 扫描器监听
		bm = new BarcodeManager(this);
		bl = new BarcodeListener() {
			// 重写barcodeEvent 方法，获取条码事件
			@Override
			public void barcodeEvent(BarcodeEvent event) {
				// 当条码事件的命令为“SCANNER_READ”时，进行操作
				if (event.getOrder().equals("SCANNER_READ")) {
					// 调用getBarcode()方法读取条码信息
					String barcode = bm.getBarcode();
					codeEdit.setText(barcode);
					if (barcode.length() != 20) {
						sp.play(State.refreshed);
						Toast.makeText(SubmmitCode.this,
								getString(R.string.not_bcm_code),
								Toast.LENGTH_SHORT).show();
					} else {
						switch (Check(barcode)) {
						case 0:
							BarcodeResult.add(barcode);
							SimpleDateFormat df = new SimpleDateFormat(
									"yyyy-MM-dd HH:mm:ss");
							String CodeDate = df.format(new java.util.Date());
							BarcodeDate.add(CodeDate);
							adapter = new CustomAdapter(SubmmitCode.this);
							Code_List.setAdapter(adapter);
							// 插入条码表
							m_db.insert_code(
									CodeDBHelper.ORDER_CODE_TABLE_NAME,
									barcode, barcode.substring(0, 2), barcode
											.substring(2, 7),
									((DaqApplication) getApplication())
											.getActorId(), CodeDate,
									getproductID(barcode), getorderID());
							UpdateCodeList();
							break;
						case 1:
							sp.play(State.refreshed);
							Toast.makeText(SubmmitCode.this,
									getString(R.string.this_barcode_is_exist),
									Toast.LENGTH_SHORT).show();
							break;
						case 2:
							Toast.makeText(SubmmitCode.this,
									getString(R.string.on_data_in_database),
									Toast.LENGTH_SHORT).show();
							break;
						default:
							break;
						}
					}

				}
			}
		};
		bm.addListener(bl);

	}

	// We need to handle any incoming intents, so let override the onNewIntent
	// method
	@Override
	public void onNewIntent(Intent i) {
		// i.addCategory("com.redinfo.daq.sub");
		// i.addCategory("android.intent.category.DEFAULT");
		setIntent(i);
		handleDecodeData(i);
	}

	private void handleDecodeData(Intent i) {
		if (i.getAction().contentEquals(ourIntentAction)) {
			String data = i.getStringExtra(DATA_STRING_TAG);
			// 调用getBarcode()方法读取条码信息
			String barcode = data;
			codeEdit.setText(barcode);
			if (barcode.length() != 20) {
				sp.play(State.refreshed);
				Toast.makeText(SubmmitCode.this,
						getString(R.string.not_bcm_code), Toast.LENGTH_SHORT)
						.show();
			} else {
				switch (Check(barcode)) {
				case 0:
					BarcodeResult.add(barcode);
					SimpleDateFormat df = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					String CodeDate = df.format(new java.util.Date());
					BarcodeDate.add(CodeDate);
					adapter = new CustomAdapter(SubmmitCode.this);
					Code_List.setAdapter(adapter);
					// 插入条码表
					m_db.insert_code(CodeDBHelper.ORDER_CODE_TABLE_NAME,
							barcode, barcode.substring(0, 2),
							barcode.substring(2, 7),
							((DaqApplication) getApplication()).getActorId(),
							CodeDate, getproductID(barcode), getorderID());
					UpdateCodeList();
					break;
				case 1:
					sp.play(State.refreshed);
					Toast.makeText(SubmmitCode.this,
							getString(R.string.this_barcode_is_exist),
							Toast.LENGTH_SHORT).show();
					break;
				case 2:
					sp.play(State.refreshed);
					Toast.makeText(SubmmitCode.this,
							getString(R.string.on_data_in_database),
							Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}

			}

		}
	}

	protected int getorderID() {
		// TODO Auto-generated method stub
		File file = new File(URL, DB_FILE_NAME);
		db = SQLiteDatabase.openOrCreateDatabase(file, null);
		String sql = "SELECT _id FROM order_data WHERE CorpOrderID='" + OrderID
				+ "'" + "AND ToCorpID='" + code + "';";
		Cursor cur = db.rawQuery(sql, null);
		if (cur != null && cur.moveToFirst()) {
			do {
				order_id = cur.getInt(cur.getColumnIndex("_id"));
			} while ((cur.moveToNext()));
		}
		cur.close();

		return order_id;
	}

	protected int getproductID(String barcode) {
		// TODO Auto-generated method stub
		String codeVersion = barcode.substring(0, 2);
		String resCode = barcode.substring(2, 7);
		File file = new File(URL, DB_FILE_NAME);
		db = SQLiteDatabase.openOrCreateDatabase(file, null);
		String sql = "SELECT productID FROM products_data WHERE codeVersion='"
				+ codeVersion + "' AND resCode='" + resCode + "';";
		Cursor cur = db.rawQuery(sql, null);
		if (cur != null && cur.moveToFirst()) {
			int id = cur.getInt(cur.getColumnIndex("productID"));
			return id;
		} else {
			return -1;
		}
	}

	/**
	 * fresh the code list
	 */
	protected void UpdateCodeList() {
		// TODO Auto-generated method stub
		File file = new File(URL, DB_FILE_NAME);
		db = SQLiteDatabase.openOrCreateDatabase(file, null);
		String sql = "SELECT resCode,codeVersion,orderID,count(*) AS num FROM orderCode_data WHERE orderID='"
				+ getorderID() + "' GROUP BY codeVersion,resCode;";
		Cursor cur = db.rawQuery(sql, null);
		if (cur != null && cur.moveToFirst()) {
			BarcodeList = new ArrayList<codeList>();
			do {
				cl = new codeList();
				cl.setcodeVersion(cur.getString(cur
						.getColumnIndex("codeVersion")));
				cl.setresCode(cur.getString(cur.getColumnIndex("resCode")));
				cl.setorderID(cur.getInt(cur.getColumnIndex("orderID")));
				cl.setnum(cur.getInt(cur.getColumnIndex("num")));
				BarcodeList.add(cl);
			} while ((cur.moveToNext()));
		}
		String codesql = "SELECT code20 FROM orderCode_data WHERE orderID='"
				+ getorderID() + "';";
		Cursor cur1 = db.rawQuery(codesql, null);
		if (cur1 != null && cur1.moveToFirst()) {
			BarcodeResult = new ArrayList<String>();
			do {
				String code = cur1.getString(cur1.getColumnIndex("code20"));
				BarcodeResult.add(code);
			} while (cur1.moveToNext());
		}
		adapter = new CustomAdapter(SubmmitCode.this);
		Code_List.setAdapter(adapter);

	}

	// 检测重复条码
	private int Check(String contents) {
		if (this.BarcodeResult.size() != 0) {
			for (int i = 0; i < this.BarcodeResult.size(); i++) {
				if ((this.BarcodeResult.get(i)).equalsIgnoreCase(contents)) {
					return 1;
				}
			}
		}
		String checksql = "SELECT * FROM products_data WHERE codeVersion='"
				+ contents.substring(0, 2) + "' AND resCode='"
				+ contents.substring(2, 7) + "';";
		String sql = "SELECT * FROM orderCode_data WHERE code20='" + contents
				+ "'AND orderID='" + getorderID() + "';";
		if (sharedpreferences.getBoolean("checkproductInfo", true)) {
			Cursor checkcur = db.rawQuery(checksql, null);
			if (checkcur != null && checkcur.moveToFirst()) {
				Cursor cur = db.rawQuery(sql, null);
				if (cur != null && cur.moveToFirst()) {
					cur.close();
					return 1;
				} else {
					cur.close();
					return 0;
				}
			} else {
				return 2;
			}
		} else {
			Cursor cur = db.rawQuery(sql, null);
			if (cur != null && cur.moveToFirst()) {
				cur.close();
				return 1;
			} else {
				cur.close();
				return 0;
			}
		}

	}

	public final class ViewHolder {
		public TextView Barcodetv;
		public TextView BarcodeNum;
		public TextView BarcodeNo;
	}

	// 自定义条码数据容纳器
	private class CustomAdapter extends BaseAdapter {
		private LayoutInflater inflater;

		public CustomAdapter(Context context) {
			this.inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return BarcodeList.size();
		}

		@Override
		public Object getItem(int position) {
			return BarcodeList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.code_item, null);
				holder = new ViewHolder();
				holder.Barcodetv = (TextView) convertView
						.findViewById(R.id.reBarcode);
				holder.BarcodeNum = (TextView) convertView
						.findViewById(R.id.code_num);
				holder.BarcodeNo = (TextView) convertView
						.findViewById(R.id.code_no);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.BarcodeNo.setText((position + 1) + "");
			holder.Barcodetv.setText(BarcodeList.get(position).getcodeVersion()
					+ "," + BarcodeList.get(position).getresCode());
			if (BarcodeList.get(position).getnum() < 10) {
				holder.BarcodeNum.setText(" "
						+ BarcodeList.get(position).getnum() + " ");
			} else {
				holder.BarcodeNum.setText(BarcodeList.get(position).getnum()
						+ "");
			}
			return convertView;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.code_add_btn:

			// 调用getBarcode()方法读取条码信息
			String barcode = codeEdit.getText().toString().trim();
			if (barcode.length() != 20) {
				sp.play(State.refreshed);
				Toast.makeText(SubmmitCode.this,
						getString(R.string.not_bcm_code), Toast.LENGTH_SHORT)
						.show();
			} else {
				switch (Check(barcode)) {
				case 0:
					sp.play(State.pull);
					BarcodeResult.add(barcode);
					SimpleDateFormat df = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					String CodeDate = df.format(new java.util.Date());
					BarcodeDate.add(CodeDate);
					adapter = new CustomAdapter(SubmmitCode.this);
					Code_List.setAdapter(adapter);
					// 插入条码表
					m_db.insert_code(CodeDBHelper.ORDER_CODE_TABLE_NAME,
							barcode, barcode.substring(0, 2),
							barcode.substring(2, 7),
							((DaqApplication) getApplication()).getActorId(),
							CodeDate, getproductID(barcode), getorderID());
					UpdateCodeList();
					break;
				case 1:
					sp.play(State.refreshed);
					Toast.makeText(SubmmitCode.this,
							getString(R.string.this_barcode_is_exist),
							Toast.LENGTH_SHORT).show();
					break;
				case 2:
					Toast.makeText(SubmmitCode.this,
							getString(R.string.on_data_in_database),
							Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}

			}

			break;
		case R.id.submmit:
			this.submmitOrder();
			break;
		case R.id.scan_btn:
			this.openScanner();
			break;
		case R.id.Product_Statistics:
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString("order_Id", OrderID);
			bundle.putString("customer", name);
			intent.putExtras(bundle);
			intent.setClass(SubmmitCode.this, StatisticsActivity.class);
			startActivity(intent);
			break;
		case R.id.Barcode_List:
			if (BarcodeList.size() != 0) {
				Intent codeIntent = new Intent();
				Bundle codebundle = new Bundle();
				codebundle.putString("order_Id", getorderID() + "");
				codebundle.putString("order", OrderID);
				codebundle.putString("customer", name);
				codeIntent.putExtras(codebundle);
				codeIntent.setClass(SubmmitCode.this, CoderListActivity.class);
				bm.removeListener(bl);
				bm.dismiss();
				// codeIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(codeIntent);
				this.finish();
			} else {
				Toast.makeText(SubmmitCode.this,
						getString(R.string.have_not_scan_barcode),
						Toast.LENGTH_SHORT).show();
			}
		default:
			break;
		}
	}

	// 提交单据
	private void submmitOrder() {
		String CorpOrderID = OrderID;
		String ToCorpID = code;
		DaqApplication daq = (DaqApplication) getApplication();
		String Actor = daq.getActorId();
		InsertCode(CorpOrderID, ToCorpID, Actor);

	}

	// 插入数据库
	private void InsertCode(String corpOrderID, String toCorpID, String actor) {
		if (BarcodeList.size() == 0) {
			Toast.makeText(SubmmitCode.this,
					getString(R.string.please_complete_order),
					Toast.LENGTH_SHORT).show();
		} else {

			// 查询客户被选择的次数
			Dialog dialog = null;
			CustomDialog.Builder customBuilder = new CustomDialog.Builder(
					SubmmitCode.this);
			customBuilder
					.setTitle(getString(R.string.submmit_success))
					.setMessage(getString(R.string.message1))
					.setNegativeButton(getString(R.string.exit_the_order),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
									// Intent intent = new Intent();
									// intent.setClass(SubmmitCode.this,
									// Function.class);
									// startActivity(intent);
									bm.removeListener(bl);
									bm.dismiss();
									SubmmitCode.this.finish();
									// MyApplication.getInstance().exit();
									db.close();
								}
							})
					.setPositiveButton(getString(R.string.exprot_order),// 导出XML单据
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									String sql = "SELECT * FROM order_data WHERE CorpOrderID ='"
											+ OrderID
											+ "' ORDER BY flag,datetime(createTime) DESC;";
									Cursor cur = db.rawQuery(sql, null);

									if (cur != null && cur.moveToFirst()) {
										do {

											type = cur.getString(cur
													.getColumnIndex("orderType"));
											Log.d("type",
													cur.getString(cur
															.getColumnIndex("orderType")));
										} while ((cur.moveToNext()));
									}
									if (type.equals("OA")) {
										flag = 4;
									} else if (type.equals("OB")) {
										flag = 5;
									} else if (type.equals("OD")) {
										flag = 6;
									} else if (type.equals("OE")) {
										flag = 8;
									} else if (type.equals("OF")) {
										flag = 7;
									} else if (type.equals("IA")) {
										flag = 0;
									} else if (type.equals("IB")) {
										flag = 3;
									} else if (type.equals("IC")) {
										flag = 1;
									} else if (type.equals("ID")) {
										flag = 2;
									}
									String code_sql = "SELECT * FROM orderCode_data WHERE orderID='"
											+ order_id + "';";
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
									} else {
										cur_code.close();
									}

									try {
										if (Environment
												.getExternalStorageState()
												.equals(Environment.MEDIA_MOUNTED)) {
											if ((BarcodeList.size() == 0)
													|| ((OrderID == "") || (OrderID == null))
													|| ((code == "") || (code == null))) {
												Toast.makeText(
														SubmmitCode.this,
														getString(R.string.please_complete_order),
														Toast.LENGTH_SHORT)
														.show();
											} else {
												File destDir = new File(
														Environment
																.getExternalStorageDirectory(),
														"/RedInfo/OrderList/"
																+ createTime
																		.substring(
																				0,
																				10)
																+ "/");
												if (!destDir.exists()) {
													destDir.mkdirs();
												}

												File file = new File(destDir,
														OrderID + ".xml");// 取得sd卡的目录及要保存的文件名
												FileOutputStream outStream = new FileOutputStream(
														file);
												new WriteXML().saxToXml(
														outStream, Code_Date,
														Code_Result, Actor_ID,
														OrderID, code, flag);
												Toast.makeText(
														SubmmitCode.this,
														getString(R.string.have_export_to_sd),
														Toast.LENGTH_SHORT)
														.show();
												m_db.update_order("order_data",
														orderType[flag],
														OrderID, code, 1,
														createTime);
												dialog.dismiss();
												Intent intent = new Intent();
												intent.setClass(
														SubmmitCode.this,
														ActionActivity.class);
												startActivity(intent);
												bm.removeListener(bl);
												bm.dismiss();
												SubmmitCode.this.finish();
												db.close();
											}

										} else {
											Toast.makeText(
													SubmmitCode.this,
													getString(R.string.sd_card_not_exist),
													Toast.LENGTH_SHORT).show();
										}
									} catch (FileNotFoundException e) {
										e.printStackTrace();
									}

								}
							});
			dialog = customBuilder.create();
			dialog.show();
		}
	}

	// protected void deleteCode() {
	// // TODO Auto-generated method stub
	// // File file = new File(URL, DB_FILE_NAME);
	// // db = SQLiteDatabase.openOrCreateDatabase(file, null);
	// for (int i = 0; i < BarcodeResult.size(); i++) {
	// // String where = "code20=?";
	// // String[] whereValue = { BarcodeResult.get(i) };
	// // db.delete(CodeDBHelper.ORDER_CODE_TABLE_NAME, where, whereValue);
	// m_db.delete_code(CodeDBHelper.ORDER_CODE_TABLE_NAME,
	// BarcodeResult.get(i));
	// }
	// }

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// menu.add(0, MENU_LOGOUT, 0, getString(R.string.logout_message));
	// return super.onCreateOptionsMenu(menu);
	// }

	@Override
	public void onBackPressed() {

		Dialog dialog = null;
		CustomDialog.Builder customBuilder = new CustomDialog.Builder(
				SubmmitCode.this);
		customBuilder
				.setTitle(getString(R.string.alert))
				.setMessage(
						getString(R.string.alert_order_message) + " "
								+ getString(R.string.exit_clean))
				.setNegativeButton(getString(R.string.confirm_to_exit),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								if (BarcodeList.size() == 0) {
									m_db.delete_order(
											CodeDBHelper.ORDER_TABLE_NAME,
											OrderID);
									bm.removeListener(bl);
									bm.dismiss();
									SubmmitCode.this.finish();
									db.close();
								} else {
									// deleteCode();
									// Intent intent = new Intent();
									// intent.setClass(SubmmitCode.this,
									// Function.class);
									// startActivity(intent);
									bm.removeListener(bl);
									bm.dismiss();
									SubmmitCode.this.finish();
									db.close();
								}

							}
						})
				.setPositiveButton(getString(R.string.return_to_order),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
		dialog = customBuilder.create();
		dialog.show();

	}

	// // 菜单
	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// switch (item.getItemId()) {
	// case MENU_LOGOUT:
	// Dialog dialog = null;
	// CustomDialog.Builder customBuilder = new CustomDialog.Builder(
	// SubmmitCode.this);
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
	// FileOutputStream outStream = SubmmitCode.this
	// .openFileOutput("userInfo.txt",
	// Context.MODE_PRIVATE);
	// String content = "" + "," + "" + ","
	// + "";
	// outStream.write(content.getBytes());
	// outStream.close();
	// } catch (IOException ex) {
	//
	// }
	// Intent intent = new Intent(
	// getApplication(),
	// LoginActivity.class);
	// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	// startActivity(intent);
	// bm.removeListener(bl);
	// bm.dismiss();
	//
	// SubmmitCode.this.finish();
	// db.close();
	// dialog.dismiss();
	// }
	// });
	// dialog = customBuilder.create();
	// dialog.show();
	// return true;
	// default:
	// return super.onOptionsItemSelected(item);
	// }
	// }

	private void openScanner() {
		Intent intent = new Intent(BARCODE_SCANER_INTENT);
		intent.setClass(SubmmitCode.this, CaptureActivity.class);
		this.startActivityForResult(intent, BARCODE_INTENT_REQ_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {

		if (requestCode == BARCODE_INTENT_REQ_CODE) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");

				switch (Check(contents)) {
				case 0:

					this.BarcodeResult.add(contents);
					SimpleDateFormat df = new SimpleDateFormat(
							"yyyy-MM-dd   HH:mm:ss");
					String CodeDate = df.format(new java.util.Date());
					this.BarcodeDate.add(CodeDate);
					m_db.insert_code(CodeDBHelper.ORDER_CODE_TABLE_NAME,
							contents, contents.substring(0, 2),
							contents.substring(2, 7),
							((DaqApplication) getApplication()).getActorId(),
							CodeDate, getproductID(contents), getorderID());
					UpdateCodeList();

					break;
				case 1:
					Toast.makeText(this,
							getString(R.string.this_barcode_is_exist),
							Toast.LENGTH_SHORT).show();
					break;
				case 2:
					Toast.makeText(SubmmitCode.this,
							getString(R.string.on_data_in_database),
							Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, getString(R.string.not_find_barcode),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

}
