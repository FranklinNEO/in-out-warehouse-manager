package com.redinfo.daq.app;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.redinfo.daq.R;
import com.redinfo.daq.data.CodeDBHelper;
import com.redinfo.daq.ui.CustomDialog;
import com.redinfo.daq.util.AbbrComparator;
import com.redinfo.daq.widget.SideBar;
import com.redinfo.daq.widget.SideBar.OnTouchingLetterChangedListener;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Loading extends Activity implements
		OnTouchingLetterChangedListener, OnItemClickListener,
		OnItemLongClickListener {

	private ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();
	public final static String URL = "/data/data/com.redinfo.daq/databases";
	public final static String DB_FILE_NAME = "info.db";

	SQLiteDatabase db = null;
	public CodeDBHelper m_db = null;
	private static final int MENU_FAVOR = Menu.FIRST;
	private MyAdapter adapter = null;
	private ListView list = null;
	private String Search = null;
	private SideBar sideBar;
	private TextView letterTv;
	private boolean isList = false;
	private boolean hasInfo = false;
	private String Names = "";
	private String Abbrs = "";
	private String Codes = "";
	public Dialog loadingdialog = null;
	private int orderCount;
	private int favoriateFlag;
	private boolean isChinese = false;
	private boolean isCapital = false;
	private boolean isLetter = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loading);
		MyApplication.getInstance().addActivity(this);

		m_db = CodeDBHelper.getInstance(Loading.this);
		File file = new File(URL, DB_FILE_NAME);
		db = SQLiteDatabase.openOrCreateDatabase(file, null);

		list = (ListView) findViewById(R.id.listView1);
		list.setOnItemClickListener(this);
		list.setOnItemLongClickListener(this);
		letterTv = (TextView) findViewById(R.id.letterTv);
		loadingdialog = new Dialog(Loading.this, R.style.mmdialog);
		loadingdialog.setContentView(R.layout.loading_dialog);
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
				loadLocationFile();
				return null;
			}

			@SuppressWarnings("unchecked")
			protected void onPostExecute(String[] result) {
				loadingdialog.dismiss();
				Collections.sort(data, new AbbrComparator());
				ListView list = (ListView) findViewById(R.id.listView1);
				adapter = new MyAdapter(Loading.this);
				list.setAdapter(adapter);
				super.onPostExecute(result);
			}
		}.execute(0);

		sideBar = (SideBar) findViewById(R.id.mySideBar);
		sideBar.setOnTouchingLetterChangedListener(this);
		Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				EditText edit = (EditText) findViewById(R.id.editText1);
				Search = edit.getText().toString().trim();
				if (Search == null || Search.equals("")) {
					// Toast.makeText(
					// Loading.this,
					// getString(R.string.please_enter_customer_name_or_abrr),
					// Toast.LENGTH_SHORT).show();

					// TODO Auto-generated method stub
					String sql = "SELECT * FROM customer_data" + ";";
					Cursor cur = db.rawQuery(sql, null);
					data = new ArrayList<HashMap<String, String>>();
					if (cur != null && cur.moveToFirst()) {
						hasInfo = true;
						do {
							HashMap<String, String> map = new HashMap<String, String>();
							map.put("name", cur.getString(cur
									.getColumnIndex("customerName")));
							map.put("abbr", cur.getString(cur
									.getColumnIndex("customerInitial")));
							map.put("code", cur.getString(cur
									.getColumnIndex("customerID")));
							data.add(map);
						} while ((cur.moveToNext()));
					} else {
						hasInfo = false;
						// Toast.makeText(getApplicationContext(),
						// getString(R.string.find_no_customer),
						// Toast.LENGTH_SHORT)
						// .show();

					}
					ListView list = (ListView) findViewById(R.id.listView1);
					adapter = new MyAdapter(Loading.this);
					list.setAdapter(adapter);
				} else {
					new AsyncTask<Integer, Integer, String[]>() {

						protected void onPreExecute() {
							super.onPreExecute();
						}

						@Override
						protected void onCancelled() {
							super.onCancelled();
						}

						protected String[] doInBackground(Integer... params) {
							Searchfile(Search);
							return null;
						}

						@SuppressWarnings("unchecked")
						protected void onPostExecute(String[] result) {
							Collections.sort(data, new AbbrComparator());
							ListView list = (ListView) findViewById(R.id.listView1);
							adapter = new MyAdapter(Loading.this);
							list.setAdapter(adapter);
							if (!hasInfo) {
								Toast.makeText(Loading.this,
										getString(R.string.find_no_customer),
										Toast.LENGTH_SHORT).show();
							}
							super.onPostExecute(result);
						}
					}.execute(0);
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_FAVOR, 0, getString(R.string.favor_list));
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_FAVOR:
			Intent intent = new Intent();
			intent.setClass(Loading.this, Favoriate.class);
			startActivity(intent);
			db.close();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected void Searchfile(String sear) {
		String judge = sear.substring(0, 1);
		isChinese = judge.matches("[\u4E00-\u9FA5]");
		isCapital = judge.matches("[A-Z]");
		isLetter = judge.matches("[a-z]");
		SearchJudge(sear, isChinese, isCapital, isLetter);

	}

	private void SearchJudge(String present, boolean Chinese, boolean Capital,
			boolean Letter) {
		// TODO Auto-generated method stub
		File file = new File(URL, DB_FILE_NAME);
		db = SQLiteDatabase.openOrCreateDatabase(file, null);
		if (Chinese) {
			String sql = "SELECT * FROM customer_data WHERE customerName LIKE '%"
					+ present + "%';";
			SetSearchResult(sql);
		} else if (Capital || Letter) {
			String sql = "SELECT * FROM customer_data WHERE customerInitial LIKE '%"
					+ present + "%';";
			SetSearchResult(sql);
		} else {
		}
	}

	private void SetSearchResult(String sql) {
		// TODO Auto-generated method stub
		Cursor cur = db.rawQuery(sql, null);
		data = new ArrayList<HashMap<String, String>>();
		if (cur != null && cur.moveToFirst()) {
			hasInfo = true;
			do {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("name",
						cur.getString(cur.getColumnIndex("customerName")));
				map.put("abbr",
						cur.getString(cur.getColumnIndex("customerInitial")));
				map.put("code", cur.getString(cur.getColumnIndex("customerID")));
				data.add(map);
			} while ((cur.moveToNext()));
		} else {
			hasInfo = false;
			// Toast.makeText(getApplicationContext(),
			// getString(R.string.find_no_customer), Toast.LENGTH_SHORT)
			// .show();

		}

	}

	protected void loadLocationFile() {

		String sql = "SELECT * FROM customer_data;";
		Cursor cur = db.rawQuery(sql, null);
		data = new ArrayList<HashMap<String, String>>();
		if (cur != null && cur.moveToFirst()) {
			do {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("name",
						cur.getString(cur.getColumnIndex("customerName")));
				map.put("abbr",
						cur.getString(cur.getColumnIndex("customerInitial")));
				map.put("code", cur.getString(cur.getColumnIndex("customerID")));
				map.put("favor", cur.getInt(cur.getColumnIndex("favoriteFlag"))
						+ "");
				data.add(map);
			} while ((cur.moveToNext()));
		} else {

		}

	}

	public final class ViewHolder {
		public TextView nameTextView;
		public TextView catalog;
	}

	private class MyAdapter extends BaseAdapter {
		private LayoutInflater inflater;

		public MyAdapter(Context context) {
			this.inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.listitem, null);
				holder = new ViewHolder();
				holder.nameTextView = (TextView) convertView
						.findViewById(R.id.textView1);
				holder.catalog = (TextView) convertView
						.findViewById(R.id.catalogTv);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			String catalog = (data.get(position).get("abbr"));
			if (catalog == null || catalog.equals("")) {
				catalog = "#";
			} else {
				catalog = catalog.substring(0, 1);
			}
			if (isList) {
				holder.catalog.setVisibility(View.GONE);
			} else {
				if (position == 0) {
					holder.catalog.setVisibility(View.VISIBLE);
					holder.catalog.setText(catalog);
				} else {
					String lastCatalog = data.get(position - 1).get("abbr");
					if (lastCatalog == null || lastCatalog.equals("")) {
						lastCatalog = "#";
					} else {
						lastCatalog = lastCatalog.substring(0, 1);
					}
					if (catalog.equals(lastCatalog)) {
						holder.catalog.setVisibility(View.GONE);
					} else {
						holder.catalog.setVisibility(View.VISIBLE);
						holder.catalog.setText(catalog);
					}
				}
			}
			holder.nameTextView.setText(data.get(position).get("name"));
			return convertView;
		}
	}

	private Handler _handler = new Handler();
	private Runnable letterThread = new Runnable() {
		public void run() {
			letterTv.setVisibility(View.GONE);
		}
	};

	@Override
	public void onTouchingLetterChanged(String s) {
		letterTv.setText(s);
		letterTv.setVisibility(View.VISIBLE);
		_handler.removeCallbacks(letterThread);
		_handler.postDelayed(letterThread, 1000);
		if (alphaIndexer(s) > 0) {
			int position = alphaIndexer(s);
			list.setSelection(position);
		}
	}

	private int alphaIndexer(String s) {
		int position = 0;
		for (int i = 0; i < data.size(); i++) {

			String py = (String) data.get(i).get("abbr");
			if (py.startsWith(s)) {
				position = i;
				break;
			}
		}
		return position;
	}

	@Override
	public void onBackPressed() {
		Intent noneIntent = new Intent();
		Loading.this.setResult(RESULT_CANCELED, noneIntent);
		Loading.this.finish();
		db.close();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Names = data.get(position).get("name");
		Abbrs = data.get(position).get("abbr");
		Codes = data.get(position).get("code");
		Dialog dialog = null;
		CustomDialog.Builder customBuilder = new CustomDialog.Builder(
				Loading.this);
		customBuilder
				.setTitle(getString(R.string.detail_info))
				.setMessage(
						getString(R.string.customer_name) + ":\n" + Names
								+ "\n" + getString(R.string.customer_name_abbr)
								+ ":\n" + Abbrs + "\n"
								+ getString(R.string.customer_code) + ":\n"
								+ Codes)
				.setNegativeButton(getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						})
				.setPositiveButton(getString(R.string.select_customer),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								Intent dataIntent = new Intent();
								dataIntent.putExtra("data_name", Names);
								dataIntent.putExtra("data_abbr", Abbrs);
								dataIntent.putExtra("data_code", Codes);
								Loading.this.setResult(RESULT_OK, dataIntent);
								Loading.this.finish();
								db.close();
							}
						});
		dialog = customBuilder.create();
		dialog.show();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View v, int position,
			long id) {
		// TODO Auto-generated method stub
		Names = data.get(position).get("name");
		Abbrs = data.get(position).get("abbr");
		Codes = data.get(position).get("code");
		Dialog dialog = null;
		CustomDialog.Builder customBuilder = new CustomDialog.Builder(
				Loading.this);
		customBuilder
				.setTitle(getString(R.string.favor_title))
				.setMessage(getString(R.string.favor_question) + "\n" + Names)
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
								File file = new File(URL, DB_FILE_NAME);
								db = SQLiteDatabase.openOrCreateDatabase(file,
										null);
								// 查询数据库是否存在此客户
								String custom_sql = "SELECT * FROM customer_data WHERE customerID='"
										+ Codes + "';";
								Cursor custom = db.rawQuery(custom_sql, null);
								if (custom.moveToFirst() != true) {
									m_db.insert_customer(
											CodeDBHelper.CUSTOMER_TABEL_NAME,
											Names, Abbrs, 0, 1, Codes);
								}
								custom.close();
								// 查询客户被选择的次数
								String count_sql = "SELECT orderCount FROM customer_data WHERE customerID='"
										+ Codes + "';";
								Cursor Count = db.rawQuery(count_sql, null);
								if (Count.moveToFirst() == true) {

									if (Count != null && Count.moveToFirst()) {
										do {
											orderCount = Count.getInt(Count
													.getColumnIndex("orderCount"));
										} while ((Count.moveToNext()));
									} else {
										orderCount = 0;
									}
								}
								Count.close();
								// 查询客户是否被收藏
								String favor_sql = "SELECT favoriteFlag FROM customer_data WHERE customerID='"
										+ Codes + "';";
								Cursor favor = db.rawQuery(favor_sql, null);
								if (favor != null && favor.moveToFirst()) {
									do {
										favoriateFlag = favor.getInt(favor
												.getColumnIndex("favoriteFlag"));
									} while ((favor.moveToNext()));
								} else {
									favoriateFlag = 0;
								}
								favor.close();
								if (favoriateFlag != 1) {
									// 更新客户信息
									m_db.update_customer(
											CodeDBHelper.CUSTOMER_TABEL_NAME,
											Names, Abbrs, orderCount, 1, Codes);
								}
							}
						});
		dialog = customBuilder.create();
		dialog.show();
		return false;
	}

}
