package com.example.aps;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import androidx.camera.core.ImageProxy;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class ImageUtils {

    public static Bitmap imageProxyToBitmap(ImageProxy image) {
        android.media.Image mediaImage = image.getImage();
        if (mediaImage == null) return null;

        ByteBuffer yBuffer = mediaImage.getPlanes()[0].getBuffer(); // Y
        ByteBuffer uBuffer = mediaImage.getPlanes()[1].getBuffer(); // U
        ByteBuffer vBuffer = mediaImage.getPlanes()[2].getBuffer(); // V

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];

        // NV21 format → Y + V + U
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(
                nv21,
                ImageFormat.NV21,
                image.getWidth(),
                image.getHeight(),
                null
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(
                new Rect(0, 0, image.getWidth(), image.getHeight()),
                100,
                out
        );

        byte[] jpegBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.length);
    }
}
