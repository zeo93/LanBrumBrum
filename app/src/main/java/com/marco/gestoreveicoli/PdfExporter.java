package com.marco.gestoreveicoli;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Genera il PDF della scheda veicolo: dati, foto e storico manutenzioni. */
public class PdfExporter {

    private static final int W = 595;   // A4 a 72 dpi
    private static final int H = 842;
    private static final int M = 44;    // margine

    private final Context ctx;
    private final Vehicle v;
    private PdfDocument doc;
    private PdfDocument.Page page;
    private Canvas canvas;
    private int y;
    private int pageNum;

    private final Paint pTitolo = paint(22, true, 0xFF0D47A1);
    private final Paint pSotto = paint(13, false, 0xFF444444);
    private final Paint pH2 = paint(14, true, 0xFF0D47A1);
    private final Paint pBody = paint(11, false, 0xFF222222);
    private final Paint pBold = paint(11, true, 0xFF222222);
    private final Paint pSmall = paint(9, false, 0xFF666666);
    private final Paint pLinea = new Paint();

    private PdfExporter(Context ctx, Vehicle v) {
        this.ctx = ctx;
        this.v = v;
        pLinea.setColor(0xFFBBBBBB);
        pLinea.setStrokeWidth(0.8f);
    }

    private static Paint paint(int size, boolean bold, int color) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTextSize(size);
        p.setColor(color);
        p.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        return p;
    }

    public static void export(Context ctx, Vehicle v, OutputStream out) throws IOException {
        new PdfExporter(ctx, v).write(out);
    }

    private void write(OutputStream out) throws IOException {
        doc = new PdfDocument();
        newPage();

        // intestazione
        canvas.drawText(ctx.getString(R.string.pdf_intestazione), M, y, pSmall);
        y += 24;
        canvas.drawText(v.targa, M, y, pTitolo);
        y += 20;
        canvas.drawText(v.marcaModello(), M, y, pSotto);
        y += 22;

        // foto
        Bitmap foto = PhotoStore.load(ctx, v.id, 900);
        if (foto != null) {
            int maxW = W - 2 * M;
            int maxH = 210;
            float scale = Math.min((float) maxW / foto.getWidth(), (float) maxH / foto.getHeight());
            int fw = Math.round(foto.getWidth() * scale);
            int fh = Math.round(foto.getHeight() * scale);
            canvas.drawBitmap(foto, null, new Rect(M, y, M + fw, y + fh), null);
            y += fh + 18;
        }

        // dati
        riga(ctx.getString(R.string.proprietario),
                v.proprietario == null || v.proprietario.isEmpty()
                        ? ctx.getString(R.string.nessun_proprietario) : v.proprietario);
        riga(ctx.getString(R.string.chilometri), String.format(Locale.ITALY, "%,d km", v.km));
        if (v.immatricolazione != null && !v.immatricolazione.isEmpty()) {
            riga(ctx.getString(R.string.pdf_immatricolazione), v.immatricolazione);
            riga(ctx.getString(R.string.tipo_revisione), v.prossimaRevisione());
            riga(ctx.getString(R.string.tipo_bollo), v.prossimoBollo());
        }
        if (v.assicurazioneScadenza != null && !v.assicurazioneScadenza.isEmpty()) {
            String val = v.assicurazioneScadenza;
            if (v.assicurazioneCompagnia != null && !v.assicurazioneCompagnia.trim().isEmpty()) {
                val += " (" + v.assicurazioneCompagnia.trim() + ")";
            }
            riga(ctx.getString(R.string.tipo_assicurazione), val);
        }

        y += 8;
        canvas.drawLine(M, y, W - M, y, pLinea);
        y += 24;

        // storico manutenzioni
        List<Maintenance> ordinate = new ArrayList<>(v.manutenzioni);
        ordinate.sort(Comparator.comparing(
                (Maintenance m) -> Maintenance.ordinabile(m.data)).reversed());

        canvas.drawText(ctx.getString(R.string.pdf_storico, ordinate.size()), M, y, pH2);
        y += 20;

        if (ordinate.isEmpty()) {
            canvas.drawText(ctx.getString(R.string.pdf_nessuna_manutenzione), M, y, pBody);
            y += 16;
        }
        for (Maintenance m : ordinate) {
            spazio(46);
            canvas.drawText(m.tipo, M, y, pBold);
            String costo = String.format(Locale.ITALY, "€ %,.2f", m.costo);
            canvas.drawText(costo, W - M - pBold.measureText(costo), y, pBold);
            y += 14;
            canvas.drawText(m.data + " · " + String.format(Locale.ITALY, "%,d km", m.km), M, y, pBody);
            y += 13;
            if (m.scadenza != null && !m.scadenza.isEmpty()) {
                canvas.drawText(ctx.getString(R.string.scadenza_prefix, m.scadenza), M, y, pSmall);
                y += 12;
            }
            if (m.note != null && !m.note.isEmpty()) {
                for (String linea : wrap(m.note, pSmall, W - 2 * M)) {
                    spazio(12);
                    canvas.drawText(linea, M, y, pSmall);
                    y += 12;
                }
            }
            y += 8;
        }

        spazio(30);
        canvas.drawLine(M, y, W - M, y, pLinea);
        y += 20;
        String totale = ctx.getString(R.string.pdf_totale,
                String.format(Locale.ITALY, "€ %,.2f", v.totaleSpese()));
        canvas.drawText(totale, W - M - pBold.measureText(totale), y, pBold);

        doc.finishPage(page);
        doc.writeTo(out);
        doc.close();
    }

    private void riga(String etichetta, String valore) {
        spazio(16);
        canvas.drawText(etichetta + ":", M, y, pSmall);
        canvas.drawText(valore == null ? "" : valore, M + 120, y, pBody);
        y += 16;
    }

    private void spazio(int necessario) {
        if (y + necessario > H - M) {
            doc.finishPage(page);
            newPage();
        }
    }

    private void newPage() {
        pageNum++;
        page = doc.startPage(new PdfDocument.PageInfo.Builder(W, H, pageNum).create());
        canvas = page.getCanvas();
        canvas.drawColor(Color.WHITE);
        y = M + 8;
    }

    private static List<String> wrap(String testo, Paint p, int maxW) {
        List<String> righe = new ArrayList<>();
        for (String paragrafo : testo.split("\n")) {
            String resto = paragrafo.trim();
            while (!resto.isEmpty()) {
                int n = p.breakText(resto, true, maxW, null);
                if (n < resto.length()) {
                    int spazio = resto.lastIndexOf(' ', n);
                    if (spazio > n / 2) {
                        n = spazio;
                    }
                }
                righe.add(resto.substring(0, n).trim());
                resto = resto.substring(n).trim();
            }
        }
        return righe;
    }
}
