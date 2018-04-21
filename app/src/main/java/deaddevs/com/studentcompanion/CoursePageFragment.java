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

public class CoursePageFragment extends Fragment {
    public CoursePageFragment() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_course_page, container, false);
        return v;
    }
}
