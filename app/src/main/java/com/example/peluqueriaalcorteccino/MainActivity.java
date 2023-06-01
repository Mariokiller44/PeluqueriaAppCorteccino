package com.example.peluqueriaalcorteccino;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private EditText usuario, contrasenia;
    private Button botonAceptar;
    private String ip;
    private int currentIpIndex = 0;

    private RequestQueue requestQueue;
    private Button botonRegistro;
    private CheckBox botonMostrarPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //URL que usar en casa
        // ip= "http://192.168.50.119";
        //URL para usar en Feval
        ip="http://192.168.75.119";
        usuario = findViewById(R.id.nombreUsuario);
        contrasenia = findViewById(R.id.contraseniaUsuario);
        botonAceptar = findViewById(R.id.btnAceptar);
        botonRegistro = findViewById(R.id.btnRegistro);
        botonMostrarPwd = findViewById(R.id.mostrarContrasenia);

        // Obtener la instancia de RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        botonAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        botonRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });
        botonMostrarPwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    contrasenia.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    contrasenia.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

    }

    private void signup() {
        Intent i = new Intent(MainActivity.this,ActivityRegistro.class);
        startActivity(i);
    }

    private void login() {
            String user = usuario.getText().toString();
            String passwd = contrasenia.getText().toString();
            String contraseniaMD5=codificarMD5(passwd);

            // URL del servicio web de inicio de sesión
            String url = ip+"/webservcapp/login.php?usuario="+user+"&contrasenia="+contraseniaMD5;
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
                    int id= objeto.getInt("ID");
                    String nombre = objeto.getString("NOMBRE");
                    //String tipoUsuario = objeto.getString("TIPO_DE_USUARIO");
                    Toast.makeText(MainActivity.this, "Bienvenido " + nombre, Toast.LENGTH_SHORT).show();
                    /*AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Entrando en el menú de " + tipoUsuario)
                            .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Acción al hacer clic en el botón "Aceptar"

                                }
                            })
                            .setCancelable(false) // Impedir que el cuadro de diálogo se cierre al tocar fuera de él
                            .create()
                            .show();*/
                 /*Intent intent = new Intent(MainActivity.this, ActivityClientes.class);
                 intent.putExtra("Nombre Usuario", nombre);
                 startActivity(intent);*/
                    new BuscarCitasHTTP().execute(id);
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
    public class BuscarCitasHTTP extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... voids) {
            String response = "";
            try {
                // URL del archivo PHP
                String url = ip+"/webservcapp/buscarCita.php?cliente_id="+voids[0];

                // Crear la conexión HTTP
                URL urlObj = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();

                // Establecer el método de solicitud
                connection.setRequestMethod("GET");

                // Obtener la respuesta de la solicitud
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();
                response = stringBuilder.toString();

                // Cerrar la conexión
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            Intent i = new Intent(MainActivity.this,ActivityClientes.class);
            i.putExtra("Resultados",result);
            startActivity(i);
        }
    }
    public static String codificarMD5(String input) {
        try {
            // Obtenemos una instancia del algoritmo MD5
            MessageDigest md = MessageDigest.getInstance("MD5");

            // Convertimos el string de entrada a bytes y lo procesamos con el algoritmo
            byte[] messageDigest = md.digest(input.getBytes());

            // Convertimos el arreglo de bytes a una representación hexadecimal
            BigInteger no = new BigInteger(1, messageDigest);
            String hashText = no.toString(16);

            // Aseguramos que el hash resultante tenga 32 caracteres
            while (hashText.length() < 32) {
                hashText = "0" + hashText;
            }

            return hashText;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }
}