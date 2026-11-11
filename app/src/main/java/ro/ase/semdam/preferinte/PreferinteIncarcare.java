package ro.ase.semdam.preferinte;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Singurul loc care stie numele fisierului de SharedPreferences si cheile din el.
 * AbonamenteViewModel scrie dupa o incarcare reusita; SecondActivity si MainActivity citesc.
 * Daca cheile ar fi scrise de mana in mai multe locuri, o greseala de tastare ar da mereu valoarea implicita.
 */
public final class PreferinteIncarcare {

    private static final String NUME_FISIER = "setari_semdam";
    private static final String CHEIE_NR_INCARCATE = "nr_incarcate";
    private static final String CHEIE_NR_ACTIVE = "nr_active";

    /** Ce intoarce citirea inainte de prima incarcare reusita (cheia nu exista inca). */
    public static final int NR_INCARCATE_LIPSA = -1;
    public static final int NR_ACTIVE_LIPSA = 0;

    private PreferinteIncarcare() { }

    private static SharedPreferences prefs(Context context) {
        // MODE_PRIVATE: fisierul e vizibil doar aplicatiei noastre
        return context.getSharedPreferences(NUME_FISIER, Context.MODE_PRIVATE);
    }

    /**
     * Salveaza rezultatul unei incarcari reusite. Ambele chei intra in acelasi edit(),
     * deci se scriu impreuna; apply() scrie pe disc in fundal, fara sa blocheze interfata.
     */
    public static void salveazaIncarcare(Context context, int nrIncarcate, int nrActive) {
        prefs(context).edit()
                .putInt(CHEIE_NR_INCARCATE, nrIncarcate)
                .putInt(CHEIE_NR_ACTIVE, nrActive)
                .apply();
    }

    public static int citesteNrIncarcate(Context context) {
        return prefs(context).getInt(CHEIE_NR_INCARCATE, NR_INCARCATE_LIPSA);
    }

    /** Cate abonamente active au venit la ultima incarcare; 0 daca nu s-a incarcat inca nimic. */
    public static int citesteNrActive(Context context) {
        return prefs(context).getInt(CHEIE_NR_ACTIVE, NR_ACTIVE_LIPSA);
    }
}
