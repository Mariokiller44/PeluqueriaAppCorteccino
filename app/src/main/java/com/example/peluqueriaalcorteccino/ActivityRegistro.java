package com.example.peluqueriaalcorteccino;

import static androidx.core.os.LocaleListCompat.create;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class ActivityRegistro extends AppCompatActivity {

    private RadioGroup radioGroup;
    private LinearLayout extraFieldsLayout;
    private EditText textoOculto,textoNombre,textoApellido,textoTelf,textoCuenta,textoEmail,textoContrasenia;
    private Button botonAceptar, btnCancelar;
    private RadioButton botonPersonal,btnCliente;
    private AlertDialog cancelDialog;
    private String tipoUsuario,descripcionCliente,tipoPersonal;
    private Spinner trabajo;
    private RadioGroup botonera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ventana_registro);

        radioGroup = findViewById(R.id.radioGroup);
        extraFieldsLayout = findViewById(R.id.extraFieldsLayout);
        final Spinner spinnerProfession = findViewById(R.id.spinnerProfession);
        textoOculto = findViewById(R.id.editTextDescription);
        btnCancelar = findViewById(R.id.botonCancelar);
        textoNombre = findViewById(R.id.editTextName);
        textoApellido = findViewById(R.id.editTextLastName);
        textoEmail = findViewById(R.id.editTextEmail);
        textoTelf = findViewById(R.id.editTextPhone);
        textoCuenta = findViewById(R.id.editTextAccount);
        textoContrasenia = findViewById(R.id.editTextPassword);
        btnCliente = findViewById(R.id.radioButtonClient);
        botonPersonal = findViewById(R.id.radioButtonPersonal);
        trabajo=findViewById(R.id.spinnerProfession);
        botonera=findViewById(R.id.radioGroup);
        botonAceptar = findViewById(R.id.botonAceptar);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.radioButtonClient) {
                    extraFieldsLayout.setVisibility(View.VISIBLE);
                    spinnerProfession.setVisibility(View.GONE);
                    textoOculto.setVisibility(View.VISIBLE);
                } else if (checkedId == R.id.radioButtonPersonal) {
                    extraFieldsLayout.setVisibility(View.VISIBLE);
                    spinnerProfession.setVisibility(View.VISIBLE);
                    textoOculto.setVisibility(View.GONE);
                } else {
                    extraFieldsLayout.setVisibility(View.GONE);
                }
            }
        });

        // Set up the spinner options for the profession
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.professions_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProfession.setAdapter(adapter);

        // Crear el diálogo de cancelación
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRegistro.this);
        builder.setTitle("Cancelar Registro")
        .setMessage("¿Estás seguro de que deseas cancelar el registro?")
        .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Acciones a realizar si el usuario confirma la cancelación
                // Por ejemplo, cerrar la actividad actual
                finish();
            }
        })
        .setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Acciones a realizar si el usuario no confirma la cancelación
                // Por ejemplo, cerrar el diálogo
                dialog.dismiss();
            }
        });
        cancelDialog=builder.create();

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelDialog.show();
            }
        });
        botonAceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String telefono=textoTelf.getText().toString();
                String email=textoEmail.getText().toString();
                if (!textoNombre.getText().toString().isEmpty()||
                        !textoNombre.getText().toString().isEmpty()||
                        !textoApellido.getText().toString().isEmpty()||
                        !textoEmail.getText().toString().isEmpty()||
                        !textoTelf.getText().toString().isEmpty()||
                        !textoCuenta.getText().toString().isEmpty()||
                        !textoContrasenia.getText().toString().isEmpty()||
                        botonera.getCheckedRadioButtonId()!=-1){
                    if (!validarTelefono(telefono)) {
                        // El teléfono no tiene 9 dígitos numéricos
                        Toast.makeText(ActivityRegistro.this, "Teléfono inválido", Toast.LENGTH_SHORT).show();
                        return;
                    }else if (!validarEmail(email)) {
                        // El email no tiene el formato válido
                        Toast.makeText(ActivityRegistro.this, "Email inválido", Toast.LENGTH_SHORT).show();
                        return;
                    }else
                    insertarUsuario();
                }else
                    Toast.makeText(ActivityRegistro.this, "Faltan registros", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Mostrar el diálogo de cancelación al presionar el botón "Atrás" del dispositivo
        cancelDialog.show();
    }

    private boolean validarTelefono(String telefono) {
        return telefono.matches("\\d{9}"); // Comprueba si el teléfono tiene 9 dígitos numéricos
    }

    private boolean validarEmail(String email) {
        String pattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"; // Patrón para validar el formato del email
        return email.matches(pattern); // Comprueba si el email tiene el formato válido
    }

    private void insertarUsuario() {
        String url = "http://192.168.14.119/webservcapp/insertar.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Manejar la respuesta del servidor (opcional)
                        Toast.makeText(ActivityRegistro.this, response, Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Manejar el error de la solicitud (opcional)
                        Toast.makeText(ActivityRegistro.this, "Error al conectar con el servidor", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                /*if (botonPersonal.isChecked()){
                    tipoUsuario="Personal";
                }else if (btnCliente.isChecked()){
                    tipoUsuario="Cliente";
                }
                tipoPersonal= trabajo.getSelectedItem().toString();
                */
                Map<String, String> params = new HashMap<>();
                params.put("nombre", textoNombre.getText().toString());
                params.put("apellidos", textoApellido.getText().toString());
                params.put("email",textoEmail.getText().toString());
                params.put("telefono", textoTelf.getText().toString());
                params.put("cuenta", textoCuenta.getText().toString());
                params.put("contrasenia", textoContrasenia.getText().toString());
                params.put("tipo_de_usuario","Cliente");
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
