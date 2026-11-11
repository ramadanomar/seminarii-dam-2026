package ro.ase.semdam;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ro.ase.semdam.databinding.ActivityBnrBinding;
import ro.ase.semdam.retea.BnrClient;
import ro.ase.semdam.retea.CursValutar;

public class BnrActivity extends AppCompatActivity {

    private static final String MONEDA_EUR = "EUR";
    private static final String MONEDA_USD = "USD";

    private ActivityBnrBinding binding;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());

        binding = ActivityBnrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.btnActualizeaza.setOnClickListener(v -> actualizeazaCursuri());
    }

    /** Descarca XML-ul BNR pe executor si scrie EUR/USD pe main thread. */
    private void actualizeazaCursuri() {
        binding.btnActualizeaza.setEnabled(false);
        executor.execute(() -> {
            try {
                List<CursValutar> cursuri = BnrClient.descarca();
                mainHandler.post(() -> afiseazaCursuri(cursuri));
            } catch (Exception e) {
                mainHandler.post(() -> Toast.makeText(this,
                        getString(R.string.mesaj_eroare_retea, e.getMessage()), Toast.LENGTH_SHORT).show());
            } finally {
                mainHandler.post(() -> binding.btnActualizeaza.setEnabled(true));
            }
        });
    }

    private void afiseazaCursuri(List<CursValutar> cursuri) {
        for (CursValutar curs : cursuri) {
            if (MONEDA_EUR.equals(curs.getMoneda())) {
                binding.tvEur.setText(getString(R.string.format_curs, MONEDA_EUR, curs.getValoare()));
            } else if (MONEDA_USD.equals(curs.getMoneda())) {
                binding.tvUsd.setText(getString(R.string.format_curs, MONEDA_USD, curs.getValoare()));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }
}
