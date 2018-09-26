package io.assignment.sportcard;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class SportsCardApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

    }
}
