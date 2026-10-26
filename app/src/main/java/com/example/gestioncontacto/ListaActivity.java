package com.example.gestioncontacto;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class ListaActivity extends AppCompatActivity {

    private ListView listViewContactos;
    private DatabaseHelper dbHelper;
    private ArrayList<Contacto> listaContactos;
    private ArrayAdapter<Contacto> adapter;
    private int contactoSeleccionadoId = -1;
    private Contacto contactoSeleccionadoObj = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);

        dbHelper = new DatabaseHelper(this);
        listViewContactos = findViewById(R.id.listViewContactos);
        EditText editBuscar = findViewById(R.id.editBuscar);

        Button btnAtras = findViewById(R.id.btnAtras);
        Button btnCompartir = findViewById(R.id.btnCompartir);
        Button btnVerImagen = findViewById(R.id.btnVerImagen);
        Button btnEliminar = findViewById(R.id.btnEliminar);
        Button btnActualizar = findViewById(R.id.btnActualizar);

        cargarContactos();

        editBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ArrayList<Contacto> listaFiltrada = new ArrayList<>();
                for (Contacto c : listaContactos) {
                    if (c.getNombre().toLowerCase().contains(s.toString().toLowerCase())) {
                        listaFiltrada.add(c);
                    }
                }
                actualizarAdapter(listaFiltrada);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        listViewContactos.setOnItemClickListener((parent, view, position, id) -> {
            contactoSeleccionadoObj = adapter.getItem(position);
            if (contactoSeleccionadoObj != null) {
                contactoSeleccionadoId = contactoSeleccionadoObj.getId();
                mostrarDetalleYNota(contactoSeleccionadoObj);
            }
        });

        btnAtras.setOnClickListener(v -> finish());

        btnEliminar.setOnClickListener(v -> {
            if (contactoSeleccionadoId != -1) {
                dbHelper.eliminarContacto(contactoSeleccionadoId);
                Toast.makeText(this, "Contacto eliminado", Toast.LENGTH_SHORT).show();
                cargarContactos();
                contactoSeleccionadoId = -1;
                contactoSeleccionadoObj = null;
            } else {
                Toast.makeText(this, "Seleccione un contacto primero", Toast.LENGTH_SHORT).show();
            }
        });

        btnCompartir.setOnClickListener(v -> {
            if (contactoSeleccionadoObj != null) {
                String txt = "Contacto: " + contactoSeleccionadoObj.getNombre() + " - Tel: " + contactoSeleccionadoObj.getPais() + " " + contactoSeleccionadoObj.getTelefono();
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, txt);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Compartir vía"));
            } else {
                Toast.makeText(this, "Seleccione un contacto para compartir", Toast.LENGTH_SHORT).show();
            }
        });

        btnVerImagen.setOnClickListener(v -> {
            if (contactoSeleccionadoObj != null && contactoSeleccionadoObj.getImagen() != null) {
                byte[] imgBytes = contactoSeleccionadoObj.getImagen();
                android.graphics.Bitmap bmp = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);
                ImageView imgView = new ImageView(this);
                imgView.setImageBitmap(bmp);
                new AlertDialog.Builder(this)
                        .setTitle("Imagen de " + contactoSeleccionadoObj.getNombre())
                        .setView(imgView)
                        .setPositiveButton("Cerrar", null)
                        .show();
            } else {
                Toast.makeText(this, "El contacto no tiene imagen guardada", Toast.LENGTH_SHORT).show();
            }
        });

        btnActualizar.setOnClickListener(v -> {
            if (contactoSeleccionadoObj != null) {
                Toast.makeText(this, "Actualizar contacto: " + contactoSeleccionadoObj.getNombre(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Seleccione un contacto para actualizar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarContactos() {
        listaContactos = new ArrayList<>();
        Cursor cursor = dbHelper.obtenerContactos();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String pais = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PAIS));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOMBRE));
                String telefono = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TELEFONO));
                String nota = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTA));
                byte[] imagen = cursor.getBlob(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGEN));

                Contacto c = new Contacto(id, pais, nombre, telefono, nota, imagen);
                listaContactos.add(c);
            } while (cursor.moveToNext());
        }
        cursor.close();
        actualizarAdapter(listaContactos);
    }

    private void actualizarAdapter(ArrayList<Contacto> lista) {
        adapter = new ArrayAdapter<Contacto>(this, android.R.layout.simple_list_item_2, android.R.id.text1, lista) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);
                Contacto c = getItem(position);
                if (c != null) {
                    text1.setText(c.getNombre());
                    text1.setTextColor(Color.parseColor("#880E4F"));
                    text1.setTextSize(16);

                    text2.setText(c.getPais() + " " + c.getTelefono());
                    text2.setTextColor(Color.parseColor("#AD1457"));
                    text2.setTextSize(14);
                }
                return view;
            }
        };
        listViewContactos.setAdapter(adapter);
    }

    private void mostrarDetalleYNota(Contacto contacto) {
        if (contacto == null) return;
        String codigoPais = contacto.getPais().replaceAll("[^0-9]", "");
        String numeroCompleto = "+" + codigoPais + contacto.getTelefono();

        String mensajeDetalle = "Teléfono: " + numeroCompleto + "\n\nNota:\n" + (contacto.getNota() != null ? contacto.getNota() : "Sin nota");

        new AlertDialog.Builder(this)
                .setTitle(contacto.getNombre())
                .setMessage(mensajeDetalle)
                .setPositiveButton("Llamar", (dialog, which) -> {
                    Intent intent = new Intent(ListaActivity.this, LlamarActivity.class);
                    intent.putExtra("numero_completo", numeroCompleto);
                    if (contacto.getImagen() != null) {
                        intent.putExtra("imagen_contacto", contacto.getImagen());
                    }
                    startActivity(intent);
                })
                .setNegativeButton("Cerrar", null)
                .show();
    }
}