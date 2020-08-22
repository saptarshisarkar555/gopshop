package com.example.whatsapplite;

import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.List;
import java.util.Locale;

public class OcrActivity extends AppCompatActivity {

    private Button captureImageBtn,detectTextBtn,copyTextBtn,listenTextBtn;
    private EditText textView;
    private Bitmap imageBitmap;
    private TextToSpeech textToSpeech;

    public TextView copyText;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr);

        captureImageBtn=findViewById(R.id.capture_image_btn);
        detectTextBtn=findViewById(R.id.detect_text_image_btn);
        copyTextBtn=findViewById(R.id.copy_ocr_text_btn);
        listenTextBtn=findViewById(R.id.text_to_speech_btn);
        textView=findViewById(R.id.text_display);

        textToSpeech=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!=textToSpeech.ERROR){
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });

        listenTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toSpeech=textView.getText().toString();
                //Toast.makeText(getApplicationContext(), toSpeech,Toast.LENGTH_SHORT).show();
                textToSpeech.speak(toSpeech, TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        captureImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
                textView.setText("");
                detectTextBtn.setVisibility(View.VISIBLE);

                copyTextBtn.setVisibility(View.VISIBLE);

                listenTextBtn.setVisibility(View.VISIBLE);
            }
        });

        detectTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectTextFromImage();
            }
        });

        copyTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*Intent intent=new Intent(OcrActivity.this,ChatActivity.class);
                intent.putExtra("TEXT", copyText);
                startActivity(intent);*/
                if(textView.equals(null))
                {
                    Toast.makeText(OcrActivity.this, "Nothing selected", Toast.LENGTH_SHORT).show();
                }
                else {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    clipboard.setText(textView.getText());
                    Toast.makeText(OcrActivity.this, "Copied", Toast.LENGTH_SHORT).show();
                    textView.setText("");
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        if(textToSpeech!=null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
        }
    }

    private void detectTextFromImage() {
        FirebaseVisionImage firebaseVisionImage= FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextRecognizer firebaseVisionTextDetector= FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        firebaseVisionTextDetector.processImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                displayTextFromImage(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(OcrActivity.this, "Failed.....", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayTextFromImage(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.TextBlock> blockList = firebaseVisionText.getTextBlocks();
        if(blockList.size()==0){
            Toast.makeText(this, "No text found in image", Toast.LENGTH_SHORT).show();
        }
        else{
            for (FirebaseVisionText.TextBlock block: firebaseVisionText.getTextBlocks()){
                String text=block.getText();
                textView.setText(text);

                //copyText.setText(text);
            }
        }
    }


}