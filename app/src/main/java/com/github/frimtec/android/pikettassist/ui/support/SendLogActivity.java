package com.github.frimtec.android.pikettassist.ui.support;

import static android.content.Intent.EXTRA_BUG_REPORT;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.ui.common.BaseActivity;

public class SendLogActivity extends BaseActivity {

  private ActivityResultLauncher<Intent> sendCrashReportActivityResultLauncher;

  @Override
  protected void doOnCreate(Bundle savedInstanceState) {
    setContentView(R.layout.activity_send_log);

    Button sendButton = findViewById(R.id.send_log_button_send);
    sendButton.setOnClickListener(v -> sendCrashReport(getIntent().getStringExtra(EXTRA_BUG_REPORT)));

    Button exitButton = findViewById(R.id.send_log_button_exit);
    exitButton.setOnClickListener(v -> terminate());

    this.sendCrashReportActivityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> terminate()
    );
  }

  private void sendCrashReport(String report) {
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("plain/text");
    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"frimtec@gmx.ch"});
    intent.putExtra(Intent.EXTRA_SUBJECT, "PAssist crash report");
    intent.putExtra(Intent.EXTRA_TEXT, report);
    sendCrashReportActivityResultLauncher.launch(intent);
  }

  private void terminate() {
    Intent intent = new Intent();
    intent.setAction(Intent.ACTION_MAIN);
    intent.addCategory(Intent.CATEGORY_HOME);
    startActivity(intent);
    finish();
  }

}
