package net.junkcode.remotesilencer;

import java.util.HashSet;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;

public class DeviceManager {
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private String[] devicesColumns     = {
            DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_NAME
    };
    private String[] permissionsColumns = {
            DatabaseHelper.COLUMN_ID,
            DatabaseHelper.COLUMN_NAME,
            DatabaseHelper.COLUMN_ALLOWED
    };
    private String[] widgetColumns      = {
            DatabaseHelper.COLUMN_W_ID,
            DatabaseHelper.COLUMN_D_ID
    };

    public DeviceManager(Context context) {
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
    }

	/*public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	}*/

    /**
     * End usage
     */
    public void close() {
        dbHelper.close();
    }

    /**
     * Add item to database
     * @param id String id
	 * @param name readable name
	 * @param i 0-device 1-permission
	 */
    public void add(String id, String name, int i){
        if(i==0) {
            addDevice(id, name);
        }else {
            addPermission(id, name);
        }
	}
	  
	public void addDevice(String id,String name){
	    ContentValues values = new ContentValues();
	    values.put(DatabaseHelper.COLUMN_ID, id);
	    values.put(DatabaseHelper.COLUMN_NAME, name);
	    try{
	    	database.insertOrThrow(DatabaseHelper.TABLE_DEVICES, null,values);
	    }catch(SQLiteConstraintException e){
			renameDevice(id, name);
		}
	}

	public void addPermission(String id,String name){
	    ContentValues values = new ContentValues();
	    values.put(DatabaseHelper.COLUMN_ID, id);
	    values.put(DatabaseHelper.COLUMN_NAME, name);
	    values.put(DatabaseHelper.COLUMN_ALLOWED, 1);
	    try{
	    	database.insertOrThrow(DatabaseHelper.TABLE_PERMISSIONS, null,values);
	    }catch(SQLiteConstraintException e){
			renamePermission(id, name);
		}
	}

	/**
	 * rename item
     * @param id String id
     * @param name readable name
	 * @param i 0-device 1-permission
	 */
	public void rename(String id, String name, int i){
        if(i==0) {
            renameDevice(id, name);
        }else {
            renamePermission(id, name);
        }
    }
	  
	public void renameDevice(String id,String name) {
	    ContentValues values = new ContentValues();
	    values.put(DatabaseHelper.COLUMN_NAME, name);
	    database.update(DatabaseHelper.TABLE_DEVICES,values, DatabaseHelper.COLUMN_ID+ "= '" + id +"'",null);
	}

	public void renamePermission(String id,String name) {
	    ContentValues values = new ContentValues();
	    values.put(DatabaseHelper.COLUMN_NAME, name);
	    database.update(DatabaseHelper.TABLE_PERMISSIONS,values, DatabaseHelper.COLUMN_ID+ "= '" + id +"'",null);
    }

    /**
     * delete item
     * @param id item id
     * @param i 0-device 1-permission
     */
    public void delete(String id, int i){
        if(i==0) {
            deleteDevice(id);
        }else {
            deletePermission(id);
        }
    }
	  
	  public void deleteDevice(String id) {
	    database.delete(DatabaseHelper.TABLE_DEVICES, DatabaseHelper.COLUMN_ID + " = '" + id +"'", null);
	  }

	  public void deletePermission(String id) {
	    database.delete(DatabaseHelper.TABLE_PERMISSIONS, DatabaseHelper.COLUMN_ID + " = '" + id +"'", null);
	  }

	  /**
	   * get all
	   * @param i 0-device 1-permission
	   * @return db Cursor
	   */
      @Deprecated
	  public Cursor getAll(int i){
		  if(i==0)
			  return getAllDevices();
		  else
			  return getAllPermissions();
	  }
	  
	  @Deprecated
	  public Cursor getAllDevices() {
	    return database.query(DatabaseHelper.TABLE_DEVICES,
	        devicesColumns, null, null, null, null, null);
	  }
	  public RemoteDevice[] getAllDevicesN() {
		    Cursor cursor = database.query(DatabaseHelper.TABLE_DEVICES,
		    		devicesColumns, null, null, null, null, null);
		    RemoteDevice[] remoteDevice = new RemoteDevice[cursor.getCount()];
		    cursor.moveToFirst();
			for(int i=0; i<cursor.getCount();i++){
				remoteDevice[i]= new RemoteDevice(cursor.getString(0), cursor.getString(1));
				cursor.moveToNext();
			}
			cursor.close();
			return remoteDevice;
	  }
	  
