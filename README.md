# LanCare

App Android per gestire i veicoli di proprietà e le relative manutenzioni.
Gli aggiornamenti vengono distribuiti tramite le release di questo repository e installati direttamente dall'app.

## Funzionalità

- **Elenco veicoli**: ogni veicolo ha targa, modello, proprietario e chilometri.
- **Aggiungi veicolo**: pulsante `+` nella schermata principale.
- **Dettaglio veicolo**: tocca un veicolo per aprire la scheda con i suoi dati, la spesa totale e lo storico delle manutenzioni.
- **Manutenzioni**: pulsante `+` nella scheda del veicolo. Tipi disponibili: tagliando, cambio olio, filtri, pneumatici, freni, batteria, distribuzione, frizione, climatizzatore, revisione, assicurazione, bollo, carrozzeria, riparazione, altro. Per ogni intervento si registrano data, chilometri, costo, prossima scadenza (opzionale) e note.
- **Modifica/elimina**:
  - Veicolo: menu ⋮ nella scheda del veicolo (oppure pressione lunga sull'elenco per eliminare).
  - Manutenzione: tocco per modificare, pressione lunga per eliminare.
- Se una manutenzione registra più km di quelli del veicolo, i km del veicolo vengono aggiornati automaticamente.
- I dati sono salvati in locale sul telefono (nessuna connessione richiesta).

## Installazione dell'APK

1. Copia `app/build/outputs/apk/release/app-release.apk` sul telefono (via cavo USB, email, Drive, ecc.).
2. Sul telefono apri il file APK.
3. Alla richiesta, consenti l'installazione da origini sconosciute (l'app non proviene dal Play Store).
4. Conferma l'installazione: troverai "Gestore Veicoli" tra le app.

## Ricompilare il progetto

Il progetto si apre direttamente con Android Studio (`File → Open → GestoreVeicoli`).
Da riga di comando, con Android SDK e JDK 17+ installati:

```
gradle assembleRelease
```

L'APK firmato viene generato in `app/build/outputs/apk/release/app-release.apk`
(chiave di firma: `app/release.keystore`, alias e password `gestoreveicoli`).
