package deaddevs.com.studentcompanion;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import deaddevs.com.studentcompanion.utils.FontAwesomeHelper;


public class AccountFragment extends Fragment {
    public AccountFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_account, container, false);
        TextView backbutton = v.findViewById(R.id.backbutton);
        backbutton.setTypeface(FontAwesomeHelper.getTypeface(getContext(), FontAwesomeHelper.FONTAWESOME));
        return v;
    }
}
