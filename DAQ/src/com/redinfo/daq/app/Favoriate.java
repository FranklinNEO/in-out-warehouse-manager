package com.redinfo.daq.app;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.redinfo.daq.R;
import com.redinfo.daq.data.CodeDBHelper;
import com.redinfo.daq.ui.CustomDialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Favoriate extends Activity implements OnItemClickListener {
	public final static String URL = "/data/data/com.redinfo.daq/databases";
	public final static String DB_FILE_NAME = "info.db";
	SQLiteDatabase db = null;
	public CodeDBHelper m_db = null;
	private ListView FavorList = null;
	private FavorAdapter madapter = null;
	private ArrayList<HashMap<String, String>> favordata = new ArrayList<HashMap<String, String>>();
	private ArrayList<Integer> count = new ArrayList<Integer>();
	private int pos;
	public Dialog loadingdialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.favor);
		MyApplication.getInstance().addActivity(this);

		m_db = CodeDBHelper.getInstance(Favoriate.this);
		File file = new File(URL, DB_FILE_NAME);
		db = SQLiteDatabase.openOrCreateDatabase(file, null);

		FavorList = (ListView) findViewById(R.id.favorlist);
		loadingdialog = new Dialog(Favoriate.this, R.style.mmdialog);
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
				String favor_sql = "SELECT * FROM customer_data WHERE favoriteFlag=1;";
				Cursor favor = db.rawQuery(favor_sql, null);
				favordata = new ArrayList<HashMap<String, String>>();
				if (favor != null && favor.moveToFirst()) {
					do {
						HashMap<String, String> map = new HashMap<String, String>();
						map.put("name", favor.getString(favor
								.getColumnIndex("customerName")));
						map.put("abbr", favor.getString(favor
								.getColumnIndex("customerInitial")));
						map.put("id", favor.getString(favor
								.getColumnIndex("customerID")));
						favordata.add(map);
						count.add(favor.getInt(favor
								.getColumnIndex("orderCount")));
					} while ((favor.moveToNext()));
				}
				favor.close();
				return null;
			}

			protected void onPostExecute(String[] result) {
				loadingdialog.dismiss();
				madapter = new FavorAdapter(Favoriate.this);
				FavorList.setAdapter(madapter);
				super.onPostExecute(result);
			}
		}.execute(0);
		FavorList.setOnItemClickListener(this);
	}

	public final class ViewHolder {
		public TextView favorText;
		public TextView numText;
	}

	private class FavorAdapter extends BaseAdapter {
		private LayoutInflater inflater;

		public FavorAdapter(Context context) {
			this.inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return favordata.size();
		}

		@Override
		public Object getItem(int position) {
			return favordata.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.favor_list, null);
				holder = new ViewHolder();
				holder.favorText = (TextView) convertView
						.findViewById(R.id.favorTv);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.favorText.setText(favordata.get(position).get("name"));
			return convertView;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		// TODO Auto-generated method stub
		Dialog dialog = null;
		pos = position;
		CustomDialog.Builder customBuilder = new CustomDialog.Builder(
				Favoriate.this);
		customBuilder
				.setTitle(getString(R.string.favor_cancel))
				.setMessage(getString(R.string.favor_message))
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
								m_db.update_customer(
										CodeDBHelper.CUSTOMER_TABEL_NAME,
										favordata.get(pos).get("name"),
										favordata.get(pos).get("abbr"), count
												.get(pos), 0, favordata
												.get(pos).get("id"));
								favordata.remove(pos);
								madapter = new FavorAdapter(Favoriate.this);
								FavorList.setAdapter(madapter);
							}
						});
		dialog = customBuilder.create();
		dialog.show();
	}
}
