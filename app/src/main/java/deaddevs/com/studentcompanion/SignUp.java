package deaddevs.com.studentcompanion;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

	private FirebaseAuth mAuth;
	private final String TAG = "SignUP";
	private FirebaseFirestore db;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		mAuth = FirebaseAuth.getInstance();
		db = FirebaseFirestore.getInstance();
	}

	public void backToLogin(View v) {
		Intent i = new Intent(getApplicationContext(), MainActivity.class);
		startActivity(i);
		setContentView(R.layout.activity_main);
	}


	public void handleSignUp(View v) {
		String first = ((EditText)findViewById(R.id.firstsignup)).getText().toString();
		String last = ((EditText)findViewById(R.id.lastsignup)).getText().toString();
		String email = ((EditText)findViewById(R.id.emailsignup)).getText().toString();
		String canvasKey = ((EditText)findViewById(R.id.tokensignup)).getText().toString();
		String password = ((EditText)findViewById(R.id.passwordsignup)).getText().toString();


		mAuth.createUserWithEmailAndPassword(email, password)
				.addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
					@Override
					public void onComplete(@NonNull Task<AuthResult> task) {
						if (task.isSuccessful()) {
							// Sign in success, update UI with the signed-in user's information
							Log.d(TAG, "createUserWithEmail:success");

						} else {
							Log.w(TAG, "createUserWithEmail:failure", task.getException());
							Toast.makeText(SignUp.this, "Authentication failed.",
									Toast.LENGTH_SHORT).show();
						}
					}
				});

		if(mAuth.getCurrentUser() != null) {
			String uid = mAuth.getCurrentUser().getUid();
			writeNewUser(uid, first, last, email, canvasKey);
			Intent i = new Intent(getApplicationContext(), CoreActivity.class);
			startActivity(i);
			setContentView(R.layout.activity_core);
		}
	}

	public void writeNewUser(String uid, String first, String last, String email, String canvas) {
		Map<String, Object> user = new HashMap<>();
		user.put("uid", uid);
		user.put("first", first);
		user.put("last", last);
		user.put("Email", email);
		user.put("Canvas", canvas);

		db.collection("users").document(uid).set(user)
				.addOnSuccessListener(new OnSuccessListener<Void>() {
					@Override
					public void onSuccess(Void aVoid) {
						Log.d(TAG, "DocumentSnapshot successfully written!");
					}
				})
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Log.w(TAG, "Error writing document", e);
					}
				});
	}

}
