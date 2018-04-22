package deaddevs.com.studentcompanion;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import deaddevs.com.studentcompanion.utils.FontAwesomeHelper;

public class CourseListFragment extends Fragment {
    public CourseListFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_course_list, container, false);
        TextView profile = v.findViewById(R.id.ProfilePic);
        TextView setting = v.findViewById(R.id.SettingPic);
        TextView courselist = v.findViewById(R.id.CourseListPic);
        TextView todoList = v.findViewById(R.id.TodoPic);

        profile.setTypeface(FontAwesomeHelper.getTypeface(getContext(), FontAwesomeHelper.FONTAWESOME));
        setting.setTypeface(FontAwesomeHelper.getTypeface(getContext(), FontAwesomeHelper.FONTAWESOME));
        courselist.setTypeface(FontAwesomeHelper.getTypeface(getContext(), FontAwesomeHelper.FONTAWESOME));
        todoList.setTypeface(FontAwesomeHelper.getTypeface(getContext(), FontAwesomeHelper.FONTAWESOME));
        return v;
    }
}
