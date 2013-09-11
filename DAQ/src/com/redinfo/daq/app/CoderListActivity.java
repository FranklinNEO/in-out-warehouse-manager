package com.redinfo.daq.app;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import mexxen.mx5010.barcode.BarcodeEvent;
import mexxen.mx5010.barcode.BarcodeListener;
import mexxen.mx5010.barcode.BarcodeManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.redinfo.daq.R;
import com.redinfo.daq.data.CodeDBHelper;
import com.redinfo.daq.ui.CustomDialog;
import com.redinfo.daq.util.SoundPlayer;
import com.redinfo.daq.util.SoundPlayer.State;

public class CoderListActivity extends Activity implements
		OnItemLongClickListener, OnClickListener {
	// This intent string contains the captured data as a string
	// (in the case of MSR this data string contains a concatenation of the
	// track data)
	private static final String DATA_STRING_TAG = "com.motorolasolutions.emdk.datawedge.data_string";
	private static String ourIntentAction = "com.redinfo.daq.codelist.RECVR";
	public final static String URL = "/data/data/com.redinfo.daq/databases";
	public final static String DB_FILE_NAME = "info.db";
	SQLiteDatabase db = null;
	public CodeDBHelper m_db = null;
	private int orderID = -1;
	private ArrayList<String> code_arr = null;
	public Dialog loadingdialog = null;
	private ListView codeListView = null;
	private MyAdapter adapter = null;
	private EditText search_et = null;
	private Button code_searchBtn = null;
	private int pos = -1;
	String order = null;
	String createTime = null;
	String ToCorpID = null;
	String customerName = null;
	String customerInitial = null;
	private boolean exsit = true;
	private BarcodeManager bm = null;
	private BarcodeListener bl = null;
	private String barcode = null;
	private com.redinfo.daq.util.SoundPlayer sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.code_list_activity);
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			orderID = Integer.parseInt(bundle.getString("order_Id"));
			customerName = bundle.getString("customer");
			order = bundle.getString("order");
		} else {

		}
		sp = new SoundPlayer();
		sp.addSound(CoderListActivity.this, R.raw.alert, State.refreshed);
		sp.addSound(CoderListActivity.this, R.raw.refresh_pulling, State.pull);
		codeListView = (ListView) findViewById(R.id.codelv);
		codeListView.setOnItemLongClickListener(this);
		search_et = (EditText) findViewById(R.id.search_bar_et);
		// search_et.setInputType(InputType.TYPE_CLASS_NUMBER);
		search_et.setInputType(InputType.TYPE_DATETIME_VARIATION_NORMAL);
		code_searchBtn = (Button) findViewById(R.id.code_search);
		code_searchBtn.setOnClickListener(this);
		loadingdialog = new Dialog(CoderListActivity.this, R.style.mmdialog);
		loadingdialog.setContentView(R.layout.loading_dialog);
		m_db = CodeDBHelper.getInstance(CoderListActivity.this);
		File file = new File(URL, DB_FILE_NAME);
		db = SQLiteDatabase.openOrCreateDatabase(file, null);
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
				loadCodeInfo();
				return null;
			}

			protected void onPostExecute(String[] result) {
				// init();
				adapter = new MyAdapter(CoderListActivity.this);
				codeListView.setAdapter(adapter);
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
							resetList(barcode);
							return null;
						}

						protected void onPostExecute(String[] result) {
							// init();
							if (exsit) {
								adapter = new MyAdapter(CoderListActivity.this);
								codeListView.setAdapter(adapter);

							} else {
								sp.play(State.refreshed);
								Toast.makeText(CoderListActivity.this,
										getString(R.string.code_not_exsit),
										Toast.LENGTH_SHORT).show();
							}
							search_et.setText(barcode);
							loadingdialog.dismiss();
							super.onPostExecute(result);
						}
					}.execute(0);

				}
			}
		};
		bm.addListener(bl);
	}

	// We need to handle any incoming intents, so let override the onNewIntent
	// method
	@Override
	public void onNewIntent(Intent i) {
		// i.addCategory("com.redinfo.daq.app.code");
		// i.addCategory("android.intent.category.DEFAULT");
		handleDecodeData(i);
	}

	private void handleDecodeData(Intent i) {
		if (i.getAction().contentEquals(ourIntentAction)) {
			String data = i.getStringExtra(DATA_STRING_TAG);
			// 调用getBarcode()方法读取条码信息
			barcode = data;

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
					resetList(barcode);
					return null;
				}

				protected void onPostExecute(String[] result) {
					// init();
					if (exsit) {
						adapter = new MyAdapter(CoderListActivity.this);
						codeListView.setAdapter(adapter);

					} else {
						sp.play(State.refreshed);
						Toast.makeText(CoderListActivity.this,
								getString(R.string.code_not_exsit),
								Toast.LENGTH_SHORT).show();
					}
					search_et.setText(barcode);
					loadingdialog.dismiss();
					super.onPostExecute(result);
				}
			}.execute(0);

		}
	}

	// protected void init() {
	// // TODO Auto-generated method stub
	// search_et.setOnKeyListener(new OnKeyListener() {
	// public boolean onKey(View arg0, int keyCode, KeyEvent KeyEvent) {
	// if (keyCode == android.view.KeyEvent.KEYCODE_DEL) {
	// EditText edit = (EditText) arg0;
	// resetList(edit.getText().toString().trim());
	// }
	// return false;
	// }
	//
	// });
	//
	// search_et.addTextChangedListener(new TextWatcher() {
	// public void afterTextChanged(Editable s) {
	// String str = s.toString();
	// loadingdialog.show();
	// resetList(str);
	// loadingdialog.dismiss();
	// }
	//
	// public void beforeTextChanged(CharSequence s, int start, int count,
	// int after) {
	// }
	//
	// public void onTextChanged(CharSequence s, int start, int before,
	// int count) {
	// }
	// });
	// }

	protected void resetList(String trim) {
		// TODO Auto-generated method stub
		String sql = "SELECT code20 FROM orderCode_data WHERE orderID='"
				+ orderID + "' AND code20 LIKE '%" + trim + "%';";
		Cursor cur = db.rawQuery(sql, null);
		if (cur != null && cur.moveToFirst()) {
			code_arr = new ArrayList<String>();
			do {
				code_arr.add(cur.getString(cur.getColumnIndex("code20")));
			} while (cur.moveToNext());
			exsit = true;
		} else {
			// Toast.makeText(CoderListActivity.this,
			// getString(R.string.code_not_exsit), Toast.LENGTH_SHORT)
			// .show();
			exsit = false;
		}
	}

	protected void loadCodeInfo() {
		// TODO Auto-generated method stub
		String sql = "SELECT code20 FROM orderCode_data WHERE orderID='"
				+ orderID + "';";
		Cursor cur = db.rawQuery(sql, null);
		if (cur != null && cur.moveToFirst()) {
			code_arr = new ArrayList<String>();
			do {
				code_arr.add(cur.getString(cur.getColumnIndex("code20")));
			} while (cur.moveToNext());

		} else {
		}
	}

	public final class ViewHolder {
		public TextView codeText;
		public TextView codeNum;
	}

	private class MyAdapter extends BaseAdapter {
		private LayoutInflater inflater;

		public MyAdapter(Context context) {
			this.inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return code_arr.size();
		}

		@Override
		public Object getItem(int position) {
			return code_arr.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.code_list_item, null);
				holder = new ViewHolder();
				holder.codeText = (TextView) convertView
						.findViewById(R.id.codetv);
				holder.codeNum = (TextView) convertView
						.findViewById(R.id.code_num);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			int pos = position + 1;
			if (position < 9) {
				holder.codeNum.setText("0" + pos);
			} else {
				holder.codeNum.setText(pos + "");
			}
			holder.codeText.setText(code_arr.get(position));
			return convertView;
		}

	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View v, int position,
			long id) {
		// TODO Auto-generated method stub
		pos = position;
		Dialog dialog = null;
		CustomDialog.Builder customBuilder = new CustomDialog.Builder(
				CoderListActivity.this);
		customBuilder
				.setTitle("提示")
				.setMessage(
						getString(R.string.delete_message) + "\n"
								+ code_arr.get(position))
				.setNegativeButton(getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						})
				.setPositiveButton(getString(R.string.button_ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								m_db = CodeDBHelper
										.getInstance(CoderListActivity.this);
								m_db.delete_code(
										CodeDBHelper.ORDER_CODE_TABLE_NAME,
										code_arr.get(pos), orderID);
								code_arr.remove(pos);
								adapter = new MyAdapter(CoderListActivity.this);
								codeListView.setAdapter(adapter);
							}
						});
		dialog = customBuilder.create();
		dialog.show();
		return false;
	}

	@Override
	public void onBackPressed() {
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
				String sql = "SELECT * FROM order_data WHERE CorpOrderID='"
						+ order + "';";
				Cursor cur = db.rawQuery(sql, null);
				// bundle.putString("order_Id", OrderID);

				if (cur != null && cur.moveToFirst()) {
					createTime = cur
							.getString(cur.getColumnIndex("createTime"));
					Log.d("createTime", createTime);
					ToCorpID = cur.getString(cur.getColumnIndex("ToCorpID"));
					Log.d("ToCorpID", ToCorpID);
					String customsql = "SELECT * FROM customer_data WHERE customerID='"
							+ ToCorpID + "';";
					Cursor cur1 = db.rawQuery(customsql, null);
					if (cur1 != null && cur1.moveToFirst()) {
						customerName = cur1.getString(cur1
								.getColumnIndex("customerName"));
						customerInitial = cur1.getString(cur1
								.getColumnIndex("customerInitial"));
					}
				}
				return null;
			}

			protected void onPostExecute(String[] result) {
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putString("order_Id", order);
				bundle.putString("order_createTime", createTime);
				bundle.putString("customer_Name", customerName);
				bundle.putString("customer_Abbr", customerInitial);
				bundle.putString("customer_Code", ToCorpID);
				intent.putExtras(bundle);
				intent.setClass(CoderListActivity.this, SubmmitCode.class);
				CoderListActivity.this.finish();
				startActivity(intent);
				bm.removeListener(bl);
				bm.dismiss();

				db.close();
				loadingdialog.dismiss();
				super.onPostExecute(result);
			}
		}.execute(0);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.code_search:

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
					resetList(search_et.getText().toString().trim());
					return null;
				}

				protected void onPostExecute(String[] result) {
					// init();
					if (exsit) {
						adapter = new MyAdapter(CoderListActivity.this);
						codeListView.setAdapter(adapter);

					} else {
						Toast.makeText(CoderListActivity.this,
								getString(R.string.code_not_exsit),
								Toast.LENGTH_SHORT).show();
					}

					loadingdialog.dismiss();
					super.onPostExecute(result);
				}
			}.execute(0);

			break;
		default:
			break;
		}
	}
}
