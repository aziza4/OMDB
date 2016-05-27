package com.example.jbt.omdb;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class ImageHelper {

    public static File createImageFile(Context context)
    {
        String timestampFormat = context.getString(R.string.filename_timestap_format);
        String timeStamp = new SimpleDateFormat(timestampFormat, Locale.US).format( new Date());
        String imageFileName = "JPEG_" + timeStamp + "_" ;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = null;

        try {

            image = File. createTempFile( imageFileName, ".jpg", storageDir);

        } catch (IOException e) {
            Log.e(MainActivity.LOG_CAT, "" + e.getMessage());
        }

        return image;
    }


    public static Bitmap getImageFromGallery(String path)
    {
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = 8; // cannot pass too large data within intent extra, see: http://stackoverflow.com/questions/34460827/i-cant-pass-too-large-arraylist-of-objects-between-2-activities

        Bitmap image = BitmapFactory.decodeFile(path, o2);

        int rotate = getCameraPhotoOrientation(path);

        if (rotate < 0)
            return null;

        if (rotate != 0)
            image = RotateImage(image, rotate);

        return image;
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
            rotate = -1;
            Log.e(MainActivity.LOG_CAT, "" + e.getMessage());
        }

        return rotate;
    }


    private static Bitmap RotateImage(Bitmap image, int rotate)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        return Bitmap.createBitmap(image , 0, 0, image.getWidth(), image.getHeight(), matrix, true);
    }

    public static byte[] convertBitmapToByteArray(Bitmap bitmap) {

        if (bitmap == null)
            return null;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap convertByteArrayToBitmap(byte[] byteArray) {

        if (byteArray == null)
            return null;

        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
}
