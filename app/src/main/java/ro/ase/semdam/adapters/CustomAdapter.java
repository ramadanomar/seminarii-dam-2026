package ro.ase.semdam.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import ro.ase.semdam.R;
import ro.ase.semdam.databinding.ElemListviewBinding;
import ro.ase.semdam.domeniu.Abonament;
import ro.ase.semdam.domeniu.Frecventa;

/**
 * Adapter pentru ListView-ul de abonamente: fiecare rand vine din elem_listview.xml.
 * Foloseste tiparul ViewHolder ca sa nu refaca inflate + cautarea view-urilor la derulare.
 */
public class CustomAdapter extends ArrayAdapter<Abonament> {

    public CustomAdapter(@NonNull Context context, @NonNull List<Abonament> abonamente) {
        // layout-ul randului e dat la inflate in ViewHolder, deci resource-ul parintelui nu conteaza
        super(context, 0, abonamente);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            // rand nou: construim view-urile din XML o singura data
            holder = new ViewHolder(LayoutInflater.from(getContext()), parent);
            convertView = holder.itemView;
            convertView.setTag(holder);
        } else {
            // rand reciclat de ListView: luam holder-ul agatat de el
            holder = (ViewHolder) convertView.getTag();
        }

        Abonament abonament = getItem(position);
        if (abonament != null) {
            holder.bind(abonament);
        }
        return convertView;
    }

    /** Tine referintele unui rand si stie sa-l umple cu datele unui abonament. */
    private static class ViewHolder {
        final View itemView;
        private final ElemListviewBinding binding;

        ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            binding = ElemListviewBinding.inflate(inflater, parent, false);
            itemView = binding.getRoot();
        }

        void bind(Abonament abonament) {
            binding.tvNume.setText(abonament.getNume());
            binding.tvCost.setText(itemView.getContext().getString(R.string.format_cost, abonament.getCost()));
            binding.tvFrecventa.setText(Frecventa.etichetaDin(abonament.getFrecventa()));
        }
    }
}
