package com.example.standard.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by vince on 12.04.2017.
 */

public class InventoryContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.

    public InventoryContract() {
    }

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.standard.inventoryapp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.standard.inventoryapp/products/ is a valid path for
     * looking at product data. content://com.example.standard.inventoryapp/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_PRODUCT = "products";

    /**
     * Inner class that defines constant values for the products database table.
     * Each entry in the table represents a single product.
     */
    public static final class InventoryEntry implements BaseColumns {

        /** The content URI to access the product data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCT);

        /** Name of database table for products */
        public final static String TABLE_NAME = "products";
        /**
         * Unique ID number for the product (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;
        /**
         * Name of the product.
         *
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_NAME ="name";
        /**
         * Price of the product.
         *
         * Type: REAL
         */
        public final static String COLUMN_PRODUCT_PRICE ="price";
        /**
         * Remainder of the product.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_REMAINDER ="remainder";

        /**
         * Image of the product.
         *
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_IMAGE ="image";

        /**
         * Supplier name which belongs to the product.
         *
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_SUPPLIER_NAME = "supplierName";

        /**
         * Supplier name which belongs to the product.
         *
         * Type: Integer
         */
        public final static String COLUMN_PRODUCT_SUPPLIER_PHONE = "supplierPhone";

        /**
         * * The MIME type of the {@link #CONTENT_URI} for a list of products.
         * */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCT;
    }
}
