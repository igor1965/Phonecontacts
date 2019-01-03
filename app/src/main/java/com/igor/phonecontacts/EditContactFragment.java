package com.igor.phonecontacts;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.igor.phonecontacts.R.string;



import com.igor.phonecontacts.models.Contact;
import com.igor.phonecontacts.utils.ChangePhotoDialog;
import com.igor.phonecontacts.utils.DatabaseHelper;
import com.igor.phonecontacts.utils.Init;
import com.igor.phonecontacts.utils.RotateBitmap;
import com.igor.phonecontacts.utils.UniversalImageLoader;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditContactFragment extends Fragment implements ChangePhotoDialog.OnPhotoReceivedListener {

    private static final String TAG = "EditContactFragment";

    public EditContactFragment(){
        super();
        setArguments(new Bundle());
    }

    private Contact mContact;
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
        View view = inflater.inflate(R.layout.fragment_editcontact,container,false);
        mPhoneNumber = (EditText)view.findViewById(R.id.etContactPhone);
        mName = (EditText)view.findViewById(R.id.etContactName);
        mEmail = (EditText)view.findViewById(R.id.etContactEmail);
        mContactImage = (CircleImageView)view.findViewById(R.id.contactImage);
        mSelectDevice = (Spinner)view.findViewById(R.id.selectDevice);
        toolbar = (Toolbar) view.findViewById(R.id.editContactToolbar);
        Log.d(TAG, "onCreateView: started");

        mSelectedImagePath = null;


        //set the heading for toolbar
        TextView heading = (TextView)view.findViewById(R.id.textContactToolbar);
        heading.setText(getString(string.edit_contact));


        //required to set up the toolbar
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        // get the contact from the bundle
        mContact = getContactFromBundle();

        if (mContact != null){
            init();
        }


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
        //save changes to the contact
        ImageView ivCheckMark = (ImageView)view.findViewById(R.id.ivCheckMark);
        ivCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: saving the contact");
                //execute saving method for database
                if (checkStringIfNull(mName.getText().toString())) {
                    Log.d(TAG, "onClick: saving changes to the contact " + mName.getText().toString());
                    DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
                    Cursor cursor = databaseHelper.getContactID(mContact);

                    int contactID = -1;
                    while (cursor.moveToNext()) {
                        contactID = cursor.getInt(0);
                    }
                    if (contactID > -1) {
                        if (mSelectedImagePath != null) {
                            mContact.setProfileImage(mSelectedImagePath);
                        }
                        mContact.setName(mName.getText().toString());
                        mContact.setPhonenumber(mPhoneNumber.getText().toString());
                        mContact.setDevice(mSelectDevice.getSelectedItem().toString());
                        mContact.setEmail(mEmail.getText().toString());
                        databaseHelper.updateContact(mContact, contactID);
                        Toast.makeText(getActivity(), "Contact updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "database error", Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });
        // initiation dialog box for choosing an image
        ImageView ivCamera = (ImageView)view.findViewById(R.id.ivCamera);
        ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Make sure all permissions have ben verified before opening the dialog

                /*for (int i = 0; i < Init.PERMISSIONS.length; i++) {
                    String[] permission = {Init.PERMISSIONS[i]};
                    if (((MainActivity) getActivity()).checkPermission(permission)) {
                        if (i == Init.PERMISSIONS.length - 1) {
                            Log.d(TAG, "opening image selection dialog box");
                            ChangePhotoDialog dialog = new ChangePhotoDialog();
                            dialog.show(getFragmentManager(), getString(string.change_photo_dialog));
                            dialog.setTargetFragment(EditContactFragment.this, 0);
                        }
                    } else {
                        ((MainActivity) getActivity()).verifyPermission(permission);
                    }
                }
*/
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
        dialog.setTargetFragment(EditContactFragment.this, 0);
    }
    private boolean checkStringIfNull(String string){
        if (string.equals("")){
            return false;
        }else {
            return true;
        }
    }
    private void init(){
        mPhoneNumber.setText(mContact.getPhonenumber());
        mName.setText(mContact.getName());
        mEmail.setText(mContact.getEmail());
        UniversalImageLoader.setImage(mContact.getProfileImage(),mContactImage,null,"");
        /*if (mContact.getProfileImage() == null){
           // mContactImage.setImageResource(R.drawable.ic_android);
            UniversalImageLoader.setImage(mContact.getProfileImage(),mContactImage,null,"");
        }else {
            Bitmap bitmap;
            Uri imagePathUri = Uri.parse(mContact.getProfileImage());
            try {
                RotateBitmap rotateBitmap = new RotateBitmap();
                bitmap = rotateBitmap.HandleSamplingAndRotationBitmap(getActivity(), imagePathUri);
                mContactImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

        //setting the selected device to the spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),R.array.device_options,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSelectDevice.setAdapter(adapter);
        int position = adapter.getPosition(mContact.getDevice());
        mSelectDevice.setSelection(position);

    }

    /**
     * Retrives the selected contact from the bundle (coming from MainActivity)
     * @return
     */
    private Contact getContactFromBundle(){
        Log.d(TAG, "getContactFromBundle: arguments: " + getArguments());

        Bundle bundle = this.getArguments();
        if (bundle != null){
            return bundle.getParcelable(getString(R.string.contact));
        }else{
            return null;
        }
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
                DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
                Cursor cursor = databaseHelper.getContactID(mContact);
                int contactID = -1;
                while (cursor.moveToNext()){
                    contactID = cursor.getInt(0);
                }
                if (contactID >-1){
                    if (databaseHelper.deleteContact(contactID)>0){
                        Toast.makeText(getActivity(), "Contact deleted", Toast.LENGTH_SHORT).show();
                        //clear the arguments ont he current bundle since contact is deleted
                        this.getArguments().clear();
                        //remove previous fragment from the backstack (navigate back)
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                    else {
                        Toast.makeText(getActivity(), "Database error", Toast.LENGTH_SHORT).show();
                    }
                }
        }
        return super.onOptionsItemSelected(item);
    }



  @Override
  public void getImagePath(String imagePath) {
      Log.d(TAG, "getImagePath: got the image path: " + imagePath);
      if(!imagePath.equals("")){

           imagePath = imagePath.replace(":/","://");
           mSelectedImagePath = imagePath;

          UniversalImageLoader.setImage(imagePath,mContactImage,null,"");
          /*Bitmap bitmap;
          Uri imagePathUri = Uri.parse(mSelectedImagePath);
          try{
              RotateBitmap rotateBitmap = new RotateBitmap();
              bitmap = rotateBitmap.HandleSamplingAndRotationBitmap(getActivity(),imagePathUri);
              mContactImage.setImageBitmap(bitmap);
          }catch(IOException e){
              e.printStackTrace();
          }*/

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

    }
}
