package ro.ase.semdam;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

    private ActivityMainBinding binding;

    private ActivityResultLauncher<Intent> addLauncher;

    private List<Abonament> abonamente = new ArrayList<>();
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

//        binding.btnDeschide.setOnClickListener(v -> {
//            Toast.makeText(MainActivity.this, "Salut!", Toast.LENGTH_LONG).show();
//        });

        //Button buton = findViewById(R.id.btnDeschide);
//        buton.setOnClickListener(v -> {
//            Toast.makeText(MainActivity.this, "Salut!", Toast.LENGTH_LONG).show();
//
//            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
//            startActivity(intent);
//
//        });

        addLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                if(result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Abonament abonamentPrimit = IntentCompat.getSerializableExtra(result.getData(), "abonament", Abonament.class);
                    int pozSelectie = result.getData().getIntExtra("pozitie", -1);
                    if(abonamentPrimit != null)
                    {
                        if(pozSelectie >= 0)
                        {
                            abonamente.set(pozSelectie, abonamentPrimit);
                        }
                        else {
                            abonamente.add(abonamentPrimit);
                        }
                        Toast.makeText(this,   abonamentPrimit.toString(), Toast.LENGTH_LONG).show();
//                        abonamente.add(abonamentPrimit);
                        adapter.notifyDataSetChanged();
                    }
                }
        }
        );

        binding.fabAdauga.setOnClickListener(view -> {
            Intent intent = new Intent(this, AddActivity.class);
            addLauncher.launch(intent);
        });

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, abonamente);
        // ListView lvElemente = findViewById(R.id.lvAbonamente);
        binding.lvAbonamente.setAdapter(adapter);

        binding.lvAbonamente.setOnItemClickListener((parent, view, position, id) -> {
            Abonament selectat = abonamente.get(position);
            Intent intent = new Intent(this, AddActivity.class);
            intent.putExtra("abonament", selectat);
            intent.putExtra("pozitie", position);
            addLauncher.launch(intent);
        });

        binding.lvAbonamente.setOnItemLongClickListener((parent, view, position, id) -> {
            new AlertDialog.Builder(this)
                    .setTitle("Stergere abonament")
                    .setMessage("Sigur stergi abonamentul?")
                    .setPositiveButton("Da", (d, w) -> {
                        abonamente.remove(position);
                        adapter.notifyDataSetChanged();
                    })
                    .setNegativeButton("Nu", null)
                    .show();
            return true;
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