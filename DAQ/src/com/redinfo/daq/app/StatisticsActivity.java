package com.redinfo.daq.app;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.redinfo.daq.R;
import com.redinfo.daq.data.CodeDBHelper;
import com.redinfo.daq.xml.OrderproductList;

public class StatisticsActivity extends Activity {
	public final static String URL = "/data/data/com.redinfo.daq/databases";
	public final static String DB_FILE_NAME = "info.db";
	SQLiteDatabase db = null;
	public CodeDBHelper m_db = null;
	private int orderID = -1;
	private ListView statisticsLv = null;
	private statisticsAdapter adapter = null;
	private OrderproductList opl = null;
	private ArrayList<OrderproductList> productList = null;
	private ArrayList<HashMap<Integer, Integer>> productNumInfo = null;
	public Dialog loadingdialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.statistics);
		Bundle bundle = this.getIntent().getExtras();
		m_db = CodeDBHelper.getInstance(StatisticsActivity.this);
		File file = new File(URL, DB_FILE_NAME);
		db = SQLiteDatabase.openOrCreateDatabase(file, null);
		TextView orderText = (TextView) findViewById(R.id.showorder);
		TextView customerText = (TextView) findViewById(R.id.showcustomer);
		if (bundle != null) {
			getOrderID(bundle.getString("order_Id"));
			// orderID = Integer.parseInt(bundle.getString("order_Id"));
			Log.d("orderID", orderID + "");
			orderText.setText(getString(R.string.order_id) + ":	"
					+ bundle.getString("order_Id"));
			customerText.setText(getString(R.string.customer) + ":	"
					+ bundle.getString("customer"));
		} else {

		}
		loadingdialog = new Dialog(StatisticsActivity.this, R.style.mmdialog);
		loadingdialog.setContentView(R.layout.loading_dialog);
		statisticsLv = (ListView) findViewById(R.id.stattistics_lv);
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
				m_db.delete_statistics(CodeDBHelper.CODE_STATISTICS_TABEL_NAME);
				InsertCodeInfo(orderID);
				loadOrderInfo(orderID);
				return null;
			}

			protected void onPostExecute(String[] result) {
				adapter = new statisticsAdapter(StatisticsActivity.this);
				statisticsLv.setAdapter(adapter);
				loadingdialog.dismiss();
				super.onPostExecute(result);
			}
		}.execute(0);

	}

	private int getOrderID(String OrderID) {
		// TODO Auto-generated method stub
		String sql = "SELECT _id FROM order_data WHERE CorpOrderID='" + OrderID
				+ "';";
		Cursor cur = db.rawQuery(sql, null);
		if (cur != null && cur.moveToFirst()) {
			do {
				orderID = cur.getInt(cur.getColumnIndex("_id"));
			} while ((cur.moveToNext()));
		}
		cur.close();
		return orderID;
	}

	protected void loadOrderInfo(int orderID) {
		// TODO Auto-generated method stub
		String sql = "SELECT * FROM codeStatistics_data Where orderID='"
				+ orderID + "';";
		Cursor cur = db.rawQuery(sql, null);
		if (cur != null && cur.moveToFirst()) {
			productList = new ArrayList<OrderproductList>();
			do {
				int productID = cur.getInt(cur.getColumnIndex("productID"));
				opl = new OrderproductList();
				setproductInfo(opl, productID);
				// String productsql = "SELECT * FROM products_data WHERE _id'"
				// + productID + "';";
				// Cursor cur1 = db.rawQuery(productsql, null);
				// if (cur1 != null && cur1.moveToFirst()) {
				// opl.setproductName(cur1.getString(cur1
				// .getColumnIndex("productName")));
				// opl.setpackUnit(cur1.getString(cur1
				// .getColumnIndex("packunit")));
				// opl.setpackageSpec(cur1.getString(cur1
				// .getColumnIndex("packageSpec")));
				// opl.setspec(cur1.getString(cur1.getColumnIndex("spec")));
				// opl.settype(cur1.getString(cur1.getColumnIndex("type")));
				// opl.setpkgRatio(cur1.getString(cur1
				// .getColumnIndex("pkgRatio")));
				// }

				opl.setnum1(cur.getInt(cur.getColumnIndex("codeNum1")));
				opl.setnum2(cur.getInt(cur.getColumnIndex("codeNum2")));
				opl.setnum3(cur.getInt(cur.getColumnIndex("codeNum3")));
				String ratio = opl.getpkgRatio();
				char[] ratio_arr = ratio.toCharArray();
				int flag = 0;
				for (int i = 0; i < ratio_arr.length; i++) {
					if (ratio_arr[i] == ':') {
						flag++;
					}
				}
				if (flag == 1) {
					String level1 = ratio.split(":")[0];
					String level2 = ratio.split(":")[1];
					opl.setsum(opl.getnum1()
							+ opl.getnum2()
							* (Integer.parseInt(level2) / Integer
									.parseInt(level1)));
				} else if (flag == 2) {
					String level1 = ratio.split(":")[0];
					String level2 = ratio.split(":")[1];
					String level3 = ratio.split(":")[2];
					opl.setsum(opl.getnum1()
							+ opl.getnum2()
							* (Integer.parseInt(level3) / Integer
									.parseInt(level2))
							+ opl.getnum3()
							* (Integer.parseInt(level3) / Integer
									.parseInt(level1)));
				} else {

				}
				productList.add(opl);
			} while (cur.moveToNext());
		} else {
			productList = new ArrayList<OrderproductList>();
		}
	}

	private void setproductInfo(OrderproductList opl2, int productID) {
		// TODO Auto-generated method stub
		String productsql = "SELECT * FROM products_data WHERE productID='"
				+ productID + "';";
		Log.d("sql", productsql);
		Cursor cur1 = db.rawQuery(productsql, null);
		if (cur1 != null && cur1.moveToFirst()) {
			do {
				opl2.setproductName(cur1.getString(cur1
						.getColumnIndex("productName")));
				opl2.setpackUnit(cur1.getString(cur1.getColumnIndex("packUnit")));
				opl2.setpackageSpec(cur1.getString(cur1
						.getColumnIndex("packageSpec")));
				opl2.setspec(cur1.getString(cur1.getColumnIndex("spec")));
				opl2.settype(cur1.getString(cur1.getColumnIndex("type")));
				opl2.setpkgRatio(cur1.getString(cur1
						.getColumnIndex("packRatio")));
			} while (cur1.moveToNext());
		}
	}

	protected void InsertCodeInfo(int orderID) {
		// TODO Auto-generated method stub
		String sql = "SELECT productID,codeVersion,resCode,count(*) AS codeNum FROM orderCode_data WHERE orderID='"
				+ orderID + "'GROUP BY resCode,codeVersion;";
		Cursor cur = db.rawQuery(sql, null);
		if (cur != null && cur.moveToFirst()) {
			do {
				int productID = cur.getInt(cur.getColumnIndex("productID"));
				String codeVersion = cur.getString(cur
						.getColumnIndex("codeVersion"));
				String resCode = cur.getString(cur.getColumnIndex("resCode"));
				String codeLevel = getCodeLevel(codeVersion, resCode);
				Log.d("codelevel", codeLevel);
				int codeNum = cur.getInt(cur.getColumnIndex("codeNum"));
				int cl = Integer.parseInt(codeLevel);
				switch (cl) {
				case 1:
					insertnum1(productID, orderID, codeNum);
					break;
				case 2:
					insertnum2(productID, orderID, codeNum);
					break;
				case 3:
					insertnum3(productID, orderID, codeNum);
					break;
				default:
					break;
				}
				// m_db.insert_codestatistics(
				// CodeDBHelper.CODE_STATISTICS_TABEL_NAME, codeLevel,
				// productID, orderID, codeNum);
			} while (cur.moveToNext());
		} else {
		}

	}

	private void insertnum3(int productID, int orderID2, int codeNum) {
		// TODO Auto-generated method stub
		String sql = "SELECT codeNum1,codeNum2,codeNum3 FROM codeStatistics_data WHERE productID='"
				+ productID + "' AND orderID='" + orderID2 + "';";
		Cursor cur = db.rawQuery(sql, null);
		if (cur != null && cur.moveToFirst()) {
			int code1 = cur.getInt(cur.getColumnIndex("codeNum1"));
			int code2 = cur.getInt(cur.getColumnIndex("codeNum2"));
			int code3 = cur.getInt(cur.getColumnIndex("codeNum3"));
			Log.d("update", "3");
			Log.d("code1", code1 + "");
			Log.d("code2", code2 + "");
			Log.d("code3", code3 + "");
			m_db.update_codestatistics(CodeDBHelper.CODE_STATISTICS_TABEL_NAME,
					productID, orderID2, code1, code2, codeNum);

		} else {
			Log.d("insert", "3");
			m_db.insert_codestatistics(CodeDBHelper.CODE_STATISTICS_TABEL_NAME,
					productID, orderID2, 0, 0, codeNum);
		}
	}

	private void insertnum2(int productID, int orderID2, int codeNum) {
		// TODO Auto-generated method stub
		String sql = "SELECT codeNum1,codeNum2,codeNum3 FROM codeStatistics_data WHERE productID='"
				+ productID + "' AND orderID='" + orderID2 + "';";
		Cursor cur = db.rawQuery(sql, null);
		if (cur != null && cur.moveToFirst()) {

			int code1 = cur.getInt(cur.getColumnIndex("codeNum1"));
			int code2 = cur.getInt(cur.getColumnIndex("codeNum2"));
			int code3 = cur.getInt(cur.getColumnIndex("codeNum3"));
			Log.d("update", "2");
			Log.d("code1", code1 + "");
			Log.d("code2", code2 + "");
			Log.d("code3", code3 + "");
			m_db.update_codestatistics(CodeDBHelper.CODE_STATISTICS_TABEL_NAME,
					productID, orderID2, code1, codeNum, code3);

		} else {
			Log.d("insert", "2");
			m_db.insert_codestatistics(CodeDBHelper.CODE_STATISTICS_TABEL_NAME,
					productID, orderID2, 0, codeNum, 0);
		}
	}

	private void insertnum1(int productID, int orderID2, int codeNum) {
		// TODO Auto-generated method stub
		String sql = "SELECT codeNum1,codeNum2,codeNum3 FROM codeStatistics_data WHERE productID='"
				+ productID + "' AND orderID='" + orderID2 + "';";
		Cursor cur = db.rawQuery(sql, null);
		if (cur != null && cur.moveToFirst()) {
			int code1 = cur.getInt(cur.getColumnIndex("codeNum1"));
			int code2 = cur.getInt(cur.getColumnIndex("codeNum2"));
			int code3 = cur.getInt(cur.getColumnIndex("codeNum3"));
			Log.d("update", "1");
			Log.d("code1", code1 + "");
			Log.d("code2", code2 + "");
			Log.d("code3", code3 + "");
			m_db.update_codestatistics(CodeDBHelper.CODE_STATISTICS_TABEL_NAME,
					productID, orderID2, codeNum, code2, code3);
		} else {
			Log.d("insert", "1");
			m_db.insert_codestatistics(CodeDBHelper.CODE_STATISTICS_TABEL_NAME,
					productID, orderID2, codeNum, 0, 0);
		}
	}

	private String getCodeLevel(String codeVersion, String resCode) {
		// TODO Auto-generated method stub
		String sql = "SELECT codeLevel FROM products_data WHERE codeVersion='"
				+ codeVersion + "' AND resCode='" + resCode + "';";
		Cursor cur = db.rawQuery(sql, null);
		if (cur != null && cur.moveToFirst()) {
			return cur.getString(cur.getColumnIndex("codeLevel"));
		} else {

			return null;
		}
	}

	public final class ViewHolder {
		public TextView productNameTv;
		public TextView typeTV;
		public TextView packageSpecTv;
		public TextView pkgRatio;
		public TextView codeLevelTv;
		public TextView sumTv;

	}

	// 自定义条码数据容纳器
	private class statisticsAdapter extends BaseAdapter {
		private LayoutInflater inflater;

		public statisticsAdapter(Context context) {
			this.inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return productList.size();
		}

		@Override
		public Object getItem(int position) {
			return productList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.product_listitem, null);
				holder = new ViewHolder();
				holder.productNameTv = (TextView) convertView
						.findViewById(R.id.productNameTv);
				holder.typeTV = (TextView) convertView
						.findViewById(R.id.typeTv);
				holder.packageSpecTv = (TextView) convertView
						.findViewById(R.id.packageSpecTv);
				holder.pkgRatio = (TextView) convertView
						.findViewById(R.id.pkgRatioTv);
				holder.codeLevelTv = (TextView) convertView
						.findViewById(R.id.codeLevelInfoTv);
				holder.sumTv = (TextView) convertView.findViewById(R.id.sumTv);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.productNameTv.setText(productList.get(position)
					.getproductName());
			holder.typeTV.setText(productList.get(position).gettype());
			holder.packageSpecTv.setText(productList.get(position)
					.getpackageSpec());
			holder.pkgRatio.setText(productList.get(position).getpkgRatio());
			holder.codeLevelTv.setText(getString(R.string.code_level1) + ":"
					+ productList.get(position).getnum1() + ";	"
					+ getString(R.string.code_level2) + ":"
					+ productList.get(position).getnum2() + ";	"
					+ getString(R.string.code_level3) + ":"
					+ productList.get(position).getnum3());
			holder.sumTv.setText(productList.get(position).getsum() + "	"
					+ getString(R.string.unit));
			return convertView;
		}
	}
}
