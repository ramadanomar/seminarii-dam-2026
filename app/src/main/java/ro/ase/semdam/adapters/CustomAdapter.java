package ro.ase.semdam.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import ro.ase.semdam.R;
import ro.ase.semdam.domeniu.Abonament;
import ro.ase.semdam.domeniu.Frecventa;

public class CustomAdapter extends ArrayAdapter<Abonament> {

    // tine referintele la controalele unui rand, ca sa nu mai cautam cu findViewById la fiecare derulare
    private static class ViewHolder {
        TextView tvNume;
        TextView tvCost;
        TextView tvFrecventa;
    }

    public CustomAdapter(@NonNull Context context, int resource, @NonNull List<Abonament> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            // rand nou: inflate din XML + cautam controalele o singura data
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.elem_listview, parent, false);
            holder = new ViewHolder();
            holder.tvNume = convertView.findViewById(R.id.tvNume);
            holder.tvCost = convertView.findViewById(R.id.tvCost);
            holder.tvFrecventa = convertView.findViewById(R.id.tvFrecventa);
            convertView.setTag(holder);
        } else {
            // rand reciclat: luam holder-ul agatat de el
            holder = (ViewHolder) convertView.getTag();
        }

        Abonament abonament = getItem(position);
        if (abonament != null) {
            holder.tvNume.setText(abonament.getNume());
            holder.tvCost.setText(String.valueOf(abonament.getCost()));
            holder.tvFrecventa.setText(Frecventa.etichetaDin(abonament.getFrecventa()));
        }
        return convertView;
    }
}
