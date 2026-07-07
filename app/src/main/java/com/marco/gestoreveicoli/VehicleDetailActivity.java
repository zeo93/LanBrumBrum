package com.marco.gestoreveicoli;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

public class VehicleDetailActivity extends AppCompatActivity {

    private Storage storage;
    private Vehicle vehicle;
    private ImageView fotoView;
    private Button btnManutenzioni;

    private final ActivityResultLauncher<String> pdfLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/pdf"), uri -> {
                if (uri != null) {
                    try (java.io.OutputStream out = getContentResolver().openOutputStream(uri, "wt")) {
                        PdfExporter.export(this, vehicle, out);
                        Toast.makeText(this, R.string.pdf_salvato, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(this, getString(R.string.errore_generico, e.getMessage()),
                                Toast.LENGTH_LONG).show();
                    }
                }
            });

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

        setSupportActionBar(findViewById(R.id.toolbar));
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

        btnManutenzioni = findViewById(R.id.btnManutenzioni);
        btnManutenzioni.setOnClickListener(v -> {
            Intent i = new Intent(this, MaintenanceActivity.class);
            i.putExtra("vehicle_id", vehicle.id);
            startActivity(i);
        });

        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (vehicle != null) {
            refresh();
        }
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

        TextView imm = findViewById(R.id.detailImmatricolazione);
        TextView rev = findViewById(R.id.detailRevisione);
        TextView bollo = findViewById(R.id.detailBollo);
        boolean hasImm = vehicle.immatricolazione != null && !vehicle.immatricolazione.isEmpty();
        imm.setVisibility(hasImm ? View.VISIBLE : View.GONE);
        rev.setVisibility(hasImm ? View.VISIBLE : View.GONE);
        bollo.setVisibility(hasImm ? View.VISIBLE : View.GONE);
        if (hasImm) {
            imm.setText(getString(R.string.immatricolata_il, vehicle.immatricolazione));
            rev.setText(getString(R.string.revisione_entro, vehicle.prossimaRevisione()));
            bollo.setText(getString(R.string.bollo_entro, vehicle.prossimoBollo()));
        }

        TextView assic = findViewById(R.id.detailAssicurazione);
        boolean hasAssic = vehicle.assicurazioneScadenza != null && !vehicle.assicurazioneScadenza.isEmpty();
        assic.setVisibility(hasAssic ? View.VISIBLE : View.GONE);
        if (hasAssic) {
            String compagnia = vehicle.assicurazioneCompagnia == null ? "" : vehicle.assicurazioneCompagnia.trim();
            boolean scaduta = vehicle.assicurazioneScaduta();
            String testo = scaduta
                    ? getString(R.string.assicurazione_scaduta_il, vehicle.assicurazioneScadenza)
                    : getString(R.string.assicurazione_entro, vehicle.assicurazioneScadenza);
            if (!compagnia.isEmpty()) {
                testo += " · " + compagnia;
            }
            assic.setText(testo);
            assic.setTextColor(scaduta ? 0xFFC62828 : bollo.getCurrentTextColor());
        }

        btnManutenzioni.setText(getString(R.string.apri_manutenzioni, vehicle.manutenzioni.size()));
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
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_edit) {
            VehicleDialog.show(this, storage, vehicle, this::refresh);
            return true;
        } else if (id == R.id.action_pdf) {
            pdfLauncher.launch("LanCare_" + vehicle.targa.replaceAll("[^A-Za-z0-9]", "") + ".pdf");
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
}
