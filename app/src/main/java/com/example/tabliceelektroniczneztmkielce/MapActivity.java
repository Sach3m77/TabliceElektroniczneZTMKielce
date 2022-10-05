package com.example.tabliceelektroniczneztmkielce;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Główne activity wyświetlające się gdy użytkownik uruchamia piewszy raz aplikację.
 * Activity wyświetlające mapę.
 * @author Paweł Sacha, Mateusz Pacak, Bartosz Ryś
 * @version 1.0
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MapActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final float DEFAULT_ZOOM = 15f;
    private static final float BUS_STOP_ZOOM = 18f;

    //widgets
    private AutoCompleteTextView mSearchText;
    private RelativeLayout mGpsLayout;
    private RelativeLayout mFavoriteLayout;
    private RelativeLayout mTimeLayout;
    private ImageView mFavoriteIcon;
    private ImageView mCancelIcon;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Toolbar mToolbar;

    //vars
    private Boolean mLocationPermissionGranted = false;
    private Boolean mAnimateCameraWork = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Database mDataBase;
    private SimpleCursorAdapter mCursorAdapter;
    private Cursor mCursor;
    private Marker mMarker;
    private ArrayList<Marker> mMarkerArray;
    private double mZoom;
    private InputMethodManager mInputMethodManager;
    private SharedPreferences mSharedPreferences;
    private String mFirstRun;
    private String mThemeKey;
    private boolean mIsSoftKeyboardVisible = false;

    /**
     * Metoda tworząca activity.
     * @param savedInstanceState Nieużuwane.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPreferences.edit().putBoolean("first_run", true).commit();

        mSearchText = findViewById(R.id.searchText);
        mGpsLayout = findViewById(R.id.gpsLayout);
        mFavoriteLayout = findViewById(R.id.heartLayout);
        mTimeLayout = findViewById(R.id.timeLayout);
        mFavoriteIcon = findViewById(R.id.favoriteIcon);
        mCancelIcon = findViewById(R.id.ic_cancel);
        mDrawerLayout = findViewById(R.id.drawerLayout);
        mNavigationView = findViewById(R.id.navigationView);
        mToolbar = findViewById(R.id.toolbar);

        mMarkerArray = new ArrayList<Marker>();


        mNavigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        mNavigationView.setNavigationItemSelectedListener(this);
        mNavigationView.setCheckedItem(R.id.nav_map);

        if (isServicesOK())
            getLocationPermission();
    }

    /**
     * Metoda ładująca mapę.
     */
    private void initMap() {
        Log.d(TAG, "initMap: Initializig map...");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Metoda wywołana gdy mapa jest gotowa do użycia.
     * @param googleMap Zmienna odwołująca się do mapy.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: Map is Ready!");

        mMap = googleMap;

        if (mLocationPermissionGranted) {
            getDeviceLocation();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.style_json_no_bus_stops)));

            mDataBase = new Database(MapActivity.this);
            mDataBase.createDataBase();
            mDataBase.openDataBase();

            mCursor = mDataBase.fetchAllBusStops();

            mCursorAdapter = new CustomSimpleCursorAdapter(this,
                    R.layout.cursor_item,
                    mCursor, new String[]{"nazwa"},
                    new int[]{R.id.cursorItemText}, 0);

            mSearchText.setAdapter(mCursorAdapter);

            init();
        }
    }

    /**
     * Metoda inicjalizująca wszytkie metody anonimowe
     */
    private void init() {
        Log.d(TAG, "init: Inintializing");

        Intent intent2 = getIntent();
        mFirstRun = intent2.getStringExtra("first_run");

        setTheme();
        setLanguage();
        addAllBusStopsMarker();

        mCursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence charSequence) {
                if (mIsSoftKeyboardVisible) {
                    return mDataBase.fetchBusStopsByName(charSequence.toString());
                }else
                    return null;
            }
        });

        mCursorAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter() {
            @Override
            public CharSequence convertToString(Cursor cursor) {
                mCancelIcon.setVisibility(View.VISIBLE);
                return cursor.getString(1);
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                if (!(mSearchText.getText().toString()).equals(""))
                    mSearchText.setText("");
                mCancelIcon.setVisibility(View.GONE);
                hideSoftKeyboard();
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                if (marker.getSnippet().startsWith("http://sip.ztm.kielce.pl/Home/TimeTableReal?busStopId=")) {
                    Log.d(TAG, "onMarkerClick: Marker clicked with position:" + marker.getPosition().latitude + " " + marker.getPosition().longitude);

                    hideSoftKeyboard();
                    if (!mSearchText.getText().toString().equals(marker.getTitle()))
                        mSearchText.setText(marker.getTitle());

                    mCursor = mDataBase.fetchBusStopsByUrl(marker.getSnippet());
                    mCursor.moveToFirst();
                    mFavoriteLayout.setVisibility(View.VISIBLE);
                    mTimeLayout.setVisibility(View.VISIBLE);
                    updateFavoriteIcon(mCursor.getInt(5));

                    mAnimateCameraWork = true;

                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), BUS_STOP_ZOOM), new GoogleMap.CancelableCallback() {
                        @Override
                        public void onCancel() {

                        }

                        @Override
                        public void onFinish() {
                            mAnimateCameraWork = false;
                        }
                    });

                    return true;
                }

                return false;
            }
        });

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {

                mZoom = mMap.getCameraPosition().zoom;

                if (mZoom < DEFAULT_ZOOM) {
                    for (Marker marker : mMarkerArray)
                        marker.setVisible(false);
                } else {
                    for (Marker marker : mMarkerArray)
                        marker.setVisible(true);
                }

                if (!mAnimateCameraWork) {
                    Log.d(TAG, "onCameraMove: Camera moved...");
                    mTimeLayout.setVisibility(View.GONE);
                    mFavoriteLayout.setVisibility(View.GONE);
                }
            }
        });

        mSearchText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                hideSoftKeyboard();
                mCursor = (Cursor) adapterView.getItemAtPosition(i);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCursor.getDouble(2), mCursor.getDouble(3)), BUS_STOP_ZOOM));
                mTimeLayout.setVisibility(View.VISIBLE);
                mFavoriteLayout.setVisibility(View.VISIBLE);
                updateFavoriteIcon(mCursor.getInt(5));
            }
        });

        mSearchText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                mIsSoftKeyboardVisible = true;
            }
        });

        mSearchText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mIsSoftKeyboardVisible = true;
            }
        });

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.ACTION_DOWN || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                    geoLocate();
                    return true;
                }

                return false;
            }
        });

        mGpsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Click GPS icon");
                mSearchText.setText("");
                mFavoriteLayout.setVisibility(View.GONE);
                getDeviceLocation();
            }
        });

        mFavoriteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCursor = mDataBase.fetchBusStopsByUrl(mCursor.getString(4));
                mCursor.moveToFirst();

                ContentValues contentValues = new ContentValues();
                int isFavorite = mCursor.getInt(5) == 0 ? 1 : 0;
                updateFavoriteIcon(isFavorite);

                contentValues.put("ulubione", isFavorite);
                String[] id = {String.valueOf(mCursor.getInt(0))};

                if (mDataBase.updateBusStopToFavorite(contentValues, id)) {
                    if (mCursor.getInt(5) == 0)
                        Toast.makeText(MapActivity.this, R.string.stop_added_to_favorites, Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(MapActivity.this, R.string.stop_removed_from_favorites, Toast.LENGTH_SHORT).show();
                }

            }
        });

        mTimeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mCursor = mDataBase.fetchBusStopsByUrl(mCursor.getString(4));
                mCursor.moveToFirst();

                Intent intent = new Intent(MapActivity.this, WebViewActivity.class);
                intent.putExtra("link", mCursor.getString(4));
                startActivity(intent);
            }
        });

        mCancelIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchText.setText("");
                mCancelIcon.setVisibility(View.GONE);
            }
        });
        hideSoftKeyboard();
    }

    /**
     * Metoda służąca do pobrania uprawnień przez aplikację do używania lokalizacji.
     */
    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: Getting locations permission...");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            }
        } else
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
    }

    /**
     * Metoda sprawdzająca czy użytkownik przyznał uprawnienia dla aplikacji do używania lokalizacji.
     * @param requestCode Kod żądania przekazany w requestPermissions.
     * @param permissions Żądane uprawnienia. Ta wartość nie może być pusta.
     * @param grantResults Wyniki przyznania dla odpowiednich uprawnień, czyli PERMISSION_GRANTED lub PERMISSION_DENIED. Ta wartość nie może być pusta.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: Called.");
        mLocationPermissionGranted = false;

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        mLocationPermissionGranted = false;
                        Log.d(TAG, "onRequestPermissionsResult: Permission failed.");
                        return;
                    }
                }
                Log.d(TAG, "onRequestPermissionsResult: Permission granted.");
                mLocationPermissionGranted = true;
                initMap();
            }
        }
    }

    /**
     * Metoda odpowiadająca za geolokalizację.
     */
    private void geoLocate() {
        Log.d(TAG, "geoLocate: Geolocating...");

        String searchString = mSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(this);
        List<Address> list = new ArrayList<>();

        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException:" + e.getMessage());
        }


        if (list.size() > 0) {
            Address address = list.get(0);
            Log.d(TAG, "geoLocate: Found a location: " + address.toString());
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM);
        } else {
            Toast.makeText(this, "Location not found!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "geoLocate: Location not found");
        }
    }

    /**
     * Metoda odpowiadająca za wyznaczenie lokalizacji urządzenia.
     */
    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: Getting the device's current location....");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Location found!");
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                        } else {
                            Log.d(TAG, "onComplete: Device current location not found!");
                            Toast.makeText(MapActivity.this, "Unable to get current location!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.d(TAG, "getDeviceLocation: " + e.getMessage());
        }
    }

    /**
     * Metoda, która przesuwa kamerę na współrzędne przekazane przez "latLng" i o przybliża widok o wartość przekazaną przez "zoom".
     * @param latLng Przechowuje współrzędne.
     * @param zoom Przechowuje wartość przybliżenia.
     */
    private void moveCamera(LatLng latLng, float zoom) {

        Log.d(TAG, "moveCamera: Moving camera to lat: " + latLng.latitude + ",lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        hideSoftKeyboard();
    }

    /**
     * Metoda pobiera wszystkie przystanki z bazy danych i dodaje je jako pinezki na mapę.
     * Wszystkie pinezki zostają także dodane do tablicy typu Marker
     */
    private void addAllBusStopsMarker() {

        mCursor = mDataBase.fetchAllBusStops();

        while (mCursor.moveToNext()) {
            mMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(mCursor.getDouble(2), mCursor.getDouble(3)))
                    .title(mCursor.getString(1))
                    .snippet(mCursor.getString(4)));
            mMarkerArray.add(mMarker);
        }
    }

    /**
     * Wywoływane, gdy activity wykryło naciśnięcie przez użytkownika klawisza wstecz.
     */
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    /**
     * Metoda sprawdzająca czy na urządzeniu jest dostępne Google Play Service.
     * @return True lub false.
     */
    private boolean isServicesOK() {
        Log.d(TAG, "isServicesOK: Checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MapActivity.this);

        if (available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "isServicesOK: Google Play Services is working.");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "isServicesOK: An error occured but we can fix it.");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MapActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map request.", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    /**
     * Metoda wywoływana, gdy wybrana jest pozycja w menu nawigacji. Pozwala obsłużyć każde zdarzenie osobno.
     * @param item Element, który został wciśnięty w menu nawigacji.
     * @return True lub false.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_map:
                break;
            case R.id.nav_favorite:
                startActivity(new Intent(MapActivity.this, FavoriteActivity.class));
                break;
            case R.id.nav_settings:
                startActivity(new Intent(MapActivity.this, SettingsActivity.class));
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    /**
     * Metoda aktualizująca kolor ikony "serca" w zależności czy przystanek jest dodany do ulubionych czy nie.
     * @param isFavorite "0" - Przystanek nie dodany do ulubionych; "1" - przystanek dodany do ulubionych.
     */
    private void updateFavoriteIcon(int isFavorite) {

        if (isFavorite == 0) {
            mFavoriteIcon.setColorFilter(Color.parseColor("#333333"));
        }else if (isFavorite == 1)
            mFavoriteIcon.setColorFilter(Color.parseColor("#F40808"));
    }

    /**
     * Metoda ustawiająca odpowiedni motyw dla activity.
     */
    private void setTheme() {

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mThemeKey = mSharedPreferences.getString("theme_key", "0");

        switch (mThemeKey) {
            case "0":
                switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                    case Configuration.UI_MODE_NIGHT_YES:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        mMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.style_json_dark_mode)));
                        break;
                    case Configuration.UI_MODE_NIGHT_NO:
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        break;
                }
                break;
            case "1":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                if(mFirstRun == null) {
                    Intent intent1 = new Intent(this, MapActivity.class);
                    intent1.putExtra("first_run", "foo");
                    startActivity(intent1);
                }
                break;
            case "2":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                mMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.style_json_dark_mode)));

                if(mFirstRun == null) {
                    Intent intent1 = new Intent(this, MapActivity.class);
                    intent1.putExtra("first_run", "foo");
                    startActivity(intent1);
                }
                break;
        }

    }

    /**
     * Metoda ustawiająca odpowiedni język dla activity.
     */
    private void setLanguage() {

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String languageKey = mSharedPreferences.getString("language_key", "");

        switch (languageKey) {
            case "0":
                String langCode = getResources().getConfiguration().locale.getLanguage();
                setLocal(this, langCode);
                if(mFirstRun == null) {
                    Intent intent1 = new Intent(this, MapActivity.class);
                    intent1.putExtra("first_run", "foo");
                    startActivity(intent1);
                }
                break;
            case "1":
                setLocal(this, "en");
                if(mFirstRun == null) {
                    Intent intent1 = new Intent(this, MapActivity.class);
                    intent1.putExtra("first_run", "foo");
                    startActivity(intent1);
                }
                break;
            case "2":
                setLocal(this, "pl");
                if(mFirstRun == null) {
                    Intent intent1 = new Intent(this, MapActivity.class);
                    intent1.putExtra("first_run", "foo");
                    startActivity(intent1);
                }
                break;
            case "3":
                setLocal(this, "it");
                if(mFirstRun == null) {
                    Intent intent1 = new Intent(this, MapActivity.class);
                    intent1.putExtra("first_run", "foo");
                    startActivity(intent1);
                }
                break;
        }

    }

    /**
     * Metoda wywołana przez system, gdy konfiguracja urządzenia ulegnie zmianie podczas aktywności użytkownika.
     * @param newConfig Nowa konfiguracja urządzenia. Ta wartość nie może być pusta.
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mThemeKey = mSharedPreferences.getString("theme_key", "");

        if (mThemeKey.equals("0")) {
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
     * Metoda chowająca systemową klawiature.
     */
    private void hideSoftKeyboard() {
        View view = this.getCurrentFocus();

        if (view != null) {
            mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            mInputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            mIsSoftKeyboardVisible = false;
        }
    }

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
}