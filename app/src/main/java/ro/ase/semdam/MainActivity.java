package ro.ase.semdam;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ro.ase.semdam.adapters.CustomAdapter;
import ro.ase.semdam.databinding.ActivityMainBinding;
import ro.ase.semdam.domeniu.Abonament;
import ro.ase.semdam.json.IncarcareJson;
import ro.ase.semdam.preferinte.PreferinteIncarcare;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    /** Cheile extras-urilor schimbate cu AddActivity. */
    public static final String EXTRA_ABONAMENT = "abonament";
    public static final String EXTRA_POZITIE = "pozitie";
    private static final int POZITIE_NOUA = -1;

    private ActivityMainBinding binding;
    private ActivityResultLauncher<Intent> addLauncher;

    private final List<Abonament> abonamente = new ArrayList<>();
    private ArrayAdapter<Abonament> adapter;

    /** Un singur fir de fundal pentru retea, refolosit la fiecare click (nu cate un executor nou). */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    /** Trimite rezultatul inapoi pe main thread: doar acolo avem voie sa atingem View-urile. */
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    /** Blocheaza click-urile repetate pe "Incarca JSON" cat timp o descarcare e in curs. */
    private boolean incarcareInCurs = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // tema e NoActionBar: toolbar-ul din layout devine bara pentru options menu
        setSupportActionBar(binding.toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this::onRezultatAdd);

        configureazaLista();

        binding.fabAdauga.setOnClickListener(v -> deschideFormular(null, POZITIE_NOUA));

        Log.d(TAG, "s-a apelat onCreate");
    }

    private void configureazaLista() {
        adapter = new CustomAdapter(this, abonamente);
        binding.lvAbonamente.setAdapter(adapter);

        binding.lvAbonamente.setOnItemClickListener((parent, view, position, id) ->
                deschideFormular(abonamente.get(position), position));

        binding.lvAbonamente.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmaStergere(position);
            return true;
        });
    }

    /** Deschide AddActivity gol (abonament null) sau precompletat pentru editare. */
    private void deschideFormular(Abonament abonament, int pozitie) {
        Intent intent = new Intent(this, AddActivity.class);
        if (abonament != null) {
            intent.putExtra(EXTRA_ABONAMENT, abonament);
            intent.putExtra(EXTRA_POZITIE, pozitie);
        }
        addLauncher.launch(intent);
    }

    private void onRezultatAdd(ActivityResult result) {
        Intent data = result.getData();
        if (result.getResultCode() != RESULT_OK || data == null) {
            return;
        }

        Abonament abonament = IntentCompat.getSerializableExtra(data, EXTRA_ABONAMENT, Abonament.class);
        if (abonament == null) {
            return;
        }

        int pozitie = data.getIntExtra(EXTRA_POZITIE, POZITIE_NOUA);
        if (pozitie == POZITIE_NOUA) {
            abonamente.add(abonament);
        } else {
            abonamente.set(pozitie, abonament);
        }
        adapter.notifyDataSetChanged();
    }

    private void confirmaStergere(int pozitie) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.titlu_stergere)
                .setMessage(R.string.mesaj_stergere)
                .setPositiveButton(R.string.da, (dialog, which) -> {
                    abonamente.remove(pozitie);
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton(R.string.nu, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meniu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_json) {
            incarcaJson();
            return true;
        }
        if (id == R.id.action_ultima) {
            // nu trimitem nimic prin Intent: SecondActivity citeste singura din SharedPreferences
            startActivity(new Intent(this, SecondActivity.class));
            return true;
        }
        if (id == R.id.action_total_persoane) {
            afiseazaTotalPersoane();
            return true;
        }
        if (id == R.id.action_bnr) {
            startActivity(new Intent(this, BnrActivity.class));
            return true;
        }
        if (id == R.id.action_despre) {
            Toast.makeText(this, R.string.mesaj_despre, Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.action_fragmente) {
            startActivity(new Intent(this, HostActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Descarca si parseaza JSON-ul pe executor; lista, adapterul si SharedPreferences
     * se actualizeaza pe main thread, doar daca descarcarea si parsarea au reusit.
     */
    private void incarcaJson() {
        if (incarcareInCurs) {
            Toast.makeText(this, R.string.mesaj_incarcare_in_curs, Toast.LENGTH_SHORT).show();
            return;
        }
        incarcareInCurs = true;

        executor.execute(() -> {
            try {
                String text = IncarcareJson.descarca();
                List<Abonament> citite = IncarcareJson.dinText(text);
                mainHandler.post(() -> afiseazaAbonamenteIncarcate(citite));
            } catch (Exception e) {
                Log.e(TAG, "Incarcarea JSON a esuat", e);
                mainHandler.post(() -> afiseazaEroareJson(e));
            }
        });
    }

    private void afiseazaAbonamenteIncarcate(List<Abonament> citite) {
        incarcareInCurs = false;
        if (isDestroyed()) {
            return;
        }

        abonamente.clear();
        abonamente.addAll(citite);
        adapter.notifyDataSetChanged();

        // in prefs pastram doar un numar mic, nu toata colectia
        PreferinteIncarcare.salveazaNrIncarcate(this, citite.size());

        Toast.makeText(this, getString(R.string.mesaj_incarcat, citite.size()), Toast.LENGTH_SHORT).show();
    }

    private void afiseazaEroareJson(Exception e) {
        incarcareInCurs = false;
        if (isDestroyed()) {
            return;
        }
        Toast.makeText(this, getString(R.string.mesaj_eroare_json, e.getMessage()), Toast.LENGTH_LONG).show();
    }

    /** Suma nrPersoane din lista; pe lista goala afiseaza 0. */
    private void afiseazaTotalPersoane() {
        int total = 0;
        for (Abonament abonament : abonamente) {
            total += abonament.getNrPersoane();
        }
        Toast.makeText(this, getString(R.string.mesaj_total_persoane, total), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "s-a apelat onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause?");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // oprim firul de fundal odata cu ecranul; altfel ramane viu dupa ce Activity-ul moare
        executor.shutdownNow();
        Log.d(TAG, "onDestroy!!!");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }
}
