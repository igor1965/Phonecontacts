package com.igor.phonecontacts;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.igor.phonecontacts.models.Contact;
import com.igor.phonecontacts.utils.ChangePhotoDialog;
import com.igor.phonecontacts.utils.DatabaseHelper;
import com.igor.phonecontacts.utils.Init;
import com.igor.phonecontacts.utils.RotateBitmap;
import com.igor.phonecontacts.utils.UniversalImageLoader;

import java.io.IOException;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddContactFragment extends Fragment implements ChangePhotoDialog.OnPhotoReceivedListener {

    private static final String TAG = "AddContactFragment";

   // private Contact mContact;
    private EditText mPhoneNumber,mName,mEmail;
    private CircleImageView mContactImage;
    private Spinner mSelectDevice;
    private Toolbar toolbar;
    private String mSelectedImagePath;
    private int mPreviousKeyStroke;
    private static final int REQUEST_CODE = 11;
    String[] permissionsRequired = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_addcontact,container,false);
        mPhoneNumber = (EditText)view.findViewById(R.id.etContactPhone);
        mName = (EditText)view.findViewById(R.id.etContactName);
        mEmail = (EditText)view.findViewById(R.id.etContactEmail);
        mContactImage = (CircleImageView)view.findViewById(R.id.contactImage);
        mSelectDevice = (Spinner)view.findViewById(R.id.selectDevice);
        toolbar = (Toolbar) view.findViewById(R.id.editContactToolbar);
        Log.d(TAG, "onCreateView: started");

        mSelectedImagePath = null;
        //load the default image
        UniversalImageLoader.setImage(null,mContactImage,null,"");

        TextView heading = (TextView)view.findViewById(R.id.textContactToolbar);
        heading.setText(getString(R.string.add_contact));

        //required to set up the toolbar
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        // navigation for the backarrow
        ImageView ivBackArrow = (ImageView)view.findViewById(R.id.ivBackArrow);
        ivBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked back arrow");
                //remove previous fragment from the the backstack(therefore navigation back)
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        //save the new contact
        ImageView ivCheckMark = (ImageView)view.findViewById(R.id.ivCheckMark);
        ivCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: saving the contact");
                //execute saving method for database
                if (checkStringIfNull(mName.getText().toString())){
                    Log.d(TAG, "onClick: saving new contact " + mName.getText().toString());
                    DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
                    Contact contact = new Contact(mName.getText().toString(),
                            mPhoneNumber.getText().toString(),
                            mSelectDevice.getSelectedItem().toString(),
                            mEmail.getText().toString(),mSelectedImagePath);
                    if (databaseHelper.addContact(contact)){
                        Toast.makeText(getActivity(), "Contact Saved", Toast.LENGTH_SHORT).show();
                        getActivity().getSupportFragmentManager().popBackStack();
                    }else{
                        Toast.makeText(getActivity(), "Error saving", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        // initiation dialog box for choosing an image
        ImageView ivCamera = (ImageView)view.findViewById(R.id.ivCamera);
        ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*//Make sure all permissions have ben verified before opening the dialog

                for (int i = 0; i < permissionsRequired.length; i++) {
                    String[] permission = {permissionsRequired[i]};
                    if (((MainActivity) getActivity()).checkPermission(permission)) {
                        if (i == permissionsRequired.length - 1) {
                            Log.d(TAG, "opening image selection dialog box");
                            ChangePhotoDialog dialog = new ChangePhotoDialog();
                            dialog.show(getFragmentManager(), getString(R.string.change_photo_dialog));
                            dialog.setTargetFragment(AddContactFragment.this, 0);

                        }
                    } else {
                        ((MainActivity) getActivity()).verifyPermission(permissionsRequired);
                    }
                }*/
                if (android.os.Build.VERSION.SDK_INT >= 23) {
                    if (((ActivityCompat.checkSelfPermission(getContext(),permissionsRequired[0])) != PackageManager.PERMISSION_GRANTED) || (ActivityCompat.checkSelfPermission(getContext(),permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED) || (ActivityCompat.checkSelfPermission(getContext(),permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED)) {
                        ActivityCompat.requestPermissions(getActivity(), permissionsRequired, REQUEST_CODE);
                    }
                }
                photoDialog();
            }
        });


        initOnTextChangeListener();
        return view;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED && grantResults[2]==PackageManager.PERMISSION_GRANTED){
                photoDialog();
            }else{
                Toast.makeText(getContext(), "Pemission not granted", Toast.LENGTH_SHORT).show();
            }
        }else{
            super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }
    public void photoDialog(){
        ChangePhotoDialog dialog = new ChangePhotoDialog();
        dialog.show(getFragmentManager(), getString(R.string.change_photo_dialog));
        dialog.setTargetFragment(AddContactFragment.this, 0);
    }
    private boolean checkStringIfNull(String string){
        if (string.equals("")){
            return false;
        }else {
            return true;
        }
    }

    /**
     * Initialize OnTextChangeListener for formatting the phone number
     */
    private void initOnTextChangeListener(){

        mPhoneNumber.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
               // mPreviousKeyStroke= i;
                String number = "";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    if (i == 66){
                        number = PhoneNumberUtils.formatNumber(mPhoneNumber.getText().toString(),Locale.getDefault().getCountry());
                        mPhoneNumber.setText(number);
                    }
                }else{
                    number = PhoneNumberUtils.formatNumber(mPhoneNumber.getText().toString());
                    mPhoneNumber.setText(number);
                }
                return false;
            }
        });
        /*mPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String number = editable.toString();
                Log.d(TAG, "afterTextChanged:  " + number);

                if(number.length() == 3 && mPreviousKeyStroke != KeyEvent.KEYCODE_DEL && !number.contains("(")){
                    number = String.format("(%s",editable.toString().substring(0,3));
                    mPhoneNumber.setText(number);
                    mPhoneNumber.setSelection(number.length());
                }
                else if (number.length() == 5 && mPreviousKeyStroke != KeyEvent.KEYCODE_DEL && !number.contains(")")){
                    number = String.format("(%s)%s",editable.toString().substring(1,4),editable.toString().substring(4,5));
                    mPhoneNumber.setText(number);
                    mPhoneNumber.setSelection(number.length());
                }
                else if (number.length() == 10 && mPreviousKeyStroke != KeyEvent.KEYCODE_DEL && !number.contains("-")){
                    number = String.format("(%s) %s-%s",editable.toString().substring(1,4),editable.toString().substring(6,9),editable.toString().substring(9,10));
                    mPhoneNumber.setText(number);
                    mPhoneNumber.setSelection(number.length());
                }

            }
        });*/
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.contact_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.menuitem_delete:
                Log.d(TAG, "onOptionsItemSelected: deleting contact");
        }
        return super.onOptionsItemSelected(item);
    }
   /* @Override
    public void getBitmapImage(Bitmap bitmap,String imagePath) {
        Log.d(TAG, "getBitmapImage: got the bitmap" + bitmap);
        //get bitmap from ChangePhotoDialog
        if(bitmap != null){
            ((MainActivity)getActivity()).compressBitmap(bitmap,70);
            mContactImage.setImageBitmap(bitmap);
            mSelectedImagePath = imagePath;
        }
    }*/

    @Override
    public void getImagePath(String imagePath) {
        Log.d(TAG, "getImagePath: got the image path: " + imagePath);
        if(!imagePath.equals("")){

            imagePath = imagePath.replace(":/","://");
            mSelectedImagePath = imagePath;
            UniversalImageLoader.setImage(imagePath,mContactImage,null,"");
            Bitmap bitmap;
            Uri imagePathUri = Uri.parse(mSelectedImagePath);
            try{
                RotateBitmap rotateBitmap = new RotateBitmap();
                bitmap = rotateBitmap.HandleSamplingAndRotationBitmap(getActivity(),imagePathUri);
                mContactImage.setImageBitmap(bitmap);
            }catch(IOException e){
                e.printStackTrace();
            }

        }
       /* else {
            mContactImage.setImageResource(R.drawable.ic_android);
        }*/
    }


}
