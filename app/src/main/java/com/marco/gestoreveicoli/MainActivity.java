package com.marco.gestoreveicoli;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private Storage storage;
    private VehicleAdapter adapter;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        storage = Storage.get(this);
        emptyView = findViewById(R.id.emptyView);

        RecyclerView list = findViewById(R.id.vehicleList);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VehicleAdapter(storage.vehicles(), new VehicleAdapter.Listener() {
            @Override
            public void onClick(Vehicle v) {
                Intent i = new Intent(MainActivity.this, VehicleDetailActivity.class);
                i.putExtra("vehicle_id", v.id);
                startActivity(i);
            }

            @Override
            public void onLongClick(Vehicle v) {
                confirmDelete(v);
            }
        });
        list.setAdapter(adapter);

        findViewById(R.id.fabAddVehicle)
                .setOnClickListener(view -> VehicleDialog.show(this, storage, null, this::refresh));

        NotificationHelper.ensureChannels(this);
        DailyWorker.schedule(this);
        UpdateChecker.checkOnStartup(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        adapter.notifyDataSetChanged();
        emptyView.setVisibility(storage.vehicles().isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void confirmDelete(Vehicle v) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.elimina_veicolo)
                .setMessage(getString(R.string.conferma_elimina_veicolo, v.targa))
                .setPositiveButton(R.string.elimina, (d, w) -> {
                    PhotoStore.delete(this, v.id);
                    storage.delete(v);
                    refresh();
                })
                .setNegativeButton(R.string.annulla, null)
                .show();
    }

    static String text(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString().trim();
    }

    static long parseLong(String s) {
        try {
            return Long.parseLong(s.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return 0;
        }
    }
}
