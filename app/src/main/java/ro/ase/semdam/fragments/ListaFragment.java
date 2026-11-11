package ro.ase.semdam.fragments;

import androidx.fragment.app.Fragment;

import ro.ase.semdam.R;

/**
 * Fragment simplu: layout-ul e dat in constructor, deci Fragment face singur inflate-ul
 * in onCreateView (nu mai e nevoie sa-l suprascriem).
 */
public class ListaFragment extends Fragment {

    // constructorul public fara argumente e obligatoriu: sistemul recreeaza fragmentul cu el
    public ListaFragment() {
        super(R.layout.fragment_lista);
    }
}
