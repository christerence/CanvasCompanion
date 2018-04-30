package deaddevs.com.studentcompanion;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import deaddevs.com.studentcompanion.utils.DatabaseManager;

public class AddActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

	private FirebaseAuth mAuth;
	private FirebaseFirestore db;

	private Button cancel_addItem;

	private String TAG = "ADDPAGE";

	int day, month, year, hour, minute;
	int dayFinal, monthFinal, yearFinal, hourFinal, minuteFinal;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add);
		mAuth = FirebaseAuth.getInstance();
		db = FirebaseFirestore.getInstance();

		cancel_addItem = (Button)findViewById(R.id.cancel_addItem);

		Spinner dropdown = findViewById(R.id.importancespinner);
		String[] items = new String[]{"Low", "Medium", "High"};
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
		dropdown.setAdapter(adapter);

		findViewById(R.id.datepicker).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Calendar c = Calendar.getInstance();
				year = c.get(Calendar.YEAR);
				month = c.get(Calendar.MONTH);
				day = c.get(Calendar.DAY_OF_MONTH);

				DatePickerDialog datePickerDialog = new DatePickerDialog(AddActivity.this, AddActivity.this, year, month, day);
				datePickerDialog.show();
			}
		});
	}

	public void handleReset(View v) {
		EditText title = findViewById(R.id.tasktitle);
		EditText description = findViewById(R.id.taskdescription);
		Spinner drop = findViewById(R.id.importancespinner);

		title.setText("");
		description.setText("");
		drop.setSelection(0);
	}

	// added override for back press to prevent crashing to go back to activity_core screen
	@Override
    public void onBackPressed() {
	    this.handleCancel(cancel_addItem);
    }

	public void handleCancel(View v) {
		Intent outState = new Intent(getApplicationContext(), CoreActivity.class);
		outState.putExtra("FIRST", getIntent().getStringExtra("FIRST"));
		outState.putExtra("SECOND", getIntent().getStringExtra("SECOND"));
		outState.putExtra("EMAIL", getIntent().getStringExtra("EMAIL"));
		outState.putExtra("CANVASKEY", getIntent().getStringExtra("CANVASKEY"));
		outState.putExtra("CLEAR", getIntent().getBooleanExtra("CLEAR", true));
		outState.putExtra("COURSE_LIST", getIntent().getStringArrayListExtra("COURSE_LIST"));
		outState.putExtra("CURRPAGE", getIntent().getStringExtra("CURRPAGE"));
		outState.putExtra("FROM", "ADD");
		outState.putExtra("TODOLIST", getIntent().getStringArrayListExtra("TODOLIST"));
		startActivity(outState);
		setContentView(R.layout.activity_core);
	}

	public void handleAddToFirebase(View v) {
		@SuppressLint("RestrictedApi") String uid = mAuth.getUid();
		EditText title = findViewById(R.id.tasktitle);
		EditText description = findViewById(R.id.taskdescription);
		Spinner drop = findViewById(R.id.importancespinner);
		Button date = findViewById(R.id.datepicker);

		// toast message for when title is blank or due date has not been set
		if(title.getText().toString() == null || title.getText().toString().equals("") &&
				date.getText().toString().equals("Set Due Date")) {
			Toast.makeText(getApplicationContext(),"Please enter a title and set a due date", Toast.LENGTH_SHORT).show();
		}
		else if (title.getText().toString() == null || title.getText().toString().equals("")) {
			Toast.makeText(getApplicationContext(),"Please enter a title", Toast.LENGTH_SHORT).show();
		}
		else if (date.getText().toString().equals("Set Due Date")) {
			Toast.makeText(getApplicationContext(),"Please set a due date", Toast.LENGTH_SHORT).show();
		}
		else {

			if (title.getText() != null && description.getText() != null && drop.getSelectedItem().toString() != null) {
				final Map<String, Object> toadd = new HashMap<>();
				toadd.put("title", title.getText().toString());
				toadd.put("description", description.getText().toString());
				toadd.put("importance", drop.getSelectedItem().toString());
				toadd.put("due date", date.getText().toString());

				final ArrayList<Map> toSend = new ArrayList<>();
				toSend.add(toadd);

				final DocumentReference docRef = db.collection("users").document(uid);
				docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
					@Override
					public void onComplete(@NonNull Task<DocumentSnapshot> task) {
						if (task.isSuccessful()) {
							DocumentSnapshot document = task.getResult();
							if (document.exists()) {
								JSONObject map = new JSONObject(document.getData());
								try {
									JSONArray oldToDoList = map.getJSONArray("To Do List");
									for (int i = 0; i < oldToDoList.length(); i++) {
										JSONObject value = oldToDoList.getJSONObject(i);
										String title = value.getString("title");
										String description = value.getString("description");
										String importance = value.getString("importance");
										String date = value.getString("due date");

										Map<String, Object> oldVal = new HashMap<>();
										oldVal.put("title", title);
										oldVal.put("description", description);
										oldVal.put("importance", importance);
										oldVal.put("due date", date);

										toSend.add(oldVal);
									}

									Map<String, Object> newArray = new HashMap<>();
									newArray.put("To Do List", toSend);
									docRef.update(newArray);

									docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
										@Override
										public void onComplete(@NonNull Task<DocumentSnapshot> task) {
											if (task.isSuccessful()) {
												DocumentSnapshot document = task.getResult();
												if (document.exists()) {
													Intent outState = new Intent(getApplicationContext(), CoreActivity.class);
													outState.putExtra("FIRST", getIntent().getStringExtra("FIRST"));
													outState.putExtra("SECOND", getIntent().getStringExtra("SECOND"));
													outState.putExtra("EMAIL", getIntent().getStringExtra("EMAIL"));
													outState.putExtra("CANVASKEY", getIntent().getStringExtra("CANVASKEY"));
													outState.putExtra("CLEAR", getIntent().getBooleanExtra("CLEAR", true));
													outState.putExtra("COURSE_LIST", getIntent().getStringArrayListExtra("COURSE_LIST"));
													outState.putExtra("CURRPAGE", getIntent().getStringExtra("CURRPAGE"));
													outState.putExtra("FROM", "ADD");
													ArrayList<String> newToDoList = (ArrayList<String>) document.get("To Do List");
													outState.putExtra("TODOLIST", newToDoList);
													startActivity(outState);
													setContentView(R.layout.activity_core);
												} else {
													Log.d(TAG, "No such document");
												}
											} else {
												Log.d(TAG, "get failed with ", task.getException());
											}
										}
									});

								} catch (JSONException e) {
									Map<String, Object> newArray = new HashMap<>();
									newArray.put("To Do List", toSend);
									docRef.update(newArray);

									docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
										@Override
										public void onComplete(@NonNull Task<DocumentSnapshot> task) {
											if (task.isSuccessful()) {
												DocumentSnapshot document = task.getResult();
												if (document.exists()) {
													Intent outState = new Intent(getApplicationContext(), CoreActivity.class);
													outState.putExtra("FIRST", getIntent().getStringExtra("FIRST"));
													outState.putExtra("SECOND", getIntent().getStringExtra("SECOND"));
													outState.putExtra("EMAIL", getIntent().getStringExtra("EMAIL"));
													outState.putExtra("CANVASKEY", getIntent().getStringExtra("CANVASKEY"));
													outState.putExtra("CLEAR", getIntent().getBooleanExtra("CLEAR", true));
													outState.putExtra("COURSE_LIST", getIntent().getStringArrayListExtra("COURSE_LIST"));
													outState.putExtra("CURRPAGE", getIntent().getStringExtra("CURRPAGE"));
													outState.putExtra("FROM", "ADD");
													ArrayList<String> newToDoList = (ArrayList<String>) document.get("To Do List");
													outState.putExtra("TODOLIST", newToDoList);
													startActivity(outState);
													setContentView(R.layout.activity_core);
												} else {
													Log.d(TAG, "No such document");
												}
											} else {
												Log.d(TAG, "get failed with ", task.getException());
											}
										}
									});
								}

							} else {
								Log.d(TAG, "get failed with ", task.getException());
							}
						}
					}
				});
			}
		}
	}


	@Override
	public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
		yearFinal = i;
		monthFinal = i1 + 1;
		dayFinal = i2;

		Calendar c = Calendar.getInstance();
		hour = c.get(Calendar.HOUR_OF_DAY);
		minute = c.get(Calendar.MINUTE);

		TimePickerDialog timePickerDialog = new TimePickerDialog(AddActivity.this, AddActivity.this, hour, minute, android.text.format.DateFormat.is24HourFormat(this));
		timePickerDialog.show();
	}

	@Override
	public void onTimeSet(TimePicker timePicker, int i, int i1) {
		hourFinal = i;
		minuteFinal = i1;
		((Button)findViewById(R.id.datepicker)).setText(monthFinal + "/" + dayFinal + "/" + yearFinal + " " + hourFinal + ":" + minuteFinal);
	}
}
