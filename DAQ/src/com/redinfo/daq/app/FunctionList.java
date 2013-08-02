package com.redinfo.daq.app;

import java.util.ArrayList;
import java.util.HashMap;

import com.redinfo.daq.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FunctionList extends Activity implements OnItemClickListener {
	private ListView FunList = null;
	private MyAdapter adapter = null;
	public Dialog loadingdialog = null;
	private boolean ProduceWareHouseIn_CheckBoxPreference = false;
	private boolean ReturnWareHouseIn_CheckBoxPreference = false;
	private boolean PurchaseWareHouseIn_CheckBoxPreference = false;
	private boolean AllocateWareHouseIn_CheckBoxPreference = false;
	private boolean SalesWareHouseOut_CheckBoxPreference = false;
	private boolean DestoryWareHouseOut_CheckBoxPreference = false;
	private boolean CheckWareHouseOut_CheckBoxPreference = false;
	private boolean ReturnWareHouseOut_CheckBoxPreference = false;
	private boolean AllocateWareHouseOut_CheckBoxPreference = false;
	private ArrayList<HashMap<String, String>> FuncListResult = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.function_list);
		MyApplication.getInstance().addActivity(this);
		SharedPreferences sharedpreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		Functionenable[0] = sharedpreferences.getBoolean("ProduceWareHouseIn",
				false);
		Functionenable[1] = sharedpreferences.getBoolean("ReturnWareHouseIn",
				false);
		Functionenable[2] = sharedpreferences.getBoolean("PurchaseWareHouseIn",
				false);
		Functionenable[3] = sharedpreferences.getBoolean("AllocateWareHouseIn",
				false);
		Functionenable[4] = sharedpreferences.getBoolean("SalesWareHouseOut",
				false);
		Functionenable[5] = sharedpreferences.getBoolean("DestoryWareHouseOut",
				false);
		Functionenable[6] = sharedpreferences.getBoolean("CheckWareHouseOut",
				false);
		Functionenable[7] = sharedpreferences.getBoolean("ReturnWareHouseOut",
				false);
		Functionenable[8] = sharedpreferences.getBoolean(
				"AllocateWareHouseOut", false);
		loadingdialog = new Dialog(FunctionList.this, R.style.mmdialog);
		loadingdialog.setContentView(R.layout.loading_dialog);
		FunList = (ListView) findViewById(R.id.functionlist);
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
				return null;
			}

			protected void onPostExecute(String[] result) {
				loadingdialog.dismiss();
				adapter = new MyAdapter(FunctionList.this);
				FunList.setAdapter(adapter);
				super.onPostExecute(result);
			}
		}.execute(0);

		FunList.setOnItemClickListener(this);
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

	// private String[] FunctionStr = {
	// getString(R.string.produce_ware_house_in),
	// getString(R.string.return_ware_house_in),
	// getString(R.string.purchase_ware_house_in),
	// getString(R.string.allocate_ware_house_in),
	// getString(R.string.sales_ware_house_out),
	// getString(R.string.destory_ware_house_out),
	// getString(R.string.check_ware_house_out),
	// getString(R.string.return_ware_house_out),
	// getString(R.string.allocate_ware_house_out) };
	private Integer[] FunctionStr = { R.string.produce_ware_house_in,
			R.string.return_ware_house_in, R.string.purchase_ware_house_in,
			R.string.allocate_ware_house_in, R.string.sales_ware_house_out,
			R.string.destory_ware_house_out, R.string.check_ware_house_out,
			R.string.return_ware_house_out, R.string.allocate_ware_house_out };
	private boolean[] Functionenable = { ProduceWareHouseIn_CheckBoxPreference,
			ReturnWareHouseIn_CheckBoxPreference,
			PurchaseWareHouseIn_CheckBoxPreference,
			AllocateWareHouseIn_CheckBoxPreference,
			SalesWareHouseOut_CheckBoxPreference,
			DestoryWareHouseOut_CheckBoxPreference,
			CheckWareHouseOut_CheckBoxPreference,
			ReturnWareHouseOut_CheckBoxPreference,
			AllocateWareHouseOut_CheckBoxPreference };

	public final class ViewHolder {
		public TextView funcText;
	}

	private class MyAdapter extends BaseAdapter {
		private LayoutInflater inflater;

		public MyAdapter(Context context) {
			this.inflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return FuncListResult.size();
			// return FunctionStr.length;
		}

		@Override
		public Object getItem(int position) {
			return FuncListResult.get(position);
			// return FunctionStr[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater
						.inflate(R.layout.functionlist_item, null);
				holder = new ViewHolder();
				holder.funcText = (TextView) convertView
						.findViewById(R.id.functionText);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			// holder.funcText.setText(getString(FunctionStr[position]));
			holder.funcText.setText(FuncListResult.get(position).get("Func"));
			return convertView;
		}
	}

	@Override
	public void onBackPressed() {
		Intent noreply = new Intent();
		FunctionList.this.setResult(RESULT_CANCELED, noreply);
		FunctionList.this.finish();
		super.onBackPressed();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Intent FunctionIntent = new Intent();
		FunctionIntent.putExtra("POS",
				Integer.parseInt(FuncListResult.get(position).get("position")));
		FunctionList.this.setResult(RESULT_OK, FunctionIntent);
		FunctionList.this.finish();
	}
}
