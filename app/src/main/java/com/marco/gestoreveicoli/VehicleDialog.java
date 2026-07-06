package com.marco.gestoreveicoli;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

/** Dialog condiviso per creare/modificare un veicolo, con tendine marca/modello. */
public class VehicleDialog {

    public static void show(Activity activity, Storage storage, Vehicle existing, Runnable onSaved) {
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_vehicle, null);
        TextInputEditText inTarga = view.findViewById(R.id.inputTarga);
        AutoCompleteTextView inMarca = view.findViewById(R.id.inputMarca);
        AutoCompleteTextView inModello = view.findViewById(R.id.inputModello);
        TextInputEditText inProprietario = view.findViewById(R.id.inputProprietario);
        TextInputEditText inImmatricolazione = view.findViewById(R.id.inputImmatricolazione);
        TextInputEditText inKm = view.findViewById(R.id.inputKm);

        inImmatricolazione.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            Calendar current = Vehicle.parseData(
                    inImmatricolazione.getText() == null ? "" : inImmatricolazione.getText().toString());
            if (current != null) {
                c = current;
            }
            new DatePickerDialog(activity, (picker, year, month, day) ->
                    inImmatricolazione.setText(String.format(Locale.ITALY, "%02d/%02d/%04d", day, month + 1, year)),
                    c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        inMarca.setAdapter(new ArrayAdapter<>(activity,
                android.R.layout.simple_list_item_1, CarData.marche()));
        inMarca.setOnClickListener(v -> inMarca.showDropDown());

        Runnable updateModelli = () -> inModello.setAdapter(new ArrayAdapter<>(activity,
                android.R.layout.simple_list_item_1, CarData.modelli(inMarca.getText().toString())));
        inMarca.setOnItemClickListener((parent, v, pos, id) -> {
            inModello.setText("");
            updateModelli.run();
        });
        inModello.setOnClickListener(v -> {
            updateModelli.run();
            inModello.showDropDown();
        });

        if (existing != null) {
            inTarga.setText(existing.targa);
            inMarca.setText(existing.marca);
            inModello.setText(existing.modello);
            inProprietario.setText(existing.proprietario);
            inImmatricolazione.setText(existing.immatricolazione);
            inKm.setText(String.valueOf(existing.km));
        }
        updateModelli.run();

        AlertDialog dialog = new MaterialAlertDialogBuilder(activity)
                .setTitle(existing == null ? R.string.nuovo_veicolo : R.string.modifica_veicolo)
                .setView(view)
                .setPositiveButton(R.string.salva, null)
                .setNegativeButton(R.string.annulla, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(b -> {
            String targa = MainActivity.text(inTarga).toUpperCase();
            String marca = inMarca.getText().toString().trim();
            String modello = inModello.getText().toString().trim();
            if (targa.isEmpty()) {
                inTarga.setError(activity.getString(R.string.campo_obbligatorio));
                return;
            }
            if (marca.isEmpty()) {
                inMarca.setError(activity.getString(R.string.campo_obbligatorio));
                return;
            }
            if (modello.isEmpty()) {
                inModello.setError(activity.getString(R.string.campo_obbligatorio));
                return;
            }
            Vehicle v = existing == null ? new Vehicle() : existing;
            v.targa = targa;
            v.marca = marca;
            v.modello = modello;
            v.proprietario = MainActivity.text(inProprietario);
            v.immatricolazione = MainActivity.text(inImmatricolazione);
            v.km = MainActivity.parseLong(MainActivity.text(inKm));
            if (existing == null) {
                storage.vehicles().add(v);
            }
            storage.save();
            if (onSaved != null) {
                onSaved.run();
            }
            dialog.dismiss();
        }));
        dialog.show();
    }
}
