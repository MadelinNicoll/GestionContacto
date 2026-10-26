package com.example.gestioncontacto;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class LlamarActivity extends AppCompatActivity {

    private static final int REQUEST_CALL_PERMISSION = 101;
    private String numeroCompleto;
    private ImageView imgLlamarContacto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_llamar);

        TextView txtNumero = findViewById(R.id.txtNumeroLlamar);
        Button btnAtras = findViewById(R.id.btnAtrasLlamar);
        Button btnDevolverLlamada = findViewById(R.id.btnDevolverLlamada);
        imgLlamarContacto = findViewById(R.id.imgLlamarContacto);

        numeroCompleto = getIntent().getStringExtra("numero_completo");
        byte[] imagenBytes = getIntent().getByteArrayExtra("imagen_contacto");

        txtNumero.setText(numeroCompleto);

        if (imagenBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length);
            imgLlamarContacto.setImageBitmap(bitmap);
        } else {
            imgLlamarContacto.setImageResource(android.R.drawable.ic_menu_myplaces);
        }

        btnAtras.setOnClickListener(v -> finish());

        btnDevolverLlamada.setOnClickListener(v -> verificarYRealizarLlamada());

        verificarYRealizarLlamada();
    }

    private void verificarYRealizarLlamada() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PERMISSION);
        } else {
            realizarLlamada();
        }
    }

    private void realizarLlamada() {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + numeroCompleto));
            startActivity(callIntent);
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "Permiso de llamada denegado", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                realizarLlamada();
            } else {
                Toast.makeText(this, "Se requiere el permiso CALL_PHONE para realizar llamadas", Toast.LENGTH_SHORT).show();
            }
        }
    }
}