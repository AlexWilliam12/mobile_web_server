package com.example.minimalistserver.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.minimalistserver.models.Produto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PersistenceUnit extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "maria";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "produtos";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "nome";
    private static final String COLUMN_PRICE = "preco";

    public PersistenceUnit(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLE_NAME + " (" + COLUMN_ID + " INTEGER PRIMARY KEY, " + COLUMN_NAME + " TEXT NOT NULL, " + COLUMN_PRICE + " REAL NOT NULL)";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Ação de atualização não implementada ainda
    }

    public List<Produto> list() {
        List<Produto> produtos = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
            String nome = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
            double preco = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE));
            produtos.add(new Produto(id, nome, BigDecimal.valueOf(preco)));
        }

        cursor.close();
        db.close();
        return produtos;
    }

    public Produto select(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null, "1");

        Produto produto = null;
        if (cursor.moveToNext()) {
            String nome = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
            double preco = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE));
            produto = new Produto(id, nome, BigDecimal.valueOf(preco));
        }

        cursor.close();
        db.close();
        return produto;
    }

    public long insert(Produto produto) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ID, produto.getId());
        contentValues.put(COLUMN_NAME, produto.getNome());
        contentValues.put(COLUMN_PRICE, produto.getPreco().doubleValue());
        long newRowId = db.insert(TABLE_NAME, null, contentValues);
        db.close();
        return newRowId;
    }

    public int update(Produto produto) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, produto.getNome());
        contentValues.put(COLUMN_PRICE, produto.getPreco().doubleValue());
        int rowsAffected = db.update(TABLE_NAME, contentValues, COLUMN_ID + " = ?", new String[]{String.valueOf(produto.getId())});
        db.close();
        return rowsAffected;
    }

    public int delete(int id) {
        SQLiteDatabase db = getWritableDatabase();
        int rowsAffected = db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
        return rowsAffected;
    }
}

