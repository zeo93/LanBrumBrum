package com.marco.gestoreveicoli;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private Storage storage;
    private MaterialSwitch switchNotifiche;
    private TextView updateStatus;

    private final ActivityResultLauncher<String> exportLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/json"), uri -> {
                if (uri != null) {
                    doExport(uri);
                }
            });

    private final ActivityResultLauncher<String[]> importLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    confirmImport(uri);
                }
            });

    private final ActivityResultLauncher<String> notifPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (!granted) {
                    switchNotifiche.setChecked(false);
                    Prefs.setNotificheAttive(this, false);
                    Toast.makeText(this, R.string.permesso_notifiche_negato, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(R.string.impostazioni);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        storage = Storage.get(this);

        // --- Notifiche ---
        switchNotifiche = findViewById(R.id.switchNotifiche);
        switchNotifiche.setChecked(Prefs.notificheAttive(this));
        switchNotifiche.setOnCheckedChangeListener((btn, checked) -> {
            Prefs.setNotificheAttive(this, checked);
            if (checked) {
                NotificationHelper.ensureChannels(this);
                NotificationHelper.scheduleDaily(this);
                if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this,
                        Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }
            } else {
                NotificationHelper.cancelDaily(this);
            }
        });

        TextInputEditText giorni = findViewById(R.id.inputGiorniPreavviso);
        giorni.setText(String.valueOf(Prefs.giorniPreavviso(this)));
        giorni.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {
            }

            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int g = Integer.parseInt(s.toString().trim());
                    if (g > 0) {
                        Prefs.setGiorniPreavviso(SettingsActivity.this, g);
                    }
                } catch (Exception ignored) {
                }
            }
        });

        Button testNotifica = findViewById(R.id.btnTestNotifica);
        testNotifica.setOnClickListener(v -> {
            NotificationHelper.ensureChannels(this);
            NotificationHelper.checkScadenze(this);
            Toast.makeText(this, R.string.notifica_prova_inviata, Toast.LENGTH_SHORT).show();
        });

        // --- Dati ---
        Button export = findViewById(R.id.btnEsporta);
        export.setOnClickListener(v -> {
            String name = "gestore_veicoli_" +
                    new SimpleDateFormat("yyyyMMdd", Locale.ITALY).format(new Date()) + ".json";
            exportLauncher.launch(name);
        });

        Button importBtn = findViewById(R.id.btnImporta);
        importBtn.setOnClickListener(v -> importLauncher.launch(new String[]{"application/json", "text/plain", "application/octet-stream"}));

        // --- Aggiornamenti ---
        TextView versione = findViewById(R.id.textVersione);
        versione.setText(getString(R.string.versione_installata, UpdateChecker.currentVersion(this)));
        updateStatus = findViewById(R.id.textUpdateStatus);

        Button check = findViewById(R.id.btnCercaAggiornamenti);
        check.setOnClickListener(v -> {
            updateStatus.setText(R.string.ricerca_in_corso);
            UpdateChecker.checkAsync(this, (update, error) -> {
                if (isFinishing()) {
                    return;
                }
                if (update != null) {
                    updateStatus.setText(getString(R.string.trovata_versione, update.version));
                    UpdateChecker.showUpdateDialog(this, update);
                } else if (error != null) {
                    updateStatus.setText(getString(R.string.errore_aggiornamento, error));
                } else {
                    updateStatus.setText(R.string.nessun_aggiornamento);
                }
            });
        });
    }

    private void doExport(Uri uri) {
        try (OutputStream out = getContentResolver().openOutputStream(uri, "wt")) {
            out.write(storage.exportJson().getBytes(StandardCharsets.UTF_8));
            Toast.makeText(this, getString(R.string.export_ok, storage.vehicles().size()), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.errore_generico, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    private void confirmImport(Uri uri) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.importa_dati)
                .setMessage(R.string.conferma_import)
                .setPositiveButton(R.string.importa, (d, w) -> doImport(uri))
                .setNegativeButton(R.string.annulla, null)
                .show();
    }

    private void doImport(Uri uri) {
        try (InputStream in = getContentResolver().openInputStream(uri)) {
            StringBuilder sb = new StringBuilder();
            BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append('\n');
            }
            int n = storage.importJson(sb.toString());
            if (n < 0) {
                Toast.makeText(this, R.string.import_non_valido, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getString(R.string.import_ok, n), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.errore_generico, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
