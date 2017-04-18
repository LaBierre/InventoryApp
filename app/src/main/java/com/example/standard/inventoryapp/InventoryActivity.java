package com.example.standard.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.standard.inventoryapp.data.InventoryContract.InventoryEntry;

public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TITEL_LABEL = "title";

    private InventoryCursorAdapter mCursorAdapter;

    private static final int INVENTORY_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inventory_activity);

        // Find the ListView which will be populated with the pet data
        ListView inventoryList = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        TextView emptyView = (TextView) findViewById(R.id.empty_text);
        inventoryList.setEmptyView(emptyView);

        mCursorAdapter = new InventoryCursorAdapter(this, null);
        inventoryList.setAdapter(mCursorAdapter);

        inventoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                Uri currentPetUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

                Intent intent = new Intent(InventoryActivity.this, EditActivity.class);
                intent.setData(currentPetUri);

                startActivity(intent);
            }
        });
    }

    public void addNewButton (View v){
        Intent intent = new Intent(InventoryActivity.this, EditActivity.class);
        intent.putExtra(TITEL_LABEL, "Add Product");
        startActivity(intent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;
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
