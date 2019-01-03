package com.igor.phonecontacts.utils;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.igor.phonecontacts.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.igor.phonecontacts.utils.Init.PICKFILE_REQUEST_CODE;

public class ChangePhotoDialog extends DialogFragment {
    private static final String TAG = "ChangePhotoDialog";
    private Uri mCapturedImageURI;

    public interface OnPhotoReceivedListener{
     //   public void getBitmapImage(Bitmap bitmap);
        public void getImagePath(String imagePath);
    }
    OnPhotoReceivedListener mOnPhotoReceiver;
    private String mCurrentPhotoPath;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.dialog_changephoto,container,false);

       //initialize the text view for starting camera
        TextView takePhoto = (TextView)view.findViewById(R.id.dialogTakePhoto);
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: starting camera");
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);


                if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null){
                    //Create tne file where photo should go
                    File photoFile = null;
                    try{
                        photoFile = createImageFile();
                    }catch (IOException e){
                        //Error occurred while creating the File
                        Log.d(TAG, "onClick: error" + e.getMessage());
                    }
                    //Continue only if the File was successfully created
                    if (photoFile != null){
                        Uri photoUri = FileProvider.getUriForFile(getActivity(),"com.example.android.fileprovider",photoFile);

                       // Uri photoUri = FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName() + ".provider", photoFile);
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                        startActivityForResult(cameraIntent,Init.CAMERA_REQUEST_CODE);

                    }
                }

            }
        });
        //initialize the text view for choosing image from memory
        TextView selectPhoto = (TextView)view.findViewById(R.id.dialogChoosePhoto);
        selectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: accessing phone memory");
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,Init.PICKFILE_REQUEST_CODE);

            }
        });
        //Cancel button for closing dialog
        TextView cancelDialog = (TextView)view.findViewById(R.id.dialogCancel);
        cancelDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: closing dialog");
                getDialog().dismiss();

            }
        });

       return view;
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mOnPhotoReceiver = (OnPhotoReceivedListener)getTargetFragment();
           // mOnPhotoReceiver = (OnPhotoReceivedListener)getActivity();
        }catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException" + e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*
        Result when taking image from the camera
         */
        if (requestCode == Init.CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            Log.d(TAG, "onActivityResult: image uri" + mCurrentPhotoPath);
            //get the new image bitmap
            Uri takedImage = Uri.fromFile(new File(mCurrentPhotoPath));
            mOnPhotoReceiver.getImagePath(takedImage.toString());
            getDialog().dismiss();

        }
        /*
        Results when selecting new image from phone memory
         */
        if (requestCode == PICKFILE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            Uri selectedImageUri = data.getData();
            File file = new File(selectedImageUri.toString());
            Log.d(TAG, "onActivityResult: image: " + selectedImageUri);

            //send the bitmap and fragment to the interface
            mOnPhotoReceiver.getImagePath(file.getPath());
             getDialog().dismiss();
        }

    }
}
