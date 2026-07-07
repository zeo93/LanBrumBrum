package com.marco.gestoreveicoli;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Locale;

/** Pagina dedicata allo storico manutenzioni di un veicolo. */
public class MaintenanceActivity extends AppCompatActivity {

    private Storage storage;
    private Vehicle vehicle;
    private MaintenanceAdapter adapter;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenances);

        storage = Storage.get(this);
        vehicle = storage.byId(getIntent().getStringExtra("vehicle_id"));
        if (vehicle == null) {
            finish();
            return;
        }

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setTitle(getString(R.string.titolo_manutenzioni, vehicle.targa));

        emptyView = findViewById(R.id.emptyMaintenance);
        RecyclerView list = findViewById(R.id.maintenanceList);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MaintenanceAdapter(vehicle.manutenzioni, new MaintenanceAdapter.Listener() {
            @Override
            public void onClick(Maintenance m) {
                showMaintenanceDialog(m);
            }

            @Override
            public void onLongClick(Maintenance m) {
                confirmDelete(m);
            }
        });
        list.setAdapter(adapter);

        findViewById(R.id.fabAddMaintenance)
                .setOnClickListener(v -> showMaintenanceDialog(null));

        refresh();
    }

    private void refresh() {
        vehicle.manutenzioni.sort(
                Comparator.comparing((Maintenance m) -> Maintenance.ordinabile(m.data)).reversed());
        adapter.notifyDataSetChanged();
        emptyView.setVisibility(vehicle.manutenzioni.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmDelete(Maintenance m) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.elimina_manutenzione)
                .setMessage(getString(R.string.conferma_elimina_manutenzione, m.tipo, m.data))
                .setPositiveButton(R.string.elimina, (d, w) -> {
                    vehicle.manutenzioni.remove(m);
                    storage.save();
                    refresh();
                })
                .setNegativeButton(R.string.annulla, null)
                .show();
    }

    private void showMaintenanceDialog(Maintenance existing) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_maintenance, null);
        AutoCompleteTextView inTipo = view.findViewById(R.id.inputTipo);
        TextInputEditText inData = view.findViewById(R.id.inputData);
        TextInputEditText inKm = view.findViewById(R.id.inputKmManutenzione);
        TextInputEditText inCosto = view.findViewById(R.id.inputCosto);
        TextInputEditText inScadenza = view.findViewById(R.id.inputScadenza);
        TextInputEditText inNote = view.findViewById(R.id.inputNote);

        String[] tipi = getResources().getStringArray(R.array.tipi_manutenzione);
        inTipo.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tipi));

        inData.setOnClickListener(v -> pickDate(inData));
        inScadenza.setOnClickListener(v -> pickDate(inScadenza));

        if (existing != null) {
            inTipo.setText(existing.tipo, false);
            inData.setText(existing.data);
            inKm.setText(String.valueOf(existing.km));
            inCosto.setText(existing.costo == 0 ? "" : String.format(Locale.US, "%.2f", existing.costo));
            inScadenza.setText(existing.scadenza);
            inNote.setText(existing.note);
        } else {
            Calendar c = Calendar.getInstance();
            inData.setText(String.format(Locale.ITALY, "%02d/%02d/%04d",
                    c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH) + 1, c.get(Calendar.YEAR)));
            inKm.setText(String.valueOf(vehicle.km));
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(existing == null ? R.string.nuova_manutenzione : R.string.modifica_manutenzione)
                .setView(view)
                .setPositiveButton(R.string.salva, null)
                .setNegativeButton(R.string.annulla, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(b -> {
            String tipo = inTipo.getText() == null ? "" : inTipo.getText().toString().trim();
            String data = MainActivity.text(inData);
            if (tipo.isEmpty()) {
                inTipo.setError(getString(R.string.campo_obbligatorio));
                return;
            }
            if (data.isEmpty()) {
                inData.setError(getString(R.string.campo_obbligatorio));
                return;
            }
            Maintenance m = existing == null ? new Maintenance() : existing;
            m.tipo = tipo;
            m.data = data;
            m.km = MainActivity.parseLong(MainActivity.text(inKm));
            m.costo = parseCosto(MainActivity.text(inCosto));
            m.scadenza = MainActivity.text(inScadenza);
            m.note = MainActivity.text(inNote);
            if (existing == null) {
                vehicle.manutenzioni.add(m);
            }
            if (m.km > vehicle.km) {
                vehicle.km = m.km;
            }
            storage.save();
            refresh();
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void pickDate(TextInputEditText target) {
        Calendar c = Calendar.getInstance();
        String current = MainActivity.text(target);
        try {
            String[] p = current.split("/");
            c.set(Integer.parseInt(p[2]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[0]));
        } catch (Exception ignored) {
        }
        new DatePickerDialog(this, (view, year, month, day) ->
                target.setText(String.format(Locale.ITALY, "%02d/%02d/%04d", day, month + 1, year)),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    static double parseCosto(String s) {
        try {
            return Double.parseDouble(s.replace("€", "").replace(",", ".").trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
