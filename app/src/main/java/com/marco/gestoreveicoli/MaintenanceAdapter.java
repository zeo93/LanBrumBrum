package com.marco.gestoreveicoli;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class MaintenanceAdapter extends RecyclerView.Adapter<MaintenanceAdapter.Holder> {

    public interface Listener {
        void onClick(Maintenance m);

        void onLongClick(Maintenance m);
    }

    private final List<Maintenance> items;
    private final Listener listener;

    public MaintenanceAdapter(List<Maintenance> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_maintenance, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Maintenance m = items.get(position);
        h.tipo.setText(m.tipo);
        h.data.setText(m.data);
        h.km.setText(String.format(Locale.ITALY, "%,d km", m.km));
        h.costo.setText(String.format(Locale.ITALY, "€ %,.2f", m.costo));

        StringBuilder extra = new StringBuilder();
        if (m.scadenza != null && !m.scadenza.isEmpty()) {
            extra.append(h.itemView.getContext().getString(R.string.scadenza_prefix, m.scadenza));
        }
        if (m.note != null && !m.note.isEmpty()) {
            if (extra.length() > 0) {
                extra.append("\n");
            }
            extra.append(m.note);
        }
        h.note.setText(extra.toString());
        h.note.setVisibility(extra.length() == 0 ? View.GONE : View.VISIBLE);

        h.itemView.setOnClickListener(view -> listener.onClick(m));
        h.itemView.setOnLongClickListener(view -> {
            listener.onLongClick(m);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tipo, data, km, costo, note;

        Holder(@NonNull View itemView) {
            super(itemView);
            tipo = itemView.findViewById(R.id.mTipo);
            data = itemView.findViewById(R.id.mData);
            km = itemView.findViewById(R.id.mKm);
            costo = itemView.findViewById(R.id.mCosto);
            note = itemView.findViewById(R.id.mNote);
        }
    }
}
