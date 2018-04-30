package deaddevs.com.studentcompanion;

import android.*;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import deaddevs.com.studentcompanion.utils.CanvasApi;
import deaddevs.com.studentcompanion.utils.DatabaseManager;
import deaddevs.com.studentcompanion.utils.MusicCompletionReceiver;
import deaddevs.com.studentcompanion.utils.MusicService;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class CoreActivity extends AppCompatActivity {
    CourseListFragment course;
    AccountFragment profile;
    SettingsFragment settings;
    CalendarFragment calendar;
    CoursePageFragment coursepage;
    AssignmentPageFragment assignmentpage;
    TextFragment textFragment;

    String currPage = "Course";

    String first;
    String last;
    String email;
    String canvasKey;
    String response;
    String courseName;
    String id;

    Boolean cleared = false;

    CanvasApi canvas;

    List<String> courses;
    ArrayList<String> todos;
    DatabaseManager db;

    List<String> toRemove;

    MusicService musicService;
    MusicCompletionReceiver musicCompletionReceiver;
    Intent startMusicServiceIntent;
    boolean isInitialized = false;
    boolean isBound = false;

    public static final String INITIALIZE_STATUS = "intialization status";
    public static final String MUSIC_PLAYING = "music playing";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core);
        db = new DatabaseManager(this);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (getIntent() != null && getIntent().getStringExtra("FROM").equals("LOGIN")) {
            first = getIntent().getStringExtra("USER_FIRST");
            last = getIntent().getStringExtra("USER_LAST");
            email = getIntent().getStringExtra("USER_EMAIL");
            canvasKey = getIntent().getStringExtra("CANVAS_KEY");
            todos = getIntent().getStringArrayListExtra("TO_DO_LIST");
            if (todos == null) {
                todos = new ArrayList<String>();
            }
            if (canvasKey != null) {
                canvas = new CanvasApi(this);
                canvas.initiateRestCallForCourses();
            }
        } else if (getIntent() != null && getIntent().getStringExtra("FROM").equals("ADD")) {
            first = getIntent().getStringExtra("FIRST");
            last = getIntent().getStringExtra("SECOND");
            email = getIntent().getStringExtra("EMAIL");
            canvasKey = getIntent().getStringExtra("CANVASKEY");
            cleared = getIntent().getBooleanExtra("CLEAR", true);
            courses = getIntent().getStringArrayListExtra("COURSE_LIST");
            currPage = getIntent().getStringExtra("CURRPAGE");
            todos = getIntent().getStringArrayListExtra("TODOLIST");
        }

        switch (currPage) {
            case "Course":
                if (findViewById(R.id.CourseList) != null) {
                    if (savedInstanceState != null) {
                        return;
                    }
                    course = new CourseListFragment(this);
                    course.setArguments(getIntent().getExtras());
                    getSupportFragmentManager().beginTransaction().add(R.id.CourseList, course).commit();
                }
                break;
            case "Profile":
                if (findViewById(R.id.Profile) != null) {
                    if (savedInstanceState != null) {
                        return;
                    }
                    profile = new AccountFragment();
                    profile.setArguments(getIntent().getExtras());
                    getSupportFragmentManager().beginTransaction().add(R.id.Profile, profile).commit();
                }
                break;
            case "Settings":
                if (findViewById(R.id.Settings) != null) {
                    if (savedInstanceState != null) {
                        return;
                    }
                    settings = new SettingsFragment();
                    settings.setArguments(getIntent().getExtras());
                    getSupportFragmentManager().beginTransaction().add(R.id.Settings, settings).commit();
                }
                break;
            case "ToDo":
                if (findViewById(R.id.ToDoList) != null) {
                    if (savedInstanceState != null) {
                        return;
                    }
                    calendar = new CalendarFragment();
                    calendar.setArguments(getIntent().getExtras());
                    getSupportFragmentManager().beginTransaction().add(R.id.ToDoList, calendar).commit();
                }
                getSupportFragmentManager().executePendingTransactions();
                break;
            case "Text":
                if (findViewById(R.id.textPage) != null) {
                    if (savedInstanceState != null) {
                        return;
                    }
                    textFragment = new TextFragment();
                    textFragment.setArguments(getIntent().getExtras());
                    getSupportFragmentManager().beginTransaction().add(R.id.textPage, textFragment).commit();
                }
                getSupportFragmentManager().executePendingTransactions();
                break;
        }
        if (savedInstanceState != null && currPage.equals("Course")) {
            isInitialized = savedInstanceState.getBoolean(INITIALIZE_STATUS);
            ((Button) findViewById(R.id.thegood)).setText(savedInstanceState.getString(MUSIC_PLAYING));
        }
        startMusicServiceIntent = new Intent(this, MusicService.class);
        if (!isInitialized) {
            startService(startMusicServiceIntent);
            isInitialized = true;
        }
        musicCompletionReceiver = new MusicCompletionReceiver(this);
        handleLocation();
        //Log.d("address", address);
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
        if (currPage.equals("Course")) {
            outState.putBoolean(INITIALIZE_STATUS, isInitialized);
            outState.putString(MUSIC_PLAYING, ((Button) findViewById(R.id.thegood)).getText().toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            first = savedInstanceState.getString("FIRST");
            last = savedInstanceState.getString("SECOND");
            email = savedInstanceState.getString("EMAIL");
            canvasKey = savedInstanceState.getString("CANVASKEY");
            cleared = savedInstanceState.getBoolean("CLEAR");
            courses = savedInstanceState.getStringArrayList("COURSE_LIST");
            currPage = savedInstanceState.getString("CURRPAGE");
            canvas = new CanvasApi(this);


            switch (currPage) {
                case "Course":
                    if (findViewById(R.id.CourseList) != null) {
                        course = new CourseListFragment(this);
                        course.setArguments(getIntent().getExtras());
                        getSupportFragmentManager().beginTransaction().add(R.id.CourseList, course).commit();
                    }
                    updateList();
                    break;
                case "Profile":
                    if (findViewById(R.id.Profile) != null) {
                        profile = new AccountFragment();
                        profile.setArguments(getIntent().getExtras());
                        getSupportFragmentManager().beginTransaction().add(R.id.Profile, profile).commit();
                    }
                    break;
                case "Settings":
                    if (findViewById(R.id.Settings) != null) {
                        settings = new SettingsFragment();
                        settings.setArguments(getIntent().getExtras());
                        getSupportFragmentManager().beginTransaction().add(R.id.Settings, settings).commit();
                    }
                    break;
                case "ToDo":
                    if (findViewById(R.id.ToDoList) != null) {
                        calendar = new CalendarFragment();
                        calendar.setArguments(getIntent().getExtras());
                        getSupportFragmentManager().beginTransaction().add(R.id.ToDoList, calendar).commit();
                    }
                    break;
                case "Text":
                    if (findViewById(R.id.textPage) != null) {
                        textFragment = new TextFragment();
                        textFragment.setArguments(getIntent().getExtras());
                        getSupportFragmentManager().beginTransaction().add(R.id.textPage, textFragment).commit();
                    }
                    break;
            }
        }
    }

    public void handleGood(View v) {
        if (isBound) {
            switch (musicService.getPlayingStatus()) {
                case 0:
                    musicService.startMusic();
                    break;
                case 1:
                    musicService.pauseMusic();
                    break;
                case 2:
                    musicService.resumeMusic();
                    break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currPage.equals("Course")) {
            TextView view = findViewById(R.id.HelloText);
            view.setText("Hello, " + first + ".");
        } else if (currPage.equals("ToDo")) {
            try {
                updateToDo();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleNav(View v) {
        getSupportFragmentManager().beginTransaction().remove(course).commit();
        switch (v.getId()) {
            case R.id.ProfilePic:
                currPage = "Profile";
                if (findViewById(R.id.Profile) != null) {
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
                if (findViewById(R.id.Settings) != null) {
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
                if (findViewById(R.id.ToDoList) != null) {
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
        if (isBound) {
            unbindService(musicServiceConnection);
            isBound = false;
        }
        unregisterReceiver(musicCompletionReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        db.open();
        if (!cleared) {
            db.deleteAll();
            cleared = true;
        }
        if (isInitialized && !isBound) {
            bindService(startMusicServiceIntent, musicServiceConnection, Context.BIND_AUTO_CREATE);
        }
        registerReceiver(musicCompletionReceiver, new IntentFilter(MusicService.COMPLETE_INTENT));
    }

    public void signOut(View v) {
        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
        setContentView(R.layout.activity_main);
        overridePendingTransition(R.anim.goup, R.anim.godown);
    }

    public void handleBack(View v) {
        Bundle savedInstanceState;
        TextView view;
        switch (currPage) {
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
                canvas = new CanvasApi(this);
                getSupportFragmentManager().beginTransaction().remove(profile).commit();
                if (findViewById(R.id.CourseList) != null) {
                    course = new CourseListFragment(this);
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
                canvas = new CanvasApi(this);
                getSupportFragmentManager().beginTransaction().remove(settings).commit();
                if (findViewById(R.id.CourseList) != null) {
                    course = new CourseListFragment(this);
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
                canvas = new CanvasApi(this);
                getSupportFragmentManager().beginTransaction().remove(calendar).commit();
                if (findViewById(R.id.CourseList) != null) {
                    course = new CourseListFragment(this);
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
            case "CoursePage":
                savedInstanceState = coursepage.getArguments();
                first = savedInstanceState.getString("FIRST");
                last = savedInstanceState.getString("SECOND");
                email = savedInstanceState.getString("EMAIL");
                canvasKey = savedInstanceState.getString("CANVASKEY");
                cleared = savedInstanceState.getBoolean("CLEAR");
                courses = savedInstanceState.getStringArrayList("COURSE_LIST");
                currPage = savedInstanceState.getString("CURRPAGE");
                todos = savedInstanceState.getStringArrayList("TODOLIST");
                canvas = new CanvasApi(this);
                getSupportFragmentManager().beginTransaction().remove(coursepage).commit();
                if (findViewById(R.id.CourseList) != null) {
                    course = new CourseListFragment(this);
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
                //course.setOnClicks();
                currPage = "Course";
                break;
            case "Text":
                savedInstanceState = textFragment.getArguments();
                first = savedInstanceState.getString("FIRST");
                last = savedInstanceState.getString("SECOND");
                email = savedInstanceState.getString("EMAIL");
                canvasKey = savedInstanceState.getString("CANVASKEY");
                cleared = savedInstanceState.getBoolean("CLEAR");
                courses = savedInstanceState.getStringArrayList("COURSE_LIST");
                currPage = savedInstanceState.getString("CURRPAGE");
                todos = savedInstanceState.getStringArrayList("TODOLIST");
                getSupportFragmentManager().beginTransaction().remove(textFragment).commit();
                if (findViewById(R.id.Settings) != null) {
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
                getSupportFragmentManager().executePendingTransactions();
                currPage = "Settings";
                break;
            case "Assignment":
                assignmentpage.stopAsync();
                savedInstanceState = assignmentpage.getArguments();
                first = savedInstanceState.getString("FIRST");
                last = savedInstanceState.getString("SECOND");
                email = savedInstanceState.getString("EMAIL");
                canvasKey = savedInstanceState.getString("CANVASKEY");
                cleared = savedInstanceState.getBoolean("CLEAR");
                courses = savedInstanceState.getStringArrayList("COURSE_LIST");
                currPage = savedInstanceState.getString("CURRPAGE");
                todos = savedInstanceState.getStringArrayList("TODOLIST");
                getSupportFragmentManager().beginTransaction().remove(assignmentpage).commit();
                if (findViewById(R.id.Settings) != null) {
                    coursepage = new CoursePageFragment(this);
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
                    coursepage.setArguments(outState);
                    getSupportFragmentManager().beginTransaction().add(R.id.CoursePage, coursepage).commit();
                }
                getSupportFragmentManager().executePendingTransactions();
                canvas = new CanvasApi(this);
                ((TextView) findViewById(R.id.CourseTitle)).setText(courseName);

                canvas.initiateRestCallForAssignments(id);
                currPage = "CoursePage";
                break;
        }
    }

    public void updateToDo() throws JSONException {
        getSupportFragmentManager().executePendingTransactions();
        ListView todo = findViewById(R.id.ToDos);
        ArrayList<String> todoname = new ArrayList<>();
        JSONArray convertTodo = new JSONArray(todos);
        for (int i = 0; i < todos.size(); i++) {
            JSONObject value = convertTodo.getJSONObject(i);
            String name = value.getString("title");
            todoname.add(name);
        }
        List<String> todoList = todoname;
        CustomAdapter adapter = new CustomAdapter(todoList);
        todo.setAdapter(adapter);
    }

    public void updateList() {
        ListView courselist = findViewById(R.id.CourseListView);
        ArrayList<String> coursesName = new ArrayList<>();
        for (int i = 0; i < courses.size(); i++) {
            String name = courses.get(i).split("///")[1];
            coursesName.add(name);
        }
        List<String> coursesNameAsList = coursesName;
        ArrayAdapter<String> coursesadapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, coursesNameAsList);
        if (courselist != null) {
            courselist.setAdapter(coursesadapter);
        }
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
        overridePendingTransition(R.anim.goup, R.anim.godown);
    }

    public void handleRemove(View v) {
        if (toRemove != null) {
            JSONArray json = new JSONArray(todos);
            for (int i = 0; i < toRemove.size(); i++) {
                for (int j = 0; j < json.length(); j++) {
                    try {
                        JSONObject val = json.getJSONObject(j);
                        if (val.getString("title").equals(toRemove.get(i))) {
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

    public String getResponse() {
        return response;
    }

    public void saveHomeworkResponse(String response) {
        try {
            this.response = response;
            ArrayList<String> names = new ArrayList<>();
            JSONArray obj = new JSONArray(response);
            for (int i = 0; i < obj.length(); i++) {
                JSONObject value = obj.getJSONObject(i);
                names.add(value.getString("name"));
            }
            ListView list = findViewById(R.id.AssignmentList);
            List<String> namesAsList = names;
            ArrayAdapter<String> coursesadapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, namesAsList);
            //added null pointer check to fix crash when rapidly going back and forth from classes
            if (list != null) {
                list.setAdapter(coursesadapter);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public DatabaseManager getDB() {
        return db;
    }

    public void saveInfo(String name, String start, String uid) {
        db.insertCanvasInfo(name, start, uid);
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
            String description = "";
            String date = "";
            String importance = "";

            JSONArray convertTodo = new JSONArray(todos);
            for (int j = 0; j < todos.size(); j++) {
                JSONObject value = null;
                try {
                    value = convertTodo.getJSONObject(j);
                    String tofind = value.getString("title");
                    if (name.equals(tofind)) {
                        description = value.getString("description");
                        date = value.getString("due date");
                        importance = value.getString("importance");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            TextView desc = view.findViewById(R.id.tododesc);
            desc.setText("Description: " + description);
            TextView duedate = view.findViewById(R.id.tododate);
            duedate.setText("Due Date: " + date);

            switch (importance) {
                case "Low":
                    ((LinearLayout) view.findViewById(R.id.wholebackground)).setBackgroundColor(Color.parseColor("#FFB4AC"));
                    break;
                case "Medium":
                    ((LinearLayout) view.findViewById(R.id.wholebackground)).setBackgroundColor(Color.parseColor("#FF4A36"));
                    break;
                case "High":
                    ((LinearLayout) view.findViewById(R.id.wholebackground)).setBackgroundColor(Color.parseColor("#FF1B00"));
                    break;
            }

            if (toRemove == null) {
                toRemove = new ArrayList<>();
            }

            CheckBox repeatChkBx = (CheckBox) view.findViewById(R.id.CheckBoxItem);
            repeatChkBx.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((CheckBox) v).isChecked()) {
                        toRemove.add(name);
                    } else {
                        toRemove.remove(name);
                    }
                }
            });
            return view;
        }
    }

    public void navToCoursePage(String courseName, String id) {
        this.courseName = courseName;
        this.id = id;
        getSupportFragmentManager().beginTransaction().remove(course).commit();
        currPage = "CoursePage";
        if (findViewById(R.id.CourseList) != null) {
            coursepage = new CoursePageFragment(this);
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
            coursepage.setArguments(outState);
            getSupportFragmentManager().beginTransaction().add(R.id.CoursePage, coursepage).commit();
        }
        canvas = new CanvasApi(this);
        getSupportFragmentManager().executePendingTransactions();
        ((TextView) findViewById(R.id.CourseTitle)).setText(courseName);

        canvas.initiateRestCallForAssignments(id);
    }

    public void navToAssignmentPage(String assignmentName) {
        getSupportFragmentManager().beginTransaction().remove(coursepage).commit();
        currPage = "Assignment";
        if (findViewById(R.id.CoursePage) != null) {
            assignmentpage = new AssignmentPageFragment(this);
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
            assignmentpage.setArguments(outState);
            getSupportFragmentManager().beginTransaction().add(R.id.AssignmentPage, assignmentpage).commit();
        }
        canvas = new CanvasApi(this);
        getSupportFragmentManager().executePendingTransactions();
        ((TextView) findViewById(R.id.AssignmentTitle)).setText(assignmentName);
    }

    public void onClickSettings(View view) {
        Bundle savedInstanceState;
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
        if (findViewById(R.id.CourseList) != null) {
            textFragment = new TextFragment();
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
            textFragment.setArguments(outState);
            getSupportFragmentManager().beginTransaction().add(R.id.textPage, textFragment).commit();
        }
        getSupportFragmentManager().executePendingTransactions();
        switch (view.getId()) {
            case R.id.support:
                textFragment.setString(0);
                break;
            case R.id.privacy:
                textFragment.setString(1);
        }
        currPage = "Text";
    }

    public void startStop(View view) {
        assignmentpage.startStop();
        Log.d("startStop in core", "startStop in core");
    }

    public void startDialog() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(CoreActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog, null);
        mBuilder.setView(mView);
        AlertDialog dialog = mBuilder.create();
        dialog.show();

        TextView location = (TextView) mView.findViewById(R.id.dialogLocation);
        location.setText(address);
        //location.setText(Double.toString(lon));
    }

    public void updateName(String musicName) {
        Button good = findViewById(R.id.thegood);
        if (good != null) {
            good.setText(musicName);
        }
    }

    private ServiceConnection musicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicService.MyBinder binder = (MusicService.MyBinder) iBinder;
            musicService = binder.getService();
            isBound = true;
            updateName(musicService.getMusicName());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicService = null;
            isBound = false;
        }
    };

    private FusedLocationProviderClient mFusedLocationClient;
    double lon = 0;
    double lat = 0;
    String address = "";

    public void handleLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            lon = 69;
            lat = 69;
            return;
        }
        final Geocoder geocoder;
        final List<Address>[] addresses = new List[]{null};
        geocoder = new Geocoder(this, Locale.getDefault());
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            lat = location.getLatitude();
                            lon = location.getLongitude();

                            try {
                                addresses[0] = geocoder.getFromLocation(lat, lon, 5); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (addresses[0] != null) {
                                address = addresses[0].get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                            }
                        }
                    }
                });
    }
}

