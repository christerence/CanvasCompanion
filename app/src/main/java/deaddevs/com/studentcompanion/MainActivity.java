package deaddevs.com.studentcompanion;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import deaddevs.com.studentcompanion.utils.DatabaseManager;
import deaddevs.com.studentcompanion.utils.FontAwesomeHelper;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String TAG = "LoginScreen";
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        TextView TradeMark = findViewById(R.id.TradeMark);
        TradeMark.setTypeface(FontAwesomeHelper.getTypeface(this, FontAwesomeHelper.FONTAWESOME));

        EditText passInfo = findViewById(R.id.passInfo);
        passInfo.setTransformationMethod(new PasswordTransformationMethod());
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    public void handleSign(View v) {
        EditText user = findViewById(R.id.userInfo);
        EditText pass = findViewById(R.id.passInfo);

        if(user.getText() != null && pass.getText() != null) {
            signIn(user.getText().toString(), pass.getText().toString());
        }
    }

    public void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "User Not Logged In",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void updateUI(FirebaseUser x) {
        if(x != null) {
            Task<DocumentSnapshot> task =
                    FirebaseFirestore.getInstance().collection("users").document(x.getUid()).get();
            task.addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot o) {
                    Intent i = new Intent(getApplicationContext(), CoreActivity.class);
                    i.putExtra("USER_FIRST", o.getString("first"));
                    i.putExtra("USER_LAST", o.getString("last"));
                    i.putExtra("CANVAS_KEY", o.getString("Canvas"));
                    i.putExtra("USER_EMAIL", o.getString("Email"));
                    ArrayList<String> collection = (ArrayList<String>) o.get("To Do List");
                    i.putExtra("TO_DO_LIST", collection);
                    i.putExtra("FROM", "LOGIN");
                    startActivity(i);
                    setContentView(R.layout.activity_core);
                }
            });
            task.addOnFailureListener(new OnFailureListener() {
                public void onFailure(Exception e) {
                    // handle any errors here
                }
            });

        }
    }

    public String getUserData(String uid) {
        DocumentReference docRef = db.collection("users").document(uid);
        final String[] toReturn = {""};
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, document.getData().toString());
                        toReturn[0] = String.valueOf(document.getData());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
        return toReturn[0];
    }

    public void signUp(View v) {
        Intent i = new Intent(getApplicationContext(), SignUp.class);
        startActivity(i);
        setContentView(R.layout.activity_sign_up);
    }
}
