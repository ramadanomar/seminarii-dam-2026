package ro.ase.semdam.viewmodel;

import androidx.annotation.Nullable;

/**
 * Invelis pentru un mesaj care trebuie tratat o singura data (de ex. un Toast).
 * LiveData retrimite ultima valoare oricarui observator nou: fara invelisul asta,
 * dupa fiecare rotire Activity-ul nou ar primi din nou ultimul mesaj si ar arata iar Toast-ul.
 */
public final class Eveniment<T> {

    private final T continut;
    private boolean consumat = false;

    public Eveniment(T continut) {
        this.continut = continut;
    }

    /** Intoarce continutul prima data, apoi null. */
    @Nullable
    public T consuma() {
        if (consumat) {
            return null;
        }
        consumat = true;
        return continut;
    }
}
