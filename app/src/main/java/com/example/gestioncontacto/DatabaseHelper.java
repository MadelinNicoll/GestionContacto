package com.example.gestioncontacto;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "contactos.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_CONTACTOS = "contactos";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PAIS = "pais";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_TELEFONO = "telefono";
    public static final String COLUMN_NOTA = "nota";
    public static final String COLUMN_IMAGEN = "imagen";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_CONTACTOS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_PAIS + " TEXT, "
                + COLUMN_NOMBRE + " TEXT, "
                + COLUMN_TELEFONO + " TEXT, "
                + COLUMN_NOTA + " TEXT, "
                + COLUMN_IMAGEN + " BLOB)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTOS);
        onCreate(db);
    }

    public boolean insertarContacto(String pais, String nombre, String telefono, String nota, byte[] imagen) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PAIS, pais);
        values.put(COLUMN_NOMBRE, nombre);
        values.put(COLUMN_TELEFONO, telefono);
        values.put(COLUMN_NOTA, nota);
        values.put(COLUMN_IMAGEN, imagen);
        long result = db.insert(TABLE_CONTACTOS, null, values);
        return result != -1;
    }

    public Cursor obtenerContactos() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_CONTACTOS, null);
    }

    public boolean eliminarContacto(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_CONTACTOS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }
}