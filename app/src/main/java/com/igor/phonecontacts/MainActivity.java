package com.igor.phonecontacts;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.support.v4.app.Fragment;

import com.igor.phonecontacts.models.Contact;
import com.igor.phonecontacts.utils.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity  implements ViewContactsFragment.OnContactSelectedListener,ContactFragment.OnEditContactListener,ViewContactsFragment.OnAddContactListener {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 1;


    @Override
    public void onEditContactSelected(Contact contact) {
        Log.d(TAG, "onEditContactSelected: contact selected from " + getString(R.string.edit_contactfragment) + " " + contact.getName());
        EditContactFragment fragment = new EditContactFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.contact),contact);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container,fragment);
        transaction.addToBackStack(getString(R.string.edit_contactfragment));
        transaction.commit();

    }

    @Override
    public void OnContactSelected(Contact con) {
        Log.d(TAG, "OnContactSelected: contact selected from " + getString(R.string.view_contact_fragment) + " " + con.getName());
        ContactFragment fragment = new ContactFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.contact),con);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container,fragment);
        transaction.addToBackStack(getString(R.string.contact_fragment));
        transaction.commit();
    }
    @Override
    public void onAddContact() {
        Log.d(TAG, "onAddContact: navigate to " + getString(R.string.add_contact_fragment));
        AddContactFragment fragment = new AddContactFragment();


        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container,fragment);
        transaction.addToBackStack(getString(R.string.add_contact_fragment));
        transaction.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: started");

        initImageLoader();
        init();
    }
    private void init(){
        ViewContactsFragment fragment = new ViewContactsFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container,fragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }

    /**
     * Compress a bitmap by the @param "quality"
     * Quality can be anywhere from 1 to 100
     * @param bitmap
     * @param quality
     * @return
     */
    public Bitmap compressBitmap(Bitmap bitmap,int quality){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,quality,stream);
        return bitmap;
    }
    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(MainActivity.this);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());

    }

    /**
     * method for asking permissions
     * @param permissions
     */
    public void verifyPermission(String[] permissions){
       // String[] perm = permissions;
        ActivityCompat.requestPermissions(MainActivity.this,permissions,REQUEST_CODE);
    }
    public boolean checkPermission(String[] permissions){
        Log.d(TAG, "checkPermission: checking permission");

        int permissionRequest = ActivityCompat.checkSelfPermission(MainActivity.this,permissions[0]);
        if (permissionRequest != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkPermission: \n Permission was not granted for: " + permissions[0]);
            return false;
        }else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CODE:
                for (int i = 0;i < permissions.length;i++){
                    if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                        Log.d(TAG, "onRequestPermissionsResult: User has allowed permission to access" + permissions[i]);
                    }else{
                        break;
                    }
                }
             break;
        }
    }

/*public Bitmap rotateImage (String imagePath){
    Bitmap yourSelectedImage= BitmapFactory.decodeFile(imPath);
    ExifInterface exifInterface = null;
    try {
        exifInterface = new ExifInterface(imPath);
    }catch (IOException e){
        e.printStackTrace();
    }
    int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_UNDEFINED);
    Matrix matrix = new Matrix();
    switch (orientation){
        case ExifInterface.ORIENTATION_ROTATE_90:
            matrix.setRotate(90);
            break;
        case ExifInterface.ORIENTATION_ROTATE_180:
            matrix.setRotate(180);
            break;
        default:
    }
    Bitmap rotatedBitmap = Bitmap.createBitmap(yourSelectedImage,0,0,yourSelectedImage.getWidth(),yourSelectedImage.getHeight(),matrix,true);
    mContactImage.setImageBitmap(rotatedBitmap);
}*/

}