	  @Deprecated
	  public Cursor getAllPermissions() {
		    return database.query(DatabaseHelper.TABLE_PERMISSIONS,
		        permissionsColumns, null, null, null, null, null);
	  }
	  public ControlPermission[] getAllPermissionsN() {
		    Cursor cursor = database.query(DatabaseHelper.TABLE_PERMISSIONS,
		        permissionsColumns, null, null, null, null, null);
		    ControlPermission[] controlPermissions = new ControlPermission[cursor.getCount()];
		    cursor.moveToFirst();
			for(int i=0; i<cursor.getCount();i++){
				controlPermissions[i]= new ControlPermission(cursor.getString(0), cursor.getString(1), cursor.getInt(2));
				cursor.moveToNext();
			}
			cursor.close();
			return controlPermissions;
	  }

	public void changePermission(String id, boolean activated) {
		ContentValues values = new ContentValues();
	    values.put(DatabaseHelper.COLUMN_ALLOWED, (activated?1:0));
	    database.update(DatabaseHelper.TABLE_PERMISSIONS, values, DatabaseHelper.COLUMN_ID + "= '" + id + "'", null);
	}

    /*
	@Deprecated
	public boolean isAllowed(String from) {
		Cursor cursor = database.query(DatabaseHelper.TABLE_PERMISSIONS,
		        permissionsColumns,  
		        DatabaseHelper.COLUMN_ID+"='"+from+"' and "+ DatabaseHelper.COLUMN_ALLOWED+" = 1 ",
		        null, null, null, null);
		boolean something = cursor.getCount()>0;
		cursor.close();
		return something;
	}*/

	public ControlPermission getPermissionById(String id) {
		Cursor cursor = database.query(DatabaseHelper.TABLE_PERMISSIONS,
		        permissionsColumns,  
		        DatabaseHelper.COLUMN_ID+"='"+id+"'",
		        null, null, null, null);
		if(cursor.getCount()==0)return null;
		cursor.moveToFirst();
		ControlPermission p = new ControlPermission(cursor.getString(0), cursor.getString(1), cursor.getInt(2));
		cursor.close();
		return p;
	}
	
	public void addWidgetConnection(int extraAppwidgetId, Set<String> dev_ids) {
		for (String dev_id: dev_ids){
			ContentValues values = new ContentValues();
		    values.put(DatabaseHelper.COLUMN_W_ID, extraAppwidgetId);
		    values.put(DatabaseHelper.COLUMN_D_ID, dev_id);
		    database.insert(DatabaseHelper.TABLE_WIDGETS, null,values);
		}
	}

	public Set<String> getDevicesToSilentByWidgetId(int id) {
		Set<String> result = new HashSet<String>();
		Cursor cursor = database.query(DatabaseHelper.TABLE_WIDGETS,
		        widgetColumns,  
		        DatabaseHelper.COLUMN_W_ID+"='"+id+"'",
		        null, null, null, null);
		if(cursor.getCount()==0)return result;
		cursor.moveToFirst();
		for(int i=0; i<cursor.getCount();i++){
			result.add(cursor.getString(1));
			cursor.moveToNext();
		}
		cursor.close();
		return result;
	}

    public Set<String> getWidgetsTiedToLocalVolume() {
        Set<String> result = new HashSet<String>();
        Cursor cursor = database.query(DatabaseHelper.TABLE_WIDGETS,
                                       widgetColumns,
                                       DatabaseHelper.COLUMN_D_ID+"='self'",
                                       null, null, null, null);
        if(cursor.getCount()==0)return result;
        cursor.moveToFirst();
        for(int i=0; i<cursor.getCount();i++){
            result.add(cursor.getString(0));
            cursor.moveToNext();
        }
        cursor.close();
        return result;
    }

	public class ControlPermission {
		public String id;
		public String name;
		public boolean allowed;
		
		public ControlPermission(String id, String name, int allowed) {
			this.id=id;
			this.name=name;
			this.allowed=allowed==1;
		}
	}

	public class RemoteDevice {
		public String id;
		public String name;
		
		public RemoteDevice(String id, String name) {
			this.id=id;
			this.name=name;
		}
	}
  
}
