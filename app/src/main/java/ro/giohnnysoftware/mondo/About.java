package ro.giohnnysoftware.mondo;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

public class About extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        String version = "1.0";
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        TextView tvAbout = findViewById(R.id.tvAbout);
        tvAbout.setTextColor(ContextCompat.getColor(tvAbout.getContext(), R.color.colorMondo2));
        tvAbout.setText(getString(R.string.Credits, version)
        );
    }
}
