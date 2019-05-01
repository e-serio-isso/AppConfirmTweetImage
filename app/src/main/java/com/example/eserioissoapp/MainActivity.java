package com.example.eserioissoapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    Button btnSelectPhoto, btnConfirm;
    ImageView imgSelected;
    Uri selectedUri;
    Bitmap bitmap;
    JSONObject jsonObject;
    String encodedImage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startComponents();

        btnSelectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImgFromGallery();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendImgToServer();
            }
        });



    }

    private void sendImgToServer() {
        //encoded process
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        byte[] byteArrayImage = baos.toByteArray();
        encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);


        if(encodedImage != null){
            new UploadImages().execute();
        }
        else{
            Log.d("test", "image not found");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(data.getData()!=null){
                selectedUri = data.getData();
            }else{
                finish();
            }
            bitmap = null;
            try{
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedUri);
                imgSelected.setImageDrawable(new BitmapDrawable(bitmap));
            }catch (IOException e){
                Log.d("test", e.getMessage());
            }
        }
    }

    private void selectImgFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,0);
    }

    private void startComponents(){
        btnSelectPhoto = findViewById(R.id.btnSelectPhoto);
        btnConfirm = findViewById(R.id.btnConfirm);
        imgSelected = findViewById(R.id.imgView);
    }

    private class UploadImages extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                Log.d("test", "encodedImage = " + encodedImage);
                jsonObject = new JSONObject();
                jsonObject.put("imageString", encodedImage);
                jsonObject.put("imageName", "+917358513024");
                String data = jsonObject.toString();
                String yourURL = "http://192.168.0.101:5000/post";
                URL url = new URL(yourURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setFixedLengthStreamingMode(data.getBytes().length);
                connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                OutputStream out = new BufferedOutputStream(connection.getOutputStream());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
                writer.write(data);
                Log.d("test", "Data to server is = " + data);
                writer.flush();
                writer.close();
                out.close();
                connection.connect();

                InputStream in = new BufferedInputStream(connection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        in, "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                in.close();
                String result = sb.toString();
                Log.d("test", "Response from server = " + result);
                //Response = new JSONObject(result);
                connection.disconnect();
            } catch (Exception e) {
                Log.d("test", "Error Encountered");
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void args) {

        }
    }
}


