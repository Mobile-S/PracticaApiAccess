package com.example.clase07_apiaccess

import android.content.Context
import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ListView
import java.util.concurrent.Executors

import com.example.clase07_apiaccess.api.cls_UsoApi

class AddTaskActivity : AppCompatActivity() {

    private lateinit var etTask: EditText
    private lateinit var btnSaveTask: Button

    // Create executor and handler for async tasks,
    // this is for use the api library in a background thread
    private val myExecutor = Executors.newSingleThreadExecutor()
    private val myHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_task)

        etTask = findViewById(R.id.et_task)
        btnSaveTask = findViewById(R.id.btn_save_task)

        btnSaveTask.setOnClickListener {
            val task = etTask.text.toString().trim()
            if (task.isNotEmpty()) {
                // Llamar a la API para agregar la nueva tarea en un hilo en segundo plano
                myExecutor.execute {
                    val api = cls_UsoApi()
                    val token = getSharedPreferences("appData", Context.MODE_PRIVATE)
                        .getString("token", "")
                    val resultado = api.addtask(task, token!!)

                    // Volver al hilo principal para mostrar un mensaje y actualizar la UI
                    myHandler.post {
                        if (resultado != null) {
                            Toast.makeText(this, "Error al agregar tarea", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Tarea agregada", Toast.LENGTH_SHORT).show()

                            // Regresa a la actividad principal
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Ingrese una tarea", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
