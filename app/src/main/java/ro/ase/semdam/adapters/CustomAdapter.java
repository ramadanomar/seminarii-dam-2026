package ro.ase.semdam.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import ro.ase.semdam.R;
import ro.ase.semdam.domeniu.Anunt;
import ro.ase.semdam.domeniu.Categorie;

public class CustomAdapter extends ArrayAdapter<Anunt> {
    public CustomAdapter(@NonNull Context context, int resource, @NonNull List<Anunt> objects) {
        super(context, resource, objects);
    }

    private static class ViewHolder {
        TextView tvTitlu;
        TextView tvPret;
        TextView tvCategorie;
        TextView tvCantitate;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.elem_listview, parent, false);
            holder = new ViewHolder();
            holder.tvTitlu = convertView.findViewById(R.id.tvTitlu);
            holder.tvPret = convertView.findViewById(R.id.tvPret);
            holder.tvCategorie = convertView.findViewById(R.id.tvCategorie);
            holder.tvCantitate = convertView.findViewById(R.id.tvCantitate);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        Anunt anunt = getItem(position);
        if(anunt != null)
        {
            holder.tvTitlu.setText(anunt.getTitlu());
            holder.tvPret.setText(String.valueOf(anunt.getPret()));
            holder.tvCategorie.setText(Categorie.etichetaDin(anunt.getCategorie()));
            holder.tvCantitate.setText(String.valueOf(anunt.getCantitate()));
            // Folosit: titlu rosu; Nou: culoare normala (reciclare ViewHolder)
            if(!anunt.isNou())
                holder.tvTitlu.setTextColor(Color.RED);
            else
                holder.tvTitlu.setTextColor(Color.BLACK);
        }

        return convertView;
    }
}
