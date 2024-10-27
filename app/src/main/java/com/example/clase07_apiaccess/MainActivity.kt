package com.example.clase07_apiaccess

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.content.Context
import android.content.Intent

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ListView
import java.util.concurrent.Executors

import com.example.clase07_apiaccess.api.cls_Nota
import com.example.clase07_apiaccess.api.cls_UsoApi

import android.widget.Button

class MainActivity : AppCompatActivity() {

    private var token: String? = null

    private lateinit var lstComp: ListView

    // Create executor and handler for async tasks,
    // this is for use the api library in a background thread
    private val myExecutor = Executors.newSingleThreadExecutor()
    private val myHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // intenta obtener el token del usuario del local storage, sino llama a la ventana de registro
        val prefe = getSharedPreferences("appData", Context.MODE_PRIVATE)
        token = prefe.getString("token", "")


        //valida la variable token
        if (token.toString().trim { it <= ' ' }.length == 0) {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        } else {
            lstComp = findViewById(R.id.lstComp)
            doLoadTasks()

        }

        val btnAddTask = findViewById<Button>(R.id.btn_add_task)
        btnAddTask.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }

    }

    private fun doLoadTasks(): Array<cls_Nota?>? {
        val TAG = "Uso de API"
        var tareas: Array<cls_Nota?>? = null

        myExecutor.execute {
            try {
                Log.d(TAG, "Llamando a la API para obtener tareas")
                val api = cls_UsoApi()
                tareas = api.gettask(token)

                // Usar el handler para volver al hilo principal
                myHandler.post {
                    if (tareas != null) {
                        // Declarar los arreglos para almacenar datos
                        val ids = arrayOfNulls<String>(tareas?.count() ?: 0)
                        val tasks = arrayOfNulls<String>(tareas?.count() ?: 0)
                        val fechas = arrayOfNulls<String>(tareas?.count() ?: 0)

                        // Llenar los arreglos con datos
                        for (i in 0 until ids.size) {
                            ids[i] = tareas?.get(i)?.id
                            tasks[i] = tareas?.get(i)?.tarea
                            fechas[i] = tareas?.get(i)?.fecha
                        }

                        // Establecer el adaptador en el ListView
                        lstComp.adapter = ListTaskAdapter(
                            this@MainActivity, // Asegúrate de usar el contexto correcto
                            ids,
                            tasks,
                            token!!, // Pasar el token al adaptador
                            onTaskDeleted = { doLoadTasks() },
                            fechas)
                    } else {
                        Log.e(TAG, "Error al obtener tareas: tareas es null")
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        return tareas
    }
}