package com.example.tabliceelektroniczneztmkielce;

import static android.content.ContentValues.TAG;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Klasa odopwiedzialna za funkcjonalność bazy danych.
 * @author Paweł Sacha, Mateusz Pacak, Bartosz Ryś
 * @version 1.0
 */

public class Database extends SQLiteOpenHelper {

    private static String DB_PATH = null;
    private static final String DB_NAME = "przystanki.db";
    private static SQLiteDatabase sqLiteDatabase;
    private static Context myContext;
    private static final int BUFFOR = 1024;

    /**
     * Konstruktor klasy Database.
     * @param context Kontekst.
     */
    public Database(Context context) {
        super(context, DB_NAME, null, 1);
        this.DB_PATH = "/data/data/" + context.getPackageName() + "/" + "databases/";
        this.myContext = context;
    }

    /**
     * Metoda sprawdzająca czy da się otworzyć bazę danych.
     * @return True lub false.
     */
    public boolean checkDataBase() {
        SQLiteDatabase checkDB = null;

        try{
            String dbPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
        }catch(SQLException e){
            Log.e(TAG, "checkDataBase: ", e);
        }

        if(checkDB != null)
            checkDB.close();

        return checkDB != null ? true : false;

    }

    /**
     * Metoda tworząca bazę danych.
     */
    public void createDataBase() {
        if(!checkDataBase()) {
            this.getReadableDatabase();

            try{
                copyDataBase();
            }catch(IOException e){
                e.printStackTrace();
            }

        }
    }

    /**
     * Metoda kopiująca bazę danych.
     * @throws IOException Wyjątek.
     */
    public void copyDataBase() throws IOException {
        InputStream inputStream = myContext.getAssets().open(DB_NAME);
        String outFileName = DB_PATH + DB_NAME;
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFileName), Database.BUFFOR);
        byte[] buffer = new byte[Database.BUFFOR];
        int lenght;
        while((lenght = inputStream.read(buffer, 0, Database.BUFFOR)) != -1) {
            outputStream.write(buffer, 0, lenght);
        }
        inputStream.close();
        outputStream.flush();
        outputStream.close();
    }

    /**
     * Metoda otwierająca bazę danych.
     */
    public void openDataBase() {
        String dbPath = DB_PATH + DB_NAME;
        sqLiteDatabase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    /**
     * Metoda zamykająca bazę danych.
     */
    @Override
    public synchronized void close() {
        if(sqLiteDatabase != null)
            sqLiteDatabase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
    }

    /**
     * Metoda aktualizująca bazę danych.
     * @param sqLiteDatabase Baza danych.
     * @param oldVersion Stara wersja.
     * @param newVersion Nowa wersja.
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if(newVersion > oldVersion) {
            try{
                copyDataBase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Metoda zwracająca kursor na wszyskie przystanki w bazie danych.
     * @return Kursor lub null.
     */
    public Cursor fetchAllBusStops() {
        return sqLiteDatabase.query("przystanki", new String[]{"_id", "nazwa", "geoX", "geoY", "link", "ulubione"}, null, null, null, null, null);
    }

    /**
     * Metoda zwracająca kursor na przystanki w bazie danych o nazwie przekazanej jako paramentr "inputText".
     * @param inputText Nazwa prystaku, którego chcemy pobrać.
     * @return Kursor lub null.
     * @throws SQLException Wyjątek.
     */
    public Cursor fetchBusStopsByName(String inputText) throws SQLException {
        if(inputText == null || inputText.length() == 0) {
            return fetchAllBusStops();
        }else {
            return sqLiteDatabase.query("przystanki", new String[]{"_id", "nazwa", "geoX", "geoY", "link", "ulubione"}, "nazwa like '%" + inputText + "%'", null, null, null, null);
        }
    }

    /**
     * Metoda zwracająca kursor na przystanki w bazie danych o linku przekazanym jako parametr "url".
     * @param url Link przystanku, którego chcemy pobrać.
     * @return Kursor lub null.
     */
    public Cursor fetchBusStopsByUrl(String url) {
        return sqLiteDatabase.query("przystanki", new String[]{"_id", "nazwa", "geoX", "geoY", "link", "ulubione"}, "link like '%" + url + "%'", null, null, null, null);
    }

    /**
     * Metoda zwracająca kursor na przystanki, które zostały dodane przez użytkownika do ulubionych.
     * Domyślnie wszystkie przystanki w bazie w kolumnie "ulubione" mają ustawioną wartość "0" co oznacza, że dany przystanek nie został dodany do ulubionych.
     * Gdy użytkownik doda przystanek do ulubionych wartość w kolumnie "ulubione" zostaje zmieniona na "1".
     * @see #updateBusStopToFavorite(ContentValues, String[])
     * @return Kursors lub null.
     */
    public Cursor fetchAllFavoriteBusStops() {
        return sqLiteDatabase.query("przystanki", new String[]{"_id", "nazwa", "geoX", "geoY", "link", "ulubione"}, "ulubione like '%" + 1 + "%'", null, null, null, null);
    }

    /**
     * Metoda aktualizująca w bazie danych, wartość w kolumnie "ulubione" dla przystanku, który właśnie został dodany do ulubionych lub został usunięty z ulubionych.
     * @param contentValues Ta klasa służy do przechowywania zestawu wartości, które może przetwarzać ContentResolver.
     * @param busId Id dla którego rekordu ma zostać zmieniona wartość dla kolumny "ulubione".
     * @return True
     */
    public boolean updateBusStopToFavorite(ContentValues contentValues, String[] busId) {
        sqLiteDatabase = this.getWritableDatabase();

            for(String id : busId)
                sqLiteDatabase.update("przystanki", contentValues, "_id = " + id, null);

        return true;
    }
}
