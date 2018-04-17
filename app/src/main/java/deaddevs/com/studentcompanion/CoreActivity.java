package deaddevs.com.studentcompanion;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class CoreActivity extends AppCompatActivity {
    CourseListFragment course;
    AccountFragment profile;
    SettingsFragment settings;
    CalendarFragment calendar;
    CoursePageFragment coursepage;
    AssignmentPageFragment assignmentpage;

    String currPage = "Course";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core);

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




}
