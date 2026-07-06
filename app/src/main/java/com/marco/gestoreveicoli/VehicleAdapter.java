package com.marco.gestoreveicoli;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.Holder> {

    public interface Listener {
        void onClick(Vehicle v);

        void onLongClick(Vehicle v);
    }

    private final List<Vehicle> vehicles;
    private final Listener listener;

    public VehicleAdapter(List<Vehicle> vehicles, Listener listener) {
        this.vehicles = vehicles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vehicle, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        Vehicle v = vehicles.get(position);
        Bitmap foto = PhotoStore.load(h.itemView.getContext(), v.id, 136);
        if (foto != null) {
            h.foto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            h.foto.setImageBitmap(foto);
        } else {
            h.foto.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            h.foto.setImageResource(R.drawable.ic_car);
        }
        h.targa.setText(v.targa);
        h.modello.setText(v.marcaModello());
        h.proprietario.setText(v.proprietario.isEmpty()
                ? h.itemView.getContext().getString(R.string.nessun_proprietario)
                : v.proprietario);
        h.km.setText(String.format(Locale.ITALY, "%,d km", v.km));
        int n = v.manutenzioni.size();
        h.manutenzioni.setText(h.itemView.getContext().getResources()
                .getQuantityString(R.plurals.num_manutenzioni, n, n));
        h.itemView.setOnClickListener(view -> listener.onClick(v));
        h.itemView.setOnLongClickListener(view -> {
            listener.onLongClick(v);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        ImageView foto;
        TextView targa, modello, proprietario, km, manutenzioni;

        Holder(@NonNull View itemView) {
            super(itemView);
            foto = itemView.findViewById(R.id.itemFoto);
            targa = itemView.findViewById(R.id.itemTarga);
            modello = itemView.findViewById(R.id.itemModello);
            proprietario = itemView.findViewById(R.id.itemProprietario);
            km = itemView.findViewById(R.id.itemKm);
            manutenzioni = itemView.findViewById(R.id.itemManutenzioni);
        }
    }
}
