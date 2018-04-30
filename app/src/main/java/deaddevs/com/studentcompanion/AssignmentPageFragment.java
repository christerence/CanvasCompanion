package deaddevs.com.studentcompanion;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import deaddevs.com.studentcompanion.utils.FontAwesomeHelper;


public class AssignmentPageFragment extends Fragment {

    String time = "00:00:00";
    TextView timeText;
    boolean startBool;
    Button startStop;
    TimerAsyncTask myTask;
    CoreActivity core;

    //private OnFragmentInteractionListener mListener;

    public AssignmentPageFragment() {
        // Required empty public constructor
    }

    @SuppressLint("ValidFragment")
    public AssignmentPageFragment(CoreActivity core) {
        this.core = core;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_assignment_page, container, false);
        TextView TradeMark = v.findViewById(R.id.assignmentbackbutton);
        TradeMark.setTypeface(FontAwesomeHelper.getTypeface(getContext(), FontAwesomeHelper.FONTAWESOME));
        timeText = v.findViewById(R.id.timer);
        startStop = v.findViewById(R.id.stopStudying);
        startBool = false;
        myTask = new TimerAsyncTask();
        return v;
    }


    public void startStop() {
        if (!startBool) {
            startStop.setText("Stop Studying");
            myTask = new TimerAsyncTask();
            myTask.execute();
            startBool = true;
            Log.d("should be executing", "should be executing");
        } else {
            startStop.setText("Start Studying");
            myTask.cancel();
            //myTask = new TimerAsyncTask();
            startBool = false;
            core.startDialog();
        }
    }

    public void stopAsync() {
        myTask.cancel();
        Log.d("stopAsync", "stopAsync");
    }

    int secondsLeft = 0;
    int secondsRight = 0;
    int minutesLeft = 0;
    int minutesRight = 0;
    int hoursLeft = 0;
    int hoursRight = 0;
    int totalSeconds = 0;

    private class TimerAsyncTask extends AsyncTask<Integer, Integer, Void> {


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            timeText.setText(time);
        }

        public void cancel() {
            super.cancel(true);
        }

        @Override
        protected Void doInBackground(Integer... integers) {
            while (!isCancelled()) {
                Log.d("executing", "executing");
                while (startBool) {
                    totalSeconds += 1;
                    if (secondsRight != 9) {
                        secondsRight++;
                    } else if (secondsLeft != 6) {
                        secondsRight = 0;
                        secondsLeft++;
                    } else if (minutesRight != 9) {
                        secondsLeft = 0;
                        minutesRight++;
                    } else if (minutesLeft != 6) {
                        minutesRight = 0;
                        minutesLeft++;
                    } else if (hoursRight != 9) {
                        minutesLeft = 0;
                        hoursRight++;
                    } else if (hoursLeft != 6) {
                        hoursRight = 0;
                        hoursLeft++;
                    }
                    time = Integer.toString(hoursLeft) + Integer.toString(hoursRight) + ":" +
                            Integer.toString(minutesLeft) + Integer.toString(minutesRight) + ":" +
                            Integer.toString(secondsLeft) + Integer.toString(secondsRight);

                    publishProgress();
                    try {
                        Log.d("executing", "executing");
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
    }
}
