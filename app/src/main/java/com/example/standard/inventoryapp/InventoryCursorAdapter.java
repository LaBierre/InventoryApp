package com.example.standard.inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
    public void bindView(View view, final Context context, Cursor cursor) {

        // Find fields to populate in inflated template
        TextView name = (TextView) view.findViewById(R.id.product_name_text);
        TextView price = (TextView) view.findViewById(R.id.price_text);
        final TextView remainder = (TextView) view.findViewById(R.id.remainder_text);

        ImageView image = (ImageView) view.findViewById(R.id.image_list);

        Button sale = (Button) view.findViewById(R.id.sale_btn_list);

        //Find the columns of pet attributes
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
        int remainderColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_REMAINDER);
        int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_IMAGE);

        // Extract properties from cursor
        final String productName = cursor.getString(nameColumnIndex);
        final float productPrice = cursor.getFloat(priceColumnIndex);
        int productRemainder = cursor.getInt(remainderColumnIndex);
        String productImage = cursor.getString(imageColumnIndex);

//        if (TextUtils.isEmpty(petBreed)){
//            petBreed = "Unknown Breed!";
//        }

        // Populate fields with extracted properties
        name.setText(productName);
        price.setText(String.valueOf(productPrice));
        remainder.setText(String.valueOf(productRemainder));
        image.setImageBitmap(BitmapFactory.decodeFile(productImage));

        // That happens by clicking sale button in ListView
        sale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int rest = Integer.parseInt(remainder.getText().toString());
                rest --;
                remainder.setText(String.valueOf(rest));

                ContentValues values = new ContentValues();
                values.put(InventoryEntry.COLUMN_PRODUCT_NAME, productName);
                values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, productPrice);
                values.put(InventoryEntry.COLUMN_PRODUCT_REMAINDER, rest);
                //int rowUpdated = context.getContentResolver()
            }
        });
    }
}
