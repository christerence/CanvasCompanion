package deaddevs.com.studentcompanion;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

public class CoreActivity extends AppCompatActivity {
    CourseListFragment course;

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


}
