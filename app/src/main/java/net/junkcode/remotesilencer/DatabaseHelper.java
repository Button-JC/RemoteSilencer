package net.junkcode.remotesilencer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	  public static final String TABLE_DEVICES = "devices";
	  public static final String TABLE_PERMISSIONS = "permissions";
	  public static final String TABLE_WIDGETS = "widgets";
	  public static final String COLUMN_ID = "_id";
	  public static final String COLUMN_NAME = "name";
	  
	  public static final String COLUMN_ALLOWED = "allowed";
	  
	  public static final String COLUMN_W_ID = "wid";
	  public static final String COLUMN_D_ID = "did";

	  private static final String DATABASE_NAME = "devices.db";
	  private static final int DATABASE_VERSION = 4;

	  // Database creation sql statement
	  private static final String DATABASE_CREATE_1 = "create table "
		      + TABLE_DEVICES + "(" 
				  + COLUMN_ID + " VARCHAR(13) primary key, "
				  + COLUMN_NAME + " text not null);";
	  private static final String DATABASE_CREATE_2 = "create table "
		      + TABLE_PERMISSIONS + "(" 
		      	  + COLUMN_ID + " VARCHAR(13) primary key, "
				  + COLUMN_ALLOWED + " INTEGER(1), "
				  + COLUMN_NAME + " text not null);";
	  private static final String DATABASE_CREATE_3 = "create table "
		      + TABLE_WIDGETS + "(" 
		      	  + COLUMN_W_ID + " INTEGER, "
				  + COLUMN_D_ID + " VARCHAR(13));";

	  public DatabaseHelper(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	  }

	  @Override
	  public void onCreate(SQLiteDatabase database) {
		  database.execSQL(DATABASE_CREATE_1);
		  database.execSQL(DATABASE_CREATE_2);
          database.execSQL(DATABASE_CREATE_3);
	  }

	  @Override
	  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    Log.w(DatabaseHelper.class.getName(),
	        "Upgrading database from version " + oldVersion + " to "
	            + newVersion + ", which will destroy all old data");
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES);
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_PERMISSIONS);
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_WIDGETS);
	    onCreate(db);
	  }

	} 
