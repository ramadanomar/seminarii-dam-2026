package ro.ase.semdam;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import ro.ase.semdam.databinding.ActivityHostBinding;
import ro.ase.semdam.fragments.InfoFragment;
import ro.ase.semdam.fragments.ListaFragment;

public class HostActivity extends AppCompatActivity {

    private ActivityHostBinding binding;
    private boolean afiseazaInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityHostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // fragmentul de start se adauga doar la prima creare; la rotatie FragmentManager il restaureaza singur
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.fragment_container, new ListaFragment())
                    .commit();
        }

        binding.btnComuta.setOnClickListener(v -> {
            afiseazaInfo = !afiseazaInfo;
            Fragment urmatorul = afiseazaInfo ? new InfoFragment() : new ListaFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.fragment_container, urmatorul)
                    .addToBackStack(null) // Back revine la fragmentul anterior
                    .commit();
        });
    }
}
