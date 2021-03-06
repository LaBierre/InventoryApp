package com.example.standard.inventoryapp;


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.example.standard.inventoryapp.data.InventoryContract.InventoryEntry;

import java.io.ByteArrayOutputStream;

public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private InventoryCursorAdapter mCursorAdapter;

    private static final int INVENTORY_LOADER = 0;

    Uri mCurrentProductUri, mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventory_activity);

        // Find the ListView which will be populated with the product data
        ListView inventoryList = (ListView) findViewById(R.id.list);

        mCursorAdapter = new InventoryCursorAdapter(this, null);
        inventoryList.setAdapter(mCursorAdapter);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        LinearLayout emptyView = (LinearLayout) findViewById(R.id.empty_layout);
        inventoryList.setEmptyView(emptyView);

        inventoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                mCurrentProductUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

                Intent intent = new Intent(InventoryActivity.this, EditActivity.class);
                intent.setData(mCurrentProductUri);
                startActivity(intent);
            }
        });
        getSupportLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_insert_dummy:
                insertData();
                return true;
            case R.id.action_delete_list:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.dialog_title_delete))
                        .setMessage(getString(R.string.dialog_message_delete_list))
                        .setPositiveButton(getString(R.string.dialog_positive_button), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                deleteData();
                            }
                        })
                        .setNegativeButton(getString(R.string.dialog_negative_button), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (dialog != null){
                                    dialog.dismiss();
                                }
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Insert Dummy Data in the Database------------------------------------------------------------
    private void insertData() {
        // Create a ContentValues object where column names are the keys,
        // and Schokohase's attributes are the values.
        ContentValues values = new ContentValues();

        // Declare the dummy image from the drawable folder as uri and use them as value for the database
        Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.frohe_ostern);
        mImageUri = Uri.parse(getString(R.string.image_uri) + R.drawable.frohe_ostern);
        String imgTextForDb = String.valueOf(mImageUri);

        values.put(InventoryEntry.COLUMN_PRODUCT_IMAGE, imgTextForDb);
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, getString(R.string.name_dummy));
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, 1.99);
        values.put(InventoryEntry.COLUMN_PRODUCT_REMAINDER, 200);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, getString(R.string.supplier_name_dummy));
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE, 1234567890);

        // Insert a new row for the dummy product into the provider using the ContentResolver.
        getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
    }
    //----------------------------------------------------------------------------------------------

    private void deleteData() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);

        if (rowsDeleted == 0) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.delete_failed_edit_activity), Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.delete_succed_edit_activity), Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(this, InventoryActivity.class);
        startActivity(intent);
    }
    // Clicking the Add New Button the detail layout is called and you can generate
    // a new Product
    public void addNewButton(View v) {
        Intent intent = new Intent(InventoryActivity.this, EditActivity.class);
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the product table
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_REMAINDER,
                InventoryEntry.COLUMN_PRODUCT_IMAGE,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,       // Parent activity context
                InventoryEntry.CONTENT_URI, // Query the content URI for the current product
                projection,                 // Columns to include in the resulting Cursor
                null,                       // No selection clause
                null,                       // No selection arguments
                null);                      // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@InventoryAdapter} with this new cursor containing updated product data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

}
