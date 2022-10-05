package com.example.tabliceelektroniczneztmkielce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashSet;
import java.util.Set;

/**
 * Activity wyświetlające sekcję z przystankami dodanymi przez użytkownika do ulubionych.
 * @author Paweł Sacha, Mateusz Pacak, Bartosz Ryś
 * @version 1.0
 */

public class FavoriteActivity extends AppCompatActivity {

    //widgets
    private ListView mFavoriteListView;
    private RelativeLayout mFavoriteLayout;
    private RelativeLayout mDeleteLayout;
    private TextView mFavoriteText;

    //vars
    private Database mDataBase;
    private Cursor mCursor;
    private SimpleCursorAdapter mCursorAdapter;
    private Set<String> mRowId;

    /**
     * Metoda tworząca activity.
     * @param savedInstanceState Nieużuwane.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        mFavoriteListView = findViewById(R.id.favoriteListView);
        mFavoriteLayout = findViewById(R.id.favoriteLayout);
        mDeleteLayout = findViewById(R.id.deleteLayout);
        mFavoriteText = findViewById(R.id.favoriteText);
        mRowId = new HashSet<String>();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.favorite_stops);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mDataBase = new Database(this);
        mDataBase.createDataBase();
        mDataBase.openDataBase();

        mCursor = mDataBase.fetchAllFavoriteBusStops();

        mCursorAdapter = new SimpleCursorAdapter(this,
                R.layout.favorite_item,
                mCursor, new String[]{"nazwa"},
                new int[]{R.id.favoriteItemText}, 0);

        mFavoriteListView.setAdapter(mCursorAdapter);

        if(mFavoriteListView.getAdapter().getCount() == 0) {
            mFavoriteText.setVisibility(View.VISIBLE);
        }

        mFavoriteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i = 0; i < mFavoriteListView.getCount(); i++) {
                    mDeleteLayout.setVisibility(View.GONE);
                    mFavoriteListView.getAdapter().getView(i, mFavoriteListView.getChildAt(i), mFavoriteListView).setBackgroundColor(Color.TRANSPARENT);
                }
            }
        });

        mFavoriteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mCursor = (Cursor) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(FavoriteActivity.this, WebViewActivity.class);
                intent.putExtra("link", mCursor.getString(4));
                startActivity(intent);
            }
        });

        mFavoriteListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                mCursor = (Cursor) adapterView.getItemAtPosition(i);

                if(!mRowId.contains(String.valueOf(mCursor.getInt(0)))) {
                    mDeleteLayout.setVisibility(View.VISIBLE);
                    view.setBackgroundColor(Color.parseColor("#99818080"));
                    mRowId.add(String.valueOf(mCursor.getInt(0)));
                }else {
                    view.setBackgroundColor(Color.TRANSPARENT);
                    mRowId.remove(String.valueOf(mCursor.getInt(0)));
                }

                return true;
            }
        });

        mDeleteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] id = mRowId.toArray(new String[mRowId.size()]);

                ContentValues contentValues = new ContentValues();
                contentValues.put("ulubione", 0);

                if(mDataBase.updateBusStopToFavorite(contentValues, id)){
                    if(mRowId.size() == 1)
                        Toast.makeText(FavoriteActivity.this, R.string.stop_removed_from_favorites, Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(FavoriteActivity.this, R.string.stops_removed_from_favorites, Toast.LENGTH_SHORT).show();
                }

                mCursor = mDataBase.fetchAllFavoriteBusStops();
                mCursorAdapter.changeCursor(mCursor);
                mCursorAdapter.notifyDataSetChanged();

                mDeleteLayout.setVisibility(View.GONE);

                recreate();
            }
        });
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