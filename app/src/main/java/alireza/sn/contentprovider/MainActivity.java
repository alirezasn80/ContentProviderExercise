package alireza.sn.contentprovider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    TextView textView;
    EditText textViewChange;
    Button button;

    boolean isCreated = false;

    String[] projection = new String[]{
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
    };

    String selection = null;
    String[] selectionArgs = null;
    String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init views ==============================================================================
        textViewChange = findViewById(R.id.textview_change);
        textView = findViewById(R.id.text_view);
        button = findViewById(R.id.button);
        //==========================================================================================

        //get permission============================================================================
        ActivityCompat.requestPermissions(
                this, new String[]{Manifest.permission.READ_CONTACTS
                        , Manifest.permission.WRITE_CONTACTS},
                PackageManager.PERMISSION_GRANTED);
        //==========================================================================================

        // set on click listener====================================================================
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isCreated) {
                    getLoaderManager().restartLoader(1, null, MainActivity.this);
                } else {
                    getLoaderManager().initLoader(1, null, MainActivity.this);
                    isCreated = true;
                }

            }
        });
        //==========================================================================================

//        ContentResolver contentResolver = getContentResolver();
//
//        Cursor cursor = contentResolver.query(
//                ContactsContract.Contacts.CONTENT_URI,
//                projection,
//                selection,
//                selectionArgs,
//                sortOrder
//        );

//        if (cursor!=null && cursor.getCount()>0){
//            StringBuilder sBuilder = new StringBuilder();
//
//            while (cursor.moveToNext()){
//                sBuilder.append(cursor.getString(0) +" "+ cursor.getString(1)+ " " + "\n");
//            }
//            textView.setText(sBuilder.toString());
//        } else
//            textView.setText("no contacts in device");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == 1) {
            return new CursorLoader(this, ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection, null, null, sortOrder);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data != null && data.getCount() > 0) {
            StringBuilder sBuilder = new StringBuilder();

            while (data.moveToNext()) {
                sBuilder.append(data.getString(0) + " " + data.getString(1) + " " + data.getString(2) + "\n");
            }
            textView.setText(sBuilder.toString());
        } else
            textView.setText("no contacts in device");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void addContact(View view) {
        ArrayList<ContentProviderOperation> operation = new ArrayList<>();
        operation.add(ContentProviderOperation.newInsert
                (ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, "new").withValue(
                        ContactsContract.RawContacts.ACCOUNT_NAME, "bang"
                ).build());
        operation.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds
                        .StructuredName.CONTENT_ITEM_TYPE).
                        withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME
                                , textViewChange.getText().toString()).build());
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, operation);
        } catch (OperationApplicationException | RemoteException e) {
            e.printStackTrace();
        }

//        ContentValues values = new ContentValues();
//        values.put(ContactsContract.RawContacts.ACCOUNT_TYPE, "account_type");
//        values.put(ContactsContract.RawContacts.ACCOUNT_NAME, "account_name");
//      //  ContentValues values = new ContentValues();
//        //values.put(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY , textViewChange.getText().toString());
//        getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI ,
//                values);
    }

    public void updateContact(View view) {
        String split[] = textViewChange.getText().toString().split(",");
        String where = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
        selectionArgs = new String[]{split[0]};
        ContentValues values = new ContentValues();
        values.put(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY, split[1]);
        values.put(ContactsContract.Data.DATA1, split[2]);

        getContentResolver().update(ContactsContract.RawContacts.CONTENT_URI,
                values, where, selectionArgs);
    }

    public void deleteContact(View view) {
        String where = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " = '" + textViewChange.getText().toString() + "'";
        getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI, where,null);
    }
}