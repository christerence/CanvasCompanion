package deaddevs.com.studentcompanion;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class AssignmentPageFragment extends Fragment {

    String time = "00:00:00";
    TextView timeText;
    boolean startBool;
    Button startStop;
    TimerAsyncTask myTask = new TimerAsyncTask();

    //private OnFragmentInteractionListener mListener;

    public AssignmentPageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_assignment_page, container, false);
        timeText = v.findViewById(R.id.timer);
        startStop = v.findViewById(R.id.stopStudying);
        return v;
    }

//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }
//
//    /**
//     * This interface must be implemented by activities that contain this
//     * fragment to allow an interaction in this fragment to be communicated
//     * to the activity and potentially other fragments contained in that
//     * activity.
//     * <p>
//     * See the Android Training lesson <a href=
//     * "http://developer.android.com/training/basics/fragments/communicating.html"
//     * >Communicating with Other Fragments</a> for more information.
//     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }

    public void startStop(View view) {
        if (!startBool) {
            startStop.setText("Stop Studying");
            myTask.execute();
            startBool = true;
        } else {
            startStop.setText("Start Studying");
            myTask = new TimerAsyncTask();
            startBool = false;
        }
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

        @Override
        protected Void doInBackground(Integer... integers) {
            while (true) {
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
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
