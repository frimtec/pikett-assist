package com.github.frimtec.android.pikettassist.ui.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.annotation.StringRes;

import com.github.frimtec.android.pikettassist.R;

import java.util.function.BiConsumer;

public class DialogHelper {

  public static void infoDialog(Context context, int titleResourceId, int textResourceId, BiConsumer<DialogInterface, Integer> action) {
    SpannableString message = new SpannableString(Html.fromHtml(context.getString(textResourceId), Html.FROM_HTML_MODE_COMPACT));
    AlertDialog alertDialog = new AlertDialog.Builder(context)
        // set dialog message
        .setTitle(titleResourceId)
        .setMessage(message)
        .setCancelable(true)
        .setPositiveButton("OK", action::accept).create();
    alertDialog.show();
    ((TextView) alertDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
  }

  public static void requirePermissions(Context context, int titleResourceId, int textResourceId, BiConsumer<DialogInterface, Integer> action) {
    AlertDialog alertDialog = new AlertDialog.Builder(context)
        .setTitle(context.getString(R.string.permission_required) + " " + context.getString(titleResourceId))
        .setMessage(textResourceId)
        .setCancelable(true)
        .setPositiveButton("OK", action::accept)
        .create();
    alertDialog.show();
  }

  public static void areYouSure(Context context, DialogInterface.OnClickListener onYes, DialogInterface.OnClickListener onNo) {
    yesNoDialog(context, R.string.general_are_you_sure, onYes, onNo);
  }

  public static void yesNoDialog(Context context, @StringRes int messageId, DialogInterface.OnClickListener onYes, DialogInterface.OnClickListener onNo) {
    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setMessage(messageId)
        .setPositiveButton(R.string.general_yes, onYes)
        .setNegativeButton(R.string.general_no, onNo)
        .show();
  }
}
