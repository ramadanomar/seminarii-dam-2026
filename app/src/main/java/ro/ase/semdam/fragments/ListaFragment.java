package ro.ase.semdam.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ro.ase.semdam.R;

public class ListaFragment extends Fragment {

    private static final String TAG = "ListaFragment";

    // constructorul public gol e obligatoriu: sistemul recreeaza fragmentul fara argumente
    public ListaFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        return inflater.inflate(R.layout.fragment_lista, container, false);
    }
}
