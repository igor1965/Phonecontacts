package com.igor.phonecontacts;

import android.content.Context;
import android.database.Cursor;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.igor.phonecontacts.models.Contact;
import com.igor.phonecontacts.utils.ContactListAdapter;
import com.igor.phonecontacts.utils.DatabaseHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class ViewContactsFragment extends Fragment {

    private static final String TAG = "ViwContactsFragment";
    private String testImageURL = "pbs.twimg.com/profile_images/875443327835025408/ZvmtaSXW_400x400.jpg";



    public interface OnContactSelectedListener{
        public void OnContactSelected(Contact con);
    }
    OnContactSelectedListener mContactListener;

    public interface OnAddContactListener{
        public void onAddContact();
    }
    OnAddContactListener mOnAddContact;

    //variable and widgets
    private static final int STANDART_APPBAR = 0;
    private static final int SEARCH_APPBAR = 1;
    private int mAppBarState;
    private AppBarLayout viewContactBar,searchBar;
    private ContactListAdapter adapter;
    private ListView contactsList;
    private EditText mSearchContacts;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_viewcontacts,container,false);
        viewContactBar = (AppBarLayout)view.findViewById(R.id.viewContactsToolbar);
        searchBar = (AppBarLayout)view.findViewById(R.id.searchToolbar);
        contactsList = (ListView)view.findViewById(R.id.contactsList);
        mSearchContacts= (EditText)view.findViewById(R.id.etSearchContacts);
        Log.d(TAG, "onCreateView: started");

        setAppBarState(STANDART_APPBAR);
        setupContactList();

        FloatingActionButton fab = (FloatingActionButton)view.findViewById(R.id.fabAddContact);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked fab");
                mOnAddContact.onAddContact();
            }
        });
        ImageView ivSearchContact = (ImageView)view.findViewById(R.id.ivSearchIcon);
        ivSearchContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked search icon");
                toggleToolbarState();
            }


        });
        ImageView ivBackArrow = (ImageView)view.findViewById(R.id.ivBackArrow);
        ivBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked back arrow");
                toggleToolbarState();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            mContactListener = (OnContactSelectedListener)getActivity();
            mOnAddContact = (OnAddContactListener)getActivity();
        }catch(ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException" + e.getMessage() );
        }
    }

    private void setupContactList(){
        final ArrayList<Contact> contacts = new ArrayList<>();
       /* contacts.add(new Contact("Nataliya","514-457-3327","mobile","igor.sreznikov@hotmail.com",testImageURL));
        contacts.add(new Contact("Igor","514-457-3327","mobile","igor.sreznikov@hotmail.com",testImageURL));
        contacts.add(new Contact("Igor","514-457-3327","mobile","igor.sreznikov@hotmail.com",testImageURL));
        contacts.add(new Contact("Igor","514-457-3327","mobile","igor.sreznikov@hotmail.com",testImageURL));*/

       DatabaseHelper databaseHelper = new DatabaseHelper(getActivity());
       Cursor cursor = databaseHelper.getAllContacts();

       //iterate though all rows contained in database
        while (cursor.moveToNext()){
            contacts.add(new Contact(
                    cursor.getString(1),//name
                    cursor.getString(2),//phone number
                    cursor.getString(3),//device
                    cursor.getString(4),//email
                    cursor.getString(5)//profile image uri
            ));
        }

        //sort the arrayList based on the contact name
        Collections.sort(contacts, new Comparator<Contact>() {
            @Override
            public int compare(Contact contact, Contact t1) {
                return contact.getName().compareToIgnoreCase(t1.getName());
            }
        });
        adapter = new ContactListAdapter(getActivity(),R.layout.layout_contactslistitem,contacts,"");

        mSearchContacts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = mSearchContacts.getText().toString().toLowerCase(Locale.getDefault());
                adapter.filter(text);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        contactsList.setAdapter(adapter);

        contactsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Log.d(TAG, "onClick: navigate to " + getString(R.string.contact_fragment));

                    //pass the contact to the interface and send it to MainActivity
                mContactListener.OnContactSelected(contacts.get(i));
            }

        });

    }

    /**
     * Initiate appbar toggle
     */
    private void toggleToolbarState() {
        Log.d(TAG, "toggleToolbarState: toggiling AppBarState");
        if (mAppBarState == STANDART_APPBAR){
            setAppBarState(SEARCH_APPBAR);
        }else {
            setAppBarState(STANDART_APPBAR);
        }
    }

    /**
     * Sets the appbar state for either the search or standart mode
     * @param state
     */
    private void setAppBarState(int state){
        Log.d(TAG, "setAppBarState: changing app bar state to: " + state);
        mAppBarState = state;
        if (mAppBarState == STANDART_APPBAR){
            searchBar.setVisibility(View.GONE);
            viewContactBar.setVisibility(View.VISIBLE);
            //hide the keyboard
            View view = getView();
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            try {
                imm.hideSoftInputFromWindow(view.getWindowToken(),0);
            }catch (NullPointerException e ){
                Log.d(TAG, "setAppBarState: NullPointerException" + e.getMessage());
            }
        }
        else if (mAppBarState == SEARCH_APPBAR){
            viewContactBar.setVisibility(View.GONE);
            searchBar.setVisibility(View.VISIBLE);

            //open the keyboard
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setAppBarState(STANDART_APPBAR);
    }
}
