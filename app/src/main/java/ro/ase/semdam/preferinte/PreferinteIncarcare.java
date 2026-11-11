package ro.ase.semdam.preferinte;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Singurul loc care stie numele fisierului de SharedPreferences si cheile din el.
 * MainActivity scrie dupa o incarcare reusita, SecondActivity citeste;
 * daca cheile ar fi scrise de mana in ambele locuri, o greseala de tastare ar da mereu valoarea implicita.
 */
public final class PreferinteIncarcare {

    private static final String NUME_FISIER = "setari_semdam";
    private static final String CHEIE_NR_INCARCATE = "nr_incarcate";

    /** Ce intoarce citirea inainte de prima incarcare reusita (cheia nu exista inca). */
    public static final int NR_INCARCATE_LIPSA = -1;

    private PreferinteIncarcare() { }

    private static SharedPreferences prefs(Context context) {
        // MODE_PRIVATE: fisierul e vizibil doar aplicatiei noastre
        return context.getSharedPreferences(NUME_FISIER, Context.MODE_PRIVATE);
    }

    /** apply() scrie pe disc in fundal, fara sa blocheze interfata. */
    public static void salveazaNrIncarcate(Context context, int nrIncarcate) {
        prefs(context).edit()
                .putInt(CHEIE_NR_INCARCATE, nrIncarcate)
                .apply();
    }

    public static int citesteNrIncarcate(Context context) {
        return prefs(context).getInt(CHEIE_NR_INCARCATE, NR_INCARCATE_LIPSA);
    }
}
