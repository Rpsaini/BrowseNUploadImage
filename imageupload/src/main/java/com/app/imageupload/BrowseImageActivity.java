package com.app.imageupload;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import com.app.imageupload.util.GetImageResponseBack;
import com.app.imageupload.util.UploadImage;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BrowseImageActivity extends AppCompatActivity
{
    private  ImageView imageView;
    private  String imageType,uploadingUrl;
    private  JSONObject uploadingParms;
    private  Bitmap bmap;
    private  String path="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_image);
      }


    public void showChooser(ImageView profileImage,String ImageType,String uplodingUrl, JSONObject uploadingParams)
    {
        this.imageView=profileImage;
        this.imageType=ImageType;
        this.uploadingParms=uploadingParams;
        this.uploadingUrl=uplodingUrl;


        final AlertDialog.Builder builder = new AlertDialog.Builder(BrowseImageActivity.this);
        builder.setTitle("Choose Image");
        builder.setMessage("Browse image either from camera or gallery.");

        builder.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id)
              {
                selectImage(1);
                dialog.dismiss();
              }
        });

        builder.setNegativeButton("Camera", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                selectImage(0);
                dialog.dismiss();

            }
        });
        Dialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }



    //update image===============
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void selectImage(int actionCode)
    {
        if (checkAndRequestPermissions() == 0) {
            if (actionCode == 0)
            {
                dispatchTakePictureIntent();
            } else if (actionCode == 1) {
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(pickPhoto, actionCode);
            }
        }

    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }



    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        bmap = null;
        Uri selectedImage = null;
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case 0:
                    try {

                        bmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoURI);
                        imageView.setImageBitmap(bmap);
                        path = getRealPathFromURI(getImageUri(this, bmap));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case 1:
                    if (resultCode == RESULT_OK) {
                        if (imageReturnedIntent != null) {
                            try {
                                selectedImage = imageReturnedIntent.getData();
                                path = getRealPathFromURI(selectedImage);
                                InputStream image_stream = getContentResolver().openInputStream(selectedImage);
                                bmap = BitmapFactory.decodeStream(image_stream);
                                imageView.setImageBitmap(bmap);
//                                UploadVideo uv = new UploadVideo();
//                                uv.execute();


                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
            }
        }

    }

    private int checkAndRequestPermissions()
    {
        int permissionCAMERA = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        int readExternal = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeExternal = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionCAMERA != PackageManager.PERMISSION_GRANTED)
        {
            listPermissionsNeeded.add(android.Manifest.permission.CAMERA);
        }
        if (readExternal != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (writeExternal != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 1);
            return 1;
        }

        return 0;
    }

    public String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        @SuppressWarnings("deprecation")
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }





    private String mCurrentPhotoPath;
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 0;
    Uri photoURI;


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.getMessage();

            }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.app.imageupload.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    takePictureIntent.setClipData(ClipData.newRawUri("", photoURI));
                    takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    public  class UploadVideo extends AsyncTask<Void, Void, String> {
        ProgressDialog uploading;

        private GetImageResponseBack imageResponseBack;
        public UploadVideo(GetImageResponseBack imageResponseBack)
        {
           this.imageResponseBack=imageResponseBack;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            uploading = ProgressDialog.show(BrowseImageActivity.this, "Uploading....", "Please wait...", false, false);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            uploading.dismiss();
            try {

                imageResponseBack.imageResponse(s);
//                if(s.equalsIgnoreCase("500")) {
//                    Toast.makeText(BrowseImageActivity.this, "This file is not supported.Please select another image", Toast.LENGTH_LONG).show();
//
//                } else {
//                    final JSONObject obj = new JSONObject(s);
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                String status = obj.get("status") + "";
//                                if (status.equalsIgnoreCase("true")) {
//                                    // Toast.makeText(BrowseImageActivity.this, obj.getString("msg"), Toast.LENGTH_LONG).show();
//                                    //{"status":true,"msg":"Your profile is updated successfully!","code":200,"data":{"first_name":"siva","last_name":"ji","email":"shiva@mailinator.com","profile_image":"http:\/\/webcomclients.in\/white-city\/resources\/basic\/users\/1595836471679.jpg_"}}
//                                    obj.getJSONObject("data");
//
//                                }
//                                else
//                                {
//                                    Toast.makeText(BrowseImageActivity.this, obj.getString("msg"), Toast.LENGTH_LONG).show();
//                                }
//                            }
//                            catch (Exception e)
//                            {
//                                e.printStackTrace();
//                            }
//                        }
//                    });

               // }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... params)
        {
            UploadImage u = new UploadImage();
            return u.UploadImage(BrowseImageActivity.this,path, imageType, uploadingUrl,uploadingParms);

        }
    }
}
