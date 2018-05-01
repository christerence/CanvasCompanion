package deaddevs.com.studentcompanion;

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
import android.os.IBinder;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
import java.util.Random;

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
    Boolean notifications = true;
    Boolean location = true;
    Boolean startBool = false;

    AlertDialog dialog;

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

    public void randomText() {
        TextView question = findViewById(R.id.text);
        String[] questionList = new String[] {
                "How's Everything Going?",
                "I hope you're doing well!",
                "Keep up the good work!",
                "Keep track of Homework!",
                "Procrastination == killer.",
                "List Your Homework!",
                "Keep on top on it.",
                "Plan Ahead.",
                "Balance is key."
        };
        Random rand = new Random();
        int n = rand.nextInt(questionList.length);
        question.setText(questionList[n]);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currPage.equals("Course")) {
            TextView view = findViewById(R.id.HelloText);
            view.setText("Hello, " + first + ".");
            randomText();
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
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                @SuppressLint("RestrictedApi") String uid = mAuth.getUid();
                final DocumentReference docRef = db.collection("users").document(uid);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                if (document.get("TotalStudyTime") != null) {
                                    ArrayList<Long> time = (ArrayList<Long>) document.get("TotalStudyTime");

                                    String hourString = Long.toString(time.get(0));
                                    String minuteString = Long.toString(time.get(1));
                                    String secondsString = Long.toString(time.get(2));
                                    if (time.get(0) < 10) {
                                        hourString = "0" + hourString;
                                    }
                                    if (time.get(1) < 10) {
                                        minuteString = "0" + minuteString;
                                    }
                                    if (time.get(2) < 10) {
                                        secondsString = "0" + secondsString;
                                    }

                                    String timeToShow = hourString + ":" + minuteString + ":" + secondsString;
                                    TextView allTime = findViewById(R.id.allTimeText);
                                    allTime.setText(timeToShow);
                                } else {
                                    TextView allTime = findViewById(R.id.allTimeText);
                                    allTime.setText("00:00:00");
                                }

                                TextView location1 = findViewById(R.id.location1);
                                TextView location2 = findViewById(R.id.location2);
                                TextView location3 = findViewById(R.id.location3);

                                if (document.get("StudyLocations") != null) {
                                    ArrayList<String> location = (ArrayList<String>) document.get("StudyLocations");
                                    if (location.size() == 1) location1.setText(location.get(0));
                                    else if (location.size() == 2) {
                                        location1.setText(location.get(0));
                                        location2.setText(location.get(1));
                                    } else if (location.size() != 0) {
                                        location1.setText(location.get(0));
                                        location2.setText(location.get(1));
                                        location3.setText(location.get(2));
                                    }
                                } else {
                                    location1.setText("No Data Available");
                                    location2.setText("No Data Available");
                                    location3.setText("No Data Available");
                                }
                            } else {
                                //Need to Add Error
                            }
                        }
                    }
                });
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

    public void handleAddData(View v) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        @SuppressLint("RestrictedApi") String uid = mAuth.getUid();

        final DocumentReference docRef = db.collection("users").document(uid);

        final String loc = locText.getText().toString();
        final String confidence = confidenceText.getText().toString();
        final String concentration = concentrationText.getText().toString();

        if (concentration.equals("")) {
            Toast.makeText(this, "fill in concentration", Toast.LENGTH_SHORT).show();
            return;
        }
        if (confidence.equals("")) {
            Toast.makeText(this, "fill in confidence", Toast.LENGTH_SHORT).show();
            return;
        }
        assignmentpage.zeroOutTime();


        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<String> oldLoc = null;
                        if (document.get("StudyLocations") != null) {
                            oldLoc = (ArrayList<String>) document.get("StudyLocations");
                            if (!oldLoc.contains(loc)) {
                                oldLoc.add(loc);
                            }
                        } else {
                            oldLoc = new ArrayList<>();
                            oldLoc.add(loc);
                        }

                        Map<String, Object> newLoc = new HashMap<>();
                        newLoc.put("StudyLocations", oldLoc);
                        docRef.update(newLoc);

                        TextView time = findViewById(R.id.timer);
                        String timeTxt = time.getText().toString();
                        String[] parsedTime = timeTxt.split(":");

                        Long hour = Long.parseLong(parsedTime[0]);
                        Long minute = Long.parseLong(parsedTime[1]);
                        Long sec = Long.parseLong(parsedTime[2]);

                        if (document.get("TotalStudyTime") != null) {
                            ArrayList<Long> back = (ArrayList<Long>) document.get("TotalStudyTime");

                            Long newhr = hour + back.get(0);
                            Long newmin = minute + back.get(1);
                            Long newsec = sec + back.get(2);

                            back = new ArrayList<>();
                            back.add(newhr);
                            back.add(newmin);
                            back.add(newsec);

                            Map<String, Object> newTotal = new HashMap<>();
                            newTotal.put("TotalStudyTime", back);
                            docRef.update(newTotal);
                        } else {
                            ArrayList<Long> back = new ArrayList<>();

                            back = new ArrayList<>();
                            back.add(hour);
                            back.add(minute);
                            back.add(sec);

                            Map<String, Object> newTotal = new HashMap<>();
                            newTotal.put("TotalStudyTime", back);
                            docRef.update(newTotal);
                        }

                        TextView courseNameHw = findViewById(R.id.CourseNameHW);
                        TextView hwName = findViewById(R.id.AssignmentTitle);
                        final String title = courseNameHw.getText().toString() + ":" + hwName.getText().toString();

                        ArrayList<Map> individual;
                        Map<String, Object> curr = null;
                        if (document.get("Studied") != null) {
                            individual = (ArrayList<Map>) document.get("Studied");
                            for (int i = 0; i < individual.size(); i++) {
                                Map<String, Object> x = individual.get(i);
                                if (x.get("title").toString().equals(title)) {
                                    curr = individual.get(i);
                                }
                            }
                        } else {
                            individual = new ArrayList<>();
                        }

                        if (curr != null) {
                            ArrayList<Long> oldStudyTime = (ArrayList<Long>) curr.get("StudyTime");
                            Long newIndividualHr = hour + oldStudyTime.get(0);
                            Long newIndividualMin = minute + oldStudyTime.get(1);
                            Long newIndividualSec = sec + oldStudyTime.get(2);

                            oldStudyTime.set(0, newIndividualHr);
                            oldStudyTime.set(1, newIndividualMin);
                            oldStudyTime.set(2, newIndividualSec);

                            final Map<String, Object> toadd = new HashMap<>();
                            toadd.put("title", title);
                            toadd.put("StudyTime", oldStudyTime);
                            int confidenceTotal = Integer.parseInt(confidence);

                            if (confidenceTotal > 10) {
                                Toast.makeText(CoreActivity.this, "confidence too high", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            toadd.put("Confidence", confidenceTotal);

                            individual.add(toadd);

                            Map<String, Object> newIndividual = new HashMap<>();
                            newIndividual.put("Studied", individual);
                            docRef.update(newIndividual);
                        } else {
                            ArrayList<Long> indi = new ArrayList<>(3);
                            indi.add(1L);
                            indi.add(1L);
                            indi.add(1L);
                            indi.set(0, hour);
                            indi.set(1, minute);
                            indi.set(2, sec);

                            final Map<String, Object> toadd = new HashMap<>();
                            toadd.put("title", title);
                            toadd.put("StudyTime", indi);
                            int confidenceTotal = Integer.parseInt(confidence);
                            if (confidenceTotal > 10) {
                                Toast.makeText(CoreActivity.this, "confidence too high", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            toadd.put("Confidence", confidenceTotal);

                            individual.add(toadd);

                            Map<String, Object> newIndividual = new HashMap<>();
                            newIndividual.put("Studied", individual);
                            docRef.update(newIndividual);

                        }

                        TextView timer = findViewById(R.id.timer);
                        timer.setText("00:00:00");

                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        ArrayList<Map> oldData = (ArrayList<Map>) document.get("Studied");
                                        Map<String, Object> requiredData = null;
                                        for (int i = 0; i < oldData.size(); i++) {
                                            Map<String, Object> x = oldData.get(i);
                                            if (x.get("title").equals(title)) {
                                                requiredData = x;
                                            }
                                        }
                                        if (requiredData != null) {
                                            ArrayList<Long> time = (ArrayList<Long>) requiredData.get("StudyTime");
                                            Long hour = time.get(0);
                                            Long minute = time.get(1);
                                            Long seconds = time.get(2);

                                            String hourString = Long.toString(hour);
                                            String minuteString = Long.toString(minute);
                                            String secondsString = Long.toString(seconds);
                                            if (hour < 10) {
                                                hourString = "0" + hourString;
                                            }
                                            if (minute < 10) {
                                                minuteString = "0" + minuteString;
                                            }
                                            if (seconds < 10) {
                                                secondsString = "0" + secondsString;
                                            }

                                            String displayTime = hourString + ":" + minuteString + ":" + secondsString;
                                            ((TextView) findViewById(R.id.timeStudied)).setText(displayTime);
                                            ((TextView) findViewById(R.id.confidence)).setText(requiredData.get("Confidence").toString() + "/10");
                                        } else {
                                            ((TextView) findViewById(R.id.timeStudied)).setText("00:00:00");
                                            ((TextView) findViewById(R.id.confidence)).setText("0/10");
                                        }
                                    } else {
                                        //Need to Add Error
                                    }
                                }
                            }
                        });
                        dialog.dismiss();
                    } else {
                        //Need to Add Error
                    }
                }
            }
        });
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
                randomText();
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
                randomText();
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
                randomText();
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
                randomText();
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
                if (!startBool) {
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
                } else {
                    Toast.makeText(getApplicationContext(), "Finish Studying First", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public ArrayList<String> getLocationList() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        @SuppressLint("RestrictedApi") String uid = mAuth.getUid();

        final DocumentReference docRef = db.collection("users").document(uid);

        final ArrayList<ArrayList<String>> access = new ArrayList<>();
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        access.add((ArrayList<String>) document.get("StudyLocations"));
                    }
                }
            }
        });
        return access.get(0);
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

    public void navToAssignmentPage(String assignmentName, String due) {
        String courseName = ((TextView) findViewById(R.id.CourseTitle)).getText().toString();
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
        if (due.equals("null")) {
            due = due.substring(0, due.indexOf("T"));
        }
        ((TextView) findViewById(R.id.due)).setText(due);
        ((TextView) findViewById(R.id.CourseNameHW)).setText((courseName));
        ((TextView) findViewById(R.id.AssignmentTitle)).setText(assignmentName);

        final String title = courseName + ":" + assignmentName;

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        @SuppressLint("RestrictedApi") String uid = mAuth.getUid();

        final DocumentReference docRef = db.collection("users").document(uid);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if (document.get("Studied") != null) {
                            ArrayList<Map> oldData = (ArrayList<Map>) document.get("Studied");
                            Map<String, Object> requiredData = null;
                            for (int i = 0; i < oldData.size(); i++) {
                                Map<String, Object> x = oldData.get(i);
                                if (x.get("title").equals(title)) {
                                    requiredData = x;
                                }
                            }
                            if (requiredData != null) {
                                ArrayList<Long> time = (ArrayList<Long>) requiredData.get("StudyTime");
                                Long hour = time.get(0);
                                Long minute = time.get(1);
                                Long seconds = time.get(2);

                                String hourString = Long.toString(hour);
                                String minuteString = Long.toString(minute);
                                String secondsString = Long.toString(seconds);
                                if (hour < 10) {
                                    hourString = "0" + hourString;
                                }
                                if (minute < 10) {
                                    minuteString = "0" + minuteString;
                                }
                                if (seconds < 10) {
                                    secondsString = "0" + secondsString;
                                }

                                String displayTime = hourString + ":" + minuteString + ":" + secondsString;
                                ((TextView) findViewById(R.id.timeStudied)).setText(displayTime);
                                ((TextView) findViewById(R.id.confidence)).setText(requiredData.get("Confidence").toString() + "/10");
                            } else {
                                ((TextView) findViewById(R.id.timeStudied)).setText("00:00:00");
                                ((TextView) findViewById(R.id.confidence)).setText("0/10");
                            }
                        } else {
                            ((TextView) findViewById(R.id.timeStudied)).setText("00:00:00");
                            ((TextView) findViewById(R.id.confidence)).setText("0/10");
                        }
                    } else {
                        //Need to Add Error
                    }
                }
            }
        });
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

    public void onClickPermissions(View view) {
        Button locButton = (Button) view.findViewById(R.id.location);
        Button notButton = (Button) view.findViewById(R.id.notifications);
        if (view.getId() == R.id.location) {
            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);

        } else if (view.getId() == R.id.notifications) {
            Intent intent = new Intent();
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", getPackageName());
            intent.putExtra("app_uid", getApplicationInfo().uid);
            startActivity(intent);
        }
    }

    public void startStop(View view) {
        assignmentpage.startStop();
        Log.d("startStop in core", "startStop in core");
    }

    EditText confidenceText;
    EditText concentrationText;
    TextView locText;

    public void startDialog() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(CoreActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog, null);
        mBuilder.setView(mView);
        dialog = mBuilder.create();
        dialog.show();

        locText = (TextView) mView.findViewById(R.id.dialogLocation);
        confidenceText = (EditText) mView.findViewById(R.id.confidence);
        concentrationText = (EditText) mView.findViewById(R.id.concentration);
        locText.setText(address);
    }

    TextView titleReview;
    TextView descriptionReview;


    public void startReview(View v) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(CoreActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.review, null);
        mBuilder.setView(mView);
        dialog = mBuilder.create();
        dialog.show();

        titleReview = mView.findViewById(R.id.reviewtitle);
        descriptionReview = mView.findViewById(R.id.reviewdescription);
    }

    public void handleSendData(View v) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        @SuppressLint("RestrictedApi") String uid = mAuth.getUid();

        String docTitle = uid + ":" + titleReview.getText().toString();
        String title = titleReview.getText().toString();
        String description = descriptionReview.getText().toString();

        final DocumentReference docRef = db.collection("reviews").document(docTitle);

        Map<String, Object> toSend = new HashMap<>();
        toSend.put("Title", title);
        toSend.put("Description", description);
        docRef.set(toSend);
        dialog.dismiss();
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
                        if (location != null) {
                            lat = location.getLatitude();
                            lon = location.getLongitude();

                            try {
                                addresses[0] = geocoder.getFromLocation(lat, lon, 5);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (addresses[0] != null) {
                                address = addresses[0].get(0).getAddressLine(0);
                                address = address.substring(0, address.indexOf(","));
                            }
                        }
                    }
                });
    }
}

