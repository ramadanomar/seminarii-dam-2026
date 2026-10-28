package ro.ase.semdam;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import ro.ase.semdam.databinding.ActivityAddBinding;
import ro.ase.semdam.domeniu.Abonament;
import ro.ase.semdam.domeniu.Frecventa;

public class AddActivity extends AppCompatActivity {

    private ActivityAddBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String[] frecvente = {"Lunar", "Anual", "Saptamanal"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, frecvente);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.spFrecventa.setAdapter(adapter);

        Abonament existent = IntentCompat.getSerializableExtra(
                getIntent(), MainActivity.ELEMENT, Abonament.class);
        int pozSelectie = getIntent().getIntExtra(MainActivity.POZ_SELECTIE, -1);
        if (existent != null) {
            binding.etNume.setText(existent.getNume());
            binding.etObservatii.setText(existent.getObservatii());
            binding.etCost.setText(String.valueOf(existent.getCost()));
            binding.etNrPersoane.setText(String.valueOf(existent.getNrPersoane()));
            binding.spFrecventa.setSelection(
                    adapter.getPosition(Frecventa.etichetaDin(existent.getFrecventa())));
            if (existent.isActiv()) {
                binding.rgActiv.check(binding.rbActiv.getId());
            } else {
                binding.rgActiv.check(binding.rbAnulat.getId());
            }
            binding.etDataReinnoire.setText(
                    existent.getDataReinnoire().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }

        binding.etDataReinnoire.setOnClickListener(v -> alegeData());

        binding.btnSalveaza.setOnClickListener(v -> {
            String nume = binding.etNume.getText().toString().trim();

            if(nume.isEmpty())
            {
                binding.etNume.setError("Numele este obligatoriu!");
                return;
            }

            String observatii = binding.etObservatii.getText().toString();

            if(observatii.isEmpty())
            {
                binding.etObservatii.setError("Observatiile sunt obligatorii!");
                return;
            }

            double cost;
            try {
                cost = Double.parseDouble(binding.etCost.getText().toString().trim());
            } catch (NumberFormatException e) {
                binding.etCost.setError("Introdu un cost numeric!");
                return;
            }

            if(cost <= 0)
            {
                binding.etCost.setError("Introdu un cost pozitiv!");
                return;
            }

            int nrPersoane;
            try {
                nrPersoane = Integer.parseInt(binding.etNrPersoane.getText().toString().trim());
            } catch (NumberFormatException e) {
                binding.etNrPersoane.setError("Nr. persoane trebuie sa fie un numar intreg!");
                return;
            }
            if(nrPersoane < 1 || nrPersoane > 6)
            {
                binding.etNrPersoane.setError("Nr. persoane trebuie sa fie intre 1 si 6!");
                return;
            }


            // Uitasem sa adaug isCoreLibraryDesugaringEnabled = true in build.gradle.kts din app.
            // Acum nu ar trebui sa va mai dea eroare de compilare
            LocalDate data;
            try {
                data = LocalDate.parse(binding.etDataReinnoire.getText().toString().trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                binding.etDataReinnoire.setError("Data trebuie sa fie yyyy-MM-dd!");
                return;
            }
            if(data.isBefore(LocalDate.now()))
            {
                binding.etDataReinnoire.setError("Data nu poate fi in trecut!");
                return;
            }

            Frecventa frecventa = Frecventa.dinEticheta(binding.spFrecventa.getSelectedItem().toString());
            int idBifat = binding.rgActiv.getCheckedRadioButtonId();
            if(idBifat == -1)
            {
                Toast.makeText(this, "Selectati statusul!", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean activ = idBifat == binding.rbActiv.getId();

            Abonament abonament = new Abonament(nume, observatii, cost, nrPersoane, frecventa, activ, data);
            Log.d("AddActivity", abonament.toString());

            Intent intent = new Intent();
            intent.putExtra(MainActivity.ELEMENT, abonament);
            if (pozSelectie >= 0) {
                intent.putExtra(MainActivity.POZ_SELECTIE, pozSelectie);
            }
            setResult(RESULT_OK, intent);
            finish();
        });
    }

    private void alegeData() {
        LocalDate initiala;
        try {
            initiala = LocalDate.parse(binding.etDataReinnoire.getText().toString(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            initiala = LocalDate.now();
        }

        // DatePicker numara lunile de la 0, LocalDate de la 1
        new DatePickerDialog(this, (picker, an, luna, zi) -> {
            binding.etDataReinnoire.setText(LocalDate.of(an, luna + 1, zi).format(DateTimeFormatter.ISO_LOCAL_DATE));
            binding.etDataReinnoire.setError(null);
        }, initiala.getYear(), initiala.getMonthValue() - 1, initiala.getDayOfMonth()).show();
    }
}
