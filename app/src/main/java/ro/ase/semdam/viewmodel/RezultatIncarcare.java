package ro.ase.semdam.viewmodel;

import androidx.annotation.Nullable;

/**
 * Rezultatul unei incarcari JSON, transmis de ViewModel catre ecran.
 * ViewModel-ul nu construieste textul Toast-ului (n-are voie sa tina referinte la Activity);
 * ecranul il formateaza din resurse.
 */
public final class RezultatIncarcare {

    private final boolean succes;
    private final int nrIncarcate;
    @Nullable
    private final String mesajEroare;

    private RezultatIncarcare(boolean succes, int nrIncarcate, @Nullable String mesajEroare) {
        this.succes = succes;
        this.nrIncarcate = nrIncarcate;
        this.mesajEroare = mesajEroare;
    }

    public static RezultatIncarcare succes(int nrIncarcate) {
        return new RezultatIncarcare(true, nrIncarcate, null);
    }

    public static RezultatIncarcare eroare(@Nullable String mesajEroare) {
        return new RezultatIncarcare(false, 0, mesajEroare);
    }

    public boolean isSucces() {
        return succes;
    }

    public int getNrIncarcate() {
        return nrIncarcate;
    }

    @Nullable
    public String getMesajEroare() {
        return mesajEroare;
    }
}
