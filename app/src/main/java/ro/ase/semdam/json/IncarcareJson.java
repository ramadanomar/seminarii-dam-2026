package ro.ase.semdam.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import ro.ase.semdam.domeniu.Abonament;
import ro.ase.semdam.domeniu.Frecventa;
import ro.ase.semdam.retea.ClientHttp;

/**
 * Descarca vectorul de abonamente de pe JSONBin si il transforma in obiecte Abonament.
 * Ambele metode fac I/O sau parsare, deci se apeleaza de pe executor, nu de pe main thread.
 */
public final class IncarcareJson {

    /** meta=false: raspunsul e direct vectorul [...], fara obiectul de metadate din jur. */
    private static final String URL_JSON =
            "https://api.jsonbin.io/v3/b/6ad34e67ffd5d16053733bd8/latest?meta=false";

    /** Cheile din JSON, exact cum apar in fisier (case-sensitive). */
    private static final String CHEIE_NUME = "nume";
    private static final String CHEIE_OBSERVATII = "observatii";
    private static final String CHEIE_COST = "cost";
    private static final String CHEIE_NR_PERSOANE = "nrPersoane";
    private static final String CHEIE_FRECVENTA = "frecventa";
    private static final String CHEIE_ACTIV = "activ";
    private static final String CHEIE_DATA_REINNOIRE = "dataReinnoire";

    private static final String VECTOR_GOL = "[]";

    private IncarcareJson() { }

    /** Textul brut de la server; un raspuns gol devine vector gol, ca dinText sa nu arunce. */
    public static String descarca() throws Exception {
        String text = ClientHttp.citeste(URL_JSON);
        return text.isEmpty() ? VECTOR_GOL : text;
    }

    /**
     * Parseaza vectorul de abonamente. Obiectele cu observatii goale sunt sarite
     * (nu intra in lista), dar parsarea continua cu restul vectorului.
     * getString/getDouble/getInt/getBoolean arunca JSONException daca cheia lipseste
     * sau are alt tip; LocalDate.parse arunca DateTimeParseException pe alt format decat yyyy-MM-dd.
     */
    public static List<Abonament> dinText(String text) throws JSONException {
        JSONArray vector = new JSONArray(text);
        List<Abonament> rezultat = new ArrayList<>(vector.length());
        for (int i = 0; i < vector.length(); i++) {
            JSONObject o = vector.getJSONObject(i);
            if (areObservatiiGoale(o)) {
                continue; // sarim doar obiectul acesta, nu oprim restul fisierului
            }
            rezultat.add(dinObiect(o));
        }
        return rezultat;
    }

    /** optString intoarce "" si cand cheia lipseste, deci acoperim si cazul fara observatii. */
    private static boolean areObservatiiGoale(JSONObject o) {
        return o.optString(CHEIE_OBSERVATII).trim().isEmpty();
    }

    private static Abonament dinObiect(JSONObject o) throws JSONException {
        String nume = o.getString(CHEIE_NUME);
        String observatii = o.getString(CHEIE_OBSERVATII);
        double cost = o.getDouble(CHEIE_COST);
        int nrPersoane = o.getInt(CHEIE_NR_PERSOANE);
        // eticheta din JSON trece prin acelasi dinEticheta ca valoarea din Spinner
        Frecventa frecventa = Frecventa.dinEticheta(o.getString(CHEIE_FRECVENTA));
        boolean activ = o.getBoolean(CHEIE_ACTIV);
        // JSON nu are tip de data: vine ca text si il parsam noi
        LocalDate dataReinnoire = LocalDate.parse(o.getString(CHEIE_DATA_REINNOIRE));
        return new Abonament(nume, observatii, cost, nrPersoane, frecventa, activ, dataReinnoire);
    }
}
