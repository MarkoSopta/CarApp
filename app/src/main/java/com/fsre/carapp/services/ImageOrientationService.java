package com.fsre.carapp.services;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;

public class ImageOrientationService {

    public Bitmap rotateBitmap(File imageFile) {
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        try {
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Matrix matrix = getMatrix(orientation);
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @NonNull
    private static Matrix getMatrix(int orientation) {
        int rotationAngle = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotationAngle = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotationAngle = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotationAngle = 270;
                break;
            default:
                break;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationAngle);
        return matrix;
    }
}