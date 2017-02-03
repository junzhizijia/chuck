package com.github.jgilfelt.chuck.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;

public class ChuckContentProvider extends ContentProvider {

    public static Uri TRANSACTION_URI;

    private static final int TRANSACTION = 0;
    private static final int TRANSACTIONS = 1;
    private static UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    private ChuckDbOpenHelper databaseHelper;

    @Override
    public void attachInfo(Context context, ProviderInfo info) {
        super.attachInfo(context, info);
        TRANSACTION_URI = Uri.parse("content://" + info.authority + "/transaction");
        matcher.addURI(info.authority, "transaction/#", TRANSACTION);
        matcher.addURI(info.authority, "transaction", TRANSACTIONS);
    }

    @Override
    public boolean onCreate() {
        databaseHelper = new ChuckDbOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        switch (matcher.match(uri)) {
            case TRANSACTIONS:
                return cupboard().withDatabase(db).query(HttpTransaction.class).
                        withProjection(projection).
                        withSelection(selection, selectionArgs).
                        orderBy(sortOrder).
                        getCursor();
            case TRANSACTION:
                return cupboard().withDatabase(db).query(HttpTransaction.class).
                        byId(ContentUris.parseId(uri)).
                        getCursor();
        }
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        switch (matcher.match(uri)) {
            case TRANSACTIONS:
                long id = db.insert(cupboard().getTable(HttpTransaction.class), null, contentValues);
                if (id > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return ContentUris.withAppendedId(TRANSACTION_URI, id);
                }
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int result = 0;
        switch (matcher.match(uri)) {
            case TRANSACTIONS:
                result = db.delete(cupboard().getTable(HttpTransaction.class), selection, selectionArgs);
                break;
            case TRANSACTION:
                result = db.delete(cupboard().getTable(HttpTransaction.class),
                        "_id = ?", new String[]{ uri.getPathSegments().get(1) });
                break;
        }
        if (result > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return result;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int result = 0;
        switch (matcher.match(uri)) {
            case TRANSACTIONS:
                result = db.update(cupboard().getTable(HttpTransaction.class), contentValues, selection, selectionArgs);
                break;
            case TRANSACTION:
                result = db.update(cupboard().getTable(HttpTransaction.class), contentValues,
                        "_id = ?", new String[]{ uri.getPathSegments().get(1) });
                break;
        }
        if (result > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return result;
    }
}
