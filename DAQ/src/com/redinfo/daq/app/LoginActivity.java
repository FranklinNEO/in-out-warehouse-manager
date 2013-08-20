package com.redinfo.daq.app;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import com.redinfo.daq.R;
import com.redinfo.daq.ui.PwdEditCancel;
import com.redinfo.daq.ui.UserEditCancel;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener {
	private CheckBox saveUserInfoCbox = null;
	private Button Login_Button = null;
	private UserEditCancel ec1;
	private PwdEditCancel ec2;
	public Dialog logindialog = null;
	private String Saveid = "";
	private String Saveuser = "";
	private String Savepwd = "";
	private String Actor = "";
	private String LogName = "";
	private String PassWord = "";
	private String check = "";
	private LinearLayout input_panel = null;
	private LinearLayout login_panel = null;
	private LinearLayout splash_progress = null;
	private boolean login = false;
	private boolean hasFile = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		MyApplication.getInstance().addActivity(this);

		input_panel = (LinearLayout) findViewById(R.id.input_panel);
		login_panel = (LinearLayout) findViewById(R.id.login_panel);
		splash_progress = (LinearLayout) findViewById(R.id.splash_progress);

		this.saveUserInfoCbox = (CheckBox) this
				.findViewById(R.id.remember_user_checkbox);

		this.Login_Button = (Button) this.findViewById(R.id.LoginButton);
		ec2 = (PwdEditCancel) findViewById(R.id.pwd);
		ec1 = (UserEditCancel) findViewById(R.id.name);
		String usr = ec1.getString();
		String pwd = ec2.getString();

		if (usr != null && pwd != null) {
			this.Login_Button.setEnabled(true);
		} else {
			this.Login_Button.setEnabled(false);
		}

		this.Login_Button.setOnClickListener(this);

		if (checkSD()) {
			LoginCheck();
			new AsyncTask<Integer, Integer, String[]>() {
				protected void onPreExecute() {
					super.onPreExecute();
				}

				@Override
				protected void onCancelled() {
					super.onCancelled();
				}

				protected String[] doInBackground(Integer... params) {
					// Todo
					check();
					return null;
				}

				protected void onPostExecute(String[] result) {
					if (hasFile) {
						if (login) {
							new Thread() {
								public void run() {
									try {
										sleep(1000);
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									Intent intent = new Intent();
									intent.setClass(LoginActivity.this,
											Function.class);
									intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(intent);
									LoginActivity.this.finish();
								}
							}.start();

						} else {
							input_panel.setVisibility(View.VISIBLE);
							login_panel.setVisibility(View.VISIBLE);
							splash_progress.setVisibility(View.INVISIBLE);
						}
					} else {
						Toast.makeText(LoginActivity.this,
								getString(R.string.check_file_message),
								Toast.LENGTH_LONG).show();
					}

					super.onPostExecute(result);
				}
			}.execute(0);

		} else {
			Toast.makeText(LoginActivity.this,
					getString(R.string.sd_card_not_exist), Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {

		case R.id.LoginButton:
			ec1 = (UserEditCancel) findViewById(R.id.name);
			String pno = ec1.getString().trim();
			ec2 = (PwdEditCancel) findViewById(R.id.pwd);
			String vCode = ec2.getString().trim();
			if (!Pattern.matches("^[A-Za-z0-9]+$", vCode)) {
				Toast.makeText(this, getString(R.string.login_input_alert),
						Toast.LENGTH_SHORT).show();
				break;
			}

			if (!Pattern.matches("^[A-Za-z0-9]+$", pno)) {
				Toast.makeText(this, getString(R.string.login_input_alert),
						Toast.LENGTH_SHORT).show();
				break;
			}
			check(pno, vCode);
			break;
		default:
			break;
		}
	}

	private void check(String LogName, String PassWord) {
		File sdCardDir = Environment.getExternalStorageDirectory();
		File operatorFile = new File(sdCardDir, "/RedInfo/operator.txt");
		if (operatorFile.exists()) {
			// Resources res = this.getResources();
			InputStream in = null;
			BufferedReader br = null;
			try {
				in = new FileInputStream(operatorFile);
				br = new BufferedReader(new InputStreamReader(in, "GBK"));
				String str;

				try {
					if ((LogName == null) || (PassWord == "")) {
						login = false;
					} else {
						while ((str = br.readLine()) != null) {
							String id = str.split(",")[0];
							String user = str.split(",")[1];
							String pwd = str.split(",")[2];
							if ((id.equals(LogName)) && (pwd.equals(PassWord))) {
								login = true;
								Saveid = id;
								Saveuser = user;
								Savepwd = pwd;
							}
						}
					}
				} finally {
					if (login) {
						// 验证成功跳转至表单界面
						DaqApplication daq = (DaqApplication) this
								.getApplication();
						daq.setActorId(Saveid);
						Intent intent = new Intent();
						intent.setClass(LoginActivity.this, Function.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						saveUserInfo(Saveid, Saveuser, Savepwd);
						startActivity(intent);
						LoginActivity.this.finish();
					} else {
						// 验证失败提示重新登录系统
						Toast.makeText(LoginActivity.this,
								getString(R.string.login_failed_message),
								Toast.LENGTH_SHORT).show();
					}

				}
			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
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
		} else {
			Toast.makeText(LoginActivity.this,
					getString(R.string.check_file_message), Toast.LENGTH_SHORT)
					.show();
		}

	}

	public void saveUserInfo(String id, String user, String pwd) {
		if (!this.saveUserInfoCbox.isChecked())
			return;
		try {
			FileOutputStream outStream = this.openFileOutput("userInfo.txt",
					Context.MODE_PRIVATE);
			String content = id + "," + user + "," + pwd;
			outStream.write(content.getBytes());
			outStream.close();
		} catch (IOException ex) {

		}
	}

	private boolean checkSD() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}

	}

	private void LoginCheck() {

		FileInputStream inStream = null;
		ByteArrayOutputStream outStream = null;

		// 提取上次登录的信息
		try {
			inStream = this.openFileInput("userInfo.txt");
			outStream = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length = -1;
			while ((length = inStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, length);
			}
			String content = outStream.toString();
			Log.v("content", content);
			if (content != null && content.indexOf(",") > 1) {
				Actor = content.split(",")[0];
				LogName = content.split(",")[1];
				PassWord = content.split(",")[2];
				check = Actor + "," + LogName + "," + PassWord;
			}
			outStream.close();
			inStream.close();
		} catch (IOException ex) {
		}

	}

	// 验证登录信息
	private void check() {
		File sdCardDir = Environment.getExternalStorageDirectory();
		File operatorFile = new File(sdCardDir, "/RedInfo/operator.txt");
		File destDir = new File(sdCardDir, "/RedInfo/");
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
		if (operatorFile.exists()) {
			// Resources res = this.getResources();
			hasFile = true;
			InputStream in = null;
			BufferedReader br = null;
			try {
				in = new FileInputStream(operatorFile);
				br = new BufferedReader(new InputStreamReader(in, "GBK"));
				String str;
				try {
					if ((check == null) || (check == "")) {
						login = false;
					} else {
						while ((str = br.readLine()) != null) {
							if (str.contains(check)) {
								login = true;
							}
						}
					}
				} finally {
					if (login) {
						// 验证成功跳转至表单界面
						DaqApplication daq = (DaqApplication) this
								.getApplication();
						daq.setActorId(Actor);
					} else {
					}
				}
			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
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

		} else {
			// Toast.makeText(LoginActivity.this,
			// "请导入operator.txt文件至SD卡的RedInfo目录下", Toast.LENGTH_SHORT)
			// .show();
			hasFile = false;
		}
	}
}
