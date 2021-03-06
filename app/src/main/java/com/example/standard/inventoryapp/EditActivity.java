package com.example.standard.inventoryapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.standard.inventoryapp.data.InventoryContract.InventoryEntry;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class EditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 0;

    // Edit Text fields in detail layout
    @BindView(R.id.product_name_detail)
    protected EditText mProductName;
    @BindView(R.id.product_price_detail)
    protected EditText mProductPrice;
    @BindView(R.id.product_remainder_detail)
    protected EditText mRemainder;
    @BindView(R.id.product_sale_detail)
    protected EditText mSale;
    @BindView(R.id.product_shipment_detail)
    protected EditText mShipment;
    @BindView(R.id.supplier_name_detail)
    protected EditText mSupplierName;
    @BindView(R.id.supplier_phone_detail)
    protected EditText mSupplierPhone;

    @BindView(R.id.placeholder_image)
    protected TextView placeholderImage;

    @BindView(R.id.image_product)
    protected ImageView mProductImage;

    @BindView(R.id.delete_button)
    protected Button mDeleteButton;
    @BindView(R.id.add_product_button)
    protected Button mAddProductButton;

    private String imagePath;

    private String imgTextForDb;

    /**
     * Content URI for the existing product (null if it's a new product)
     */
    private Uri mCurrentProductUri;

    /**
     * Boolean flag that keeps track of whether the product has been edited (true) or not (false)
     */
    private boolean mProductHasChanged = false;

    // Image loading result to pass to startActivityForResult method.
    private static int LOAD_IMAGE_RESULTS = 1;
    private static int TAKE_IMAGE_RESULTS = 2;

    private static final String IMAGE_BITMAP = "bitmap";
    private static final String IMAGE_PATH = "path";
    private static final String IMAGE_URI = "uri";

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mProcuctHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    Uri mUri;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_activity);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();
        // Selection if from InventoryActivity an Item is clicked or the Add Button
        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.add_title_edit_activity));
            mDeleteButton.setVisibility(View.GONE);
            mAddProductButton.setVisibility(View.GONE);
            placeholderImage.setVisibility(View.VISIBLE);
            mProductImage.setVisibility(View.GONE);
            invalidateOptionsMenu();
        } else {
            getSupportLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
            setTitle(getString(R.string.edit_title_edit_activity));
            placeholderImage.setVisibility(View.GONE);
            mProductImage.setVisibility(View.VISIBLE);
        }

        mProductName.setOnTouchListener(mTouchListener);
        mProductPrice.setOnTouchListener(mTouchListener);
        mRemainder.setOnTouchListener(mTouchListener);
        mSupplierName.setOnTouchListener(mTouchListener);
        mSupplierPhone.setOnTouchListener(mTouchListener);

        mSale.setOnTouchListener(mTouchListener);
        mShipment.setOnTouchListener(mTouchListener);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        try {
            BitmapDrawable drawable = (BitmapDrawable) mProductImage.getDrawable();
            bitmap = drawable.getBitmap();
            savedInstanceState.putParcelable(IMAGE_BITMAP, bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        savedInstanceState.putString(IMAGE_PATH, imagePath);
        savedInstanceState.putParcelable(IMAGE_URI, mUri);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {

        try {
            bitmap = savedInstanceState.getParcelable(IMAGE_BITMAP);
            mProductImage.setVisibility(View.VISIBLE);
            placeholderImage.setVisibility(View.GONE);
            mProductImage.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        imagePath = savedInstanceState.getString(IMAGE_PATH);
        mUri = savedInstanceState.getParcelable(IMAGE_URI);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // If the product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged && mUri != null) {
                    NavUtils.navigateUpFromSameTask(EditActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                dialogInterface();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged && mUri == null) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        dialogInterface();
    }

    /*
    *  Creating Alert in the case that not all date are entered and the user click save or back-----
    */
    private void dialogInterface() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_title_back_pressed))
                .setMessage(getString(R.string.dialog_message_back_pressed))
                .setPositiveButton(getString(R.string.dialog_positive_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        NavUtils.navigateUpFromSameTask(EditActivity.this);
                    }
                })
                .setNegativeButton(getString(R.string.dialog_negative_button_edit), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    //----------------------------------------------------------------------------------------------

    /*
    *  Clicking this Button the User can choose an Image from the Galerie for the ImageView in the
    *  Detail Screen
    */
    @OnClick(R.id.add_image_button)
    void addImageButton() {
        // Create the Intent for Image Gallery.
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // Start new activity with the LOAD_IMAGE_RESULTS to handle back the results when image is picked from the Image Gallery.
        startActivityForResult(intent, LOAD_IMAGE_RESULTS);
        placeholderImage.setVisibility(View.GONE);
        mProductImage.setVisibility(View.VISIBLE);
        //------------------------------------------------------------------------------------------
    }

    /*
    *  Clicking this Button the User can use the device camera for taking an Image of the product---
    *  for the ImageView in the Detail Layout
    */
    @OnClick(R.id.take_image_button)
    void takeImageButton() {
        // Create the Intent for Image Gallery.
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Start new activity with the LOAD_IMAGE_RESULTS to handle back the results when image is
        // picked from the Image Gallery.
        startActivityForResult(i, TAKE_IMAGE_RESULTS);
        placeholderImage.setVisibility(View.GONE);
        mProductImage.setVisibility(View.VISIBLE);
    }
    //----------------------------------------------------------------------------------------------

    /*
    *  Clicking this Button allows the User to track a sale in the Detail Screen--------------------
    */
    @OnClick(R.id.minus_sale_btn)
    void saleButton() {
        int sale = 0;
        int remainder;
        // Check if remainder == null, because remainder-textfield is empty
        if (TextUtils.isEmpty(mRemainder.getText().toString().trim())) {
            remainder = 0;
        } else {
            remainder = Integer.parseInt(mRemainder.getText().toString().trim());
        }
        // Substract the Sale-Text-Field-Content from remainder and check if the sale amount is
        // bigger than the remainder
        if (!TextUtils.isEmpty(mSale.getText().toString().trim())) {
            sale = Integer.parseInt(mSale.getText().toString().trim());
            if (remainder < sale) {
                Toast.makeText(EditActivity.this, getString(R.string.sale_to_big_toast), Toast.LENGTH_LONG).show();
            } else if (remainder-sale <= 50) {
                Toast.makeText(EditActivity.this, getString(R.string.new_order_toast), Toast.LENGTH_LONG).show();
                remainder -= sale;
            } else {
                remainder -= sale;
            }
        }

        mRemainder.setText(String.valueOf(remainder));
        mSale.setText("");
    }
    //----------------------------------------------------------------------------------------------

    /*
    *  Clicking this Button allows the User to track an order in the Detail Screen------------------
    */
    @OnClick(R.id.add_supply_btn)
    void supplyButton() {

        int shipment = 0;
        int remainder;
        // Check if remainder == null, because remainder-textfield is empty
        if (TextUtils.isEmpty(mRemainder.getText().toString().trim())) {
            remainder = 0;
        } else {
            remainder = Integer.parseInt(mRemainder.getText().toString().trim());
        }
        // Add the Shipment-Text-Field-Content to remainder
        if (!TextUtils.isEmpty(mShipment.getText().toString().trim())) {
            shipment = Integer.parseInt(mShipment.getText().toString().trim());
            remainder += shipment;
        }
        mRemainder.setText(String.valueOf(remainder));

        mShipment.setText("");
    }

    /*
    *  Clicking this Button the entered informations inside the Detail Screen gets stored in--------
    *  database and displayed in the List Screen
    */
    @OnClick(R.id.save_button)
    void saveButton() {
        saveData();
    }
    //----------------------------------------------------------------------------------------------

    /*
   *  Inserts values of a new Product in the database or updates changes from existing Products----
   */
    private void saveData() {
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();

        // Content of Textfiels saved in local Variables--------------------------------------------
        String productName = mProductName.getText().toString().trim();
        String priceString = mProductPrice.getText().toString().trim();
        float productPrice = 0;
        if (!TextUtils.isEmpty(priceString)) {
            productPrice = Float.parseFloat(priceString);
        }
        String remainderString = mRemainder.getText().toString().trim();
        int remainder = 0;
        if (!TextUtils.isEmpty(remainderString)) {
            remainder = Integer.parseInt(remainderString);
        }
        String supplierName = mSupplierName.getText().toString().trim();
        String phoneString = mSupplierPhone.getText().toString().trim();
        int supplierPhone = 0;
        if (!TextUtils.isEmpty(phoneString)) {
            supplierPhone = Integer.parseInt(phoneString);
        }
        //------------------------------------------------------------------------------------------
        /*
        *  Transform the Drawable from the Image View in a Base64 Text Format and the define--------
        *  the Produkt Image Value
        */
        imgTextForDb = "";

        if (mUri == null) {
            imgTextForDb = imagePath;
        } else {
            imgTextForDb = String.valueOf(mUri);
        }
        //------------------------------------------------------------------------------------------

        values.put(InventoryEntry.COLUMN_PRODUCT_IMAGE, imgTextForDb);
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productName);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, productPrice);
        values.put(InventoryEntry.COLUMN_PRODUCT_REMAINDER, remainder);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplierName);
        values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE, supplierPhone);

        // Insert new Product-----------------------------------------------------------------------
        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.insert_failed_edit_activity),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.insert_succed_edit_activity),
                        Toast.LENGTH_SHORT).show();
                NavUtils.navigateUpFromSameTask(EditActivity.this);
            }
        }
        //------------------------------------------------------------------------------------------

        // Update existing Product------------------------------------------------------------------
        else {
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
            if (rowsAffected == 0) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.update_failed_edit_activity),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.update_succed_edit_activity),
                        Toast.LENGTH_SHORT).show();

                NavUtils.navigateUpFromSameTask(EditActivity.this);
            }
        }
        //------------------------------------------------------------------------------------------
    }
    //----------------------------------------------------------------------------------------------

    /*
    *  Clicking this Button within Detailsreen one Product is deleted from Database-----------------
    */
    @OnClick(R.id.delete_button)
    void deleteButton() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_title_delete))
                .setMessage(getString(R.string.dialog_message_delete))
                .setPositiveButton(getString(R.string.dialog_positive_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteData();
                        NavUtils.navigateUpFromSameTask(EditActivity.this);
                    }
                })
                .setNegativeButton(getString(R.string.dialog_negative_button_edit), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }
    //----------------------------------------------------------------------------------------------

    /*
    *  Clicking this button within the Edit-Detailscreen an Add-Product-Detailscreen opens and the--
    *  User can enter a new Product. An Alert-Dialog ask the User to update the Changes made
    *  inside the Edit-Screen
    */
    @OnClick(R.id.add_product_button)
    void addNewProductButton() {
        mCurrentProductUri = null;

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_title_save))
                .setMessage(getString(R.string.dialog_message_save))
                .setPositiveButton(getString(R.string.dialog_positive_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        saveData();
                        Intent intent = new Intent(EditActivity.this, EditActivity.class);
                        intent.setData(mCurrentProductUri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(getString(R.string.dialog_negative_button_edit), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null) {
                            dialog.dismiss();
                            Intent intent = new Intent(EditActivity.this, EditActivity.class);
                            intent.setData(mCurrentProductUri);
                            startActivity(intent);
                        }
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    //----------------------------------------------------------------------------------------------

    /*
    *  Clicking the Order Button the phone app with inserted number gets started--------------------
    */
    @OnClick(R.id.order_btn)
    void orderButton() {
        String number = mSupplierPhone.getText().toString().trim();
        String uri = getString(R.string.phone_appendix_edit_activity) + number;
        Intent order = new Intent(Intent.ACTION_DIAL);
        order.setData(Uri.parse(uri));
        startActivity(order);
    }
    //----------------------------------------------------------------------------------------------

    /*
    * The next two Methods handles the getting of the Image inside the Detail Screen by-------------
    * clicking the Add-Image- or the Take-Image-Button
    */
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
            String[] filePath = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
            }
            imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

            // Now we need to set the GUI ImageView data with data read from the picked file.
            mProductImage.setImageBitmap(BitmapFactory.decodeFile(imagePath));

            // At the end remember to close the cursor or you will end with the RuntimeException!
            cursor.close();
        }
        if (requestCode == TAKE_IMAGE_RESULTS && resultCode == RESULT_OK && data != null) {
            // CALL THIS METHOD TO GET THE ACTUAL PATH
            imagePath = getOriginalImagePath();
            // Let's read picked image data - its URI
            Uri pickedImage = data.getData();
            mUri = pickedImage;
            Bitmap photo = (Bitmap) data.getExtras().get(getString(R.string.take_image));

            //mProductImage.setImageBitmap(BitmapFactory.decodeFile(imagePath));
            mProductImage.setImageBitmap(photo);
        }
    }

    public String getOriginalImagePath() {
        String[] projection = {MediaStore.Images.Media.DATA};

        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null
        );

        int column_index_data = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToLast();

        return cursor.getString(column_index_data);
    }
    //----------------------------------------------------------------------------------------------

    // Delete an existing Product inside Detail Screen from Database
    private void deleteData() {

        int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

        if (rowsDeleted == 0) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.delete_failed_edit_activity), Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.delete_succed_edit_activity), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all product attributes, define a projection that contains
        // all columns from the products table
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
        return new CursorLoader(this,   // Parent activity context
                mCurrentProductUri,     // Query the content URI for the current product
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of product attributes that we're interested in
            final int productNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            final int productPriceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
            final int remainderColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_REMAINDER);
            final int productImageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_IMAGE);
            final int supplierNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            final int supplierPhoneColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);

            // Extract out the value from the Cursor for the given column index
            String productName = cursor.getString(productNameColumnIndex);
            float productPrice = cursor.getFloat(productPriceColumnIndex);
            int remainder = cursor.getInt(remainderColumnIndex);
            String productImagePath = cursor.getString(productImageColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);

            imagePath = productImagePath;
            // Because in some cases only the Uri or only the Image is received, i have to handle both
            mProductImage.setImageBitmap(BitmapFactory.decodeFile(imagePath));
            mProductImage.setImageURI(Uri.parse(imagePath));

            // Update the views on the screen with the values from the database
            mProductName.setText(productName);
            mProductPrice.setText(String.valueOf(productPrice));
            mRemainder.setText(String.valueOf(remainder));
            mSupplierName.setText(supplierName);

            mSupplierPhone.setText(supplierPhone);
        }
        int position = cursor.getPosition();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProductName.setText("");
        mProductPrice.setText("");
        mRemainder.setText("");
        mSupplierName.setText("");
        mSupplierPhone.setText("");
        mProductImage.setVisibility(View.GONE);
    }
}
