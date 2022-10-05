package com.example.tabliceelektroniczneztmkielce;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceFragmentCompat;
import java.util.Locale;

/**
 * Fragment, pozwalający używać "Preference" do budowania ustawień.
 * @author Paweł Sacha, Mateusz Pacak, Bartosz Ryś
 * @version 1.0
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    private String mValue;

    /**
     * Metoda wywoływana podczas onCreate(Bundle), aby podać preferencje dla tego fragmentu.
     * @param savedInstanceState Jeśli fragment jest odtwarzany z poprzedniego zapisanego stanu, to to jest ten stan.
     * @param rootKey Jeśli nie ma wartości null, ten fragment preferencji powinien być zakorzeniony w PreferenceScreen za pomocą tego klucza.
     */
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);

    }

    /**
     * Metoda nasłuchująca zmiany w ustawieniach
     */
    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

            mValue = sharedPreferences.getString(s, "");

            if(s.equals("theme_key")) {
                switch (mValue) {
                    case "0":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                        break;
                    case "1":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        getActivity().recreate();
                        break;
                    case "2":
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        getActivity().recreate();
                        break;
                }

            } else if(s.equals("language_key")) {

                switch (mValue) {
                    case "0":
                        String langCode = getResources().getConfiguration().locale.getLanguage();
                        setLocal(getActivity(), langCode);
                        getActivity().recreate();
                        break;
                    case "1":
                        setLocal(getActivity(), "en");
                        getActivity().recreate();
                        break;
                    case "2":
                        setLocal(getActivity(), "pl");
                        getActivity().recreate();
                        break;
                    case "3":
                        setLocal(getActivity(), "it");
                        getActivity().recreate();
                        break;
                }

            }

        }
    };

    /**
     * Metoda ustawiająca język w zależnośći od "landCode".
     * @param activity Activity.
     * @param langCode Kod językowy. Np. "en", "pl", "it".
     */
    public void setLocal(@NonNull Activity activity, String langCode) {
        Locale locale = new Locale(langCode);
        locale.setDefault(locale);
        Resources resources = activity.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    /**
     * Metoda wywoływana po onRestoreInstanceState(Bundle), onRestart() lub onPause().
     */
    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Metoda wywoływana jako część cyklu życia działania, gdy użytkownik nie wchodzi już aktywnie w interakcję z activity,
     * ale nadal jest widoczny na ekranie. Odpowiednik onResume().
     */
    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
    }

}
