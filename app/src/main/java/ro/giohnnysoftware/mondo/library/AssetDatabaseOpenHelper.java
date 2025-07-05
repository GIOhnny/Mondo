package ro.giohnnysoftware.mondo.library;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AssetDatabaseOpenHelper {

    private static String DB_NAME;

    private final Context context;

    public AssetDatabaseOpenHelper(Context context, String dbNameParam) {
        this.context = context;
        DB_NAME = dbNameParam;
    }

    public SQLiteDatabase openDatabase() {
        File dbFile = context.getDatabasePath(DB_NAME);

        if (!dbFile.exists()) {
            try {
                SQLiteDatabase checkDB = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
                if (checkDB != null) {
                    checkDB.close();
                }
                copyDatabase(dbFile);
            } catch (IOException e) {
                throw new RuntimeException("Error creating source database", e);
            }
        }

        return SQLiteDatabase.openDatabase(dbFile.getPath(), null, SQLiteDatabase.OPEN_READWRITE);
        //OPEN_READWRITE  OPEN_READONLY
    }

    private void copyDatabase(File dbFile) throws IOException {
        InputStream is = context.getAssets().open(DB_NAME);
        OutputStream os = new FileOutputStream(dbFile);

        byte[] buffer = new byte[1024];
        while (is.read(buffer) > 0) {
            os.write(buffer);
        }

        os.flush();
        os.close();
        is.close();

        //Toast.makeText(context, "Initial database has been created", Toast.LENGTH_LONG).show();

    }

}
