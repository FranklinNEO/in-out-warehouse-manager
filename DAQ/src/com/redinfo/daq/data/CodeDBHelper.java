package com.redinfo.daq.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CodeDBHelper extends SQLiteOpenHelper {

	public static CodeDBHelper mInstance = null;

	/** 数据库名称 **/
	public static final String DATABASE_NAME = "info.db";

	/** 数据库版本号 **/
	private static final int DATABASE_VERSION = 1;

	/** DB对象 **/
	public SQLiteDatabase mDb = null;

	Context mContext = null;

	public final static String CUSTOMER_TABEL_NAME = "customer_data";
	public final static String ORDER_TABLE_NAME = "order_data";
	public final static String ORDER_CODE_TABLE_NAME = "orderCode_data";
	public final static String PRODUCTS_TABEL_NAME = "products_data";
	public final static String ORDER_DETAIL_TABEL_NAME = "orderDetail_data";
	public final static String CODE_STATISTICS_TABEL_NAME = "codeStatistics_data";

	/** 数据库SQL语句 创建表 **/

	public static final String CUSTOMER_TABLE_CREATE = "create table customer_data("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "customerName TEXT NOT NULL,"
			+ "customerInitial TEXT NOT NULL,"
			+ "orderCount INTEGER NOT NULL,"
			+ "favoriteFlag INTEGER NOT NULL,"
			+ "customerID TEXT NOT NULL);";

	public static final String ORDER_TABLE_CREATE = "create table order_data("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "orderType TEXT NOT NULL," + "CorpOrderID TEXT NOT NULL,"
			+ "ToCorpID TEXT," + "flag INTEGER NOT NULL,"
			+ "createTime TEXT NOT NULL);";

	public static final String ORDER_CODE_TABLE_CREATE = "create table orderCode_data("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "code20 TEXT NOT NULL,"
			+ "codeVersion TEXT NOT NULL,"
			+ "resCode TEXT NOT NULL,"
			+ "actor TEXT NOT NULL,"
			+ "actDate TEXT NOT NULL,"
			+ "productID INTEGER NOT NULL,"
			+ "orderID INTEGER NOT NULL,"
			+ "FOREIGN KEY(productID) REFERENCES products_data(_id),"
			+ "FOREIGN KEY(orderID) REFERENCES order_data(_id));";
	public static final String PRODUCTS_TABEL_CREATE = "create table products_data("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "productID INTEGER NOT NULL,"
			+ "comment TEXT,"
			+ "productName TEXT NOT NULL,"
			+ "productCode TEXT NOT NULL,"
			+ "physicDetailType TEXT NOT NULL,"
			+ "packUnit TEXT NOT NULL,"
			+ "packageSpec TEXT NOT NULL,"
			+ "spec TEXT NOT NULL,"
			+ "type TEXT NOT NULL,"
			+ "authorizedNo TEXT NOT NULL,"
			+ "typeNo TEXT NOT NULL,"
			+ "packRatio TEXT NOT NULL,"
			+ "codeLevel TEXT NOT NULL,"
			+ "codeVersion TEXT NOT NULL,"
			+ "resCode TEXT NOT NULL);";
	public static final String CODE_STATISTICS_TABEL_CREATE = "create table codeStatistics_data("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "productID INTEGER NOT NULL,"
			+ "orderID INTEGER NOT NULL,"
			+ "codeNum1 INTEGER,"
			+ "codeNum2 INTEGER,"
			+ "codeNum3 INTEGER,"
			+ "FOREIGN KEY(productID) REFERENCES products_data(_id));";

	/** 单例模式 **/
	public static synchronized CodeDBHelper getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new CodeDBHelper(context);
		}
		return mInstance;
	}

	public CodeDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// 得到数据库对象
		mDb = getReadableDatabase();
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// 创建数据库
		db.execSQL(CUSTOMER_TABLE_CREATE);
		db.execSQL(ORDER_TABLE_CREATE);
		db.execSQL(ORDER_CODE_TABLE_CREATE);
		db.execSQL(PRODUCTS_TABEL_CREATE);
		db.execSQL(CODE_STATISTICS_TABEL_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	/**
	 * insert code statistics
	 * 
	 * @param tablename
	 * @param codeLevel
	 * @param productID
	 * @param codeNum
	 */
	public void insert_codestatistics(String tablename, int productID,
			int orderID, int codeNum1, int codeNum2, int codeNum3) {
		ContentValues values = new ContentValues();
		values.put("productID", productID);
		values.put("orderID", orderID);
		values.put("codeNum1", codeNum1);
		values.put("codeNum2", codeNum2);
		values.put("codeNum3", codeNum3);
		mDb.insert(tablename, null, values);
		values.clear();
	}

	public void update_codestatistics(String tablename, int productID,
			int orderID, int codeNum1, int codeNum2, int codeNum3) {
		ContentValues values = new ContentValues();
		values.put("productID", productID);
		values.put("orderID", orderID);
		values.put("codeNum1", codeNum1);
		values.put("codeNum2", codeNum2);
		values.put("codeNum3", codeNum3);
		mDb.update(tablename, values, "productID='" + productID
				+ "' AND orderID='" + orderID + "';", null);
		values.clear();
	}

	/**
	 * insert product table
	 * 
	 * @param tablename
	 * @param comment
	 * @param productName
	 * @param productCode
	 * @param physicDetailType
	 * @param packUnit
	 * @param packageSpec
	 * @param spec
	 * @param type
	 * @param authorizedNo
	 * @param typeNo
	 * @param packRatio
	 * @param codeLevel
	 * @param codeVersion
	 * @param resCode
	 */
	public void insert_product(String tablename, int productID, String comment,
			String productName, String productCode, String physicDetailType,
			String packUnit, String packageSpec, String spec, String type,
			String authorizedNo, String typeNo, String packRatio,
			String codeLevel, String codeVersion, String resCode) {
		ContentValues values = new ContentValues();
		values.put("productID", productID);
		values.put("comment", comment);
		values.put("productName", productName);
		values.put("productCode", productCode);
		values.put("physicDetailType", physicDetailType);
		values.put("packUnit", packUnit);
		values.put("packageSpec", packageSpec);
		values.put("spec", spec);
		values.put("type", type);
		values.put("authorizedNo", authorizedNo);
		values.put("typeNo", typeNo);
		values.put("packRatio", packRatio);
		values.put("codeLevel", codeLevel);
		values.put("codeVersion", codeVersion);
		values.put("resCode", resCode);
		mDb.insert(tablename, null, values);
		values.clear();
	}

	/**
	 * insert customer table
	 * 
	 * @param tablename
	 * @param customerName
	 * @param customerInitial
	 * @param orderCount
	 * @param favoriateFlag
	 * @param customerID
	 */
	public void insert_customer(String tablename, String customerName,
			String customerInitial, int orderCount, int favoriateFlag,
			String customerID) {
		ContentValues values = new ContentValues();
		values.put("customerName", customerName);
		values.put("customerInitial", customerInitial);
		values.put("orderCount", orderCount);
		values.put("favoriteFlag", favoriateFlag);
		values.put("customerID", customerID);
		mDb.insert(tablename, null, values);
		values.clear();
	}

	/**
	 * update customer table
	 * 
	 * @param tablename
	 * @param customerName
	 * @param customerInitial
	 * @param orderCount
	 * @param favoriateFlag
	 * @param customerID
	 */
	public void update_customer(String tablename, String customerName,
			String customerInitial, int orderCount, int favoriateFlag,
			String customerID) {
		ContentValues values = new ContentValues();
		values.put("customerName", customerName);
		values.put("customerInitial", customerInitial);
		values.put("orderCount", orderCount);
		values.put("favoriteFlag", favoriateFlag);
		values.put("customerID", customerID);
		mDb.update(tablename, values, "customerName='" + customerName
				+ "' AND customerInitial='" + customerInitial
				+ "' AND customerID='" + customerID + "';", null);
		values.clear();
	}

	/**
	 * insert order table
	 * 
	 * @param tablename
	 * @param orderStringType
	 * @param CorpOrderID
	 * @param CorpID
	 * @param flag
	 * @param createTime
	 */
	public void insert_order(String tablename, String orderType,
			String CorpOrderID, String ToCorpID, int flag, String createTime) {
		ContentValues values = new ContentValues();
		values.put("orderType", orderType);
		values.put("CorpOrderID", CorpOrderID);
		values.put("ToCorpID", ToCorpID);
		values.put("flag", flag);
		values.put("createTime", createTime);
		mDb.insert(tablename, null, values);
		values.clear();
	}

	public void update_order(String tablename, String orderType,
			String CorpOrderID, String ToCorpID, int flag, String createTime) {
		ContentValues values = new ContentValues();
		values.put("orderType", orderType);
		values.put("CorpOrderID", CorpOrderID);
		values.put("ToCorpID", ToCorpID);
		values.put("flag", flag);
		values.put("createTime", createTime);
//		mDb.update(tablename, values, "orderType='" + orderType
//				+ "' AND CorpOrderID='" + CorpOrderID + "' AND ToCorpID='"
//				+ ToCorpID + "' AND createTime='" + createTime + "';", null);
		mDb.update(tablename, values, "CorpOrderID='"+CorpOrderID+"';", null);
		values.clear();
	}

	/**
	 * insert code table
	 * 
	 * @param tablename
	 * @param code20
	 * @param actor
	 * @param actDate
	 * @param orderID
	 */
	public void insert_code(String tablename, String code20,
			String codeVersion, String resCode, String actor, String actDate,
			int productID, int orderID) {
		ContentValues values = new ContentValues();
		values.put("code20", code20);
		values.put("codeVersion", codeVersion);
		values.put("resCode", resCode);
		values.put("actor", actor);
		values.put("actDate", actDate);
		values.put("productID", productID);
		values.put("orderID", orderID);
		mDb.insert(tablename, null, values);
		values.clear();
	}

	public void update_code(String tablename, String code20,
			String codeVersion, String resCode, String actor, String actDate,
			int productID, int orderID) {
		ContentValues values = new ContentValues();
		values.put("code20", code20);
		values.put("codeVersion", codeVersion);
		values.put("resCode", resCode);
		values.put("actor", actor);
		values.put("actDate", actDate);
		values.put("productID", productID);
		values.put("orderID", orderID);
		mDb.update(tablename, values, "code20='" + code20
				+ "'AND codeVersion='" + codeVersion + "'AND resCode='"
				+ resCode + "' AND actor='" + actor + "' AND actDate='"
				+ actDate + "';", null);
		values.clear();
	}

	public void delete_code(String tablename, String code20, int orderID) {
		mDb.delete(tablename, "code20='" + code20 + "' AND orderID='" + orderID
				+ "';", null);
	}

	public void delete_order(String tablename, String CorpOrderID) {
		mDb.delete(tablename, "CorpOrderID='" + CorpOrderID + "';", null);
	}

	public void delete_product(String tablename) {
		mDb.delete(tablename, null, null);
	}

	public void delete_customer(String tablename) {
		mDb.delete(tablename, null, null);
	}

	public void delete_statistics(String tablename) {
		mDb.delete(tablename, null, null);
	}
}
