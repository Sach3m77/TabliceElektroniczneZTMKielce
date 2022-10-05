package com.example.tabliceelektroniczneztmkielce;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * Klasa rozszerzająca klasę SimpleCursorAdapter.
 * Klasa pozwala w liście rozwiajanej na wyświetlenie nazwy przystanku i ikony serca jeśli przystanek jest dodany już do ulubionych.
 * @author Paweł Sacha, Mateusz Pacak, Bartosz Ryś
 * @version 1.0
 */

public class CustomSimpleCursorAdapter extends SimpleCursorAdapter {

    private Cursor cursor;
    private Context context;
    private TextView text;
    private ImageView image;

    /**
     * Konstruktor pryzjmujący context, layout, c, from, to, flags.
     * @param context Kontekst, w którym działa ListView skojarzony z SimpleListItemFactory.
     * @param layout Identyfikator zasobu pliku układu, który definiuje widoki dla tego elementu listy.
     * @param c Kursor bazy danych. Może mieć wartość null, jeśli kursor nie jest jeszcze dostępny.
     * @param from Lista nazw kolumn reprezentujących dane do powiązania z interfejsem użytkownika. Może mieć wartość null, jeśli kursor nie jest jeszcze dostępny.
     * @param to Widoki w których powinny się wyświetlić pobrane wartości. Wszystkie powinny być typu TextViews.
     * @param flags Flagi używane do określania zachowania karty, zgodnie z CursorAdapter#CursorAdapter(Context, Cursor, int).
     */
    public CustomSimpleCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.cursor = c;
        this.context = context;
    }

    /**
     * Wiąże wszystkie nazwy pól przekazane do parametru „to” konstruktora z odpowiadającymi im kolumnami kursora określonymi w parametrze „from”.
     * @param view Istniejący widok, zwrócony wcześniej przez newView.
     * @param context Interfejs do globalnych informacji aplikacji.
     * @param cursor Kursor, z którego pobierane są dane. Kursor jest już przesunięty we właściwe miejsce.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        text = view.findViewById(R.id.cursorItemText);
        image = view.findViewById(R.id.cursorItemImage);

        text.setText(cursor.getString(1));

        if(cursor.getInt(5) == 0)
            image.setVisibility(View.GONE);
        else if(cursor.getInt(5) == 1)
            image.setVisibility(View.VISIBLE);

    }
}
