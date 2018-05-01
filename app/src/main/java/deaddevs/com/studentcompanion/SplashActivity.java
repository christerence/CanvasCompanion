package deaddevs.com.studentcompanion;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import deaddevs.com.studentcompanion.MainActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread timer=new Thread()
        {
            public void run() {
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally
                {
                    Intent i=new Intent(SplashActivity.this,MainActivity.class);
                    finish();
                    startActivity(i);
                }
            }
        };
        timer.start();
    }
}
