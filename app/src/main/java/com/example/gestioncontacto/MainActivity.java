package com.example.gestioncontacto;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerPais;
    private EditText editNombre, editTelefono, editNota;
    private ImageView imgContacto;
    private DatabaseHelper dbHelper;
    private byte[] imagenBytes = null;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        spinnerPais = findViewById(R.id.spinnerPais);
        editNombre = findViewById(R.id.editNombre);
        editTelefono = findViewById(R.id.editTelefono);
        editNota = findViewById(R.id.editNota);
        imgContacto = findViewById(R.id.imgContacto);
        Button btnSalvar = findViewById(R.id.btnSalvar);
        Button btnContactosSalvados = findViewById(R.id.btnContactosSalvados);

        String[] paises = {"Honduras (504)", "Costa Rica (506)", "Guatemala (502)", "El Salvador (503)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, paises);
        spinnerPais.setAdapter(adapter);

        InputFilter filtroNombre = (source, start, end, dest, dstart, dend) -> {
            Pattern ps = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$");
            for (int i = start; i < end; i++) {
                if (!ps.matcher(String.valueOf(source.charAt(i))).matches()) return "";
            }
            return null;
        };
        editNombre.setFilters(new InputFilter[]{filtroNombre});

        InputFilter filtroTelefonoDigitos = (source, start, end, dest, dstart, dend) -> {
            Pattern ps = Pattern.compile("^[0-9]+$");
            for (int i = start; i < end; i++) {
                if (!ps.matcher(String.valueOf(source.charAt(i))).matches()) return "";
            }
            return null;
        };
        editTelefono.setFilters(new InputFilter[]{
                filtroTelefonoDigitos,
                new InputFilter.LengthFilter(8) // Límite exacto de 8 dígitos
        });

        imgContacto.setOnClickListener(v -> mostrarDialogoSeleccionImagen());
        btnSalvar.setOnClickListener(v -> validarYGuardar());
        btnContactosSalvados.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ListaActivity.class)));
    }

    private void mostrarDialogoSeleccionImagen() {
        CharSequence[] opciones = {"Cámara", "Galería", "Cancelar"};
        new AlertDialog.Builder(this)
                .setTitle("Seleccionar imagen")
                .setItems(opciones, (dialog, item) -> {
                    if (opciones[item].equals("Cámara")) {
                        verificarYPedirPermisoCamera();
                    } else if (opciones[item].equals("Galería")) {
                        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(pickPhoto, REQUEST_IMAGE_GALLERY);
                    } else {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void verificarYPedirPermisoCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            abrirCamara();
        }
    }

    private void abrirCamara() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            } else {
                Toast.makeText(this, "Se requiere permiso de cámara para tomar fotos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    if (imageBitmap != null) {
                        imgContacto.setImageBitmap(imageBitmap);
                        imagenBytes = convertirBitmapABytes(imageBitmap);
                    }
                }
            } else if (requestCode == REQUEST_IMAGE_GALLERY) {
                try {
                    Uri selectedImage = data.getData();
                    InputStream inputStream = getContentResolver().openInputStream(selectedImage);
                    Bitmap imageBitmap = BitmapFactory.decodeStream(inputStream);
                    if (imageBitmap != null) {
                        imgContacto.setImageBitmap(imageBitmap);
                        imagenBytes = convertirBitmapABytes(imageBitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private byte[] convertirBitmapABytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private void validarYGuardar() {
        String nombre = editNombre.getText().toString().trim();
        String telefono = editTelefono.getText().toString().trim();
        String nota = editNota.getText().toString().trim();
        String pais = spinnerPais.getSelectedItem().toString();

        if (nombre.isEmpty()) {
            mostrarAlerta("Debe escribir un nombre");
            return;
        }
        if (telefono.isEmpty()) {
            mostrarAlerta("Debe escribir un teléfono");
            return;
        }
        if (nota.isEmpty()) {
            mostrarAlerta("Debe escribir una nota");
            return;
        }

        boolean insertado = dbHelper.insertarContacto(pais, nombre, telefono, nota, imagenBytes);
        if (insertado) {
            Toast.makeText(this, "Contacto guardado exitosamente", Toast.LENGTH_SHORT).show();
            editNombre.setText("");
            editTelefono.setText("");
            editNota.setText("");
            imgContacto.setImageResource(android.R.drawable.ic_menu_camera);
            imagenBytes = null;
        } else {
            Toast.makeText(this, "Error al guardar el contacto", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarAlerta(String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle("Alerta")
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", null)
                .show();
    }
}