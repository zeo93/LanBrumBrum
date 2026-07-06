package com.marco.gestoreveicoli;

import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Locale;

public class VehicleDetailActivity extends AppCompatActivity {

    private Storage storage;
    private Vehicle vehicle;
    private MaintenanceAdapter adapter;
    private TextView emptyView;
    private ImageView fotoView;

    private final ActivityResultLauncher<String> photoLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    if (PhotoStore.save(this, vehicle.id, uri)) {
                        refresh();
                    } else {
                        Toast.makeText(this, R.string.foto_errore, Toast.LENGTH_LONG).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        storage = Storage.get(this);
        vehicle = storage.byId(getIntent().getStringExtra("vehicle_id"));
        if (vehicle == null) {
            finish();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        fotoView = findViewById(R.id.detailFoto);
        fotoView.setOnClickListener(v -> showPhotoOptions());
        fotoView.setOnLongClickListener(v -> {
            if (PhotoStore.has(this, vehicle.id)) {
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.rimuovi_foto)
                        .setMessage(R.string.conferma_rimuovi_foto)
                        .setPositiveButton(R.string.elimina, (d, w) -> {
                            PhotoStore.delete(this, vehicle.id);
                            refresh();
                        })
                        .setNegativeButton(R.string.annulla, null)
                        .show();
            }
            return true;
        });

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
                confirmDeleteMaintenance(m);
            }
        });
        list.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAddMaintenance);
        fab.setOnClickListener(v -> showMaintenanceDialog(null));

        refresh();
    }

    private void showPhotoOptions() {
        boolean hasPhoto = PhotoStore.has(this, vehicle.id);
        String[] options = hasPhoto
                ? new String[]{getString(R.string.cerca_foto_internet), getString(R.string.scegli_galleria), getString(R.string.rimuovi_foto)}
                : new String[]{getString(R.string.cerca_foto_internet), getString(R.string.scegli_galleria)};
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.foto_veicolo)
                .setItems(options, (d, which) -> {
                    if (which == 0) {
                        fetchWebPhoto();
                    } else if (which == 1) {
                        photoLauncher.launch("image/*");
                    } else {
                        PhotoStore.delete(this, vehicle.id);
                        refresh();
                    }
                })
                .setNegativeButton(R.string.annulla, null)
                .show();
    }

    private void fetchWebPhoto() {
        String query = vehicle.marcaModello().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, R.string.foto_non_trovata, Toast.LENGTH_LONG).show();
            return;
        }
        View v = getLayoutInflater().inflate(R.layout.dialog_progress, null);
        AlertDialog progress = new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.ricerca_foto, query))
                .setView(v)
                .setCancelable(false)
                .create();
        progress.show();
        WebPhotoFetcher.fetch(this, query, (file, error) -> {
            progress.dismiss();
            if (isFinishing()) {
                return;
            }
            if (file != null) {
                boolean ok = PhotoStore.saveFromFile(this, vehicle.id, file);
                file.delete();
                if (ok) {
                    refresh();
                } else {
                    Toast.makeText(this, R.string.foto_errore, Toast.LENGTH_LONG).show();
                }
            } else if (error != null) {
                Toast.makeText(this, getString(R.string.errore_generico, error), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.foto_non_trovata, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void refresh() {
        setTitle(vehicle.targa);
        Bitmap foto = PhotoStore.load(this, vehicle.id, 1280);
        if (foto != null) {
            fotoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            fotoView.setImageBitmap(foto);
        } else {
            fotoView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            fotoView.setImageResource(R.drawable.ic_car);
        }
        ((TextView) findViewById(R.id.detailModello)).setText(vehicle.marcaModello());
        ((TextView) findViewById(R.id.detailProprietario)).setText(
                vehicle.proprietario.isEmpty() ? getString(R.string.nessun_proprietario) : vehicle.proprietario);
        ((TextView) findViewById(R.id.detailKm)).setText(String.format(Locale.ITALY, "%,d km", vehicle.km));
        ((TextView) findViewById(R.id.detailSpese)).setText(
                String.format(Locale.ITALY, "€ %,.2f", vehicle.totaleSpese()));
        vehicle.manutenzioni.sort(Comparator.comparing((Maintenance m) -> parseDate(m.data)).reversed());
        adapter.notifyDataSetChanged();
        emptyView.setVisibility(vehicle.manutenzioni.isEmpty() ? View.VISIBLE : View.GONE);
    }

    static long parseDate(String d) {
        try {
            String[] p = d.split("/");
            return Long.parseLong(p[2]) * 10000 + Long.parseLong(p[1]) * 100 + Long.parseLong(p[0]);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new android.content.Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_edit) {
            VehicleDialog.show(this, storage, vehicle, this::refresh);
            return true;
        } else if (id == R.id.action_delete) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.elimina_veicolo)
                    .setMessage(getString(R.string.conferma_elimina_veicolo, vehicle.targa))
                    .setPositiveButton(R.string.elimina, (d, w) -> {
                        PhotoStore.delete(this, vehicle.id);
                        storage.delete(vehicle);
                        finish();
                    })
                    .setNegativeButton(R.string.annulla, null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmDeleteMaintenance(Maintenance m) {
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
