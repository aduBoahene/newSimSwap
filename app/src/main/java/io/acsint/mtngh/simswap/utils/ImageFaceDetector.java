package io.acsint.mtngh.simswap.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.SparseArray;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import io.acsint.mtngh.simswap.R;

public class ImageFaceDetector {
    private Context context;

    public ImageFaceDetector(Context context) {
        this.context = context;
    }

    public Boolean detectHasSingleFace(Uri imageUri) {
        try {
            Bitmap image = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);

            FaceDetector faceDetector = new
                    FaceDetector.Builder(context).setTrackingEnabled(false)
                    .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                    .build();
            if (!faceDetector.isOperational()) {
                Toast.makeText(context, String.format("Detector is not operational"), Toast.LENGTH_LONG).show();
                return false;
            }

            Frame frame = new Frame.Builder()
                    .setBitmap(image).build();
            SparseArray<Face> faces = faceDetector.detect(frame);

            Toast.makeText(context, String.format("%d Faces detected", faces.size()), Toast.LENGTH_LONG).show();

            if (faces.size() != 1) {
                faceDetector.release();
                return false;
            }

            faceDetector.release();

            return true;
        }catch (Exception x){
            Toast.makeText(context, String.format("Error loading image"), Toast.LENGTH_LONG).show();
            return false;
        }
    }
}
