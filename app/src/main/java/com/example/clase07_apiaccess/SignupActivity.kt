package com.example.clase07_apiaccess

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.content.Context
import android.util.Log

import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

import com.example.clase07_apiaccess.api.cls_UsoApi
import com.example.clase07_apiaccess.api.cls_Usuario


class SignupActivity : AppCompatActivity() {

    private lateinit var txt_Nombre : EditText
    private lateinit var txt_Email : EditText
    private lateinit var txt_Contra : EditText
    private lateinit var btn_Ok : Button

    // Create executor and handler for async tasks,
    // this is for use the api library in a background thread
    private val myExecutor = Executors.newSingleThreadExecutor()
    private val myHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)

        txt_Contra = findViewById(R.id.txt_Contra)
        txt_Email = findViewById(R.id.txt_Email)
        txt_Nombre = findViewById(R.id.txt_Nombre)
        btn_Ok = findViewById(R.id.btn_Ok)

        btn_Ok.setOnClickListener {
            Registrar()
        }
    }


    // Register new user function
    fun Registrar() {
        // Validate user data
        var aplicar = true
        if (txt_Nombre.text.toString().trim { it <= ' ' }.length == 0 && aplicar) {
            Toast.makeText(this, "El nombre es requerido...!", Toast.LENGTH_LONG).show()
            txt_Nombre.requestFocus()
            aplicar = false
        }
        if (txt_Email.text.toString().trim { it <= ' ' }.length == 0 && aplicar) {
            Toast.makeText(this, "El E-mail es requerido...!", Toast.LENGTH_LONG).show()
            txt_Email.requestFocus()
            aplicar = false
        }
        if (txt_Contra.text.toString().trim { it <= ' ' }.length == 0 && aplicar) {
            Toast.makeText(this, "La clave es requerida...!", Toast.LENGTH_LONG).show()
            txt_Contra.requestFocus()
            aplicar = false
        }
        if (aplicar) {
            doMyTask(txt_Nombre.text.toString().trim { it <= ' ' },
                txt_Email.text.toString().trim { it <= ' ' },
                txt_Contra.text.toString().trim { it <= ' ' }
            )

            // Close current activity and return to the previous one
            Toast.makeText(this, "Sus datos han sido registrados.", Toast.LENGTH_LONG).show()
            finish()
        }
    } // end register new user function

    private fun doMyTask(vararg params: String?){
        val TAG = "Uso de API"
        myExecutor.execute {
            try {
                //Create user data to send api register function
                val usuario = cls_Usuario()
                usuario.nombre = params[0]
                usuario.email = params[1]
                usuario.contrasena = params[2]

                Log.d(TAG, "llamando la clase de la API")
                //Apply data into the api library
                val api = cls_UsoApi()
                val user: cls_Usuario? = api.signup(usuario)

                //Register the data into the local storage
                val prefe = getSharedPreferences("appData", Context.MODE_PRIVATE)

                //Create editor object for write app data
                val editor = prefe.edit()

                //Set editor fields with the new values
                editor.putString("nombre", user?.nombre)
                editor.putString("email", user?.email)
                editor.putString("token", user?.token)

                //Write app data
                editor.commit()
            } catch (ex: Exception) {
                val trace = ex.stackTrace
                var i = 0
                while (i < trace.size) {
                    Log.d(TAG, trace[i].toString())
                    i++
                }
            }
            myHandler.post {
                Toast.makeText(this, "Sus datos han sido registrados.", Toast.LENGTH_LONG).show()
            }
        }
    }
}