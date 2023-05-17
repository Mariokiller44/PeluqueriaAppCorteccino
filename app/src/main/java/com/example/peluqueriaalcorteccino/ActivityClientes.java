package com.example.peluqueriaalcorteccino;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionBarPolicy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ActivityClientes extends AppCompatActivity {

    private String fecha;
    private Spinner spinner;
    private String servicioSeleccionado;
    private int selectedIndex;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ventana_clientes);

        CalendarView calendarView = findViewById(R.id.calendario);

        // Obtener la fecha actual
        Calendar calendar = Calendar.getInstance();
        long currentDate = calendar.getTimeInMillis();

        // Establecer la fecha actual como valor predeterminado en el CalendarView
        calendarView.setDate(currentDate);
        String usuario = getIntent().getStringExtra("Nombre Usuario");
        TextView texto = (TextView) findViewById(R.id.textoBienvenida);
        texto.setText(texto.getText() + " " + usuario);



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_clientes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.aniadirCita) {
            // Acción para la opción 1
            Calendar calendar = Calendar.getInstance();
            TextView mostrarfecha=(TextView) findViewById(R.id.fechaEscogida);
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    ActivityClientes.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            // Se llama cuando el usuario selecciona una fecha

                            // Actualizar el TextView con la fecha seleccionada
                            Calendar selectedCalendar = Calendar.getInstance();
                            selectedCalendar.set(year, monthOfYear, dayOfMonth);
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                            fecha = dateFormat.format(selectedCalendar.getTime());
                            consultarServicios();
                        }
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            // Mostrar el diálogo de selección de fecha
            datePickerDialog.show();
        } else if (id == R.id.mostrarCitas) {
            // Acción para la opción 2
            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityClientes.this);
            builder.setMessage("Has seleccionado mostrar citas")
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                        }
                    })
                    .setCancelable(false) // Impedir que el cuadro de diálogo se cierre al tocar fuera de él
                    .create()
                    .show();
        } else if (id == R.id.borrarCita) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityClientes.this);
            builder.setMessage("Borrando citas")
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                        }
                    })
                    .setCancelable(false) // Impedir que el cuadro de diálogo se cierre al tocar fuera de él
                    .create()
                    .show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void consultarServicios() {
        String url = "http://192.168.50.214/webservcapp/consultaServicios.php?fecha=" + fecha;
        ConsultaServiciosTask consultaServiciosTask = new ConsultaServiciosTask();
        consultaServiciosTask.execute(url);
    }

    public class ConsultaServiciosTask extends AsyncTask<String, Void, String> {
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
            // Aquí puedes manejar la respuesta del servidor después de la solicitud de consulta
            try {
                JSONArray jsonArray = new JSONArray(result);
                ArrayList<String> servicios = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject servicioObj = jsonArray.getJSONObject(i);
                    String nombre = servicioObj.getString("nombre_servicio");
                    int precio = servicioObj.getInt("precio");
                    String servicio = nombre + " - " + precio+"€ ";
                    servicios.add(servicio);
                }

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ActivityClientes.this,
                        android.R.layout.select_dialog_singlechoice, servicios);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityClientes.this);

                builder.setTitle("Seleccione un servicio")
                        .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setSingleChoiceItems(arrayAdapter, selectedIndex, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Acción al seleccionar una opción
                                selectedIndex = which; // Actualizar el índice de la opción seleccionada
                            }
                        })
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Acción al hacer clic en el botón "Aceptar"
                                servicioSeleccionado = servicios.get(which);
                                // Puedes realizar alguna acción aquí o simplemente cerrar el diálogo

                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}
