package com.github.frimtec.android.pikettassist.activity;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;
import com.github.frimtec.android.pikettassist.R;

public class AboutActivity extends AppCompatActivity {

  private static final String TAG = "AboutActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_about);

    TextView textView = (TextView) findViewById(R.id.about_text);

    String version = "N/A";
    int build = 0;
    try {
      PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      version = packageInfo.versionName;
      build = packageInfo.versionCode;
    } catch (PackageManager.NameNotFoundException e) {
      Log.e(TAG, "Can not read version info", e);
    }

    textView.setText(Html.fromHtml(
        "<h2><a href='https://github.com/frimtec/pikett-assist'>PAssist</a></h2>" +
            "<p>Version: " + version + " (build  " + build + ")</p>" +
            "<p>&copy; 2019 <a href='https://github.com/frimtec'>frimTEC</a></p>" +
            ""
        , Html.FROM_HTML_MODE_COMPACT));
    textView.setMovementMethod(LinkMovementMethod.getInstance());
  }

}
