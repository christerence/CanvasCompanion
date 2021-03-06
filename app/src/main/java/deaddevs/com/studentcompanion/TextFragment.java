package deaddevs.com.studentcompanion;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import deaddevs.com.studentcompanion.utils.FontAwesomeHelper;

public class TextFragment extends Fragment {
    String text;
    TextView textFrag;
    TextView title;

    public TextFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_text, container, false);
        TextView TradeMark = v.findViewById(R.id.backbutton);
        TradeMark.setTypeface(FontAwesomeHelper.getTypeface(getContext(), FontAwesomeHelper.FONTAWESOME));
        textFrag = v.findViewById(R.id.textfrag);
        title = v.findViewById(R.id.privacysupport);
        return v;
    }

    public void setString(int i) {
        if (i == 0) {
            text = "For more support call 420-666-6969";
            textFrag.setText(text);
            title.setText("Support");

        } else if (i == 1) {
            title.setText("Privacy Policy");
            text = "Privacy Notice\n" +
                    "This privacy notice discloses the privacy practices for (website address). This privacy notice applies solely to information collected by this website. It will notify you of the following:\n" +
                    "\n" +
                    "What personally identifiable information is collected from you through the website, how it is used and with whom it may be shared.\n" +
                    "What choices are available to you regarding the use of your data.\n" +
                    "The security procedures in place to protect the misuse of your information.\n" +
                    "How you can correct any inaccuracies in the information.\n" +
                    "Information Collection, Use, and Sharing \n" +
                    "We are the sole owners of the information collected on this site. We only have access to/collect information that you voluntarily give us via email or other direct contact from you. We will not sell or rent this information to anyone.\n" +
                    "\n" +
                    "We will use your information to respond to you, regarding the reason you contacted us. We will not share your information with any third party outside of our organization, other than as necessary to fulfill your request, e.g. to ship an order.\n" +
                    "\n" +
                    "Unless you ask us not to, we may contact you via email in the future to tell you about specials, new products or services, or changes to this privacy policy.\n" +
                    "\n" +
                    "Your Access to and Control Over Information \n" +
                    "You may opt out of any future contacts from us at any time. You can do the following at any time by contacting us via the email address or phone number given on our website:\n" +
                    "\n" +
                    "See what data we have about you, if any.\n" +
                    "Change/correct any data we have about you.\n" +
                    "Have us delete any data we have about you.\n" +
                    "Express any concern you have about our use of your data.\n" +
                    "Security \n" +
                    "We take precautions to protect your information. When you submit sensitive information via the website, your information is protected both online and offline.\n" +
                    "\n" +
                    "Wherever we collect sensitive information (such as credit card data), that information is encrypted and transmitted to us in a secure way. You can verify this by looking for a lock icon in the address bar and looking for \"https\" at the beginning of the address of the Web page.\n" +
                    "\n" +
                    "While we use encryption to protect sensitive information transmitted online, we also protect your information offline. Only employees who need the information to perform a specific job (for example, billing or customer service) are granted access to personally identifiable information. The computers/servers in which we store personally identifiable information are kept in a secure environment.\n" +
                    "\n" +
                    "If you feel that we are not abiding by this privacy policy, you should contact us immediately via telephone at XXX YYY-ZZZZ or via email.";
        }
        textFrag.setText(text);
    }
}
