package deaddevs.com.studentcompanion;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import deaddevs.com.studentcompanion.utils.CanvasApi;
import deaddevs.com.studentcompanion.utils.DatabaseManager;

public class CoreActivity extends AppCompatActivity {
    CourseListFragment course;
    AccountFragment profile;
    SettingsFragment settings;
    CalendarFragment calendar;
    CoursePageFragment coursepage;
    AssignmentPageFragment assignmentpage;

    String currPage = "Course";

    String first;
    String last;
    String email;
    String canvasKey;

    CanvasApi canvas;

    List<String> courses;
    DatabaseManager db;

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        db.deleteAll();
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core);
        db = new DatabaseManager(this);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if(findViewById(R.id.CourseList) != null) {
            if (savedInstanceState != null) {
                return;
            }
            course = new CourseListFragment();
            course.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.CourseList, course).commit();
        }
        if(getIntent() != null) {
            first = getIntent().getStringExtra("USER_FIRST");
            last = getIntent().getStringExtra("USER_LAST");
            email = getIntent().getStringExtra("USER_EMAIL");
            canvasKey = getIntent().getStringExtra("CANVAS_KEY");
        }

        if(canvasKey != null) {
            canvas = new CanvasApi(this);

            canvas.initiateRestCallForCourses();
        }
    }

	@Override
    protected void onStart() {
        super.onStart();
        TextView view = findViewById(R.id.HelloText);
        view.setText("Hello, " + first + ".");
    }

    public void handleNav(View v) {
        getSupportFragmentManager().beginTransaction().remove(course).commit();
        switch(v.getId()) {
            case R.id.ProfilePic:
                currPage = "Profile";
                if(findViewById(R.id.Profile) != null) {
                    profile = new AccountFragment();
                    profile.setArguments(getIntent().getExtras());
                    getSupportFragmentManager().beginTransaction().add(R.id.Profile, profile).commit();
                }
                break;
            case R.id.SettingPic:
                currPage = "Settings";
                if(findViewById(R.id.Profile) != null) {
                    settings = new SettingsFragment();
                    settings.setArguments(getIntent().getExtras());
                    getSupportFragmentManager().beginTransaction().add(R.id.Settings, settings).commit();
                }
                break;
            case R.id.TodoPic:
                currPage = "ToDo";
                if(findViewById(R.id.Profile) != null) {
                    calendar = new CalendarFragment();
                    calendar.setArguments(getIntent().getExtras());
                    getSupportFragmentManager().beginTransaction().add(R.id.ToDoList, calendar).commit();
                }
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        db.close();
    }
    @Override
    protected void onResume() {
        super.onResume();
        db.open();
    }

    public void signOut(View v) {
        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        setContentView(R.layout.activity_main);
    }

    public void handleBack(View v) {
        switch(currPage) {
            case "Profile":
                getSupportFragmentManager().beginTransaction().remove(profile).commit();
                if(findViewById(R.id.CourseList) != null) {
                    course = new CourseListFragment();
                    course.setArguments(getIntent().getExtras());
                    getSupportFragmentManager().beginTransaction().add(R.id.CourseList, course).commit();
                }
                currPage = "Course";
                break;
            case "Settings":
                getSupportFragmentManager().beginTransaction().remove(settings).commit();
                if(findViewById(R.id.CourseList) != null) {
                    course = new CourseListFragment();
                    course.setArguments(getIntent().getExtras());
                    getSupportFragmentManager().beginTransaction().add(R.id.CourseList, course).commit();
                }
                currPage = "Course";
                break;
            case "ToDo":
                getSupportFragmentManager().beginTransaction().remove(calendar).commit();
                if(findViewById(R.id.CourseList) != null) {
                    course = new CourseListFragment();
                    course.setArguments(getIntent().getExtras());
                    getSupportFragmentManager().beginTransaction().add(R.id.CourseList, course).commit();
                }
                currPage = "Course";
                break;
        }

    }

    public void updateList() {
        ListView courselist = findViewById(R.id.CourseListView);
        ArrayList<String> coursesName = new ArrayList<>();
        for(int i = 0; i < courses.size(); i++) {
            String name = courses.get(i).split("///")[1];
            coursesName.add(name);
        }
        List<String> coursesNameAsList = coursesName;
        ArrayAdapter<String> coursesadapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, coursesNameAsList);
        courselist.setAdapter(coursesadapter);
    }

    public void handleAdd(View v) {
        Intent i = new Intent(this, AddActivity.class);
        startActivity(i);
        setContentView(R.layout.activity_add);
    }

    public void handleRemove(View v) {
        //remove from list
    }

    public void saveInfo(String name) {
        db.insertCanvasInfo(name);
        courses = db.getAllRecord();
        updateList();
    }

    public String getAuthKey() {
        return canvasKey;
    }
}
