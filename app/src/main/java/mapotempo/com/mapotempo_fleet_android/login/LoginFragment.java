package mapotempo.com.mapotempo_fleet_android.login;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.couchbase.lite.android.AndroidContext;
import com.mapotempo.fleet.api.ManagerFactory;
import com.mapotempo.fleet.api.MapotempoFleetManagerInterface;

import java.util.Timer;
import java.util.TimerTask;

import mapotempo.com.mapotempo_fleet_android.R;
import mapotempo.com.mapotempo_fleet_android.utils.AlertMessageHelper;


/**
 * Responsible for establish a connection with the library that allow you to get access to Mapotempo models. The fragment launch a connection attempt that give you, in some cases, a manager. The manager allow you to get access to the following entities :
 * <ul>
 * <li>Missions</li>
 * <li>User</li>
 * <li>Mission Status</li>
 * <li>Company</li>
 * </ul>
 *
 * <b>Integration</b>
 * <p>
 * <p>As a Fragment you must implement it in your XML file through the following line of code :
 * <code>
 * {@literal <fragment class="mapotempo.com.mapotempo_fleet_android.login.LoginFragment" />}
 * </code>
 * </p>
 *
 * This fragment require the implementation of {@link OnLoginFragmentImplementation} directly in the Activity that hold the Login Fragment.
 *
 * You will have to implement the {@link OnLoginFragmentImplementation#onLoginFragmentImplementation(MapotempoFleetManagerInterface.OnServerConnexionVerify.Status, TimerTask, String[], MapotempoFleetManagerInterface)}which will be called by an async task. If the library doesn't respond then, a timeout will stop the attempt and give the user back to the login page.
 *
 * As you'ill need to use the manager during the whole life cycle of the application, we highly recommend to keep a reference to it in a descendant of Application.
 * </p>
 *
 * </b>Example:</b>
 * <pre>
 * public void onLoginFragmentImplementation(MapotempoFleetManagerInterface.OnServerConnexionVerify.Status status, TimerTask task, String[] logs) {
 *      task.cancel();
 *
 *      switch (status) {
 *         case VERIFY:
 *              if (logs != null)
 *                  keepTraceOfConnectionLogsData(logs);
 *              MapotempoApplication mapotempoApplication = (MapotempoApplication) getApplicationContext();
 *              mapotempoApplication.setManager(manager);
 *              onBackPressed();
 *              finish();
 *              break;
 *          case LOGIN_ERROR:
 *              LoginFragment loginFragment = (LoginFragment) getSupportFragmentManager().findFragmentById(R.id.hook_login_fragment);
 *              loginFragment.toogleLogginView(false);
 *              AlertMessageHelper.errorAlert(this, null, R.string.login_error_title, R.string.login_error_short_text, R.string.login_error_details);
 *              break;
 *      }
 * }
 * </pre>
 */
public class LoginFragment extends Fragment {
    private OnLoginFragmentImplementation mListener;
     // private String dataBaseUrl = "http://192.168.1.108:4984/db";
    private String dataBaseUrl = "http://192.168.1.135:4984/db";

    private String mLogin = null;
    private String mPassword = null;
    private MapotempoFleetManagerInterface iFleetManager = null;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_login, container, false);
        addButtonSubmit(view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLoginFragmentImplementation) {
            mListener = (OnLoginFragmentImplementation) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void addButtonSubmit(View view) {
        final EditText mPasswordView = view.findViewById(R.id.password);
        final EditText mLoginView = view.findViewById(R.id.login);
        final Button button = view.findViewById(R.id.login_sign_in_button);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Mapotempo", 0);

        mLoginView.setText(sharedPreferences.getString("UserLogin", ""));
        mPasswordView.setText(sharedPreferences.getString("UserPassword", ""));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLogin = mLoginView.getText().toString();
                mPassword = mPasswordView.getText().toString();

                attemptLogin();
            }
        });
    }

    /**
     * Try to get connection with existing database through the library.
     * If none as been found in 5 seconds, the TimerTask restart the view.
     */
    private void attemptLogin() {
        final CheckBox checkbox = getActivity().findViewById(R.id.remember_logs);
        final Context context = getContext();

        try {
            loginValid(mLogin, mPassword);
        } catch (InvalidLoginException e) {
            e.getStackTrace();
            return;
        }

        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toogleLogginView(false);
                        AlertMessageHelper.errorAlert(context, null, R.string.login_error_title, R.string.login_error_short_text, R.string.login_error_details);
                    }
                });
            }
        };

        MapotempoFleetManagerInterface.OnServerConnexionVerify onUserAvailable = new MapotempoFleetManagerInterface.OnServerConnexionVerify() {
            @Override
            public void connexion(final MapotempoFleetManagerInterface.OnServerConnexionVerify.Status status, final MapotempoFleetManagerInterface manager) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String[] logs = null;

                        if (checkbox.isChecked())
                            logs = new String[] { mLogin, mPassword };

                        mListener.onLoginFragmentImplementation(status, timerTask, logs, manager);
                    }
                });
            }
        };
        toogleLogginView(true);

        final Timer timer = new Timer();
        timer.schedule(timerTask, 5000);

        ManagerFactory.getManager(new AndroidContext(context.getApplicationContext()),  mLogin, mPassword, onUserAvailable, dataBaseUrl);
        hideCurrentKeyboard();
    }

    /**
     * Toggle the login form view.
     * @param active True: start spinner and kill form.
     */
    public void toogleLogginView(boolean active) {
        final ProgressBar spinner = getActivity().findViewById(R.id.login_progress);
        final LinearLayout form = getActivity().findViewById(R.id.from_login_container);
        final TextView textProgress = getActivity().findViewById(R.id.login_text_progress);

        if (active) {
            textProgress.setVisibility(View.VISIBLE);
            spinner.setVisibility(View.VISIBLE);
            form.setVisibility(View.INVISIBLE);
        } else {
            textProgress.setVisibility(View.GONE);
            spinner.setVisibility(View.GONE);
            form.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Private method called when the virtual android keyboard needs to be hidden.
     */
    private void hideCurrentKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getActivity().getCurrentFocus();
        if (view == null) {
            view = new View(getContext());
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void loginValid(String login, String password) throws InvalidLoginException {
        if ((login == null || password == null) || (login.isEmpty() || password.isEmpty())) {
            throw new InvalidLoginException("Connection login invalid");
        }
    }

    public interface OnLoginFragmentImplementation {
        void onLoginFragmentImplementation(MapotempoFleetManagerInterface.OnServerConnexionVerify.Status status, TimerTask task, String[] loggins,
                                           MapotempoFleetManagerInterface manager);
    }

    public class InvalidLoginException extends Exception {
        public InvalidLoginException(String message) {
            super(message);
        }
    }
}