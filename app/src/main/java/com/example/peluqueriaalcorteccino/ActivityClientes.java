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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;




public class ActivityClientes extends AppCompatActivity {

    private String fecha;
    private Spinner spinner;
    private String servicioSeleccionado;
    private ListView listaDeCitas;
    private int selectedIndex;
    private ArrayList<String> listaCitas = new ArrayList<>();
    private ArrayList<Date> fechasSeleccionadas = new ArrayList<>();
    private JSONArray jsonArray;
    private CalendarView calendarView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ventana_clientes);
        listaDeCitas=findViewById(R.id.listaCitas);
        calendarView=findViewById(R.id.calendario);
        Bundle datos= getIntent().getExtras();
        /*while (!getIntent().getExtras().isEmpty()){
            ArrayList<String> listaDatos= new ArrayList<>();
            String fecha= datos.getString("FechaCita");
            String hora=datos.getString("HoraCita");
            String servicio=datos.getString("Servicio");
            String empleado=datos.getString("Personal");
            String citaCompleta="Fecha: "+fecha+ ", Hora: "+hora+" ,Servicio escogido: "+servicio+" ,Empleado que realiza: "+empleado;
            listaDatos.add(citaCompleta);
            // Crear el adaptador y establecerlo en el ListView
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaDatos);
            listaDeCitas.setAdapter(adapter);
        }*/

        String resultadosConsulta=datos.getString("Resultados");
        mostrarCitas(resultadosConsulta);


    }

    private void mostrarCitas(String resultadosConsulta) {
        // Agrega comas entre los objetos JSON
        resultadosConsulta = resultadosConsulta.replace("}{", "},{");

        // Agrega corchetes alrededor del string completo para que sea un arreglo JSON válido
        resultadosConsulta = "[" + resultadosConsulta + "]";

        try {
            jsonArray = new JSONArray(resultadosConsulta);
            // Recorre el arreglo de objetos JSON
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String fecha = jsonObject.getString("FECHA");
                String hora = jsonObject.getString("HORA");
                String descripcion = jsonObject.getString("DESCRIPCION");
                String cita="Cita "+jsonObject.getInt("ID")+": \nFecha: "+fecha+" "+hora+" \nServicio: "+descripcion;
                listaCitas.add(cita);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Crear el adaptador y establecerlo en el ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaCitas);
        listaDeCitas.setAdapter(adapter);
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
            TextView mostrarfecha=new TextView(this);
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
        //192.168.50.119 IP Movil
        String url = "http://192.168.14.119/webservcapp/consultaServicios.php?fecha=" + fecha;
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
