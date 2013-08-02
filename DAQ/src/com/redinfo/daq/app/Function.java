package com.redinfo.daq.app;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.redinfo.daq.R;
import com.redinfo.daq.data.CodeDBHelper;
import com.redinfo.daq.ui.CustomDialog;
import com.redinfo.daq.util.ExportXML;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Function extends Activity implements OnItemClickListener,
		OnClickListener {
	public final static String URL = "/data/data/com.redinfo.daq/databases";
	public final static String DB_FILE_NAME = "info.db";
	public CodeDBHelper m_db = null;
	public SQLiteDatabase db;
	public Dialog loadingdialog = null;
	private Button funcBtn = null;
	private static final int FUNCTION_INTENT_REQ_CODE = 0x967;
	private static final int MENU_LOGOUT = Menu.FIRST;
	private Integer[] mThumbIds = { R.drawable.shipping, R.drawable.order,
			R.drawable.maintenance };
	private String Func;
	// private String[] GridItem = {
	// this.getResources().getString(R.string.ware_house_type),
	// this.getResources().getString(R.string.order_manager),
	// this.getResources().getString(R.string.sys_setting) };
	// private String[] FuncTxt = {
	// this.getResources().getString(R.string.produce_ware_house_in),
	// this.getResources().getString(R.string.return_ware_house_in),
	// this.getResources().getString(R.string.purchase_ware_house_in),
	// this.getResources().getString(R.string.allocate_ware_house_in),
	// this.getResources().getString(R.string.sales_ware_house_out),
	// this.getResources().getString(R.string.destory_ware_house_out),
	// this.getResources().getString(R.string.check_ware_house_out),
	// this.getResources().getString(R.string.return_ware_house_out),
	// this.getResources().getString(R.string.allocate_ware_house_out) };
	private Integer[] GridItem = { R.string.ware_house_type,
			R.string.order_manager, R.string.sys_setting };
	private Integer[] FuncTxt = { R.string.produce_ware_house_in,
			R.string.return_ware_house_in, R.string.purchase_ware_house_in,
			R.string.allocate_ware_house_in, R.string.sales_ware_house_out,
			R.string.destory_ware_house_out, R.string.check_ware_house_out,
			R.string.return_ware_house_out, R.string.allocate_ware_house_out

	};
	private File customerFile = null;
	private File productFile = null;
	private static boolean hasproduct = false;
	private static boolean hascustomer = false;
	private int flag = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.function);
		MyApplication.getInstance().addActivity(this);
		GridView gridView = (GridView) findViewById(R.id.gridView);
		gridView.setAdapter(new ImageAdapter(this));
		gridView.setOnItemClickListener(this);
		funcBtn = (Button) findViewById(R.id.function);
		funcBtn.setOnClickListener(this);
		loadingdialog = new Dialog(Function.this, R.style.mmdialog);
		loadingdialog.setContentView(R.layout.loading_dialog);
		// 验证功能选项
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
		if (flag == 100) {
			funcBtn.setText(getString(R.string.title_bar_name));
			Toast.makeText(Function.this,
					getString(R.string.please_select_ware_house_type),
					Toast.LENGTH_SHORT).show();
			funcBtn.setEnabled(false);
		} else {
			funcBtn.setText(getString(FuncTxt[flag]));
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
				CheckFile();
				return null;
			}

			protected void onPostExecute(String[] result) {
				if (!hasproduct) {
					Toast.makeText(Function.this,
							getString(R.string.please_import_product_file),
							Toast.LENGTH_SHORT).show();
				}
				if (!hascustomer) {
					Toast.makeText(Function.this,
							getString(R.string.please_import_customer_file),
							Toast.LENGTH_SHORT).show();
				}
				if (hascustomer && hasproduct) {
					funcBtn.setEnabled(true);
				} else {
					funcBtn.setEnabled(false);
				}
				loadingdialog.dismiss();
				super.onPostExecute(result);
			}
		}.execute(0);

	}

	protected void CheckFile() {
		// TODO Auto-generated method stub
		m_db = CodeDBHelper.getInstance(Function.this);
		db = SQLiteDatabase.openOrCreateDatabase(new File(URL, DB_FILE_NAME),
				null);
		File sdCardDir = Environment.getExternalStorageDirectory();
		productFile = new File(sdCardDir, "/RedInfo/products.xml");
		customerFile = new File(sdCardDir, "/RedInfo/Customers.txt");
		if (productFile.exists()) {
			loadproduct();
			hasproduct = true;
			MoveProductFile();
			productFile.delete();
		} else {
			// if (!checkproduct()) {
			// hasproduct = false;
			// } else {
			// hasproduct = true;
			// }
			hasproduct = checkproduct();
		}

		if (customerFile.exists()) {
			loadcustomer();
			hascustomer = true;
			MoveCustomerFile();
			customerFile.delete();
		} else {
			// if (!checkcustomer()) {
			// hascustomer = false;
			// } else {
			// hascustomer = true;
			// }
			hascustomer = checkcustomer();
		}
	}

	private void MoveCustomerFile() {
		// TODO Auto-generated method stub
		File sdCardDir = Environment.getExternalStorageDirectory();
		File destDir = new File(sdCardDir, "/RedInfo/bak/");
		if (!destDir.exists()) {
			destDir.mkdirs();
		}

		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String date = sDateFormat.format(new java.util.Date());
		FileInputStream fis = null;
		FileOutputStream fos = null;
		Log.d("date", date);
		File destFile = new File(sdCardDir, "/RedInfo/bak/customers_" + date
				+ ".txt");
		if (!destFile.exists()) {
			try {
				destFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			fis = new FileInputStream(customerFile);
			fos = new FileOutputStream(destFile);
			// byte[] buffer = new byte[1024];
			// while (fis.read(buffer) != -1) {
			// fos.write(buffer);
			// }
			try {
				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = fis.read(buffer)) >= 0) {
					fos.write(buffer, 0, bytesRead);
				}
			} finally {
				fos.flush();
				try {
					fos.getFD().sync();
				} catch (IOException e) {
				}
				fos.close();
			}
			// fos.flush();
			// fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		}
	}

	private void MoveProductFile() {
		// TODO Auto-generated method stub
		File sdCardDir = Environment.getExternalStorageDirectory();
		File destDir = new File(sdCardDir, "/RedInfo/bak/");
		if (!destDir.exists()) {
			destDir.mkdirs();
		}

		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String date = sDateFormat.format(new java.util.Date());
		FileInputStream fis = null;
		FileOutputStream fos = null;
		Log.d("date", date);
		File destFile = new File(sdCardDir, "/RedInfo/bak/products_" + date
				+ ".xml");
		if (!destFile.exists()) {
			try {
				destFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			fis = new FileInputStream(productFile);
			fos = new FileOutputStream(destFile);
			// byte[] buffer = new byte[1024];
			// while (fis.read(buffer) != -1) {
			// fos.write(buffer);
			// }
			try {
				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = fis.read(buffer)) >= 0) {
					fos.write(buffer, 0, bytesRead);
				}
			} finally {
				fos.flush();
				try {
					fos.getFD().sync();
				} catch (IOException e) {
				}
				fos.close();
			}
			// fos.flush();
			// fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		}
	}

	private boolean checkcustomer() {
		String sql = "SELECT * FROM customer_data;";
		Cursor cur = db.rawQuery(sql, null);
		if (cur != null && cur.moveToFirst()) {
			Log.d("customer", "true");
			return true;
		} else {
			return false;
		}
		// TODO Auto-generated method stub
	}

	private boolean checkproduct() {
		String sql = "SELECT * FROM products_data;";
		Cursor cur = db.rawQuery(sql, null);
		if (cur != null && cur.moveToFirst()) {
			return true;
		} else {
			return false;
		}
		// TODO Auto-generated method stub

	}

	// 载入产品信息
	private void loadproduct() {
		if (checkproduct()) {
			m_db.delete_product(CodeDBHelper.PRODUCTS_TABEL_NAME);
		} else {

		}
		LoadProductXML();
	}

	private void LoadProductXML() {
		// TODO Auto-generated method stub
		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dombuilder = domfac.newDocumentBuilder();
			InputStream is = new FileInputStream(productFile);
			Document doc = dombuilder.parse(is);
			Element root = doc.getDocumentElement();
			NodeList products = root.getChildNodes();
			if (products != null) {
				for (int i = 0; i < products.getLength(); i++) {
					Node product = products.item(i);

					if (product.getNodeType() == Node.ELEMENT_NODE) {

						NodeList subTypes = product.getChildNodes();
						if (subTypes != null) {
							for (int j = 0; j < subTypes.getLength(); j++) {
								Node subType = subTypes.item(j);
								if (subType.getNodeType() == Node.ELEMENT_NODE) {

									NodeList resProdCodes = subType
											.getChildNodes();
									if (resProdCodes != null) {
										for (int k = 0; k < resProdCodes
												.getLength(); k++) {
											Node resProdCode = resProdCodes
													.item(k);
											if (resProdCode.getNodeType() == Node.ELEMENT_NODE) {
												NodeList resCodes = resProdCode
														.getChildNodes();
												if (resCodes != null) {
													for (int l = 0; l < resCodes
															.getLength(); l++) {
														Node resCode = resCodes
																.item(l);
														if (resCode
																.getNodeType() == Node.ELEMENT_NODE) {
															Log.d("productCode",
																	product.getAttributes()
																			.getNamedItem(
																					"productCode")
																			.getNodeValue());
															Log.d("productName",
																	product.getAttributes()
																			.getNamedItem(
																					"productName")
																			.getNodeValue());
															Log.d("comment",
																	product.getAttributes()
																			.getNamedItem(
																					"comment")
																			.getNodeValue());

															Log.d("typeNo",
																	subType.getAttributes()
																			.getNamedItem(
																					"typeNo")
																			.getNodeValue());
															Log.d("authorizedNo",
																	subType.getAttributes()
																			.getNamedItem(
																					"authorizedNo")
																			.getNodeValue());
															Log.d("spec",
																	subType.getAttributes()
																			.getNamedItem(
																					"spec")
																			.getNodeValue());
															Log.d("type",
																	subType.getAttributes()
																			.getNamedItem(
																					"type")
																			.getNodeValue());
															Log.d("packUnit",
																	subType.getAttributes()
																			.getNamedItem(
																					"packUnit")
																			.getNodeValue());
															Log.d("physicDetailType",
																	subType.getAttributes()
																			.getNamedItem(
																					"physicDetailType")
																			.getNodeValue());
															Log.d("packageSpec",
																	subType.getAttributes()
																			.getNamedItem(
																					"packageSpec")
																			.getNodeValue());

															Log.d("codeLevel",
																	resCode.getAttributes()
																			.getNamedItem(
																					"codeLevel")
																			.getNodeValue());
															Log.d("codeVersion",
																	resCode.getAttributes()
																			.getNamedItem(
																					"codeVersion")
																			.getNodeValue());
															Log.d("pkgRatio",
																	resCode.getAttributes()
																			.getNamedItem(
																					"pkgRatio")
																			.getNodeValue());
															Log.d("resCode",
																	resCode.getFirstChild()
																			.getNodeValue());
															Log.d("end",
																	"__________________________________________________");
															m_db.insert_product(
																	CodeDBHelper.PRODUCTS_TABEL_NAME,
																	i,
																	product.getAttributes()
																			.getNamedItem(
																					"comment")
																			.getNodeValue(),
																	product.getAttributes()
																			.getNamedItem(
																					"productName")
																			.getNodeValue(),
																	product.getAttributes()
																			.getNamedItem(
																					"productCode")
																			.getNodeValue(),
																	subType.getAttributes()
																			.getNamedItem(
																					"physicDetailType")
																			.getNodeValue(),
																	subType.getAttributes()
																			.getNamedItem(
																					"packUnit")
																			.getNodeValue(),
																	subType.getAttributes()
																			.getNamedItem(
																					"packageSpec")
																			.getNodeValue(),
																	subType.getAttributes()
																			.getNamedItem(
																					"spec")
																			.getNodeValue(),
																	subType.getAttributes()
																			.getNamedItem(
																					"type")
																			.getNodeValue(),
																	subType.getAttributes()
																			.getNamedItem(
																					"authorizedNo")
																			.getNodeValue(),
																	subType.getAttributes()
																			.getNamedItem(
																					"typeNo")
																			.getNodeValue(),
																	resCode.getAttributes()
																			.getNamedItem(
																					"pkgRatio")
																			.getNodeValue(),
																	resCode.getAttributes()
																			.getNamedItem(
																					"codeLevel")
																			.getNodeValue(),
																	resCode.getAttributes()
																			.getNamedItem(
																					"codeVersion")
																			.getNodeValue(),
																	resCode.getFirstChild()
																			.getNodeValue());
														}
													}
												}
											}

										}
									}
								}
							}
						}
					}
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// 载入客户信息
	private void loadcustomer() {
		if (checkcustomer()) {
			m_db.delete_customer(CodeDBHelper.CUSTOMER_TABEL_NAME);
		} else {

		}
		InputStream in = null;
		BufferedReader br = null;
		try {
			in = new FileInputStream(customerFile);
			String str;
			br = new BufferedReader(new InputStreamReader(in, "GBK"));
			m_db.mDb.beginTransaction();
			try {
				while ((str = br.readLine()) != null) {
					String names = str.split(",")[0];
					String abbr = str.split(",")[1];
					String code = str.split(",")[2];
					m_db.insert_customer(CodeDBHelper.CUSTOMER_TABEL_NAME,
							names, abbr, 0, 0, code);
				}
				m_db.mDb.setTransactionSuccessful();
			} finally {
				m_db.mDb.endTransaction();
			}

		} catch (NotFoundException e) {
			Toast.makeText(this, getString(R.string.file_not_exist),
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			Toast.makeText(this, getString(R.string.file_exception),
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (IOException e) {
			Toast.makeText(this, getString(R.string.file_read_error),
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_LOGOUT, 0, getString(R.string.logout_account));
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_LOGOUT:
			Dialog dialog = null;
			CustomDialog.Builder customBuilder = new CustomDialog.Builder(
					Function.this);
			customBuilder
					.setTitle(getString(R.string.attention))
					.setMessage(getString(R.string.confirm_to_logout))
					.setNegativeButton(getString(R.string.cancel),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							})
					.setPositiveButton(getString(R.string.logout_button),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									try {
										FileOutputStream outStream = Function.this
												.openFileOutput("userInfo.txt",
														Context.MODE_PRIVATE);
										String content = "" + "," + "" + ","
												+ "";
										outStream.write(content.getBytes());
										outStream.close();
									} catch (IOException ex) {
									}
									Intent intent = new Intent(
											getApplication(),
											LoginActivity.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(intent);
									Function.this.finish();
									dialog.dismiss();
								}
							});
			dialog = customBuilder.create();
			dialog.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public final class ViewHolder {
		public TextView GridText;
		public ImageView GridImage;
	}

	// 设置显示图片
	public class ImageAdapter extends BaseAdapter {

		private LayoutInflater inflater;

		public ImageAdapter(Context context) {
			this.inflater = LayoutInflater.from(context);
		}

		public int getCount() {
			return mThumbIds.length;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.gridview_item, null);
				holder = new ViewHolder();
				holder.GridText = (TextView) convertView
						.findViewById(R.id.gridTV);
				holder.GridImage = (ImageView) convertView
						.findViewById(R.id.gridIV);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.GridText.setText(getString(GridItem[position]));
			holder.GridImage.setBackgroundResource(mThumbIds[position]);
			return convertView;

		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		switch (position) {
		case 0:
			Intent intent = new Intent();
			intent.setClass(Function.this, FunctionList.class);
			startActivityForResult(intent, FUNCTION_INTENT_REQ_CODE);
			break;
		case 1:
			Intent intent1 = new Intent();
			intent1.setClass(Function.this, ExportXML.class);
			startActivity(intent1);
			break;
		case 2:
			Intent intent2 = new Intent();
			intent2.setClass(Function.this, SystemSetting.class);
			startActivity(intent2);
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
			// CheckFile();
			// return null;
			// }
			//
			// protected void onPostExecute(String[] result) {
			// if (!hasproduct) {
			// Toast.makeText(Function.this,
			// getString(R.string.please_import_product_file),
			// Toast.LENGTH_SHORT).show();
			// }
			// if (!hascustomer) {
			// Toast.makeText(
			// Function.this,
			// getString(R.string.please_import_customer_file),
			// Toast.LENGTH_SHORT).show();
			// }
			// if (hascustomer && hasproduct) {
			// funcBtn.setEnabled(true);
			// } else {
			// funcBtn.setEnabled(false);
			// }
			// loadingdialog.dismiss();
			// super.onPostExecute(result);
			// }
			// }.execute(0);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		if (requestCode == FUNCTION_INTENT_REQ_CODE) {
			flag = intent.getIntExtra("POS", flag);
			if (resultCode == RESULT_OK) {
				funcBtn.setText(getString(FuncTxt[flag]));
				saveFuncInfo(flag);
				if (hasproduct && hascustomer)
					funcBtn.setEnabled(true);
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this,
						getString(R.string.please_select_func_need),
						Toast.LENGTH_LONG).show();
			}
		}
	}

	/**
	 * 保存记录的功能选项
	 * 
	 * @param function
	 */
	private void saveFuncInfo(int function) {
		try {
			FileOutputStream outStream = this.openFileOutput("funcInfo.txt",
					Context.MODE_PRIVATE);
			String content = function + "";
			outStream.write(content.getBytes());
			outStream.close();
		} catch (IOException ex) {

		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent SWHO = new Intent();
		switch (v.getId()) {
		case R.id.function:
			switch (flag) {
			case 100:
				break;
			case 0:
				SWHO.setClass(Function.this, ActionActivity.class);
				startActivity(SWHO);
				break;
			case 1:
				SWHO.setClass(Function.this, ActionActivity.class);
				startActivity(SWHO);
				break;
			case 2:
				SWHO.setClass(Function.this, ActionActivity.class);
				startActivity(SWHO);
				break;
			case 3:
				SWHO.setClass(Function.this, ActionActivity.class);
				startActivity(SWHO);
				break;
			case 4:
				SWHO.setClass(Function.this, ActionActivity.class);
				startActivity(SWHO);
				break;
			case 5:
				SWHO.setClass(Function.this, ActionActivity.class);
				startActivity(SWHO);
				break;
			case 6:
				SWHO.setClass(Function.this, ActionActivity.class);
				startActivity(SWHO);
				break;
			case 7:
				SWHO.setClass(Function.this, ActionActivity.class);
				startActivity(SWHO);
				break;
			case 8:
				SWHO.setClass(Function.this, ActionActivity.class);
				startActivity(SWHO);
				break;

			default:
				break;
			}
			break;
		default:
			break;
		}
	}

}
