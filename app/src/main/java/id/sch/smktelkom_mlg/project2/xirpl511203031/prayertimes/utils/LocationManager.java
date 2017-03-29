package id.sch.smktelkom_mlg.project2.xirpl511203031.prayertimes.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class LocationManager extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "PrayerTimes";
    private static final String TABLE_NAME = "locations", OBJ_COL_NAME = "loc";

    public LocationManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private byte[] Serialize(Object obj) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(obj);
        o.close();
        return b.toByteArray();
    }

    private Object Deserialize(byte[] bytes) throws IOException,
            ClassNotFoundException {
        ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        ObjectInputStream o = new ObjectInputStream(b);
        Object obj = o.readObject();
        o.close();
        return obj;
    }

    public boolean saveLocation(Location loc) {
        SQLiteDatabase db = null;
        try {
            db = this.getWritableDatabase();
            db.execSQL("delete from " + TABLE_NAME);
            ContentValues values = new ContentValues();
            values.put(OBJ_COL_NAME, Serialize(loc));
            values.put("country", loc.getCountryId());
            values.put("state", loc.getStateId());
            values.put("city", loc.getCityId());
            if (!Exists(loc)) {
                db.insert(TABLE_NAME, null, values);
            } else {
                db.update(TABLE_NAME, values,
                        "country=? and state=? and city=?", new String[]{
                                "" + loc.getCountryId(), "" + loc.getStateId(),
                                "" + loc.getCityId()});
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }
        return false;
    }

    public List<Location> fetchLocations() {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        List<Location> locs = new ArrayList<Location>();
        try {
            db = this.getReadableDatabase();
            cursor = db.query(TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "id ASC",
                    null);

            while (cursor.moveToNext()) {
                byte[] data = cursor.getBlob(1);
                try {
                    Location loc = (Location) Deserialize(data);
                    locs.add(loc);
                } catch (Exception e) {
                    Log.d("fetchLocations", e.toString());
                }
            }

        } catch (Exception ex) {
            Log.d("fetchLocations", ex.toString());
        } finally {
            if (db != null) {
                db.close();
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return locs;
    }

    public boolean Exists(Location loc) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME
                        + " where country=? and state=? and city=?",
                new String[]{"" + loc.getCountryId(), "" + loc.getStateId(),
                        "" + loc.getCityId()});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOC_TABLE = "CREATE TABLE " + TABLE_NAME + " ( "
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, " + OBJ_COL_NAME
                + " BLOB, country INT, state INT, city INT )";
        db.execSQL(CREATE_LOC_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        this.onCreate(db);
    }
}
