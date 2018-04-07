package deaddevs.com.studentcompanion.utils;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import deaddevs.com.studentcompanion.MainActivity;



/**
 * Grabs the Data from Canvas Api
 * Query Using Restful Api
 *
 * author: Christian Terence S. Cabauatan
 * Android-Chan
 */
public class CanvasApi implements Response.Listener<String>, Response.ErrorListener {

    private String authKey;
    MainActivity mainActivity;
    RequestQueue queue;

    //Example
    //https://canvas.instructure.com/api/v1/courses?access_token=AUTHKEY

    //master prefix
    final String PREFIX_URL = "https://canvas.instructure.com";


    //list of courses
    final String COURSES = "/api/v1/courses";

    //assignment list for a course
    final String ASSIGNNMENTS_PREFIX= "/api/v1/courses/";
    final String ASSIGNMENTS_SUFFX= "/assignments";

    //authenticate
    final String AUTHENTICATE = "?access_token=";

    public CanvasApi(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        authKey = mainActivity.getAuthKey();
    }

    public void initiateRestCallForCourses() {
        StringRequest request = new StringRequest(
                Request.Method.GET, getCourses(),
                this,
                this
        );

        queue.add(request);
    }

    public void initiateRestCallForAssignments(String ID) {
        StringRequest request = new StringRequest(
                Request.Method.GET, getAssignments(ID),
                this,
                this
        );
        queue.add(request);
    }

    public String getCourses() {
        return PREFIX_URL+COURSES+AUTHENTICATE+authKey;
    }

    public String getAssignments(String ID) {
        return PREFIX_URL+ASSIGNNMENTS_PREFIX+ID+ASSIGNMENTS_SUFFX+AUTHENTICATE+authKey;
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(String response) {
        try {
            JSONArray object = new JSONArray(response);
            for(int i = 0; i < object.length(); i++) {
                JSONObject value = object.getJSONObject(i);
                String courseName = value.getString("name");
                mainActivity.saveInfo(courseName);
            }

        } catch(JSONException e) {
            e.printStackTrace();
        }
    }


}
