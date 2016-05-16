package com.example.jbt.omdb;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class ImageHelper {

    public static File createImageFile(Context context)
    {
        String timestampFormat = context.getResources().getString(R.string.filename_timestap_format);
        String timeStamp = new SimpleDateFormat(timestampFormat, Locale.US).format( new Date());
        String imageFileName = "JPEG_" + timeStamp + "_" ;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment. DIRECTORY_PICTURES);

        File image = null;

        try {
            image = File. createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {

            Log.e(MainActivity.LOG_CAT, "" + e.getMessage());
        }

        return image;
    }

    public static Bitmap getImageFromGallery(String path)
    {
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = 4;

        Bitmap cachedImage = BitmapFactory.decodeFile(path, o2);

        int rotate = getCameraPhotoOrientation(path);

        if (rotate != 0)
            cachedImage = RotateImage(cachedImage, rotate);

        return cachedImage;
    }


    private static int getCameraPhotoOrientation(String path)
    {
        int rotate = 0;

        try {
            ExifInterface exif = new ExifInterface(path);

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            Log.e(MainActivity.LOG_CAT, "" + e.getMessage());
        }
        return rotate;
    }

    private static Bitmap RotateImage(Bitmap image, int rotate)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);

        // Here you will get the image bitmap which has changed orientation
        return Bitmap.createBitmap(image , 0, 0, image.getWidth(), image.getHeight(), matrix, true);
    }
}
