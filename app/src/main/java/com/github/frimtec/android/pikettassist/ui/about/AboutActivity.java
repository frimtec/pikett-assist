package com.github.frimtec.android.pikettassist.ui.about;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.view.ViewManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.frimtec.android.pikettassist.BuildConfig;
import com.github.frimtec.android.pikettassist.R;

public class AboutActivity extends AppCompatActivity {

  public static final String EXTRA_SPONSOR_ICONS = "sponsorIcons";

  private int[] sponsorIcons = new int[0];

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle b = getIntent().getExtras();
    if (b != null) {
      sponsorIcons = b.getIntArray(EXTRA_SPONSOR_ICONS);
    }
    setContentView(R.layout.activity_about);
    setupAppInfo();
    setupSponsoring(sponsorIcons);
    setupDocumentation();
    setupDisclaimer();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void setupAppInfo() {
    TextView textView = findViewById(R.id.app_info);
    String version = BuildConfig.VERSION_NAME;
    int build = BuildConfig.VERSION_CODE;

    textView.setText(Html.fromHtml(
        "<h2><a href='https://github.com/frimtec/pikett-assist'>PAssist</a></h2>" +
            "<p>Version: <b>" + version + "</b><br/>" + "Build: " + build + "</p>" +
            "<p>&copy; 2019-2021 <a href='https://github.com/frimtec'>frimTEC</a></p>" +
            ""
        , Html.FROM_HTML_MODE_COMPACT));
    textView.setMovementMethod(LinkMovementMethod.getInstance());
  }

  private void setupSponsoring(int[] icons) {
    TextView textView = findViewById(R.id.sponsoring);
    ImageView[] imageViews = new ImageView[]{findViewById(R.id.sponsor_0), findViewById(R.id.sponsor_1), findViewById(R.id.sponsor_2)};
    if (icons == null || icons.length == 0) {
      textView.setText(Html.fromHtml(getString(R.string.about_no_sponsoring), Html.FROM_HTML_MODE_COMPACT));
      for (ImageView imageView : imageViews) {
        ((ViewManager) imageView.getParent()).removeView(imageView);
      }
    } else {
      textView.setText(Html.fromHtml(getString(R.string.about_sponsoring), Html.FROM_HTML_MODE_COMPACT));
      for (int i = 0; i < icons.length; i++) {
        imageViews[i].setImageDrawable(ContextCompat.getDrawable(this, icons[i]));
      }
      for (int i = icons.length; i < imageViews.length; i++) {
        ((ViewManager) imageViews[i].getParent()).removeView(imageViews[i]);
      }
    }
  }

  private void setupDocumentation() {
    TextView textView = findViewById(R.id.documentation);
    textView.setText(Html.fromHtml(getString(R.string.about_documentation), Html.FROM_HTML_MODE_COMPACT));
    textView.setMovementMethod(LinkMovementMethod.getInstance());
  }

  private void setupDisclaimer() {
    TextView textView = findViewById(R.id.disclaimer);
    textView.setText(Html.fromHtml(getString(R.string.about_disclaimer), Html.FROM_HTML_MODE_COMPACT));
    textView.setMovementMethod(LinkMovementMethod.getInstance());
  }

}
