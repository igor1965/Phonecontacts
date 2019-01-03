package com.igor.phonecontacts.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.igor.phonecontacts.MainActivity;
import com.igor.phonecontacts.R;
import com.igor.phonecontacts.models.Contact;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactListAdapter extends ArrayAdapter<Contact>{

    private LayoutInflater mInflater;
    private List<Contact> mContacts = null;
    private ArrayList<Contact> arrayList;//used for search bar
    private int layoutResource;
    private Context mContext;
    private String mAppend;
    public ContactListAdapter(@NonNull Context context, int resource, @NonNull List<Contact> contacts, String append) {
        super(context, resource, contacts);
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutResource = resource;
        this.mContext = context;
        mAppend = append;
        this.mContacts = contacts;
        arrayList = new ArrayList<>();
        this.arrayList.addAll(mContacts);
    }
    private static class ViewHolder{
        TextView name;
        CircleImageView contactImage;
        ProgressBar mProgressBar;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
       // return super.getView(position, convertView, parent);

         /*View holder Build Patter Start*/
        final ViewHolder holder;
        if (convertView == null){
            convertView = mInflater.inflate(layoutResource,parent,false);
            holder = new ViewHolder();
            //Stuff to change --------------------------------------------------------------------
            holder.name = (TextView)convertView.findViewById(R.id.contactName);
            holder.contactImage = (CircleImageView)convertView.findViewById(R.id.contactImage);
            //------------------------------------------------------------------------------------
           // holder.mProgressBar = (ProgressBar)convertView.findViewById(R.id.contactProgressBar);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder)convertView.getTag();
              }
        //Stuff to change------------------------------------------------------------------------
        String name = getItem(position).getName();
        String imagePath = getItem(position).getProfileImage();
        holder.name.setText(name);


    //    if (imagePath == null) {
      //      holder.contactImage.setImageResource(R.drawable.ic_android);
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.displayImage(mAppend + imagePath, holder.contactImage, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    // holder.mProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    // holder.mProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    // holder.mProgressBar.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    // holder.mProgressBar.setVisibility(View.GONE);
                }
            });
        /*}else{
            Bitmap bitmap;
            Uri imagePathUri = Uri.parse(imagePath);
            try{
                RotateBitmap rotateBitmap = new RotateBitmap();
                bitmap = rotateBitmap.HandleSamplingAndRotationBitmap(getContext(),imagePathUri);
                holder.contactImage.setImageBitmap(bitmap);
            }catch(IOException e){
                e.printStackTrace();
            }

        }*/
        return convertView;
    }
    //Filter class
    public void filter(String characterText){
        characterText = characterText.toLowerCase(Locale.getDefault());
        mContacts.clear();
        if (characterText.length() == 0){
            mContacts.addAll(arrayList);
        }
        else {
            mContacts.clear();
            for (Contact contact: arrayList){
                if (contact.getName().toLowerCase(Locale.getDefault()).contains(characterText)){
                    mContacts.add(contact);
                }
            }
        }
        notifyDataSetChanged();
    }
}
