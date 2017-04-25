package com.example.standard.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
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
        int supplierNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
        int supplierPhoneColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);

        // Extract properties from cursor
        final String productName = cursor.getString(nameColumnIndex);
        final float productPrice = cursor.getFloat(priceColumnIndex);
        int productRemainder = cursor.getInt(remainderColumnIndex);
        final String productImageText = cursor.getString(imageColumnIndex);
        final String supplierName = cursor.getString(supplierNameColumnIndex);
        final int supplierPhone = cursor.getInt(supplierPhoneColumnIndex);

        image.setImageBitmap(BitmapFactory.decodeFile(productImageText));
        image.setImageURI(Uri.parse(productImageText));

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
                rest--;
                // Warn User if remainder get smaller than 50. finish substracting if remainder == 0
                if (rest <= 50) {
                    Toast.makeText(context, context.getString(R.string.new_order_toast), Toast.LENGTH_LONG).show();
                    if (rest <= 0) {
                        rest = 0;
                    }
                }
                Uri currentItemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, idCurrent);
                remainder.setText(String.valueOf(rest));
                ContentValues values = new ContentValues();
                values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productName);
                values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, productPrice);
                values.put(InventoryEntry.COLUMN_PRODUCT_REMAINDER, rest);
                values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplierName);
                values.put(InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE, supplierPhone);
                values.put(InventoryEntry.COLUMN_PRODUCT_IMAGE, productImageText);
                context.getContentResolver().update(currentItemUri, values, null, null);
            }
        });
    }
}
