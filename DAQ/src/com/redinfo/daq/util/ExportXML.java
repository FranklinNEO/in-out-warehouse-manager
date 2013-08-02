package com.redinfo.daq.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.redinfo.daq.R;
import com.redinfo.daq.app.DaqApplication;
import com.redinfo.daq.app.StatisticsActivity;
import com.redinfo.daq.app.SubmmitCode;
import com.redinfo.daq.data.CodeDBHelper;
import com.redinfo.daq.ui.CustomDialog;

public class ExportXML extends Activity implements OnItemClickListener {
	public final static String URL = "/data/data/com.redinfo.daq/databases";
	public final static String DB_FILE_NAME = "info.db";
	SQLiteDatabase db = null;
	public CodeDBHelper m_db = null;

	private ArrayList<HashMap<String, String>> Order = new ArrayList<HashMap<String, String>>();
	private ArrayList<Integer> flag = new ArrayList<Integer>();
	private CustomAdapter adapter;
	private ListView List;
	public Dialog loadingdialog = null;
	private int pos;
	private int Func = 100;
	private String sql = null;
	String createTime = null;
	String ToCorpID = null;
	String customerName = null;
	String customerInitial = null;
	public String orderType[] = { "IA", "IB", "IC", "ID", "OA", "OE", "OF",
			"OC", "OD" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.export_xml);

		m_db = CodeDBHelper.getInstance(ExportXML.this);

