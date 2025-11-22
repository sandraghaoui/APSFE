package com.example.aps;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.util.Collections;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int INPUT_SIZE = 640;

    private PreviewView previewView;
    private ImageView cropPreview;
    private BoundingBoxOverlay boxOverlay;

    private OrtEnvironment ortEnv;
    private OrtSession ortSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.previewView);
        cropPreview = findViewById(R.id.cropPreview);
        boxOverlay = findViewById(R.id.boxOverlay);

        loadOnnxModel();
        requestCameraPermission();
    }

    // ---------------- PERMISSIONS ----------------
    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_REQUEST_CODE
            );
        } else {
            startCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted");
            startCamera();
        } else {
            Log.e(TAG, "Camera permission denied");
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
        }
    }

    // ---------------- ONNX MODEL LOADING ----------------
    private void loadOnnxModel() {
        try {
            ortEnv = OrtEnvironment.getEnvironment();

            InputStream is = getAssets().open("license-plate-finetune-v1n.onnx");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int read;
            while ((read = is.read(buf)) != -1) {
                bos.write(buf, 0, read);
            }
            is.close();
            byte[] modelBytes = bos.toByteArray();

            OrtSession.SessionOptions opts = new OrtSession.SessionOptions();
            ortSession = ortEnv.createSession(modelBytes, opts);

            Log.d(TAG, "ONNX model loaded OK");

        } catch (Exception e) {
            Log.e(TAG, "Failed to load ONNX model (Ask Gemini)", e);
            ortSession = null;
        }
    }

    // ---------------- CAMERA SETUP ----------------
    private void startCamera() {
        Log.d(TAG, "Starting camera…");

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis =
                        new ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();

                imageAnalysis.setAnalyzer(
                        ContextCompat.getMainExecutor(this),
                        this::processFrame
                );

                CameraSelector cameraSelector =
                        new CameraSelector.Builder()
                                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                                .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageAnalysis
                );

                Log.d(TAG, "Camera started successfully");

            } catch (Exception e) {
                Log.e(TAG, "Camera initialization failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    // ---------------- FRAME PROCESSING (YOLO + OCR) ----------------
    private void processFrame(ImageProxy imageProxy) {
        try {
            if (ortSession == null) {
                imageProxy.close();
                return;
            }

            Bitmap bitmap = ImageUtils.imageProxyToBitmap(imageProxy);
            if (bitmap == null) {
                imageProxy.close();
                return;
            }

            Bitmap resized = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);

            float[][] preds = runYolo(resized);
            if (preds == null) {
                runOnUiThread(() -> boxOverlay.setBox(null));
                return;
            }

            Rect bestBox = extractBestBox(preds, bitmap.getWidth(), bitmap.getHeight());
            if (bestBox == null) {
                runOnUiThread(() -> boxOverlay.setBox(null));
                return;
            }

            // The crop is created HERE → must stay in this scope
            Bitmap plateCrop = cropBitmap(bitmap, bestBox);
            if (plateCrop == null) return;

            runOcr(plateCrop);

            // The UI update must stay inside this block
            runOnUiThread(() -> {
                cropPreview.setImageBitmap(plateCrop);

                Rect scaled = scaleRectToPreview(
                        bestBox,
                        bitmap.getWidth(),
                        bitmap.getHeight()
                );

                boxOverlay.setBox(scaled);
            });

        } finally {
            imageProxy.close();
        }
    }


    // ---------------- Scale Box ----------------
    private Rect scaleRectToPreview(Rect box, int imgW, int imgH) {
        // Rotate 90 degrees clockwise
        int rotatedLeft   = box.top;
        int rotatedTop    = imgW - box.right;
        int rotatedRight  = box.bottom;
        int rotatedBottom = imgW - box.left;

        Rect rotated = new Rect(rotatedLeft, rotatedTop, rotatedRight, rotatedBottom);

        // Now scale to PreviewView size
        int viewW = previewView.getWidth();
        int viewH = previewView.getHeight();

        float scaleX = viewW / (float) imgH; // note swapped!
        float scaleY = viewH / (float) imgW;

        int sx = (int) (rotated.left * scaleX);
        int sy = (int) (rotated.top * scaleY);
        int ex = (int) (rotated.right * scaleX);
        int ey = (int) (rotated.bottom * scaleY);

        return new Rect(sx, sy, ex, ey);
    }



    // ---------------- ONNX INFERENCE ----------------
    private float[][] runYolo(Bitmap resized) {
        try {
            int w = resized.getWidth();
            int h = resized.getHeight();
            if (w != INPUT_SIZE || h != INPUT_SIZE) return null;

            int imageSize = INPUT_SIZE * INPUT_SIZE;
            float[] input = new float[3 * imageSize];

            for (int y = 0; y < INPUT_SIZE; y++) {
                for (int x = 0; x < INPUT_SIZE; x++) {
                    int pixel = resized.getPixel(x, y);
                    float r = ((pixel >> 16) & 0xFF) / 255f;
                    float g = ((pixel >> 8) & 0xFF) / 255f;
                    float b = (pixel & 0xFF) / 255f;

                    int idx = y * INPUT_SIZE + x;
                    input[idx] = r;
                    input[idx + imageSize] = g;
                    input[idx + 2 * imageSize] = b;
                }
            }

            FloatBuffer fb = FloatBuffer.wrap(input);
            long[] shape = new long[]{1, 3, INPUT_SIZE, INPUT_SIZE};

            String inputName = ortSession.getInputNames().iterator().next();

            try (OnnxTensor tensor = OnnxTensor.createTensor(ortEnv, fb, shape);
                 OrtSession.Result result = ortSession.run(
                         Collections.singletonMap(inputName, tensor))) {

                Object outObj = result.get(0).getValue();
                // Model output: [1, 5, 8400]
                float[][][] raw = (float[][][]) outObj;
                float[][] feat = raw[0]; // [5][8400]

                int channels = feat.length;      // should be 5
                int numPreds = feat[0].length;   // should be 8400

                float[][] preds = new float[numPreds][channels];
                for (int c = 0; c < channels; c++) {
                    for (int i = 0; i < numPreds; i++) {
                        preds[i][c] = feat[c][i];
                    }
                }

                Log.d(TAG, "YOLO preds: " + numPreds + " boxes, channels=" + channels);
                return preds;
            }

        } catch (Exception e) {
            Log.e(TAG, "ONNX error (Ask Gemini)", e);
            return null;
        }
    }

    // ---------------- POSTPROCESSING: PICK BEST BOX ----------------
    // preds shape: [numPreds][5] = [cx, cy, w, h, conf]
    private Rect extractBestBox(float[][] preds, int imgW, int imgH) {

        float bestScore = 0f;
        Rect best = null;

        for (int i = 0; i < preds.length; i++) {
            float cx = preds[i][0];
            float cy = preds[i][1];
            float w  = preds[i][2];
            float h  = preds[i][3];
            float conf = preds[i][4];

            if (conf < 0.4f) continue;

            // YOLOv11 outputs are in absolute pixel units of the 640×640 input image
            float scaleX = (float) imgW / 640f;
            float scaleY = (float) imgH / 640f;

            float boxCx = cx * scaleX;
            float boxCy = cy * scaleY;
            float boxW  = w  * scaleX;
            float boxH  = h  * scaleY;

            int left   = (int) (boxCx - boxW / 2f);
            int top    = (int) (boxCy - boxH / 2f);
            int right  = (int) (boxCx + boxW / 2f);
            int bottom = (int) (boxCy + boxH / 2f);

            left = Math.max(0, Math.min(left, imgW - 1));
            right = Math.max(0, Math.min(right, imgW - 1));
            top = Math.max(0, Math.min(top, imgH - 1));
            bottom = Math.max(0, Math.min(bottom, imgH - 1));

            if (right <= left || bottom <= top) continue;

            if (conf > bestScore) {
                bestScore = conf;
                best = new Rect(left, top, right, bottom);
            }
        }

        Log.d("YOLO", "Best score=" + bestScore + " box=" + best);
        return best;
    }


    private Bitmap cropBitmap(Bitmap source, Rect box) {
        try {
            return Bitmap.createBitmap(
                    source,
                    box.left,
                    box.top,
                    box.width(),
                    box.height()
            );
        } catch (Exception e) {
            Log.e(TAG, "Crop failed", e);
            return null;
        }
    }

    // ---------------- OCR ----------------
    private void runOcr(Bitmap plateCrop) {
        TextRecognizer recognizer =
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        InputImage image = InputImage.fromBitmap(plateCrop, 0);

        recognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
                        String text = visionText.getText();
                        Log.d("OCR", "Detected text: " + text);
                        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("OCR", "Failed", e);
                            }
                        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (ortSession != null) ortSession.close();
            if (ortEnv != null) ortEnv.close();
        } catch (Exception ignored) {
        }
    }
}
