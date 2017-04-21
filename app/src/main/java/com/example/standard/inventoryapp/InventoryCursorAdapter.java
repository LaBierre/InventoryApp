package com.example.standard.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.standard.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by vince on 12.04.2017.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        // Find fields to populate in inflated template
        TextView name = (TextView) view.findViewById(R.id.product_name_text);
        TextView price = (TextView) view.findViewById(R.id.price_text);
        final TextView remainder = (TextView) view.findViewById(R.id.remainder_text);

        ImageView image = (ImageView) view.findViewById(R.id.image_list);

        Button sale = (Button) view.findViewById(R.id.sale_btn_list);

        //Find the columns of product attributes
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
        int remainderColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_REMAINDER);
        int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_IMAGE);

        // Todo: Rename all String productImage to productImagePath
        // Extract properties from cursor
        final String productName = cursor.getString(nameColumnIndex);
        final float productPrice = cursor.getFloat(priceColumnIndex);
        int productRemainder = cursor.getInt(remainderColumnIndex);
        String productImageText = cursor.getString(imageColumnIndex);

        Bitmap bitmap = null;

        try {
            byte [] encodeByte = Base64.decode(productImageText, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(encodeByte,0, encodeByte.length);
        } catch (Exception e){
            e.getMessage();
        }
        image.setImageBitmap(bitmap);

        //Log.d("Test", "CursorAdapter Imagepath: " + productImageText);

        // If the user clicked "Insert Dummy Data" in the Options Menu showed in the Inventory
        // Activity the cursor contains an emyty String "productImagePath" and the dummy image from
        // drawable folder will be loaded in the Image View "image". In other case the saved
        // productImagePath will be decoded and set as Bitmap in the Image View "image".
//        if (TextUtils.isEmpty(productImagePath)){
//            image.setImageResource(R.drawable.frohe_ostern);
//        } else {
//            image.setImageBitmap(BitmapFactory.decodeFile(productImagePath));
//        }

        // Populate fields with extracted properties
        name.setText(productName);
        price.setText(String.valueOf(productPrice));
        remainder.setText(String.valueOf(productRemainder));


        final long idCurrent = getItemId(cursor.getPosition());

        // That happens by clicking sale button in ListView
        sale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int rest = Integer.parseInt(remainder.getText().toString());
                rest --;
                // Warn User if remainder get smaller than 50. finish substracting if remainder == 0
                if (rest <= 50){
                    Toast.makeText(context, context.getString(R.string.new_order_toast), Toast.LENGTH_LONG).show();
                    if (rest <= 0){
                        rest = 0;
                    }
                }
                Uri currentItemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, idCurrent);
                remainder.setText(String.valueOf(rest));
                ContentValues values = new ContentValues();
                values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productName);
                values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, productPrice);
                values.put(InventoryEntry.COLUMN_PRODUCT_REMAINDER, rest);
                long id = context.getContentResolver().update(currentItemUri,values, null, null);
            }
        });
    }
}
