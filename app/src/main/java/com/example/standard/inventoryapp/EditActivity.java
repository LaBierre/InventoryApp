package com.example.standard.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.standard.inventoryapp.data.InventoryContract.InventoryEntry;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class EditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 0;

    // Edit Text fields in detail layout
    @BindView(R.id.product_name_detail)
    EditText mProductName;

    @BindView(R.id.product_price_detail)
    EditText mProductPrice;

    @BindView(R.id.product_remainder_detail)
    EditText mRemainder;

    @BindView(R.id.product_sale_detail)
    EditText mSale;

    @BindView(R.id.product_shipment_detail)
    EditText mShipment;

    @BindView(R.id.supplier_name_detail)
    EditText mSupplierName;

    @BindView(R.id.supplier_phone_detail)
    protected EditText mSupplierPhone;


    @BindView(R.id.image_product)
    ImageView mProductImage;

    // Button for Add picture
    private Button mAddPicture;
    // Button for Take picture
    private Button mTakePicture;
    // Button for Submit Sale or Remainder
    private Button submit;
    // Button for Safe Entries
    private Button save;
    // Button for Order Products
    private Button order;
    // Button for Add new Products
    private Button add;

    private String imagePath;

    /** Content URI for the existing product (null if it's a new product) */
    private Uri mCurrentProductUri;

    /** Boolean flag that keeps track of whether the pet has been edited (true) or not (false) */
    private boolean mProductHasChanged = false;

    // Image loading result to pass to startActivityForResult method.
    private static int LOAD_IMAGE_RESULTS = 1;
    private static int TAKE_IMAGE_RESULTS = 2;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            Log.i("Test", "something touched");
            mProductHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_activity);

        ButterKnife.bind(this);

//        addPicture = (Button) findViewById(R.id.add_image_button);
//        takePicture = (Button) findViewById(R.id.take_image_button);
//        submit = (Button) findViewById(R.id.submit_button_detail);
//        save = (Button) findViewById(R.id.save_button_detail);
//        order = (Button) findViewById(R.id.order_button_detail);
//        add = (Button) findViewById(R.id.add_button_detail);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        if (mCurrentProductUri == null) {
            setTitle("Add new Product");
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
            setTitle("Edit Product");
        }

        mProductName.setOnTouchListener(mTouchListener);
        mProductPrice.setOnTouchListener(mTouchListener);
        mRemainder.setOnTouchListener(mTouchListener);
        mSupplierName.setOnTouchListener(mTouchListener);
        mSupplierPhone.setOnTouchListener(mTouchListener);

        mSale.setOnTouchListener(mTouchListener);
        mShipment.setOnTouchListener(mTouchListener);
    }

    @OnClick(R.id.add_image_button) void addImageButton(){
        // Create the Intent for Image Gallery.
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // Start new activity with the LOAD_IMAGE_RESULTS to handle back the results when image is picked from the Image Gallery.
        startActivityForResult(i, LOAD_IMAGE_RESULTS);
    }

    @OnClick(R.id.take_image_button) void takeImageButton(){
        // Create the Intent for Image Gallery.
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Start new activity with the LOAD_IMAGE_RESULTS to handle back the results when image is picked from the Image Gallery.
        startActivityForResult(i, TAKE_IMAGE_RESULTS);
    }

    @OnClick(R.id.save_button_detail) void saveButton(){
        saveData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Here we need to check if the activity that was triggers was the Image Gallery.
        // If it is the requestCode will match the LOAD_IMAGE_RESULTS value.
        // If the resultCode is RESULT_OK and there is some data we know that an image was picked.
        if (requestCode == LOAD_IMAGE_RESULTS && resultCode == RESULT_OK && data != null) {
            // Let's read picked image data - its URI
            Uri pickedImage = data.getData();
            // Let's read picked image path using content resolver
            String[] filePath = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
            }
            String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

            Log.d("Test", "Image Path: " + imagePath);

            //Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            // Now we need to set the GUI ImageView data with data read from the picked file.
            //image.setImageBitmap(bitmap);
            mProductImage.setImageBitmap(BitmapFactory.decodeFile(imagePath));

            // At the end remember to close the cursor or you will end with the RuntimeException!
            cursor.close();
        }
        if (requestCode == TAKE_IMAGE_RESULTS && resultCode == RESULT_OK && data != null) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            // CALL THIS METHOD TO GET THE ACTUAL PATH
            String imagePath = getOriginalImagePath();

            Log.d("Test", "Image Path: " + imagePath);

            mProductImage.setImageBitmap(BitmapFactory.decodeFile(imagePath));
        }
    }

    public String getOriginalImagePath() {
        String[] projection = { MediaStore.Images.Media.DATA };

        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null
        );

        int column_index_data = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToLast();

        return cursor.getString(column_index_data);
    }

    /*
    ** Method with insert and update function. The transfered
    *  String "imagePath" comes from clicking "Add Image" or "Take Image"
    *  Buttons

     */
    private void saveData (){
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        //Werte aus den Textfeldern in lokale Variablen zwischenspeichern und
        // dann in Content Values zur Speicherung in die Datenbank übertragen
        String productName = mProductName.getText().toString().trim();
        float productPrice = Float.parseFloat(mProductPrice.getText().toString().trim());
        int remainder = Integer.parseInt(mRemainder.getText().toString().trim());
        String supplierName = mSupplierName.getText().toString().trim();
        int supplierPhone = Integer.parseInt(mSupplierPhone.getText().toString().trim());
        //Todo: get String path of image
        String productImage = imagePath;

        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productName);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, productPrice);
        values.put(InventoryEntry.COLUMN_PRODUCT_REMAINDER, remainder);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplierName);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE, supplierPhone);
        values.put(InventoryEntry.COLUMN_PRODUCT_IMAGE, productImage);

        // Check if EditActivity is called from List Item or Add Button
        if (mCurrentProductUri == null){

            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, "Inserting Product failed", Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, "Inserting Product succeed", Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentProductUri,values, null, null);

            if (rowsAffected == 0) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, "Updating Product failed", Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, "Updating Product succeed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void deleteData (){

        int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

        if (rowsDeleted == 0) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, "Deleting Product failed", Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, "Deleting Product succeed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        // Since the editor shows all pet attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_REMAINDER,
                InventoryEntry.COLUMN_PRODUCT_IMAGE,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME,
                InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,     // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()){
            // Find the columns of pet attributes that we're interested in
            int productNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int productPriceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
            int remainderColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_REMAINDER);
            int productImageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_IMAGE);
            int supplierNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);

            // Extract out the value from the Cursor for the given column index
            String productName = cursor.getString(productNameColumnIndex);
            float productPrice = cursor.getFloat(productPriceColumnIndex);
            int remainder = cursor.getInt(remainderColumnIndex);
            String productImage = cursor.getString(productImageColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);

            // Update the views on the screen with the values from the database
            mProductName.setText(productName);
            mProductPrice.setText(String.valueOf(productPrice));
            mRemainder.setText(String.valueOf(remainder));
            mSupplierName.setText(supplierName);
            mProductImage.setImageBitmap(BitmapFactory.decodeFile(productImage));
            mSupplierPhone.setText(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
