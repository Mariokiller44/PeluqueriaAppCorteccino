package com.example.peluqueriaalcorteccino;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private EditText usuario, contrasenia;
    private Button botonAceptar;
    private String ip;
    private int currentIpIndex = 0;

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ip= "http://192.168.50.214";
        usuario = findViewById(R.id.nombreUsuario);
        contrasenia = findViewById(R.id.contraseniaUsuario);
        botonAceptar = findViewById(R.id.btnAceptar);

        // Obtener la instancia de RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        botonAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    private void login() {
            String user = usuario.getText().toString();
            String passwd = contrasenia.getText().toString();

            // URL del servicio web de inicio de sesión
            String url = ip+"/webservcapp/login.php?usuario="+user+"&contrasenia="+passwd;
            // Ejecutar la solicitud de login en un hilo separado
            new LoginTask().execute(url);
    }

    private class LoginTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String response = "";

            try {
                // Crear una URL a partir de la cadena de URL proporcionada
                URL url = new URL(urls[0]);

                // Abrir una conexión HttpURLConnection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                try {
                    // Establecer el método de solicitud
                    connection.setRequestMethod("GET");

                    // Leer la respuesta del servidor
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    StringBuilder stringBuilder = new StringBuilder();
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    response = stringBuilder.toString();
                } finally {
                    // Cerrar la conexión
                    connection.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            // Aquí puedes manejar la respuesta del servidor después de la solicitud de login
             try {
                    JSONObject objeto = new JSONObject(result);
                    String nombre = objeto.getString("NOMBRE");
                    String tipoUsuario = objeto.getString("TIPO_DE_USUARIO");
                    Toast.makeText(MainActivity.this, "Bienvenido " + nombre, Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Entrando en el menú de " + tipoUsuario)
                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Acción al hacer clic en el botón "Aceptar"
                                    Intent intent = new Intent(MainActivity.this, ActivityClientes.class);
                                    intent.putExtra("Nombre Usuario", nombre);
                                    startActivity(intent);

                                }
                            })
                            .setCancelable(false) // Impedir que el cuadro de diálogo se cierre al tocar fuera de él
                            .create()
                            .show();
                } catch (JSONException e) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Error al iniciar sesión")
                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Acción al hacer clic en el botón "Aceptar"
                                    dialog.dismiss();
                                }
                            })
                            .setCancelable(false) // Impedir que el cuadro de diálogo se cierre al tocar fuera de él
                            .create()
                            .show();
                }
            }
        }
}