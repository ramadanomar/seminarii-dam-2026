package ro.ase.semdam;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import ro.ase.semdam.databinding.ActivityHostBinding;
import ro.ase.semdam.fragments.InfoFragment;
import ro.ase.semdam.fragments.ListaFragment;

/** Ecran gazda: un FragmentContainerView si un buton care comuta intre ListaFragment si InfoFragment. */
public class HostActivity extends AppCompatActivity {

    private ActivityHostBinding binding;

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
                    .add(R.id.fragment_container, ListaFragment.class, null)
                    .commit();
        }

        binding.btnComuta.setOnClickListener(v -> comutaFragment());
    }

    /**
     * Intrebam FragmentManager-ul ce fragment e afisat acum, in loc sa tinem un boolean separat:
     * un boolean s-ar desincroniza dupa Back (back stack-ul schimba fragmentul, flag-ul nu)
     * si s-ar pierde la rotatie.
     */
    private void comutaFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment curent = fm.findFragmentById(R.id.fragment_container);
        Class<? extends Fragment> urmatorul =
                curent instanceof InfoFragment ? ListaFragment.class : InfoFragment.class;

        fm.beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, urmatorul, null)
                .addToBackStack(null) // Back revine la fragmentul anterior
                .commit();
    }
}