		List = (ListView) findViewById(R.id.orderDate);
		List.setOnItemClickListener(this);
		loadingdialog = new Dialog(ExportXML.this, R.style.mmdialog);
		loadingdialog.setContentView(R.layout.loading_dialog);

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
				Func = Integer.parseInt(content.trim());
			}
			outStream.close();
			inStream.close();
		} catch (IOException ex) {
		}

		new AsyncTask<Integer, Integer, String[]>() {

			protected void onPreExecute() {
				loadingdialog.show();
				super.onPreExecute();
			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
			}

			protected String[] doInBackground(Integer... params) {
				File file = new File(URL, DB_FILE_NAME);
				db = SQLiteDatabase.openOrCreateDatabase(file, null);
				// switch (Func) {
				// case 100:
				// break;
				// case 4:
				// sql = "SELECT * FROM order_data WHERE orderType='OA';";
				// Cursor cur = db.rawQuery(sql, null);
				// Order = new ArrayList<HashMap<String, String>>();
				// if (cur != null && cur.moveToFirst()) {
				// do {
				// HashMap<String, String> map = new HashMap<String, String>();
				// map.put("oID", cur.getString(cur
				// .getColumnIndex("CorpOrderID")));
				// map.put("oCorpID", cur.getString(cur
				// .getColumnIndex("ToCorpID")));
				// map.put("oTime", cur.getString(cur
				// .getColumnIndex("createTime")));
				// flag.add(cur.getInt(cur.getColumnIndex("flag")));
				// Order.add(map);
				// } while ((cur.moveToNext()));
				// cur.close();
				// db.close();
				// } else {
				// cur.close();
				// db.close();
				// }
				// break;
				// default:
				// break;
				// }
				if (Func != 100) {
					sql = "SELECT * FROM order_data WHERE orderType='"
							+ orderType[Func] + "';";
					Cursor cur = db.rawQuery(sql, null);
					Order = new ArrayList<HashMap<String, String>>();
					if (cur != null && cur.moveToFirst()) {
						do {
							HashMap<String, String> map = new HashMap<String, String>();
							map.put("oID", cur.getString(cur
									.getColumnIndex("CorpOrderID")));
							map.put("oCorpID", cur.getString(cur
									.getColumnIndex("ToCorpID")));
							map.put("oTime", cur.getString(cur
									.getColumnIndex("createTime")));
							flag.add(cur.getInt(cur.getColumnIndex("flag")));
							Order.add(map);
						} while ((cur.moveToNext()));
						cur.close();
						db.close();
					} else {
						cur.close();
						db.close();
					}
				}
				return null;
			}

			protected void onPostExecute(String[] result) {
				loadingdialog.dismiss();
				adapter = new CustomAdapter(ExportXML.this);
				List.setAdapter(adapter);
				super.onPostExecute(result);
			}
		}.execute(0);

	}

	public final class ViewHolder {
		public TextView OrderID;
		public TextView OrderTime;
		public TextView OrderState;
		public TextView OrderIDTitle;
		public TextView OrderTimeTitle;
		public TextView OrderStateTitle;
		public LinearLayout TitelLayout;
		public View divider;
	}

	// 自定义条码数据容纳器
	private class CustomAdapter extends BaseAdapter {
		private LayoutInflater inflater;

		public CustomAdapter(Context context) {
			this.inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return Order.size();
		}

		@Override
		public Object getItem(int position) {
			return Order.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.export_item, null);
				holder = new ViewHolder();
				holder.OrderID = (TextView) convertView
						.findViewById(R.id.orderId);
				holder.OrderTime = (TextView) convertView
						.findViewById(R.id.orderTime);
				holder.OrderState = (TextView) convertView
						.findViewById(R.id.ExportState);
				holder.OrderIDTitle = (TextView) convertView
						.findViewById(R.id.orderIdTitle);
				holder.OrderTimeTitle = (TextView) convertView
						.findViewById(R.id.orderTimeTitle);
				holder.OrderStateTitle = (TextView) convertView
						.findViewById(R.id.ExportStateTitle);
				holder.TitelLayout = (LinearLayout) convertView
						.findViewById(R.id.export_title);
				holder.divider = convertView
						.findViewById(R.id.horizontal_divider);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.OrderID.setText(Order.get(position).get("oID"));
			holder.OrderTime.setText(Order.get(position).get("oTime"));
			if (position == 0) {
				holder.OrderIDTitle.setVisibility(View.VISIBLE);
				holder.OrderTimeTitle.setVisibility(View.VISIBLE);
				holder.OrderStateTitle.setVisibility(View.VISIBLE);
				holder.TitelLayout.setVisibility(View.VISIBLE);
				holder.divider.setVisibility(View.VISIBLE);
			} else {
				holder.OrderIDTitle.setVisibility(View.GONE);
				holder.OrderTimeTitle.setVisibility(View.GONE);
				holder.OrderStateTitle.setVisibility(View.GONE);
				holder.TitelLayout.setVisibility(View.GONE);
				holder.divider.setVisibility(View.GONE);
			}
			if ((flag.get(position)) == 1) {
				holder.OrderState.setText(getString(R.string.have_export_text));
			} else {
				holder.OrderState
						.setText(getString(R.string.have_not_export_text));
			}
			return convertView;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		pos = position;
		Dialog dialog = null;
		CustomDialog.Builder customBuilder = new CustomDialog.Builder(
				ExportXML.this);
		if (flag.get(position) != 1) {
			customBuilder
					.setTitle(getString(R.string.export_choice))
					.setMessage(getString(R.string.please_select_func_need))
					.setNeutralButton(getString(R.string.continue_scan),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {

									File file = new File(URL, DB_FILE_NAME);
									db = SQLiteDatabase.openOrCreateDatabase(
											file, null);
									String sql = "SELECT * FROM order_data WHERE CorpOrderID='"
											+ Order.get(pos).get("oID") + "';";
									Cursor cur = db.rawQuery(sql, null);
									// bundle.putString("order_Id", OrderID);

									if (cur != null && cur.moveToFirst()) {
										createTime = cur.getString(cur
												.getColumnIndex("createTime"));
										Log.d("createTime", createTime);
										ToCorpID = cur.getString(cur
												.getColumnIndex("ToCorpID"));
										// Log.d("ToCorpID", ToCorpID);
										if (ToCorpID.trim() != null
												&& ToCorpID.trim() != "") {
											String customsql = "SELECT * FROM customer_data WHERE customerID='"
													+ ToCorpID + "';";
											Cursor cur1 = db.rawQuery(
													customsql, null);
											if (cur1 != null
													&& cur1.moveToFirst()) {
												customerName = cur1.getString(cur1
														.getColumnIndex("customerName"));
												customerInitial = cur1.getString(cur1
														.getColumnIndex("customerInitial"));
											} else {
												customerName = "";
												customerInitial = "";
											}
										} else {
											customerName = "";
											customerInitial = "";
										}

									}
									Intent intent = new Intent();
									Bundle bundle = new Bundle();
									bundle.putString("order_Id", Order.get(pos)
											.get("oID") + "");
									bundle.putString("order_createTime",
											createTime);
									bundle.putString("customer_Name",
											customerName);
									Log.d("customerName", customerName);
									bundle.putString("customer_Abbr",
											customerInitial);
									bundle.putString("customer_Code", ToCorpID);
									intent.putExtras(bundle);
									intent.setClass(ExportXML.this,
											SubmmitCode.class);
									startActivity(intent);
									ExportXML.this.finish();
									dialog.dismiss();
								}
							})
					.setNegativeButton(getString(R.string.view_order),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {

									File file = new File(URL, DB_FILE_NAME);
									db = SQLiteDatabase.openOrCreateDatabase(
											file, null);
									String sql = "SELECT customerName FROM customer_data WHERE customerID=(SELECT ToCorpID FROM order_data WHERE CorpOrderID='"
											+ Order.get(pos).get("oID") + "');";
									Cursor cur = db.rawQuery(sql, null);
									if (cur != null && cur.moveToFirst()) {
										customerName = cur.getString(cur
												.getColumnIndex("customerName"));
									} else {
										customerName = "";
									}
									Intent intent = new Intent();
									Bundle bundle = new Bundle();
									bundle.putString("order_Id", Order.get(pos)
											.get("oID") + "");
									bundle.putString("customer", customerName);
									intent.putExtras(bundle);
									intent.setClass(ExportXML.this,
											StatisticsActivity.class);
									startActivity(intent);
									dialog.dismiss();
								}
							})
					.setPositiveButton(getString(R.string.exprot_order),// 导出XML单据
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									File file = new File(URL, DB_FILE_NAME);
									db = SQLiteDatabase.openOrCreateDatabase(
											file, null);
									String code_sql = "SELECT * FROM orderCode_data WHERE orderID='"
											+ (pos + 1) + "';";
									Log.d("code_sql", code_sql);
									Cursor cur_code = db.rawQuery(code_sql,
											null);
									ArrayList<String> Code_Result = new ArrayList<String>();
									ArrayList<String> Code_Date = new ArrayList<String>();
									if (cur_code != null
											&& cur_code.moveToFirst()) {
										do {
											Code_Result.add(cur_code.getString(cur_code
													.getColumnIndex("code20")));
											Code_Date.add(cur_code.getString(cur_code
													.getColumnIndex("actDate")));

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
											if (Order.size() == 0) {
												Toast.makeText(
														ExportXML.this,
														getString(R.string.please_complete_order),
														Toast.LENGTH_LONG)
														.show();
											} else {
												File destDir = new File(
														Environment
																.getExternalStorageDirectory(),
														"/RedInfo/OrderList/");
												if (!destDir.exists()) {
													destDir.mkdirs();
												}
												File ExportFILE = new File(
														destDir, Order.get(pos)
																.get("oID")
																+ ".xml");// 取得sd卡的目录及要保存的文件名
												FileOutputStream outStream = new FileOutputStream(
														ExportFILE);
												new WriteXML()
														.saxToXml(
																outStream,
																Code_Date,
																Code_Result,
																((DaqApplication) ExportXML.this
																		.getApplication())
																		.getActorId(),
																Order.get(pos)
																		.get("oID"),
																Order.get(pos)
																		.get("oCorpID"),
																Func);
												Toast.makeText(
														ExportXML.this,
														getString(R.string.have_export_to_sd),
														Toast.LENGTH_LONG)
														.show();
												m_db.update_order(
														"order_data",
														"OA",
														Order.get(pos).get(
																"oID"),
														Order.get(pos).get(
																"oCorpID"),
														1,
														Order.get(pos).get(
																"oTime"));
												flag.set(pos, 1);
												dialog.dismiss();
												adapter = new CustomAdapter(
														ExportXML.this);
												List.setAdapter(adapter);
											}

										} else {
											Toast.makeText(
													ExportXML.this,
													getString(R.string.sd_card_not_exist),
													Toast.LENGTH_LONG).show();
										}
									} catch (FileNotFoundException e) {
										e.printStackTrace();
									}

								}
							});
		} else {
			customBuilder
					.setTitle(getString(R.string.order_title_choice))
					.setMessage(getString(R.string.please_select_func_need))
					.setNegativeButton(getString(R.string.view_order),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {

									File file = new File(URL, DB_FILE_NAME);
									db = SQLiteDatabase.openOrCreateDatabase(
											file, null);
									String sql = "SELECT customerName FROM customer_data WHERE customerID=(SELECT ToCorpID FROM order_data WHERE CorpOrderID='"
											+ Order.get(pos).get("oID") + "');";
									Cursor cur = db.rawQuery(sql, null);
									if (cur != null && cur.moveToFirst()) {
										customerName = cur.getString(cur
												.getColumnIndex("customerName"));
									} else {
										customerName = "";
									}
									Intent intent = new Intent();
									Bundle bundle = new Bundle();
									bundle.putString("order_Id", Order.get(pos)
											.get("oID") + "");
									bundle.putString("customer", customerName);
									intent.putExtras(bundle);
									intent.setClass(ExportXML.this,
											StatisticsActivity.class);
									startActivity(intent);
									dialog.dismiss();
								}
							}).setPositiveButton(getString(R.string.view_xml),// 查看XML单据
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									File ExportFILE = new File(Environment
											.getExternalStorageDirectory(),
											"/RedInfo/OrderList/"
													+ Order.get(pos).get("oID")
													+ ".xml");//
									Intent i = new Intent(Intent.ACTION_VIEW);
									i.setDataAndType(Uri.fromFile(ExportFILE),
											"text/*");
									startActivity(i);
									// Intent it = new
									// Intent(Intent.ACTION_VIEW,
									// Uri.fromFile(ExportFILE));
									// // 调用系统的浏览器 如果不存在的话 会抛异常
									// it.setClassName("com.android.browser",
									// "com.android.browser.BrowserActivity");
									// startActivity(it);
									dialog.dismiss();
								}
							});
		}

		dialog = customBuilder.create();
		dialog.show();
	}
}
