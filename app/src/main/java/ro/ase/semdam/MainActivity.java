package ro.ase.semdam;

import android.content.Intent;
import android.os.Bundle;
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
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import ro.ase.semdam.adapters.CustomAdapter;
import ro.ase.semdam.databinding.ActivityMainBinding;
import ro.ase.semdam.domeniu.Abonament;
import ro.ase.semdam.preferinte.PreferinteIncarcare;
import ro.ase.semdam.viewmodel.AbonamenteViewModel;
import ro.ase.semdam.viewmodel.RezultatIncarcare;

/**
 * Ecranul cu lista. Nu mai tine datele: le ia din AbonamenteViewModel (care supravietuieste rotirii)
 * si doar le afiseaza; orice modificare a listei trece prin ViewModel.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    /** Cheile extras-urilor schimbate cu AddActivity. */
    public static final String EXTRA_ABONAMENT = "abonament";
    public static final String EXTRA_POZITIE = "pozitie";
    private static final int POZITIE_NOUA = -1;

    private ActivityMainBinding binding;
    private ActivityResultLauncher<Intent> addLauncher;
    private AbonamenteViewModel viewModel;

    /** Copia afisata de adapter; se reumple din ViewModel la fiecare schimbare a listei. */
    private final List<Abonament> randuri = new ArrayList<>();
    private ArrayAdapter<Abonament> adapter;

    /** Oglinda lui viewModel.getSeIncarca(), folosita in onPrepareOptionsMenu. */
    private boolean seIncarca = false;

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

        // dupa rotire, ViewModelProvider intoarce ACEEASI instanta, nu una noua
        viewModel = new ViewModelProvider(this).get(AbonamenteViewModel.class);

        addLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), this::onRezultatAdd);

        configureazaLista();
        observaViewModel();

        binding.fabAdauga.setOnClickListener(v -> deschideFormular(null, POZITIE_NOUA));

        // doar la deschiderea reala a ecranului, nu si la recrearea dupa rotire
        if (savedInstanceState == null) {
            afiseazaUltimaIncarcare();
        }

        Log.d(TAG, "s-a apelat onCreate");
    }

    /** Toast cu nr_active din SharedPreferences (0 daca nu s-a facut inca nicio incarcare). */
    private void afiseazaUltimaIncarcare() {
        int nrActive = PreferinteIncarcare.citesteNrActive(this);
        Toast.makeText(this, getString(R.string.mesaj_ultima_incarcare_active, nrActive),
                Toast.LENGTH_SHORT).show();
    }

    private void configureazaLista() {
        adapter = new CustomAdapter(this, randuri);
        binding.lvAbonamente.setAdapter(adapter);

        binding.lvAbonamente.setOnItemClickListener((parent, view, position, id) ->
                deschideFormular(randuri.get(position), position));

        binding.lvAbonamente.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmaStergere(position);
            return true;
        });
    }

    /**
     * Observatorii sunt legati de ciclul de viata al acestui Activity (this): primesc valori doar cat
     * ecranul e vizibil si se dezaboneaza singuri la onDestroy, deci nu raman agatati de Activity-ul vechi.
     */
    private void observaViewModel() {
        viewModel.getAbonamente().observe(this, lista -> {
            randuri.clear();
            randuri.addAll(lista);
            adapter.notifyDataSetChanged();
        });

        viewModel.getSeIncarca().observe(this, incarca -> {
            seIncarca = incarca;
            invalidateOptionsMenu(); // reface meniul -> onPrepareOptionsMenu
        });

        viewModel.getRezultatIncarcare().observe(this, eveniment -> {
            RezultatIncarcare rezultat = eveniment.consuma();
            if (rezultat != null) {
                afiseazaRezultatIncarcare(rezultat);
            }
        });
    }

    private void afiseazaRezultatIncarcare(RezultatIncarcare rezultat) {
        if (rezultat.isSucces()) {
            Toast.makeText(this, getString(R.string.mesaj_incarcat, rezultat.getNrIncarcate()),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.mesaj_eroare_json, rezultat.getMesajEroare()),
                    Toast.LENGTH_LONG).show();
        }
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
            viewModel.adauga(abonament);
        } else {
            viewModel.inlocuieste(pozitie, abonament);
        }
    }

    private void confirmaStergere(int pozitie) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.titlu_stergere)
                .setMessage(R.string.mesaj_stergere)
                .setPositiveButton(R.string.da, (dialog, which) -> viewModel.sterge(pozitie))
                .setNegativeButton(R.string.nu, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meniu_principal, menu);
        return true;
    }

    /** "Incarca JSON" e dezactivat cat timp o descarcare e in curs (inclusiv dupa o rotire). */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem itemJson = menu.findItem(R.id.action_json);
        if (itemJson != null) {
            itemJson.setEnabled(!seIncarca);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_json) {
            viewModel.incarcaJson();
            return true;
        }
        if (id == R.id.action_ultima) {
            // nu trimitem nimic prin Intent: SecondActivity citeste singura din SharedPreferences
            startActivity(new Intent(this, SecondActivity.class));
            return true;
        }
        if (id == R.id.action_total_persoane) {
            Toast.makeText(this, getString(R.string.mesaj_total_persoane, viewModel.totalPersoane()),
                    Toast.LENGTH_LONG).show();
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
        Log.d(TAG, "onDestroy!!!");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }
}
