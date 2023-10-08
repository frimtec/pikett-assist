package com.github.frimtec.android.pikettassist.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.github.frimtec.android.pikettassist.R;
import com.takisoft.preferencex.EditTextPreference;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegExpTextPreference extends EditTextPreference {

  private static final int NO_GROUP_MIN_COUNT_CHECK = 0;
  private static final int NO_GROUP_MAX_COUNT_CHECK = -1;

  private int minGroups = NO_GROUP_MIN_COUNT_CHECK;
  private int maxGroups = NO_GROUP_MAX_COUNT_CHECK;

  @SuppressWarnings("unused")
  public RegExpTextPreference(Context context) {
    super(context);
    setupView(context);
  }

  @SuppressWarnings("unused")
  public RegExpTextPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
    configureAttributes(context, attrs, 0);
    setupView(context);
  }

  @SuppressWarnings("unused")
  public RegExpTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    configureAttributes(context, attrs, defStyleAttr);
    setupView(context);
  }

  @SuppressWarnings("unused")
  public RegExpTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    configureAttributes(context, attrs, defStyleAttr);
    setupView(context);
  }

  private void configureAttributes(Context context, AttributeSet attrs, int defStyleAttr) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      try (TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RegExpTextPreference, defStyleAttr, 0)) {
        extractedGroups(a);
      }
    } else {
      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RegExpTextPreference, defStyleAttr, 0);
      try {
        extractedGroups(a);
      } finally {
        a.recycle();
      }
    }
  }

  private void extractedGroups(TypedArray a) {
    minGroups = a.getInteger(R.styleable.RegExpTextPreference_min_groups, NO_GROUP_MIN_COUNT_CHECK);
    maxGroups = a.getInteger(R.styleable.RegExpTextPreference_max_groups, NO_GROUP_MAX_COUNT_CHECK);
  }

  private void setupView(Context context) {
    setOnBindEditTextListener(editText -> {
      setupHelpButton(context, editText);
      setupValidator(context, editText);
    });
  }

  private void setupValidator(Context context, EditText editText) {
    editText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override
      public void afterTextChanged(Editable s) {
        boolean hasError = false;
        try {
          Pattern pattern = Pattern.compile(s.toString());
          if (minGroups > 0) {
            Matcher matcher = pattern.matcher("");
            if (matcher.groupCount() < minGroups) {
              editText.setError(String.format(context.getString(R.string.error_regexp_groups_min), RegExpTextPreference.this.minGroups));
              hasError = true;
            }
          }
          if (maxGroups > NO_GROUP_MAX_COUNT_CHECK) {
            Matcher matcher = pattern.matcher("");
            if (matcher.groupCount() > maxGroups) {
              editText.setError(String.format(context.getString(R.string.error_regexp_groups_max), RegExpTextPreference.this.maxGroups));
              hasError = true;
            }
          }
        } catch (PatternSyntaxException e) {
          editText.setError(context.getString(R.string.error_regexp_pattern));
          hasError = true;
        }
        editText.getRootView().findViewById(android.R.id.button1).setEnabled(!hasError);
      }
    });
  }

  private void setupHelpButton(Context context, EditText editText) {
    ViewGroup rootView = (ViewGroup) editText.getRootView();
    ViewGroup viewGroup = (ViewGroup) rootView.getChildAt(0);
    if (viewGroup != null) {
      Button helpButton = new Button(context);
      helpButton.setText(R.string.button_regexp_help);

      helpButton.setOnClickListener(v -> {
        String url = "https://regex101.com/?regex=" + encoded(editText.getText().toString());
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        context.startActivity(i);
      });
      viewGroup.addView(helpButton);
    }
  }

  private String encoded(String text) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      return URLEncoder.encode(text, StandardCharsets.UTF_8);
    } else {
      try {
        //noinspection CharsetObjectCanBeUsed
        return URLEncoder.encode(text, StandardCharsets.UTF_8.name());
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException("Cannot encode text", e);
      }
    }
  }
}
