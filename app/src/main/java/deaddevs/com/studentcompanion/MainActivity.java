package deaddevs.com.studentcompanion;

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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

import deaddevs.com.studentcompanion.utils.FontAwesomeHelper;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String TAG = "LoginScreen";


    private String authKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        TextView title = findViewById(R.id.Title);
        title.setTypeface(FontAwesomeHelper.getTypeface(this, FontAwesomeHelper.FONTAWESOME));
        TextView userText = findViewById(R.id.UserText);
        userText.setTypeface(FontAwesomeHelper.getTypeface(this, FontAwesomeHelper.FONTAWESOME));
        TextView passText = findViewById(R.id.PassText);
        passText.setTypeface(FontAwesomeHelper.getTypeface(this, FontAwesomeHelper.FONTAWESOME));
        TextView trademark = findViewById(R.id.TradeMark);
        trademark.setTypeface(FontAwesomeHelper.getTypeface(this, FontAwesomeHelper.FONTAWESOME));
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
            Intent i = new Intent(getApplicationContext(), CoreActivity.class);
            startActivity(i);
            setContentView(R.layout.activity_core);
        }
    }

    public void signUp(View v) {
        Intent i = new Intent(getApplicationContext(), SignUp.class);
        startActivity(i);
        setContentView(R.layout.activity_sign_up);
    }

    public void saveInfo(String name) {

    }

    public String getAuthKey() {
        return authKey;
    }
}
