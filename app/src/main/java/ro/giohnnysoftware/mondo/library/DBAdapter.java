package ro.giohnnysoftware.mondo.library;

import static ro.giohnnysoftware.mondo.interfaces.Constants.EMPTY_STRING;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Objects;

import ro.giohnnysoftware.mondo.interfaces.Constants;

/**
 * Created by GIOhnny on 14/04/2016.
 */
public class DBAdapter extends SQLiteOpenHelper {

    // DB info
    private static final String DB_NAME = "Mondo.db";
    private static final int DB_VERSION = 1;
    private static String DB_TABLE = "Mondo";

    private String systemLanguage;
    //public static String DB_PATH = Constants.EMPTY_STRING;
    private static SQLiteDatabase db;
    // tool groups table
    private final String KEY_ROWID = "_id";
    private final String KEY_TARA = "tara";
    private final String KEY_CAPITALA = "capitala";
    private final String KEY_ORAS1 = "oras1";
    private final String KEY_ORAS2 = "oras2";
    private final String KEY_ORAS3 = "oras3";
    private final String KEY_ORAS4 = "oras4";
    private final String KEY_DRAPEL = "drapel";
    private final Context context;

    public DBAdapter(Context ctx, String systemLanguage) {
        super(ctx, DB_NAME, null, DB_VERSION);
        this.context = ctx;
        this.systemLanguage = systemLanguage;
        //Log.w("GIOhnny", "System language = " + systemLanguage);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.disableWriteAheadLogging();
    }

    private void openDataBase(String dbname) {
        AssetDatabaseOpenHelper adb = new AssetDatabaseOpenHelper(context, dbname);
        db = adb.openDatabase();
    }

    private byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    private static boolean doesDatabaseExist(Context context, String dbName) {
        File dbFile = context.getDatabasePath(dbName);
        return dbFile.exists();
    }

    public void open() {
        this.openDataBase(DB_NAME);
        this.setDB_TABLE_byLanguage();
    }

    private void setDB_TABLE_byLanguage() {
        try {
            DatabaseUtils.queryNumEntries(db, systemLanguage + DB_TABLE);
        } catch (SQLiteException e) {
            if (Objects.requireNonNull(e.getMessage()).contains(Constants.SQL_ERR_NO_SUCH_TABLE)) {
                systemLanguage = EMPTY_STRING;
            }
        }
        DB_TABLE = systemLanguage + DB_TABLE;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public Long getCount() {
        return DatabaseUtils.queryNumEntries(db, DB_TABLE);
    }

    public String getTara(long l) {
        String[] columns = new String[]{KEY_ROWID, KEY_TARA, KEY_CAPITALA, KEY_ORAS1, KEY_ORAS2, KEY_ORAS3, KEY_ORAS4};
        Cursor c = db.query(DB_TABLE, columns, KEY_ROWID + "=" + l, null, null, null, null);

        if (c != null && c.moveToFirst()) {
            String tara = c.getString(1);
            c.close();
            return tara;
        }
        if (c != null && !c.isClosed()) {
            c.close();
        }
        return EMPTY_STRING;
    }

    public String getCapital(long l) {
        String[] columns = new String[]{KEY_ROWID, KEY_TARA, KEY_CAPITALA, KEY_ORAS1, KEY_ORAS2, KEY_ORAS3, KEY_ORAS4};
        Cursor c = db.query(DB_TABLE, columns, KEY_ROWID + "=" + l, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            String capitala = c.getString(2);//coloana 3 KEY_CAPITALA
            c.close();
            return capitala;
        }
        if (c != null && !c.isClosed()) {
            c.close();
        }
        return EMPTY_STRING;
    }

    public String getOras(long l, int n) {
        String[] columns = new String[]{KEY_ROWID, KEY_TARA, KEY_CAPITALA, KEY_ORAS1, KEY_ORAS2, KEY_ORAS3, KEY_ORAS4};
        Cursor c = db.query(DB_TABLE, columns, KEY_ROWID + "=" + l, null, null, null, null);
        if (c != null && c.moveToFirst()) {
            String orase = c.getString(n + 2);//coloana n+2 KEY_ORASn
            c.close();
            return orase;
        }
        if (c != null && !c.isClosed()) {
            c.close();
        }
        return EMPTY_STRING;
    }

    public Bitmap getDrapel(long l) {
        String qu = "select drapel from Flags where " + KEY_ROWID + "=" + l;
        Cursor c = db.rawQuery(qu, null);
        if (c != null && c.moveToFirst()) {
            byte[] imgByte = c.getBlob(0);
            return BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
        }
        if (c != null && !c.isClosed()) {
            c.close();
        }
        return null;
    }

    public void updateImg(int l, Bitmap image) {
        ContentValues cv = new ContentValues();
/*        cv.put(KEY_TARA,getTara(l));
        cv.put(KEY_CAPITALA,getCapital(l));
        cv.put(KEY_ORAS1,getOras(l,1));
        cv.put(KEY_ORAS2,getOras(l,2));
        cv.put(KEY_ORAS3,getOras(l,3));
        cv.put(KEY_ORAS4,getOras(l,4));*/
        byte[] data = getBitmapAsByteArray(image);
        cv.put(KEY_DRAPEL, data);
//        db.beginTransaction();
        db.update(DB_TABLE, cv, KEY_ROWID + "=" + l, null);
        //       db.setTransactionSuccessful();
        //       db.endTransaction();
    }

    public void deleteEntry(long dRow) {
        db.delete(DB_TABLE, KEY_ROWID + "=" + dRow, null);
    }

    public void updateEntry(long l, String mTara, String mCapitala, String mOras1, String mOras2, String mOras3, String mOras4, byte[] mdrapel) {
        ContentValues cvU = new ContentValues();
        cvU.put(KEY_TARA, mTara);
        cvU.put(KEY_CAPITALA, mCapitala);
        cvU.put(KEY_ORAS1, mOras1);
        cvU.put(KEY_ORAS2, mOras2);
        cvU.put(KEY_ORAS3, mOras3);
        cvU.put(KEY_ORAS4, mOras4);
        cvU.put(KEY_DRAPEL, mdrapel);

        db.update(DB_TABLE, cvU, KEY_ROWID + "=" + l, null);
    }
}