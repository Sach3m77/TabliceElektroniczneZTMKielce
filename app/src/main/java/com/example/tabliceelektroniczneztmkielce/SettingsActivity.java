package com.example.tabliceelektroniczneztmkielce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;

/**
 * Activity wyświetlające sekcję z ustawieniami.
 * @author Paweł Sacha, Mateusz Pacak, Bartosz Ryś
 * @version 1.0
 */

public class SettingsActivity extends AppCompatActivity {

    /**
     * Metoda tworząca activity.
     * @param savedInstanceState Nieużuwane.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.settings);
        actionBar.setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settingsFrameLayout, new SettingsFragment())
                .commit();

    }

    /**
     * Metoda wywołana przez system, gdy konfiguracja urządzenia ulegnie zmianie podczas aktywności użytkownika.
     * @param newConfig Nowa konfiguracja urządzenia. Ta wartość nie może być pusta.
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String themeKey = sharedPreferences.getString("theme_key", "");

        if(themeKey.equals("0")) {
            switch (newConfig.uiMode & newConfig.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_YES:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    recreate();
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    recreate();
                    break;
            }
        }
    }

    /**
     * Metoda pozwalająca określić jak ma się zachować aplikacja gdy użytkownik kliknię strzałke na pasku akcji.
     * @param item Wybrany element menu. Ta wartość nie może być pusta.
     * @return True lub false;
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Metoda wywołana gdy activity wykryje naciśnięcie przez użytkownika klawisza wstecz.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }
}