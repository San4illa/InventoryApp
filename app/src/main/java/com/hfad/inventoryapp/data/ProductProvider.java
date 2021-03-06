package com.hfad.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.hfad.inventoryapp.data.ProductContract.ProductEntry;

public class ProductProvider extends ContentProvider {

    private static final int PRODUCTS = 100;
    private static final int PRODUCT_ID = 101;
    private static final String LOG_TAG = ProductProvider.class.getSimpleName();

    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);
        uriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    private ProductDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        Cursor cursor;

        int match = uriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues contentValues) {
        String name = contentValues.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }

        String supplier = contentValues.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
        if (supplier == null) {
            throw new IllegalArgumentException("Product requires a supplier");
        }

        String supplierEmail = contentValues.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);
        if (supplierEmail == null) {
            throw new IllegalArgumentException("Product requires a supplier email");
        }

        Integer quantity = contentValues.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity != 0 && quantity < 0) {
            throw new IllegalArgumentException("Product requires valid quantity");
        }


        Integer price = contentValues.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
        if (price != 0 && price < 0) {
            throw new IllegalArgumentException("Product requires valid price");
        }

        String image = contentValues.getAsString(ProductEntry.COLUMN_PRODUCT_IMAGE);
        if (image == null) {
            throw new IllegalArgumentException("Product requires a image");
        }

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        long id = database.insert(ProductEntry.TABLE_NAME, null, contentValues);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = uriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalStateException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = contentValues.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_SUPPLIER)) {
            String supplier = contentValues.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
            if (supplier == null) {
                throw new IllegalArgumentException("Product requires a supplier");
            }
        }

        if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL)) {
            String supplierEmail = contentValues.getAsString(ProductEntry.COLUMN_PRODUCT_SUPPLIER_EMAIL);
            if (supplierEmail == null) {
                throw new IllegalArgumentException("Product requires a supplier email");
            }
        }

        if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer quantity = contentValues.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity != 0 && quantity < 0) {
                throw new IllegalArgumentException("Product requires valid quantity");
            }
        }

        if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_PRICE)) {
            Integer price = contentValues.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
            if (price != 0 && price < 0) {
                throw new IllegalArgumentException("Product requires valid price");
            }
        }

        if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_IMAGE)) {
            String image = contentValues.getAsString(ProductEntry.COLUMN_PRODUCT_IMAGE);
            if (image == null) {
                throw new IllegalArgumentException("Product requires a image");
            }
        }

        if (contentValues.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        int rowsUpdated = database.update(ProductEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
}
