package com.nychareport.backlog.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.firebase.client.Firebase;
import com.nychareport.backlog.BackLogApplication;
import com.nychareport.backlog.Constants;
import com.nychareport.backlog.R;

/**
 * Created by Ben Rosenthal on 02/05/16.
 */
public class UserOnboardingActivity extends Activity implements AdapterView.OnItemSelectedListener{

    private static final String LOG_TAG = UserOnboardingActivity.class.getSimpleName();

    private Spinner developmentsList;
    private Button proceedButton;
    private String selectedDevelopment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_onboarding);
        initializeViews();
    }

    private void initializeViews() {
        developmentsList = (Spinner) findViewById(R.id.sp_housing_prefs);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.developments_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        developmentsList.setAdapter(adapter);
        developmentsList.setOnItemSelectedListener(this);
        proceedButton = (Button) findViewById(R.id.btn_proceed);
        final SharedPreferences mSharedPref = PreferenceManager.getDefaultSharedPreferences(BackLogApplication.getCurrentInstance());
        final SharedPreferences.Editor mSharedPrefEditor = mSharedPref.edit();
        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedDevelopment != null && !selectedDevelopment.isEmpty()) {
                    mSharedPrefEditor.putString(Constants.KEY_HOUSING_DEVELOPMENT, selectedDevelopment).apply();
                    Intent intent = new Intent(UserOnboardingActivity.this, TabbedHomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedDevelopment = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

