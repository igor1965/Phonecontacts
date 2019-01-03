package com.igor.phonecontacts;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
//import android.widget.Toolbar;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.igor.phonecontacts.models.Contact;
import com.igor.phonecontacts.utils.ContactPropertyListAdapter;
import com.igor.phonecontacts.utils.DatabaseHelper;
import com.igor.phonecontacts.utils.RotateBitmap;
import com.igor.phonecontacts.utils.UniversalImageLoader;

import java.io.IOException;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactFragment extends Fragment {

    private static final String TAG = "ContactFragment";

    public interface OnEditContactListener{
        public void onEditContactSelected(Contact contact);
    }

    OnEditContactListener mOnEditContactListener;

    public ContactFragment(){
        super();
        setArguments(new Bundle());
    }
    private Toolbar toolbar;
    private Contact mContact;
    private TextView mContactName;
    private CircleImageView mContactImage;
    private ListView mListView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact,container,false);
        toolbar = (Toolbar)view.findViewById(R.id.contactToolbar);
        mContactName = (TextView)view.findViewById(R.id.contactName);
        mContactImage = (CircleImageView)view.findViewById(R.id.contactImage);
        mListView = (ListView)view.findViewById(R.id.lvContactProperties);
        Log.d(TAG, "onCreateView: started");
        mContact = getContactFromBundle();



        //required to set up the toolbar
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        init();

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
        //navigate to the edit contact fragment to edit the contact selected
            ImageView ivEdit = (ImageView)view.findViewById(R.id.ivEdit);
        ivEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: clicked file edit listener");
               /* EditContactFragment fragment = new EditContactFragment();
                FragmentTransaction transaction =getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container,fragment);
                transaction.addToBackStack(getString(R.string.edit_contactfragment));
                transaction.commit();*/
                    mOnEditContactListener.onEditContactSelected(mContact);
                }
        });
        return view;
    }
    private void init(){
        mContactName.setText(mContact.getName());
        UniversalImageLoader.setImage(mContact.getProfileImage(),mContactImage,null,"");
        /*if (mContact.getProfileImage()==null){
            mContactImage.setImageResource(R.drawable.ic_android);

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

        ArrayList<String> properties = new ArrayList<>();
        properties.add(mContact.getPhonenumber());
        properties.add(mContact.getEmail());
        ContactPropertyListAdapter adapter = new ContactPropertyListAdapter(getActivity(),R.layout.layout_cardview,properties);
        mListView.setAdapter(adapter);
        mListView.setDivider(null);
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
    public void onResume() {
        super.onResume();
        DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
        Cursor cursor = databaseHelper.getContactID(mContact);
        int contactID = -1;
        while (cursor.moveToNext()){
            contactID = cursor.getInt(0);
        }
        if (contactID >-1){//If the contact doesn't still exists then navigate back by popping the stack
            init();

        }else{
            this.getArguments().clear();
            getActivity().getSupportFragmentManager().popBackStack();
        }
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
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mOnEditContactListener = (OnEditContactListener)getActivity();
        }catch (ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException" + e.getMessage() );
        }
    }
}
