package example.com.zhaoritian_map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by edward on 10/13/15.
 */

public class heatmapHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "HeatDatabase";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String TB_NAME = "locationTable";
    public static final String TAG = "db operation";
    public static final int VERSION = 1;
    public static final String CREATE_ENTRY = "CREATE TABLE " + TB_NAME + " (" +
            "_id INTEGER PRIMARY KEY AUTOINCREMENT, " + LATITUDE + " TEXT, " +
            LONGITUDE + " TEXT"+ ");";
    public static final String DELETE_ENTRY = "DROP TABLE IF EXISTS " + TB_NAME;
    public SQLiteDatabase db;
    public heatmapHelper mheatmapHelper;
    Context context;

    public heatmapHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ENTRY);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL(DELETE_ENTRY);
        // Create tables again
        onCreate(db);
    }

    public heatmapHelper open(){
        db = this.getWritableDatabase();
        return this;
    }

    public long insertLocation(Location location) {
        db= this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(LATITUDE, "" + location.getLatitude());
        cv.put(LONGITUDE, "" + location.getLongitude());
        long newrowid;
        newrowid = db.insert(TB_NAME, null, cv);
        Log.v(TAG, ""+location.getLatitude());
        return newrowid;
    }

    public Cursor query() {
        db = this.getReadableDatabase();
        Cursor cursor = db.query(TB_NAME, new String[]{LATITUDE, LONGITUDE}, null, null, null, null, null);
        return cursor;
    }
}
