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

import java.util.ArrayList;
import java.util.List;

import ro.ase.semdam.adapters.CustomAdapter;
import ro.ase.semdam.databinding.ActivityMainBinding;
import ro.ase.semdam.domeniu.Abonament;

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
        if (id == R.id.action_despre) {
            Toast.makeText(this, R.string.mesaj_despre, Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.action_bnr) {
            // BnrActivity vine intr-un seminar viitor
            Toast.makeText(this, R.string.mesaj_bnr, Toast.LENGTH_SHORT).show();
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
