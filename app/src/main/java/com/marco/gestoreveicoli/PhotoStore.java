package com.marco.gestoreveicoli;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/** Foto dei veicoli, salvate in files/photos/&lt;idVeicolo&gt;.jpg. */
public class PhotoStore {

    private static final int MAX_DIM = 1600;

    public static File file(Context c, String vehicleId) {
        File dir = new File(c.getFilesDir(), "photos");
        dir.mkdirs();
        return new File(dir, vehicleId + ".jpg");
    }

    public static boolean has(Context c, String vehicleId) {
        return file(c, vehicleId).exists();
    }

    /** Copia l'immagine scelta, ridimensionata e raddrizzata secondo l'EXIF. */
    public static boolean save(Context c, String vehicleId, Uri source) {
        File tmp = new File(c.getCacheDir(), "photo_tmp");
        try {
            try (InputStream in = c.getContentResolver().openInputStream(source);
                 OutputStream out = new FileOutputStream(tmp)) {
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) {
                    out.write(buf, 0, n);
                }
            }

            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(tmp.getAbsolutePath(), bounds);
            int sample = 1;
            while (bounds.outWidth / sample > MAX_DIM * 2 || bounds.outHeight / sample > MAX_DIM * 2) {
                sample *= 2;
            }
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inSampleSize = sample;
            Bitmap bmp = BitmapFactory.decodeFile(tmp.getAbsolutePath(), opts);
            if (bmp == null) {
                return false;
            }

            int rotation = 0;
            try {
                ExifInterface exif = new ExifInterface(tmp.getAbsolutePath());
                switch (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotation = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotation = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotation = 270;
                        break;
                }
            } catch (Exception ignored) {
            }
            if (rotation != 0) {
                Matrix m = new Matrix();
                m.postRotate(rotation);
                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
            }

            float scale = Math.min(1f, (float) MAX_DIM / Math.max(bmp.getWidth(), bmp.getHeight()));
            if (scale < 1f) {
                bmp = Bitmap.createScaledBitmap(bmp,
                        Math.round(bmp.getWidth() * scale), Math.round(bmp.getHeight() * scale), true);
            }

            try (FileOutputStream out = new FileOutputStream(file(c, vehicleId))) {
                bmp.compress(Bitmap.CompressFormat.JPEG, 85, out);
            }
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            tmp.delete();
        }
    }

    /** Carica la foto; maxDim limita la dimensione (es. miniature nelle liste). */
    public static Bitmap load(Context c, String vehicleId, int maxDim) {
        File f = file(c, vehicleId);
        if (!f.exists()) {
            return null;
        }
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(f.getAbsolutePath(), bounds);
        int sample = 1;
        while (bounds.outWidth / sample > maxDim * 2 || bounds.outHeight / sample > maxDim * 2) {
            sample *= 2;
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = sample;
        return BitmapFactory.decodeFile(f.getAbsolutePath(), opts);
    }

    public static void delete(Context c, String vehicleId) {
        file(c, vehicleId).delete();
    }
}
