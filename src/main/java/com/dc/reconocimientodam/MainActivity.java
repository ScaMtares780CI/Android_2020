package com.dc.reconocimientodam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dc.reconocimientodam.helper.InternetCheck;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionCloudImageLabelerOptions;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceImageLabelerOptions;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import org.w3c.dom.Text;

import java.util.List;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    CameraView cameraView;
    Button btnDetectar;
    AlertDialog EsperaDialog;

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

           cameraView =(CameraView) findViewById(R.id.camera_view);
           btnDetectar =(Button) findViewById(R.id.btn_detectar);

           EsperaDialog =  new SpotsDialog.Builder().setMessage("Por favor espere").
                   setContext(this)
                   .setCancelable(false).build();
           cameraView.addCameraKitListener(new CameraKitEventListener() {
               @Override
               public void onEvent(CameraKitEvent cameraKitEvent) {

               }

               @Override
               public void onError(CameraKitError cameraKitError) {

               }

               @Override
               public void onImage(CameraKitImage cameraKitImage) {
                   EsperaDialog.show();
                   Bitmap bitmap = cameraKitImage.getBitmap();
                   bitmap = Bitmap.createScaledBitmap(bitmap, cameraView.getWidth(), cameraView.getHeight(), false);
                   cameraView.stop();

                   runDetector(bitmap);

               }

               @Override
               public void onVideo(CameraKitVideo cameraKitVideo) {

               }
           });

           btnDetectar.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   cameraView.start();
                   cameraView.captureImage();
               }
           });

    }

    private void runDetector(Bitmap bitmap) {

        final FirebaseVisionImage  image= FirebaseVisionImage.fromBitmap(bitmap);

        new InternetCheck(new InternetCheck.Consumer() {
            @Override
            public void accept(boolean internet) {
                      if(internet){
                          // si tiene inetrnet usa la nube
                          FirebaseVisionCloudDetectorOptions options =
                                  new FirebaseVisionCloudDetectorOptions.Builder()
                                          .setMaxResults(1) //obtenemos el primer resultado con la mayor concidencia
                                           .build();
                          FirebaseVisionImageLabeler  detector = FirebaseVision.getInstance().getCloudImageLabeler();

                          detector.processImage(image)
                                  .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                                      @Override
                                      public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
                                       ResultadoDelProceso(firebaseVisionImageLabels);
                                      }
                                  })
                                  .addOnFailureListener(new OnFailureListener() {
                                      @Override
                                      public void onFailure(@NonNull Exception e) {
                                              Log.d("EDMTERROR",e.getMessage());
                                      }
                                  });
                      }else {
                          FirebaseVisionOnDeviceImageLabelerOptions options =
                                  new FirebaseVisionOnDeviceImageLabelerOptions.Builder()
                                          .setConfidenceThreshold(0.8f) //get highest Confidence Threshold
                                          .build();
                          FirebaseVisionImageLabeler   detector = FirebaseVision.getInstance().getOnDeviceImageLabeler(options);

                          detector.processImage(image)
                                   .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                                       @Override
                                       public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
                                           ResultadoDelProceso(firebaseVisionImageLabels);
                                       }
                                   })
                                  .addOnFailureListener(new OnFailureListener() {
                                      @Override
                                      public void onFailure(@NonNull Exception e) {
                                          Log.d("EDMTERROR",e.getMessage());
                                      }
                                  });

                      }
            }
        }) ;
    }

    private void ResultadoDelProceso(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
        for(FirebaseVisionImageLabel label : firebaseVisionImageLabels){
            Toast.makeText(this, "Cloud result "+ label.getText(), Toast.LENGTH_SHORT).show();
        }

        if(EsperaDialog.isShowing())
        EsperaDialog.dismiss();
    }
}
