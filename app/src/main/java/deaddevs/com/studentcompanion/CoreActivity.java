package deaddevs.com.studentcompanion;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    Boolean cleared = false;

    CanvasApi canvas;

    List<String> courses;
    ArrayList<String> todos;
    DatabaseManager db;

    List<String> toRemove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core);
        db = new DatabaseManager(this);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if(getIntent() != null && getIntent().getStringExtra("FROM").equals("LOGIN")) {
            first = getIntent().getStringExtra("USER_FIRST");
            last = getIntent().getStringExtra("USER_LAST");
            email = getIntent().getStringExtra("USER_EMAIL");
            canvasKey = getIntent().getStringExtra("CANVAS_KEY");
            todos = getIntent().getStringArrayListExtra("TO_DO_LIST");
            if(todos == null) {
                todos = new ArrayList<String>();
            }
            if(canvasKey != null) {
                canvas = new CanvasApi(this);
                canvas.initiateRestCallForCourses();
            }
        } else if(getIntent() != null && getIntent().getStringExtra("FROM").equals("ADD")) {
            first = getIntent().getStringExtra("FIRST");
            last = getIntent().getStringExtra("SECOND");
            email = getIntent().getStringExtra("EMAIL");
            canvasKey = getIntent().getStringExtra("CANVASKEY");
            cleared = getIntent().getBooleanExtra("CLEAR", true);
            courses = getIntent().getStringArrayListExtra("COURSE_LIST");
            currPage = getIntent().getStringExtra("CURRPAGE");
            todos = getIntent().getStringArrayListExtra("TODOLIST");
        }


        switch(currPage) {
            case "Course":
                if(findViewById(R.id.CourseList) != null) {
                    if (savedInstanceState != null) {
                        return;
                    }
                    course = new CourseListFragment();
                    course.setArguments(getIntent().getExtras());
                    getSupportFragmentManager().beginTransaction().add(R.id.CourseList, course).commit();
                }
                break;
            case "Profile":
                if(findViewById(R.id.Profile) != null) {
                    if (savedInstanceState != null) {
                        return;
                    }
                    profile = new AccountFragment();
                    profile.setArguments(getIntent().getExtras());
                    getSupportFragmentManager().beginTransaction().add(R.id.Profile, profile).commit();
                }
                break;
            case "Settings":
                if(findViewById(R.id.Settings) != null) {
                    if (savedInstanceState != null) {
                        return;
                    }
                    settings = new SettingsFragment();
                    settings.setArguments(getIntent().getExtras());
                    getSupportFragmentManager().beginTransaction().add(R.id.Settings, settings).commit();
                }
                break;
            case "ToDo":
                if(findViewById(R.id.ToDoList) != null) {
                    if (savedInstanceState != null) {
                        return;
                    }
                    calendar = new CalendarFragment();
                    calendar.setArguments(getIntent().getExtras());
                    getSupportFragmentManager().beginTransaction().add(R.id.ToDoList, calendar).commit();
                }
                getSupportFragmentManager().executePendingTransactions();
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putString("FIRST", first);
        outState.putString("SECOND", last);
        outState.putString("EMAIL", email);
        outState.putString("CANVASKEY", canvasKey);
        outState.putBoolean("CLEAR", cleared);
        ArrayList<String> savelist = (ArrayList<String>) courses;
        outState.putStringArrayList("COURSE_LIST", savelist);
        outState.putString("CURRPAGE", currPage);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null) {
            first = savedInstanceState.getString("FIRST");
            last = savedInstanceState.getString("SECOND");
            email = savedInstanceState.getString("EMAIL");
            canvasKey = savedInstanceState.getString("CANVASKEY");
            cleared = savedInstanceState.getBoolean("CLEAR");
            courses = savedInstanceState.getStringArrayList("COURSE_LIST");
            currPage = savedInstanceState.getString("CURRPAGE");


            switch(currPage) {
                case "Course":
                    if(findViewById(R.id.CourseList) != null) {
                        course = new CourseListFragment();
                        course.setArguments(getIntent().getExtras());
                        getSupportFragmentManager().beginTransaction().add(R.id.CourseList, course).commit();
                    }
                    updateList();
                    break;
                case "Profile":
                    if(findViewById(R.id.Profile) != null) {
                        profile = new AccountFragment();
                        profile.setArguments(getIntent().getExtras());
                        getSupportFragmentManager().beginTransaction().add(R.id.Profile, profile).commit();
                    }
                    break;
                case "Settings":
                    if(findViewById(R.id.Settings) != null) {
                        settings = new SettingsFragment();
                        settings.setArguments(getIntent().getExtras());
                        getSupportFragmentManager().beginTransaction().add(R.id.Settings, settings).commit();
                    }
                    break;
                case "ToDo":
                    if(findViewById(R.id.ToDoList) != null) {
                        calendar = new CalendarFragment();
                        calendar.setArguments(getIntent().getExtras());
                        getSupportFragmentManager().beginTransaction().add(R.id.ToDoList, calendar).commit();
                    }
                    break;
            }


        }
    }

    public void handleGood(View v) {
        //need to implement music service
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=ISNBfryBkSo")));
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(currPage.equals("Course")) {
            TextView view = findViewById(R.id.HelloText);
            view.setText("Hello, " + first + ".");
        } else if (currPage.equals("ToDo")){
            try {
                updateToDo();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleNav(View v) {
        getSupportFragmentManager().beginTransaction().remove(course).commit();
        switch(v.getId()) {
            case R.id.ProfilePic:
                currPage = "Profile";
                if(findViewById(R.id.Profile) != null) {
                    profile = new AccountFragment();
                    Bundle outState = new Bundle();
                    outState.putString("FIRST", first);
                    outState.putString("SECOND", last);
                    outState.putString("EMAIL", email);
                    outState.putString("CANVASKEY", canvasKey);
                    outState.putBoolean("CLEAR", cleared);
                    ArrayList<String> savelist = (ArrayList<String>) courses;
                    outState.putStringArrayList("COURSE_LIST", savelist);
                    outState.putString("CURRPAGE", currPage);
                    outState.putStringArrayList("TODOLIST", todos);
                    profile.setArguments(outState);
                    getSupportFragmentManager().beginTransaction().add(R.id.Profile, profile).commit();
                }
                getSupportFragmentManager().executePendingTransactions();
                TextView wholename = findViewById(R.id.FullName);
                wholename.setText(first + " " + last);
                TextView emailtext = findViewById(R.id.Email);
                emailtext.setText(email);
                break;
            case R.id.SettingPic:
                currPage = "Settings";
                if(findViewById(R.id.Settings) != null) {
                    settings = new SettingsFragment();
                    Bundle outState = new Bundle();
                    outState.putString("FIRST", first);
                    outState.putString("SECOND", last);
                    outState.putString("EMAIL", email);
                    outState.putString("CANVASKEY", canvasKey);
                    outState.putBoolean("CLEAR", cleared);
                    ArrayList<String> savelist = (ArrayList<String>) courses;
                    outState.putStringArrayList("COURSE_LIST", savelist);
                    outState.putString("CURRPAGE", currPage);
                    outState.putStringArrayList("TODOLIST", todos);
                    settings.setArguments(outState);
                    getSupportFragmentManager().beginTransaction().add(R.id.Settings, settings).commit();
                }
                break;
            case R.id.TodoPic:
                currPage = "ToDo";
                if(findViewById(R.id.ToDoList) != null) {
                    calendar = new CalendarFragment();
                    Bundle outState = new Bundle();
                    outState.putString("FIRST", first);
                    outState.putString("SECOND", last);
                    outState.putString("EMAIL", email);
                    outState.putString("CANVASKEY", canvasKey);
                    outState.putBoolean("CLEAR", cleared);
                    ArrayList<String> savelist = (ArrayList<String>) courses;
                    outState.putStringArrayList("COURSE_LIST", savelist);
                    outState.putString("CURRPAGE", currPage);
                    outState.putStringArrayList("TODOLIST", todos);
                    calendar.setArguments(outState);
                    getSupportFragmentManager().beginTransaction().add(R.id.ToDoList, calendar).commit();
                }
                try {
                    updateToDo();
                } catch (JSONException e) {
                    e.printStackTrace();
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
        if(!cleared) {
            db.deleteAll();
            cleared = true;
        }

    }

    public void signOut(View v) {
        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        setContentView(R.layout.activity_main);
    }

    public void handleBack(View v) {
        Bundle savedInstanceState;
        TextView view;
        switch(currPage) {
            case "Profile":
                savedInstanceState = profile.getArguments();
                first = savedInstanceState.getString("FIRST");
                last = savedInstanceState.getString("SECOND");
                email = savedInstanceState.getString("EMAIL");
                canvasKey = savedInstanceState.getString("CANVASKEY");
                cleared = savedInstanceState.getBoolean("CLEAR");
                courses = savedInstanceState.getStringArrayList("COURSE_LIST");
                currPage = savedInstanceState.getString("CURRPAGE");
                todos = savedInstanceState.getStringArrayList("TODOLIST");
                getSupportFragmentManager().beginTransaction().remove(profile).commit();
                if(findViewById(R.id.CourseList) != null) {
                    course = new CourseListFragment();
                    Bundle outState = new Bundle();
                    outState.putString("FIRST", first);
                    outState.putString("SECOND", last);
                    outState.putString("EMAIL", email);
                    outState.putString("CANVASKEY", canvasKey);
                    outState.putBoolean("CLEAR", cleared);
                    ArrayList<String> savelist = (ArrayList<String>) courses;
                    outState.putStringArrayList("COURSE_LIST", savelist);
                    outState.putString("CURRPAGE", currPage);
                    outState.putStringArrayList("TODOLIST", todos);
                    course.setArguments(outState);
                    getSupportFragmentManager().beginTransaction().add(R.id.CourseList, course).commit();
                }
                getSupportFragmentManager().executePendingTransactions();
                view = findViewById(R.id.HelloText);
                view.setText("Hello, " + first + ".");
                updateList();
                currPage = "Course";
                break;
            case "Settings":
                savedInstanceState = settings.getArguments();
                first = savedInstanceState.getString("FIRST");
                last = savedInstanceState.getString("SECOND");
                email = savedInstanceState.getString("EMAIL");
                canvasKey = savedInstanceState.getString("CANVASKEY");
                cleared = savedInstanceState.getBoolean("CLEAR");
                courses = savedInstanceState.getStringArrayList("COURSE_LIST");
                currPage = savedInstanceState.getString("CURRPAGE");
                todos = savedInstanceState.getStringArrayList("TODOLIST");
                getSupportFragmentManager().beginTransaction().remove(settings).commit();
                if(findViewById(R.id.CourseList) != null) {
                    course = new CourseListFragment();
                    Bundle outState = new Bundle();
                    outState.putString("FIRST", first);
                    outState.putString("SECOND", last);
                    outState.putString("EMAIL", email);
                    outState.putString("CANVASKEY", canvasKey);
                    outState.putBoolean("CLEAR", cleared);
                    ArrayList<String> savelist = (ArrayList<String>) courses;
                    outState.putStringArrayList("COURSE_LIST", savelist);
                    outState.putString("CURRPAGE", currPage);
                    outState.putStringArrayList("TODOLIST", todos);
                    course.setArguments(outState);
                    getSupportFragmentManager().beginTransaction().add(R.id.CourseList, course).commit();
                }
                getSupportFragmentManager().executePendingTransactions();
                view = findViewById(R.id.HelloText);
                view.setText("Hello, " + first + ".");
                updateList();
                currPage = "Course";
                break;
            case "ToDo":
                savedInstanceState = calendar.getArguments();
                first = savedInstanceState.getString("FIRST");
                last = savedInstanceState.getString("SECOND");
                email = savedInstanceState.getString("EMAIL");
                canvasKey = savedInstanceState.getString("CANVASKEY");
                cleared = savedInstanceState.getBoolean("CLEAR");
                courses = savedInstanceState.getStringArrayList("COURSE_LIST");
                currPage = savedInstanceState.getString("CURRPAGE");
                todos = savedInstanceState.getStringArrayList("TODOLIST");
                getSupportFragmentManager().beginTransaction().remove(calendar).commit();
                if(findViewById(R.id.CourseList) != null) {
                    course = new CourseListFragment();
                    Bundle outState = new Bundle();
                    outState.putString("FIRST", first);
                    outState.putString("SECOND", last);
                    outState.putString("EMAIL", email);
                    outState.putString("CANVASKEY", canvasKey);
                    outState.putBoolean("CLEAR", cleared);
                    ArrayList<String> savelist = (ArrayList<String>) courses;
                    outState.putStringArrayList("COURSE_LIST", savelist);
                    outState.putString("CURRPAGE", currPage);
                    outState.putStringArrayList("TODOLIST", todos);
                    course.setArguments(outState);
                    getSupportFragmentManager().beginTransaction().add(R.id.CourseList, course).commit();
                }
                getSupportFragmentManager().executePendingTransactions();
                view = findViewById(R.id.HelloText);
                view.setText("Hello, " + first + ".");
                updateList();
                currPage = "Course";
                break;
        }
    }

    public void updateToDo() throws JSONException {
        getSupportFragmentManager().executePendingTransactions();
        ListView todo = findViewById(R.id.ToDos);
        ArrayList<String> todoname = new ArrayList<>();
        JSONArray convertTodo = new JSONArray(todos);
        for(int i = 0; i < todos.size(); i++) {
            JSONObject value = convertTodo.getJSONObject(i);
            String name = value.getString("title");
            todoname.add(name);
        }
        List<String> todoList = todoname;
        CustomAdapter adapter = new CustomAdapter(todoList);
        todo.setAdapter(adapter);


//        todo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                if(findViewById(R.id.AssignmentPage) != null) {
//                    assignmentpage = new AssignmentPageFragment();
//                    assignmentpage.setArguments(getIntent().getExtras());
//                    getSupportFragmentManager().beginTransaction().add(R.id.AssignmentPage, assignmentpage).commit();
//                }
//                getSupportFragmentManager().executePendingTransactions();
//
//            }
//        });
    }

    public void updateAssignmentPage(){
        TextView title = findViewById(R.id.HWTitle);
        TextView desc = findViewById(R.id.HWDescription);
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
        Intent outState = new Intent(this, AddActivity.class);
        outState.putExtra("FIRST", first);
        outState.putExtra("SECOND", last);
        outState.putExtra("EMAIL", email);
        outState.putExtra("CANVASKEY", canvasKey);
        outState.putExtra("CLEAR", cleared);
        ArrayList<String> savelist = (ArrayList<String>) courses;
        outState.putExtra("COURSE_LIST", savelist);
        outState.putExtra("CURRPAGE", currPage);
        outState.putExtra("TODOLIST", todos);
        startActivity(outState);
        setContentView(R.layout.activity_add);
    }

    public void handleRemove(View v) {
        if(toRemove != null) {
            JSONArray json = new JSONArray(todos);
            for(int i = 0; i < toRemove.size(); i++) {
                for(int j = 0; j < json.length(); j++) {
                    try {
                        JSONObject val = json.getJSONObject(j);
                        if(val.getString("title").equals(toRemove.get(i))) {
                            todos.set(i, "remove");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            todos.removeAll(Collections.singleton("remove"));
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseFirestore ffdb = FirebaseFirestore.getInstance();
            @SuppressLint("RestrictedApi") DocumentReference docRef = ffdb.collection("users").document(mAuth.getUid());
            Map<String, Object> newArray = new HashMap<>();
            newArray.put("To Do List", todos);
            docRef.update(newArray);
            try {
                updateToDo();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveInfo(String name) {
        db.insertCanvasInfo(name);
        courses = db.getAllRecord();
        updateList();
    }

    public String getAuthKey() {
        return canvasKey;
    }

    private class CustomAdapter extends BaseAdapter {
        private List<String> names;
        private int itemid;
        @Override
        public int getCount() {
            return todos.size();
        }

        @Override
        public Object getItem(int i) {
            return 0;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        public CustomAdapter(List<String> names) {
            this.names = names;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.todolistitem, null);

            itemid = i;
            TextView title = view.findViewById((R.id.ToDoItemName));
            title.setText(names.get(i));
            final String name = names.get(i);

            if(toRemove == null) {
            	toRemove = new ArrayList<>();
			}

            CheckBox repeatChkBx = ( CheckBox ) view.findViewById( R.id.CheckBoxItem );
            repeatChkBx.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if ( ((CheckBox)v).isChecked() ) {
                        toRemove.add(name);
                    } else {
                        toRemove.remove(name);
                    }
                }
            });
            return view;
        }
    }

}
