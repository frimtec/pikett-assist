package com.github.frimtec.android.pikettassist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.github.frimtec.android.pikettassist.R;

import static android.content.Intent.EXTRA_BUG_REPORT;

public class SendLogActivity extends AppCompatActivity {

  public final static String ACTION_SEND_LOG = "android.permission.SEND_LOG";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_send_log);

    Button sendButton = findViewById(R.id.send_log_button_send);
    sendButton.setOnClickListener(v -> sendCrashReport(getIntent().getStringExtra(EXTRA_BUG_REPORT)));

    Button exitButton = findViewById(R.id.send_log_button_exit);
    exitButton.setOnClickListener(v -> terminate());
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    terminate();
  }

  private void sendCrashReport(String report) {
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("plain/text");
    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"frimtec@gmx.ch"});
    intent.putExtra(Intent.EXTRA_SUBJECT, "PAssist crash report");
    intent.putExtra(Intent.EXTRA_TEXT, report);
    startActivityForResult(intent, 1);
  }

  private void terminate() {
    Intent intent = new Intent();
    intent.setAction(Intent.ACTION_MAIN);
    intent.addCategory(Intent.CATEGORY_HOME);
    startActivity(intent);
    finish();
  }

}
