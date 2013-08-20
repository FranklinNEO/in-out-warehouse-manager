package com.redinfo.daq.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import mexxen.mx5010.barcode.BarcodeEvent;
import mexxen.mx5010.barcode.BarcodeListener;
import mexxen.mx5010.barcode.BarcodeManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.redinfo.daq.R;
import com.redinfo.daq.app.StateChangeActivity;
import com.redinfo.daq.app.StatisticsActivity;
import com.redinfo.daq.app.SubmmitCode;
import com.redinfo.daq.data.CodeDBHelper;
import com.redinfo.daq.ui.CustomDialog;

public class ExportXML extends Activity implements OnItemClickListener,
		OnClickListener, OnItemLongClickListener {
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		bm.removeListener(bl);
		bm.dismiss();
	}

	private boolean ProduceWareHouseIn_CheckBoxPreference = false;
	private boolean ReturnWareHouseIn_CheckBoxPreference = false;
	private boolean PurchaseWareHouseIn_CheckBoxPreference = false;
	private boolean AllocateWareHouseIn_CheckBoxPreference = false;
	private boolean SalesWareHouseOut_CheckBoxPreference = false;
	private boolean DestoryWareHouseOut_CheckBoxPreference = false;
	private boolean CheckWareHouseOut_CheckBoxPreference = false;
	private boolean ReturnWareHouseOut_CheckBoxPreference = false;
	private boolean AllocateWareHouseOut_CheckBoxPreference = false;
	public final static String URL = "/data/data/com.redinfo.daq/databases";
	public final static String DB_FILE_NAME = "info.db";
	SQLiteDatabase db = null;
	public CodeDBHelper m_db = null;
	public PopupWindow mPopupWindow;
	private ArrayList<HashMap<String, String>> Order = new ArrayList<HashMap<String, String>>();
	private ArrayList<Integer> flag = new ArrayList<Integer>();
	private CustomAdapter adapter;
	private MyAdapter madapter;
	private ListView List;
	public Dialog loadingdialog = null;
	private Button filterBtn = null;
	private Button searchBtn = null;
	private EditText searchEdit = null;
	private int pos;
	private int filter;
	private int Func = 100;
	private String sql = null;
	String createTime = null;
	String ToCorpID = null;
	public static ExportXML instance = null;
	String customerName = null;
	String customerInitial = null;
	private BarcodeManager bm = null;
	private BarcodeListener bl = null;
	private String barcode = null;
	public static boolean listener = true;
	private SharedPreferences sharedpreferences;
	public String orderType[] = { "IA", "IC", "ID", "IB", "OA", "OB", "OD",
			"OF", "OE" };
	private View filterlayout = null;
	private ListView filterlv = null;
	private OnItemClickListener listClickListener;
	private ArrayList<HashMap<String, String>> FuncListResult = null;
	private Integer[] FunctionStr = { R.string.produce_ware_house_in,
			R.string.purchase_ware_house_in, R.string.allocate_ware_house_in,
			R.string.return_ware_house_in, R.string.sales_ware_house_out,
			R.string.return_ware_house_out, R.string.allocate_ware_house_out,
			R.string.check_ware_house_out, R.string.destory_ware_house_out

	};
	private int[] Orderstatelist = { R.string.all,
			R.string.have_not_export_text, R.string.have_export_text };
	private boolean[] Functionenable = { ProduceWareHouseIn_CheckBoxPreference,
			PurchaseWareHouseIn_CheckBoxPreference,
			AllocateWareHouseIn_CheckBoxPreference,
			ReturnWareHouseIn_CheckBoxPreference,
			SalesWareHouseOut_CheckBoxPreference,
			ReturnWareHouseOut_CheckBoxPreference,
			AllocateWareHouseOut_CheckBoxPreference,
			CheckWareHouseOut_CheckBoxPreference,
			DestoryWareHouseOut_CheckBoxPreference };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.export_xml);
		instance = this;
		sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
		m_db = CodeDBHelper.getInstance(ExportXML.this);
		SharedPreferences sharedpreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		Functionenable[0] = sharedpreferences.getBoolean("ProduceWareHouseIn",
				false);
		Functionenable[1] = sharedpreferences.getBoolean("PurchaseWareHouseIn",
				false);
		Functionenable[2] = sharedpreferences.getBoolean("AllocateWareHouseIn",
				false);
		Functionenable[3] = sharedpreferences.getBoolean("ReturnWareHouseIn",
				false);
		Functionenable[4] = sharedpreferences.getBoolean("SalesWareHouseOut",
				false);
		Functionenable[5] = sharedpreferences.getBoolean("ReturnWareHouseOut",
				false);
		Functionenable[6] = sharedpreferences.getBoolean(
				"AllocateWareHouseOut", false);
		Functionenable[7] = sharedpreferences.getBoolean("CheckWareHouseOut",
				false);
		Functionenable[8] = sharedpreferences.getBoolean("DestoryWareHouseOut",
				false);
		filterBtn = (Button) findViewById(R.id.sub_btn);
		filterBtn.setOnClickListener(this);
		searchBtn = (Button) findViewById(R.id.order_search);
		searchBtn.setOnClickListener(this);
		searchEdit = (EditText) findViewById(R.id.search_bar_et);
		searchEdit.setInputType(InputType.TYPE_DATETIME_VARIATION_NORMAL);
		List = (ListView) findViewById(R.id.orderDate);
		List.setOnItemClickListener(this);
		List.setOnItemLongClickListener(this);
		listClickListener = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mPopupWindow.dismiss();
				filter = position;
				// if (position == 0) {
				// filterBtn.setText("全部");
				// } else {
				// filterBtn.setText(FuncListResult.get(position - 1).get(
				// "Func"));
				// }
				filterBtn.setText(getString(Orderstatelist[position]));
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

						switch (filter) {
						case 0:
							sql = "SELECT * FROM order_data ORDER BY flag,datetime(createTime) DESC;";
							break;
						case 1:
							sql = "SELECT * FROM order_data WHERE flag=0 ORDER BY flag,datetime(createTime) DESC";
							break;
						case 2:
							sql = "SELECT * FROM order_data WHERE flag=1 ORDER BY flag,datetime(createTime) DESC";
							break;
						default:
							break;
						}
						Cursor cur = db.rawQuery(sql, null);
						Order = new ArrayList<HashMap<String, String>>();
						flag = new ArrayList<Integer>();
						if (cur != null && cur.moveToFirst()) {
							do {
								HashMap<String, String> map = new HashMap<String, String>();
								map.put("oID", cur.getString(cur
										.getColumnIndex("CorpOrderID")));
								map.put("oCorpID", cur.getString(cur
										.getColumnIndex("ToCorpID")));
								map.put("oTime", cur.getString(cur
										.getColumnIndex("createTime")));
								map.put("oType", cur.getString(cur
										.getColumnIndex("orderType")));
								map.put("ocodeOrderID",
										cur.getInt(cur.getColumnIndex("_id"))
												+ "");
								flag.add(cur.getInt(cur.getColumnIndex("flag")));
								String count_sql = "SELECT count (*) AS codeNum FROM orderCode_data WHERE orderID='"
										+ cur.getInt(cur.getColumnIndex("_id"))
										+ "';";
								Cursor count_cur = db.rawQuery(count_sql, null);
								if (count_cur != null & count_cur.moveToFirst()) {
									map.put("oCount",
											count_cur.getInt(count_cur
													.getColumnIndex("codeNum"))
													+ "");
								}
								String customer_sql = "SELECT * FROM customer_data WHERE customerID='"
										+ map.get("oCorpID") + "';";
								Cursor customer_cur = db.rawQuery(customer_sql,
										null);
								if (customer_cur != null
										& customer_cur.moveToFirst()) {
									map.put("oCustomer",
											customer_cur.getString(customer_cur
													.getColumnIndex("customerName")));
								} else {
									map.put("oCustomer", "");
								}
								Order.add(map);
							} while ((cur.moveToNext()));
							cur.close();
							db.close();
						} else {
							cur.close();
							db.close();
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
				// new AsyncTask<Integer, Integer, String[]>() {
				//
				// protected void onPreExecute() {
				// loadingdialog.show();
				// super.onPreExecute();
				// }
				//
				// @Override
				// protected void onCancelled() {
				// super.onCancelled();
				// }
				//
				// protected String[] doInBackground(Integer... params) {
				// LoadingFunc();
				// File file = new File(URL, DB_FILE_NAME);
				// db = SQLiteDatabase.openOrCreateDatabase(file, null);
				//
				// if (filter == 0) {
				// sql = "SELECT * FROM order_data;";
				//
				// } else {
				// switch (Integer.parseInt(FuncListResult.get(
				// filter - 1).get("position"))) {
				// case 100:
				//
				// break;
				// case 0:
				// sql = "SELECT * FROM order_data WHERE orderType='IA';";
				// break;
				// case 1:
				// sql = "SELECT * FROM order_data WHERE orderType='IB';";
				// break;
				// case 2:
				// sql = "SELECT * FROM order_data WHERE orderType='IC';";
				// break;
				// case 3:
				// sql = "SELECT * FROM order_data WHERE orderType='ID';";
				// break;
				// case 4:
				// sql = "SELECT * FROM order_data WHERE orderType='OA';";
				// break;
				// case 5:
				// sql = "SELECT * FROM order_data WHERE orderType='OE';";
				// break;
				// case 6:
				// sql = "SELECT * FROM order_data WHERE orderType='OF';";
				// break;
				// case 7:
				// sql = "SELECT * FROM order_data WHERE orderType='OC';";
				// break;
				// case 8:
				// sql = "SELECT * FROM order_data WHERE orderType='OD';";
				// break;
				// default:
				// break;
				// }
				// }
				//
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
				// return null;
				// }
				//
				// protected void onPostExecute(String[] result) {
				// loadingdialog.dismiss();
				// adapter = new CustomAdapter(ExportXML.this);
				// List.setAdapter(adapter);
				// super.onPostExecute(result);
				// }
				// }.execute(0);
			}
		};
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
				LoadingFunc();
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
					// sql = "SELECT * FROM order_data WHERE orderType='"
					// + orderType[Func] + "';";
					sql = "SELECT * FROM order_data ORDER BY flag,datetime(createTime) DESC;";
					Cursor cur = db.rawQuery(sql, null);
					Order = new ArrayList<HashMap<String, String>>();
					flag = new ArrayList<Integer>();
					if (cur != null && cur.moveToFirst()) {
						do {
							HashMap<String, String> map = new HashMap<String, String>();
							map.put("oID", cur.getString(cur
									.getColumnIndex("CorpOrderID")));
							map.put("oCorpID", cur.getString(cur
									.getColumnIndex("ToCorpID")));
							map.put("oTime", cur.getString(cur
									.getColumnIndex("createTime")));
							map.put("oType", cur.getString(cur
									.getColumnIndex("orderType")));
							Log.d("type", cur.getString(cur
									.getColumnIndex("orderType")));
							map.put("ocodeOrderID",
									cur.getInt(cur.getColumnIndex("_id")) + "");
							flag.add(cur.getInt(cur.getColumnIndex("flag")));
							String count_sql = "SELECT count (*) AS codeNum FROM orderCode_data WHERE orderID='"
									+ cur.getInt(cur.getColumnIndex("_id"))
									+ "';";
							Cursor count_cur = db.rawQuery(count_sql, null);
							if (count_cur != null & count_cur.moveToFirst()) {
								map.put("oCount",
										count_cur.getInt(count_cur
												.getColumnIndex("codeNum"))
												+ "");
							}
							String customer_sql = "SELECT * FROM customer_data WHERE customerID='"
									+ map.get("oCorpID") + "';";
							Cursor customer_cur = db.rawQuery(customer_sql,
									null);
							if (customer_cur != null
									& customer_cur.moveToFirst()) {
								map.put("oCustomer",
										customer_cur.getString(customer_cur
												.getColumnIndex("customerName")));
							} else {
								map.put("oCustomer", "");
							}
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
				adapter = new CustomAdapter(ExportXML.this);
				List.setAdapter(adapter);
				loadingdialog.dismiss();
				super.onPostExecute(result);
			}
		}.execute(0);
		bm = new BarcodeManager(this);

		bl = new BarcodeListener() {
			// 重写barcodeEvent 方法，获取条码事件
			@Override
			public void barcodeEvent(BarcodeEvent event) {
				// 当条码事件的命令为“SCANNER_READ”时，进行操作
				if (event.getOrder().equals("SCANNER_READ")) {
					// 调用getBarcode()方法读取条码信息
					if (listener) {

						barcode = bm.getBarcode();

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
								listener = false;
								File file = new File(URL, DB_FILE_NAME);
								db = SQLiteDatabase.openOrCreateDatabase(file,
										null);
								String orderCode_sql = "SELECT orderID FROM orderCode_data WHERE code20='"
										+ barcode + "';";
								Cursor orderCode_cur = db.rawQuery(
										orderCode_sql, null);
								if (orderCode_cur != null
										&& orderCode_cur.moveToFirst()) {
									Order = new ArrayList<HashMap<String, String>>();
									flag = new ArrayList<Integer>();
									do {
										sql = "SELECT * FROM order_data WHERE _id='"
												+ orderCode_cur
														.getInt(orderCode_cur
																.getColumnIndex("orderID"))
												+ "'  ORDER BY flag,datetime(createTime) DESC;";
										Cursor cur = db.rawQuery(sql, null);
										if (cur != null && cur.moveToFirst()) {
											do {
												HashMap<String, String> map = new HashMap<String, String>();
												map.put("oID",
														cur.getString(cur
																.getColumnIndex("CorpOrderID")));
												map.put("oCorpID",
														cur.getString(cur
																.getColumnIndex("ToCorpID")));
												map.put("oTime",
														cur.getString(cur
																.getColumnIndex("createTime")));
												map.put("oType",
														cur.getString(cur
																.getColumnIndex("orderType")));
												map.put("ocodeOrderID",
														cur.getInt(cur
																.getColumnIndex("_id"))
																+ "");
												flag.add(cur.getInt(cur
														.getColumnIndex("flag")));
												String count_sql = "SELECT count (*) AS codeNum FROM orderCode_data WHERE orderID='"
														+ cur.getInt(cur
																.getColumnIndex("_id"))
														+ "';";
												Cursor count_cur = db.rawQuery(
														count_sql, null);
												if (count_cur != null
														& count_cur
																.moveToFirst()) {
													map.put("oCount",
															count_cur
																	.getInt(count_cur
																			.getColumnIndex("codeNum"))
																	+ "");
												}
												String customer_sql = "SELECT * FROM customer_data WHERE customerID='"
														+ map.get("oCorpID")
														+ "';";
												Cursor customer_cur = db
														.rawQuery(customer_sql,
																null);
												if (customer_cur != null
														& customer_cur
																.moveToFirst()) {
													map.put("oCustomer",
															customer_cur
																	.getString(customer_cur
																			.getColumnIndex("customerName")));
												} else {
													map.put("oCustomer", "");
												}
												Order.add(map);
											} while ((cur.moveToNext()));
											cur.close();
										} else {
											cur.close();
										}
									} while ((orderCode_cur.moveToNext()));
								}

								return null;
							}

							protected void onPostExecute(String[] result) {
								loadingdialog.dismiss();
								searchEdit.setText(barcode);
								adapter = new CustomAdapter(ExportXML.this);
								List.setAdapter(adapter);
								listener = true;
								super.onPostExecute(result);
							}
						}.execute(0);
					}

				}
			}
		};
		bm.addListener(bl);
	}

	protected void LoadingFunc() {
		// TODO Auto-generated method stub
		FuncListResult = new ArrayList<HashMap<String, String>>();
		for (int i = 0; i < Functionenable.length; i++) {
			if (Functionenable[i]) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("Func", getString(FunctionStr[i]));
				map.put("position", i + "");
				FuncListResult.add(map);
			}
		}

	}

	public final class mHolder {
		public TextView filterText;
	}

	// 自定义条码数据容纳器
	private class MyAdapter extends BaseAdapter {
		private LayoutInflater inflater;

		public MyAdapter(Context context) {
			this.inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			// return FuncListResult.size() + 1;
			return Orderstatelist.length;
		}

		@Override
		public Object getItem(int position) {
			// if (position == 0) {
			// HashMap<String, String> map = new HashMap<String, String>();
			// map.put("Func", "全部");
			// map.put("position", "100");
			// return map;
			// } else {
			// return FuncListResult.get(position - 1);
			// }
			return getString(Orderstatelist[position]);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			mHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.filter_list_item, null);
				holder = new mHolder();
				holder.filterText = (TextView) convertView
						.findViewById(R.id.filtertv);
				convertView.setTag(holder);
			} else {
				holder = (mHolder) convertView.getTag();
			}
			// if (position == 0) {
			// holder.filterText.setText("全部");
			// } else {
			// holder.filterText.setText(FuncListResult.get(position - 1).get(
			// "Func"));
			// }
			holder.filterText.setText(getString(Orderstatelist[position]));
			return convertView;
		}
	}

	public final class ViewHolder {
		public TextView OrderType;
		public TextView OrderID;
		public TextView OrderInfo;
		public TextView OrderState;
		public TextView OrderDate;
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
				convertView = inflater.inflate(R.layout.export_list_item, null);
				holder = new ViewHolder();
				holder.OrderID = (TextView) convertView
						.findViewById(R.id.orderIdTv);
				holder.OrderType = (TextView) convertView
						.findViewById(R.id.typeTv);
				holder.OrderState = (TextView) convertView
						.findViewById(R.id.orderState);
				holder.OrderInfo = (TextView) convertView
						.findViewById(R.id.orderInfoTv);
				holder.OrderDate = (TextView) convertView
						.findViewById(R.id.createTimeTv);
				// holder.divider = convertView
				// .findViewById(R.id.horizontal_divider);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			String type = Order.get(position).get("oType").trim();
			if (type.equals("OA")) {
				holder.OrderType.setText(getString(R.string.oa));
			} else if (type.equals("OB")) {
				holder.OrderType.setText(getString(R.string.ob));
			} else if (type.equals("OD")) {
				holder.OrderType.setText(getString(R.string.od));
			} else if (type.equals("OE")) {
				holder.OrderType.setText(getString(R.string.oe));
			} else if (type.equals("OF")) {
				holder.OrderType.setText(getString(R.string.of));
			} else if (type.equals("IA")) {
				holder.OrderType.setText(getString(R.string.ia));
			} else if (type.equals("IB")) {
				holder.OrderType.setText(getString(R.string.ib));
			} else if (type.equals("IC")) {
				holder.OrderType.setText(getString(R.string.ic));
			} else if (type.equals("ID")) {
				holder.OrderType.setText(getString(R.string.id));
			}
			String createTime = Order.get(position).get("oTime");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat df = new SimpleDateFormat("yyyy");
			Date d = null;
			Date nowdate = null;
			Date yd = null;
			Date ynowdate = null;
			try {
				d = sdf.parse(createTime.substring(0, 10));
				nowdate = sdf.parse(sdf.format(new java.util.Date()).substring(
						0, 10));
				yd = df.parse(createTime);
				ynowdate = df.parse(df.format(new java.util.Date()).substring(
						0, 4));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (d.before(nowdate)) {
				if (yd.before(ynowdate)) {

					holder.OrderDate.setText(createTime.substring(0, 10));
				} else {
					holder.OrderDate.setText(createTime.substring(5, 10));
				}

			} else {
				holder.OrderDate.setText(createTime.substring(10, 16));
			}
			// holder.OrderDate.setText(createTime);

			holder.OrderID.setText(Order.get(position).get("oID"));
			holder.OrderInfo.setText(getString(R.string.tiaomazongliang)
					+ Order.get(position).get("oCount") + "\n"
					+ Order.get(position).get("oCustomer"));
			if ((flag.get(position)) == 1) {
				holder.OrderState.setTextColor(Color.parseColor("#ff2e5f93"));
				holder.OrderState.setText(getString(R.string.have_export_text));
			} else {
				holder.OrderState.setTextColor(Color.parseColor("#60000000"));
				holder.OrderState
						.setText(getString(R.string.have_not_export_text));
			}
			return convertView;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		pos = position;

		if (sharedpreferences.getBoolean("checkproductInfo", true)) {
			Dialog dialog = null;
			CustomDialog.Builder customBuilder = new CustomDialog.Builder(
					ExportXML.this);

			if (flag.get(position) != 1) {
				customBuilder
						.setTitle(
								getString(R.string.danjuhao)
										+ Order.get(pos).get("oID"))
						.setMessage(
								Order.get(pos).get("oCustomer") + "\n"
										+ Order.get(pos).get("oTime") + "\n"
										+ getString(R.string.tiaomazongliang)
										+ Order.get(pos).get("oCount"))
						.setNeutralButton(getString(R.string.continue_scan),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {

										File file = new File(URL, DB_FILE_NAME);
										db = SQLiteDatabase
												.openOrCreateDatabase(file,
														null);
										String sql = "SELECT * FROM order_data WHERE CorpOrderID='"
												+ Order.get(pos).get("oID")
												+ "';";
										Cursor cur = db.rawQuery(sql, null);
										// bundle.putString("order_Id",
										// OrderID);

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
										bundle.putString("order_Id",
												Order.get(pos).get("oID") + "");
										bundle.putString("order_createTime",
												createTime);
										bundle.putString("customer_Name",
												customerName);
										Log.d("customerName", customerName);
										bundle.putString("customer_Abbr",
												customerInitial);
										bundle.putString("customer_Code",
												ToCorpID);
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
										db = SQLiteDatabase
												.openOrCreateDatabase(file,
														null);
										String sql = "SELECT customerName FROM customer_data WHERE customerID=(SELECT ToCorpID FROM order_data WHERE CorpOrderID='"
												+ Order.get(pos).get("oID")
												+ "');";
										Cursor cur = db.rawQuery(sql, null);
										if (cur != null && cur.moveToFirst()) {
											customerName = cur.getString(cur
													.getColumnIndex("customerName"));
										} else {
											customerName = "";
										}
										Intent intent = new Intent();
										Bundle bundle = new Bundle();
										bundle.putString("order_Id",
												Order.get(pos).get("oID") + "");
										bundle.putString("customer",
												customerName);
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
										db = SQLiteDatabase
												.openOrCreateDatabase(file,
														null);
										String code_sql = "SELECT * FROM orderCode_data WHERE orderID='"
												+ Order.get(pos).get(
														"ocodeOrderID") + "';";
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
												if (Order.size() == 0) {
													Toast.makeText(
															ExportXML.this,
															getString(R.string.please_complete_order),
															Toast.LENGTH_SHORT)
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
															destDir, Order.get(
																	pos).get(
																	"oID")
																	+ ".xml");// 取得sd卡的目录及要保存的文件名
													FileOutputStream outStream = new FileOutputStream(
															ExportFILE);
													new WriteXML().saxToXml(
															outStream,
															Code_Date,
															Code_Result,
															Actor_ID,
															Order.get(pos).get(
																	"oID"),
															Order.get(pos).get(
																	"oCorpID"),
															Func);
													Toast.makeText(
															ExportXML.this,
															getString(R.string.have_export_to_sd),
															Toast.LENGTH_SHORT)
															.show();
													m_db.update_order(
															"order_data",
															orderType[Func],
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
														Toast.LENGTH_SHORT)
														.show();
											}
										} catch (FileNotFoundException e) {
											e.printStackTrace();
										}

									}
								});
				dialog = customBuilder.create();
				dialog.show();
			} else {
				File file = new File(URL, DB_FILE_NAME);
				db = SQLiteDatabase.openOrCreateDatabase(file, null);
				String sql = "SELECT * FROM order_data WHERE CorpOrderID='"
						+ Order.get(pos).get("oID") + "';";
				Cursor cur = db.rawQuery(sql, null);
				// bundle.putString("order_Id",
				// OrderID);

				if (cur != null && cur.moveToFirst()) {
					createTime = cur
							.getString(cur.getColumnIndex("createTime"));
					Log.d("createTime", createTime);
					ToCorpID = cur.getString(cur.getColumnIndex("ToCorpID"));
					// Log.d("ToCorpID", ToCorpID);
					if (ToCorpID.trim() != null && ToCorpID.trim() != "") {
						String customsql = "SELECT * FROM customer_data WHERE customerID='"
								+ ToCorpID + "';";
						Cursor cur1 = db.rawQuery(customsql, null);
						if (cur1 != null && cur1.moveToFirst()) {
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

				bundle.putString("order_Id", Order.get(pos).get("oID") + "");
				bundle.putString("order_createTime",
						Order.get(position).get("oTime"));
				bundle.putString("customer_Name",
						Order.get(position).get("oCustomer"));
				bundle.putString("customer_Abbr", customerInitial);
				bundle.putString("customer_Code", ToCorpID);
				bundle.putString("codeNum", Order.get(pos).get("oCount"));
				bundle.putString("order", Order.get(pos).get("ocodeOrderID"));
				bundle.putString("func", Func + "");
				bundle.putString("flag", "1");
				intent.putExtras(bundle);
				intent.setClass(ExportXML.this, StateChangeActivity.class);
				startActivity(intent);
				// customBuilder
				// .setTitle("单据号：" + Order.get(pos).get("oID"))
				// .setMessage(
				// Order.get(pos).get("oCustomer") + "\n"
				// + Order.get(pos).get("oTime") + "\n"
				// + "条码总量："
				// + Order.get(pos).get("oCount"))
				// .setNegativeButton(getString(R.string.view_order),
				// new DialogInterface.OnClickListener() {
				// public void onClick(DialogInterface dialog,
				// int which) {
				//
				// File file = new File(URL, DB_FILE_NAME);
				// db = SQLiteDatabase
				// .openOrCreateDatabase(file,
				// null);
				// String sql =
				// "SELECT customerName FROM customer_data WHERE customerID=(SELECT ToCorpID FROM order_data WHERE CorpOrderID='"
				// + Order.get(pos).get("oID")
				// + "');";
				// Cursor cur = db.rawQuery(sql, null);
				// if (cur != null && cur.moveToFirst()) {
				// customerName = cur.getString(cur
				// .getColumnIndex("customerName"));
				// } else {
				// customerName = "";
				// }
				// Intent intent = new Intent();
				// Bundle bundle = new Bundle();
				// bundle.putString("order_Id",
				// Order.get(pos).get("oID") + "");
				// bundle.putString("customer",
				// customerName);
				// intent.putExtras(bundle);
				// intent.setClass(ExportXML.this,
				// StatisticsActivity.class);
				// startActivity(intent);
				// dialog.dismiss();
				// }
				// })
				// .setPositiveButton(getString(R.string.view_xml),// 查看XML单据
				// new DialogInterface.OnClickListener() {
				// public void onClick(DialogInterface dialog,
				// int which) {
				// File ExportFILE = new File(Environment
				// .getExternalStorageDirectory(),
				// "/RedInfo/OrderList/"
				// + Order.get(pos).get(
				// "oID") + ".xml");//
				// Intent i = new Intent(
				// Intent.ACTION_VIEW);
				// i.setDataAndType(
				// Uri.fromFile(ExportFILE),
				// "text/*");
				// startActivity(i);
				// // Intent it = new
				// // Intent(Intent.ACTION_VIEW,
				// // Uri.fromFile(ExportFILE));
				// // // 调用系统的浏览器 如果不存在的话 会抛异常
				// // it.setClassName("com.android.browser",
				// // "com.android.browser.BrowserActivity");
				// // startActivity(it);
				// dialog.dismiss();
				// }
				// });
			}

		} else {
			Dialog dialog = null;
			CustomDialog.Builder customBuilder = new CustomDialog.Builder(
					ExportXML.this);

			if (flag.get(position) != 1) {
				customBuilder
						.setTitle(
								getString(R.string.danjuhao)
										+ Order.get(pos).get("oID"))
						.setMessage(
								Order.get(pos).get("oCustomer") + "\n"
										+ Order.get(pos).get("oTime") + "\n"
										+ getString(R.string.tiaomazongliang)
										+ Order.get(pos).get("oCount"))

						.setPositiveButton(getString(R.string.exprot_order),// 导出XML单据
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										File file = new File(URL, DB_FILE_NAME);
										db = SQLiteDatabase
												.openOrCreateDatabase(file,
														null);
										String code_sql = "SELECT * FROM orderCode_data WHERE orderID='"
												+ Order.get(pos).get(
														"ocodeOrderID") + "';";
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
												if (Order.size() == 0) {
													Toast.makeText(
															ExportXML.this,
															getString(R.string.please_complete_order),
															Toast.LENGTH_SHORT)
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
															destDir, Order.get(
																	pos).get(
																	"oID")
																	+ ".xml");// 取得sd卡的目录及要保存的文件名
													FileOutputStream outStream = new FileOutputStream(
															ExportFILE);
													new WriteXML().saxToXml(
															outStream,
															Code_Date,
															Code_Result,
															Actor_ID,
															Order.get(pos).get(
																	"oID"),
															Order.get(pos).get(
																	"oCorpID"),
															Func);
													Toast.makeText(
															ExportXML.this,
															getString(R.string.have_export_to_sd),
															Toast.LENGTH_SHORT)
															.show();
													m_db.update_order(
															"order_data",
															orderType[Func],
															Order.get(pos).get(
																	"oID"),
															Order.get(pos).get(
																	"oCorpID"),
															1,
															Order.get(pos).get(
																	"oTime"));
													Log.d("export",
															Order.get(pos).get(
																	"oID"));
													Log.d("export",
															Order.get(pos).get(
																	"oCorpID"));
													Log.d("export",
															Order.get(pos).get(
																	"oTime"));
													Log.d("export",
															orderType[Func]);
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
														Toast.LENGTH_SHORT)
														.show();
											}
										} catch (FileNotFoundException e) {
											e.printStackTrace();
										}

									}
								})
						.setNegativeButton(getString(R.string.continue_scan),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {

										File file = new File(URL, DB_FILE_NAME);
										db = SQLiteDatabase
												.openOrCreateDatabase(file,
														null);
										String sql = "SELECT * FROM order_data WHERE CorpOrderID='"
												+ Order.get(pos).get("oID")
												+ "';";
										Cursor cur = db.rawQuery(sql, null);
										// bundle.putString("order_Id",
										// OrderID);

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
										bundle.putString("order_Id",
												Order.get(pos).get("oID") + "");
										bundle.putString("order_createTime",
												createTime);
										bundle.putString("customer_Name",
												customerName);
										Log.d("customerName", customerName);
										bundle.putString("customer_Abbr",
												customerInitial);
										bundle.putString("customer_Code",
												ToCorpID);

										intent.putExtras(bundle);
										intent.setClass(ExportXML.this,
												SubmmitCode.class);
										startActivity(intent);
										ExportXML.this.finish();
										dialog.dismiss();
									}
								});
				dialog = customBuilder.create();
				dialog.show();
			} else {
				// customBuilder
				// .setTitle("单据号：" + Order.get(pos).get("oID"))
				// .setMessage(
				// Order.get(pos).get("oCustomer") + "\n"
				// + Order.get(pos).get("oTime") + "\n"
				// + "条码总量："
				// + Order.get(pos).get("oCount"))
				// .setPositiveButton(getString(R.string.view_xml),// 查看XML单据
				// new DialogInterface.OnClickListener() {
				// public void onClick(DialogInterface dialog,
				// int which) {
				// File ExportFILE = new File(Environment
				// .getExternalStorageDirectory(),
				// "/RedInfo/OrderList/"
				// + Order.get(pos).get(
				// "oID") + ".xml");//
				// Intent i = new Intent(
				// Intent.ACTION_VIEW);
				// i.setDataAndType(
				// Uri.fromFile(ExportFILE),
				// "text/*");
				// startActivity(i);
				// // Intent it = new
				// // Intent(Intent.ACTION_VIEW,
				// // Uri.fromFile(ExportFILE));
				// // // 调用系统的浏览器 如果不存在的话 会抛异常
				// // it.setClassName("com.android.browser",
				// // "com.android.browser.BrowserActivity");
				// // startActivity(it);
				// dialog.dismiss();
				// }
				// });
				File file = new File(URL, DB_FILE_NAME);
				db = SQLiteDatabase.openOrCreateDatabase(file, null);
				String sql = "SELECT * FROM order_data WHERE CorpOrderID='"
						+ Order.get(pos).get("oID") + "';";
				Cursor cur = db.rawQuery(sql, null);
				// bundle.putString("order_Id",
				// OrderID);

				if (cur != null && cur.moveToFirst()) {
					createTime = cur
							.getString(cur.getColumnIndex("createTime"));
					Log.d("createTime", createTime);
					ToCorpID = cur.getString(cur.getColumnIndex("ToCorpID"));
					// Log.d("ToCorpID", ToCorpID);
					if (ToCorpID.trim() != null && ToCorpID.trim() != "") {
						String customsql = "SELECT * FROM customer_data WHERE customerID='"
								+ ToCorpID + "';";
						Cursor cur1 = db.rawQuery(customsql, null);
						if (cur1 != null && cur1.moveToFirst()) {
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
				bundle.putString("order", Order.get(pos).get("ocodeOrderID"));
				bundle.putString("order_Id", Order.get(pos).get("oID") + "");
				bundle.putString("order_createTime",
						Order.get(position).get("oTime"));
				bundle.putString("customer_Name",
						Order.get(position).get("oCustomer"));
				bundle.putString("customer_Abbr", customerInitial);
				bundle.putString("customer_Code", ToCorpID);
				bundle.putString("codeNum", Order.get(pos).get("oCount"));
				bundle.putString("func", Func + "");
				bundle.putString("flag", "0");
				intent.putExtras(bundle);
				intent.setClass(ExportXML.this, StateChangeActivity.class);
				startActivity(intent);
			}

		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.sub_btn:
			if (mPopupWindow == null) {
				filterlayout = getLayoutInflater().inflate(
						R.layout.poplistview, null);
				filterlv = (ListView) filterlayout.findViewById(R.id.pop_list);
				madapter = new MyAdapter(ExportXML.this);
				filterlv.setAdapter(madapter);
				filterlv.setOnItemClickListener(listClickListener);
				mPopupWindow = new PopupWindow(filterlayout,
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			}
			mPopupWindow.setOutsideTouchable(true); // 设置popwindow外的区域可点击
			mPopupWindow.setFocusable(true);
			mPopupWindow.setTouchable(true);
			mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
			mPopupWindow.showAtLocation(findViewById(R.id.export_layout),
					Gravity.RIGHT | Gravity.TOP, 0, (int) this.getResources()
							.getDimension(R.dimen.titleheightsize));

			break;
		case R.id.order_search:
			final String searchWords = searchEdit.getText().toString().trim();
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
					if (searchWords.length() < 20) {
						db = SQLiteDatabase.openOrCreateDatabase(file, null);
						sql = "SELECT * FROM order_data WHERE CorpOrderID LIKE '%"
								+ searchWords
								+ "%' ORDER BY flag,datetime(createTime) DESC;";
						Cursor cur = db.rawQuery(sql, null);
						Order = new ArrayList<HashMap<String, String>>();
						flag = new ArrayList<Integer>();
						if (cur != null && cur.moveToFirst()) {
							do {
								HashMap<String, String> map = new HashMap<String, String>();
								map.put("oID", cur.getString(cur
										.getColumnIndex("CorpOrderID")));
								map.put("oCorpID", cur.getString(cur
										.getColumnIndex("ToCorpID")));
								map.put("oTime", cur.getString(cur
										.getColumnIndex("createTime")));
								map.put("oType", cur.getString(cur
										.getColumnIndex("orderType")));
								map.put("ocodeOrderID",
										cur.getInt(cur.getColumnIndex("_id"))
												+ "");
								flag.add(cur.getInt(cur.getColumnIndex("flag")));
								String count_sql = "SELECT count (*) AS codeNum FROM orderCode_data WHERE orderID='"
										+ cur.getInt(cur.getColumnIndex("_id"))
										+ "';";
								Cursor count_cur = db.rawQuery(count_sql, null);
								if (count_cur != null & count_cur.moveToFirst()) {
									map.put("oCount",
											count_cur.getInt(count_cur
													.getColumnIndex("codeNum"))
													+ "");
								}

								String customer_sql = "SELECT * FROM customer_data WHERE customerID='"
										+ map.get("oCorpID") + "';";
								Cursor customer_cur = db.rawQuery(customer_sql,
										null);
								if (customer_cur != null
										& customer_cur.moveToFirst()) {
									map.put("oCustomer",
											customer_cur.getString(customer_cur
													.getColumnIndex("customerName")));
								} else {
									map.put("oCustomer", "");
								}
								Order.add(map);
							} while ((cur.moveToNext()));
							cur.close();
							db.close();
						} else {
							cur.close();
							db.close();
						}
					} else {
						db = SQLiteDatabase.openOrCreateDatabase(file, null);
						String orderCode_sql = "SELECT orderID FROM orderCode_data WHERE code20='"
								+ searchWords + "';";
						Cursor orderCode_cur = db.rawQuery(orderCode_sql, null);
						if (orderCode_cur != null
								&& orderCode_cur.moveToFirst()) {
							Order = new ArrayList<HashMap<String, String>>();
							flag = new ArrayList<Integer>();
							do {
								sql = "SELECT * FROM order_data WHERE _id='"
										+ orderCode_cur.getInt(orderCode_cur
												.getColumnIndex("orderID"))
										+ "'  ORDER BY flag,datetime(createTime) DESC;";
								Cursor cur = db.rawQuery(sql, null);
								if (cur != null && cur.moveToFirst()) {
									do {
										HashMap<String, String> map = new HashMap<String, String>();
										map.put("oID", cur.getString(cur
												.getColumnIndex("CorpOrderID")));
										map.put("oCorpID", cur.getString(cur
												.getColumnIndex("ToCorpID")));
										map.put("oTime", cur.getString(cur
												.getColumnIndex("createTime")));
										map.put("oType", cur.getString(cur
												.getColumnIndex("orderType")));
										map.put("ocodeOrderID",
												cur.getInt(cur
														.getColumnIndex("_id"))
														+ "");
										flag.add(cur.getInt(cur
												.getColumnIndex("flag")));
										String count_sql = "SELECT count (*) AS codeNum FROM orderCode_data WHERE orderID='"
												+ cur.getInt(cur
														.getColumnIndex("_id"))
												+ "';";
										Cursor count_cur = db.rawQuery(
												count_sql, null);
										if (count_cur != null
												& count_cur.moveToFirst()) {
											map.put("oCount",
													count_cur.getInt(count_cur
															.getColumnIndex("codeNum"))
															+ "");
										}
										String customer_sql = "SELECT * FROM customer_data WHERE customerID='"
												+ map.get("oCorpID") + "';";
										Cursor customer_cur = db.rawQuery(
												customer_sql, null);
										if (customer_cur != null
												& customer_cur.moveToFirst()) {
											map.put("oCustomer",
													customer_cur
															.getString(customer_cur
																	.getColumnIndex("customerName")));
										} else {
											map.put("oCustomer", "");
										}
										Order.add(map);
									} while ((cur.moveToNext()));
									cur.close();
								} else {
									cur.close();
								}
							} while ((orderCode_cur.moveToNext()));
						}

					}
					return null;
				}

				protected void onPostExecute(String[] result) {
					adapter = new CustomAdapter(ExportXML.this);
					List.setAdapter(adapter);
					loadingdialog.dismiss();
					super.onPostExecute(result);
				}
			}.execute(0);
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View v, int position,
			long id) {
		// TODO Auto-generated method stub
		pos = position;
		Dialog dialog = null;
		CustomDialog.Builder customBuilder = new CustomDialog.Builder(
				ExportXML.this);
		customBuilder
				.setTitle(
						getString(R.string.danjuhao)
								+ Order.get(pos).get("oID"))
				.setMessage(getString(R.string.delete_order_message))
				.setNegativeButton(getString(R.string.cancel),
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						}).setPositiveButton(getString(R.string.delete_order),// 导出XML单据
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								m_db = CodeDBHelper.getInstance(ExportXML.this);
								m_db.delete_orderCode(
										CodeDBHelper.ORDER_CODE_TABLE_NAME,
										Integer.parseInt(Order.get(pos).get(
												"ocodeOrderID")));
								m_db.delete_order(
										CodeDBHelper.ORDER_TABLE_NAME, Order
												.get(pos).get("oID"));
								m_db.delete_codeStatistics(
										CodeDBHelper.CODE_STATISTICS_TABEL_NAME,
										Integer.parseInt(Order.get(pos).get(
												"ocodeOrderID")));
								Order.remove(pos);
								dialog.dismiss();
								adapter = new CustomAdapter(ExportXML.this);
								List.setAdapter(adapter);
							}
						});
		dialog = customBuilder.create();
		dialog.show();
		return false;
	}
}
