package com.github.frimtec.android.pikettassist.donation;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;

import com.github.frimtec.android.pikettassist.R;
import com.github.frimtec.android.pikettassist.ui.billing.BillingProvider;
import com.github.frimtec.android.pikettassist.ui.common.EdgeToEdgeHelper;

public class DonationFragment extends DialogFragment {

  private static final String PAYPAL_ME_BASE_LINK = "https://paypal.me/frimtec";
  private static final String PAYPAL_ME_LINK = PAYPAL_ME_BASE_LINK + "?country.x=CH&locale.x=de_DE";
  private static final String PAYPAL_DONATION_TEXT_TEMPLATE =
      "<h3>" +
          "%s" +
          "</h3>" +
          "</p>" +
          "<p>" +
          "<br>" +
          "<a href=\"" + PAYPAL_ME_LINK + "\">" +
          "<img src=\"paypalme.png\"/>" +
          "</<a>" +
          "<br>" +
          "<br>" +
          "<h1>" +
          "<a href=\"" + PAYPAL_ME_LINK + "\">" +
          PAYPAL_ME_BASE_LINK +
          "</a>" +
          "</h1>" +
          "</p>" +
          "<p>" +
          "<br>" +
          "<h3>" +
          "%s" +
          "</h3>" +
          "</p>";

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme);
  }

  @Nullable
  @Override
  public View onCreateView(
      LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState
  ) {
    View root = inflater.inflate(R.layout.donation_fragment, container, false);
    TextView alternativeDonationText = root.findViewById(R.id.alternative_donation_text);
    alternativeDonationText.setVisibility(View.VISIBLE);
    alternativeDonationText.setText(Html.fromHtml(
        String.format(
            PAYPAL_DONATION_TEXT_TEMPLATE,
            getString(R.string.alternative_donation_text),
            getString(R.string.alternative_donation_thanks)
        ),
        Html.FROM_HTML_MODE_COMPACT,
        source -> {
          Drawable image = ResourcesCompat.getDrawable(getResources(), R.drawable.paypalme, null);
          if (image != null) {
            image.setBounds(0, 0, 550, 215);
          }
          return image;
        },
        null
    ));
    alternativeDonationText.setMovementMethod(LinkMovementMethod.getInstance());

    // Setup a toolbar for this fragment
    Toolbar toolbar = root.findViewById(R.id.toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_up);
    toolbar.setNavigationOnClickListener(v -> dismiss());
    toolbar.setTitle(R.string.menu_donate);
    EdgeToEdgeHelper.handleInsets(root);
    return root;
  }

  /**
   * Notifies the fragment that billing manager is ready and provides a BillingProviders
   * instance to access it
   */
  public void onManagerReady(@SuppressWarnings("unused") @Nullable BillingProvider billingProvider) {
  }
}