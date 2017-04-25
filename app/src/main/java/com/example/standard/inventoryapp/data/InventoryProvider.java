package com.example.standard.inventoryapp.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.example.standard.inventoryapp.R;
import com.example.standard.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by vince on 12.04.2017.
 */

public class InventoryProvider extends ContentProvider {

    private InventoryDbHelper mDbHelper;
    /** Tag for the log messages */
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the product table */
    private static final int PRODUCTS = 100;

    /** URI matcher code for the content URI for a single product in the product table */
    private static final int PRODUCT_ID = 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        /*
        * Sets the integer value for multiple rows in table "products"
        * */
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCT, PRODUCTS);
        /*
        * Sets a single row in table "products"
        */
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCT + "/#", PRODUCT_ID);
    }

    /**
     * Initialize the provider and the database helper object.
     */

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);

        switch (match) {
            case PRODUCTS:
                // For the product code, query the product table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the products table.

                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);

                break;
            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.standard.inventoryapp/products/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.

                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the products table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.exception_one) + uri);
        }

        // Set notification URI on the Cursor
        // so we know what content URI the Cursor was created for
        // If the data at this URI changes, then we know we need to update the Cursor
        cursor.setNotificationUri(getContext().getApplicationContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException(getContext().getString(R.string.exception_two) + uri
                        + getContext().getString(R.string.exception_three) + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, values);
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.exception_four) + uri);
        }
    }

    /**
     * Insert a product into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertProduct (Uri uri, ContentValues values){

        boolean getAll = checkInputs(values);
        if (getAll){
            // Get writeable database
            SQLiteDatabase database = mDbHelper.getWritableDatabase();

            // Insert the new product with the given values
            long id = database.insert(InventoryEntry.TABLE_NAME, null, values);
            // If the ID is -1, then the insertion failed. Log an error and return null.
            if (id == -1) {
                Log.e(LOG_TAG, getContext().getString(R.string.exception_nine) + uri);
                return null;
            }

            // Notify all listeners that the data has changed for the product content URI
            getContext().getContentResolver().notifyChange(uri, null);

            // Return the new URI with the ID (of the newly inserted row) appended at the end
            return ContentUris.withAppendedId(uri, id);
        }

        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs)  {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                // For  case PRODUCTS:
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);

                return rowsDeleted;
            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // For case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);

                return rowsDeleted;
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.exception_ten) + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, values, selection, selectionArgs);
            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryEntry._ID + getContext().getString(R.string.equal_symbol_provider);
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException(getContext().getString(R.string.exception_eleven) + uri);
        }
    }

    private int updateProduct (Uri uri, ContentValues values, String selection, String[] selectionArgs){

        boolean getAll = checkInputs(values);
        if (getAll){

            // Otherwise, get writeable database to update the data
            SQLiteDatabase database = mDbHelper.getWritableDatabase();

            // Perform the update on the database and get the number of rows affected
            int rowsUpdated = database.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
            if (rowsUpdated != 0){
                getContext().getContentResolver().notifyChange(uri, null);
            }

            // Returns the number of database rows affected by the update statement
            return rowsUpdated;

        }

        return 0;
    }

    public boolean checkInputs (ContentValues values){

        String productName = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
        if (TextUtils.isEmpty(productName)){
            Toast.makeText(getContext(), getContext().getString(R.string.provider_toast_name),Toast.LENGTH_SHORT).show();
            return false;
        }
        Float productPrice = values.getAsFloat(InventoryEntry.COLUMN_PRODUCT_PRICE);
        if (productPrice == 0.0){
            Toast.makeText(getContext(), getContext().getString(R.string.provider_insert_message), Toast.LENGTH_SHORT).show();
            return false;
        }
        Integer remainder = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_REMAINDER);
        if (remainder == 0){
            Toast.makeText(getContext(), getContext().getString(R.string.provider_remainder_message),Toast.LENGTH_SHORT).show();
            return false;
        }
        String supplierName = values.getAsString(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
        if (TextUtils.isEmpty(supplierName)){
            Toast.makeText(getContext(), getContext().getString(R.string.provier_supplier_name_message),Toast.LENGTH_SHORT).show();
            return false;
        }
        Integer supplierPhone = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);
        if (supplierPhone == 0){
            Toast.makeText(getContext(), getContext().getString(R.string.provider_supplier_phone_message),Toast.LENGTH_SHORT).show();
            return false;
        }
        String productImage = values.getAsString(InventoryEntry.COLUMN_PRODUCT_IMAGE);
        if (TextUtils.isEmpty(productImage)){
            Toast.makeText(getContext(), getContext().getString(R.string.provider_image_message),Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
