package deaddevs.com.studentcompanion;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import deaddevs.com.studentcompanion.utils.FontAwesomeHelper;

public class CalendarFragment extends Fragment {
    public CalendarFragment() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_calendar, container, false);
        TextView courselist = v.findViewById(R.id.CourseListPic2);
        TextView todoList = v.findViewById(R.id.TodoPic2);

        courselist.setTypeface(FontAwesomeHelper.getTypeface(getContext(), FontAwesomeHelper.FONTAWESOME));
        todoList.setTypeface(FontAwesomeHelper.getTypeface(getContext(), FontAwesomeHelper.FONTAWESOME));
        return v;
    }
}
