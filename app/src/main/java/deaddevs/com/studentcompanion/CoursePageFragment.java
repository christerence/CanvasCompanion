package deaddevs.com.studentcompanion;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import deaddevs.com.studentcompanion.utils.DatabaseManager;
import deaddevs.com.studentcompanion.utils.FontAwesomeHelper;

public class CoursePageFragment extends Fragment {

    CoreActivity core;
    ListView assignmentList;
    DatabaseManager db;

    public CoursePageFragment() {}

    @SuppressLint("ValidFragment")
    public CoursePageFragment(CoreActivity core) {
        this.core = core;
        db = core.getDB();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_course_page, container, false);
        TextView TradeMark = v.findViewById(R.id.backbutton);
        TradeMark.setTypeface(FontAwesomeHelper.getTypeface(getContext(), FontAwesomeHelper.FONTAWESOME));
        assignmentList = v.findViewById(R.id.AssignmentList);

        setOnClicks();

        return v;
    }

    public void setOnClicks() {
        assignmentList.setClickable(true);
        assignmentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                Object o = assignmentList.getItemAtPosition(position);
                String str = (String) o;
                ArrayList<String> names = new ArrayList<>();
                JSONArray obj = null;
                try {
                    obj = new JSONArray(core.getResponse());
                    for(int i = 0; i < obj.length(); i++) {
                        JSONObject value = obj.getJSONObject(i);
                        names.add(value.getString("name"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                core.navToAssignmentPage(str);
            }
        });
    }
}
