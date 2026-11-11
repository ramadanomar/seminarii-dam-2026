package ro.ase.semdam;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import ro.ase.semdam.databinding.ActivitySecondBinding;
import ro.ase.semdam.preferinte.PreferinteIncarcare;

/**
 * Afiseaza cate abonamente au intrat la ultima incarcare JSON reusita.
 * Valoarea nu vine prin Intent: o citim din SharedPreferences, deci supravietuieste inchiderii aplicatiei.
 */
public class SecondActivity extends AppCompatActivity {

    private ActivitySecondBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivitySecondBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        int nrIncarcate = PreferinteIncarcare.citesteNrIncarcate(this);
        binding.tvUltimaIncarcare.setText(getString(R.string.format_ultima_incarcare, nrIncarcate));
    }
}
