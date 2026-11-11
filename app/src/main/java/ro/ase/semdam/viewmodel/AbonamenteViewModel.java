package ro.ase.semdam.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ro.ase.semdam.domeniu.Abonament;
import ro.ase.semdam.json.IncarcareJson;
import ro.ase.semdam.preferinte.PreferinteIncarcare;

/**
 * Tine lista de abonamente si incarcarea JSON in afara Activity-ului.
 *
 * La rotire, Android distruge MainActivity si creeaza una noua; ViewModel-ul NU se distruge:
 * Activity-ul nou primeste aceeasi instanta, cu lista plina si cu o eventuala descarcare inca in curs.
 * ViewModel-ul moare abia cand ecranul se inchide de tot (Back / finish) - atunci se apeleaza onCleared().
 *
 * Regula: aici nu tinem nicio referinta la Activity, View sau binding (ar ramane agatate de
 * Activity-ul vechi, distrus). Pentru SharedPreferences folosim Application, care traieste cat procesul.
 */
public class AbonamenteViewModel extends AndroidViewModel {

    private static final String TAG = "AbonamenteViewModel";

    /** Sursa de adevar; modificata DOAR pe main thread, deci nu are nevoie de sincronizare. */
    private final List<Abonament> abonamente = new ArrayList<>();

    private final MutableLiveData<List<Abonament>> abonamenteLive =
            new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> seIncarca = new MutableLiveData<>(false);
    private final MutableLiveData<Eveniment<RezultatIncarcare>> rezultatIncarcare = new MutableLiveData<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public AbonamenteViewModel(@NonNull Application application) {
        super(application);
    }

    // ---- ce observa ecranul (doar citire: LiveData, nu MutableLiveData) ----

    public LiveData<List<Abonament>> getAbonamente() {
        return abonamenteLive;
    }

    public LiveData<Boolean> getSeIncarca() {
        return seIncarca;
    }

    public LiveData<Eveniment<RezultatIncarcare>> getRezultatIncarcare() {
        return rezultatIncarcare;
    }

    // ---- operatii pe lista (apelate de pe main thread) ----

    public void adauga(Abonament abonament) {
        abonamente.add(abonament);
        publicaLista();
    }

    public void inlocuieste(int pozitie, Abonament abonament) {
        abonamente.set(pozitie, abonament);
        publicaLista();
    }

    public void sterge(int pozitie) {
        abonamente.remove(pozitie);
        publicaLista();
    }

    public int totalPersoane() {
        int total = 0;
        for (Abonament abonament : abonamente) {
            total += abonament.getNrPersoane();
        }
        return total;
    }

    // ---- incarcarea JSON ----

    /** Descarcare + parsare pe executor; rezultatul se aplica pe main thread prin mainHandler. */
    public void incarcaJson() {
        if (Boolean.TRUE.equals(seIncarca.getValue())) {
            return; // o descarcare e deja in curs; meniul e oricum dezactivat
        }
        seIncarca.setValue(true);

        executor.execute(() -> {
            try {
                String text = IncarcareJson.descarca();
                List<Abonament> citite = IncarcareJson.dinText(text);
                mainHandler.post(() -> aplicaIncarcarea(citite));
            } catch (Exception e) {
                Log.e(TAG, "Incarcarea JSON a esuat", e);
                mainHandler.post(() -> {
                    seIncarca.setValue(false);
                    rezultatIncarcare.setValue(new Eveniment<>(RezultatIncarcare.eroare(e.getMessage())));
                });
            }
        });
    }

    private void aplicaIncarcarea(List<Abonament> citite) {
        abonamente.clear();
        abonamente.addAll(citite);
        publicaLista();

        // in prefs pastram doar un numar mic, nu toata colectia
        PreferinteIncarcare.salveazaNrIncarcate(getApplication(), citite.size());

        seIncarca.setValue(false);
        rezultatIncarcare.setValue(new Eveniment<>(RezultatIncarcare.succes(citite.size())));
    }

    /** Ecranul primeste o copie nemodificabila: lista interna se schimba doar prin metodele de mai sus. */
    private void publicaLista() {
        abonamenteLive.setValue(Collections.unmodifiableList(new ArrayList<>(abonamente)));
    }

    /** Ecranul s-a inchis definitiv (nu rotire): oprim firul de fundal si anulam ce era in coada. */
    @Override
    protected void onCleared() {
        executor.shutdownNow();
        mainHandler.removeCallbacksAndMessages(null);
        super.onCleared();
    }
}
