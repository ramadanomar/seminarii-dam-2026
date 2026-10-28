package ro.ase.semdam;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

import ro.ase.semdam.databinding.ActivityMainBinding;
import ro.ase.semdam.domeniu.Abonament;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final String ELEMENT = "element";
    public static final String POZ_SELECTIE = "pozSelectie";

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

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, abonamente);
        binding.lvElemente.setAdapter(adapter);

        addLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent data = result.getData();
                    if (result.getResultCode() != RESULT_OK || data == null) return;

                    Abonament abonament = IntentCompat.getSerializableExtra(data, ELEMENT, Abonament.class);
                    if (abonament == null) return;

                    int pozSelectie = data.getIntExtra(POZ_SELECTIE, -1);
                    if (pozSelectie >= 0) {
                        abonamente.set(pozSelectie, abonament);
                    } else {
                        abonamente.add(abonament);
                    }
                    adapter.notifyDataSetChanged();
                });

        binding.lvElemente.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(this, AddActivity.class);
            intent.putExtra(ELEMENT, abonamente.get(position));
            intent.putExtra(POZ_SELECTIE, position);
            addLauncher.launch(intent);
        });

        binding.lvElemente.setOnItemLongClickListener((parent, view, position, id) -> {
            new AlertDialog.Builder(this)
                    .setTitle("Stergere")
                    .setMessage("Stergeti abonamentul selectat?")
                    .setPositiveButton("Da", (d, w) -> {
                        abonamente.remove(position);
                        adapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("Nu", null)
                    .show();
            return true;
        });

        binding.fabAdauga.setOnClickListener(view -> {
            Intent intent = new Intent(this, AddActivity.class);
            addLauncher.launch(intent);
        });

        Log.d(TAG, "s-a apelat onCreate");
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