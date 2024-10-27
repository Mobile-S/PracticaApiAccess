package com.example.clase07_apiaccess

import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.app.Activity
import android.app.AlertDialog
import android.widget.Toast
import com.example.clase07_apiaccess.api.cls_UsoApi
import java.util.concurrent.Executors
import android.os.Handler
import android.os.Looper

class ListTaskAdapter(
    val context: Activity,
    val ids: Array<String?>,
    val tasks: Array<String?>,
    val token: String,
    val onTaskDeleted: () -> Unit,
    val fechas: Array<String?>
) : ArrayAdapter<String?>(context, 0, ids) {

    // Crear el executor y el handler para manejar operaciones en segundo plano
    private val myExecutor = Executors.newSingleThreadExecutor()
    private val myHandler = Handler(Looper.getMainLooper())

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView: View = inflater.inflate(R.layout.multidata, null, true)

        val txtId = rowView.findViewById<TextView>(R.id.txtid)
        val txtTask = rowView.findViewById<TextView>(R.id.txtTask)
        val txtFecha = rowView.findViewById<TextView>(R.id.txtFecha)

        txtId.text = ids[position]
        txtTask.text = tasks[position]
        txtFecha.text = fechas[position]

        // Manejar la eliminación de la tarea al mantener presionado
        rowView.setOnLongClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Eliminar tarea")
            builder.setMessage("¿Estás seguro de eliminar esta tarea?")
            builder.setPositiveButton("Sí") { _, _ ->
                // Ejecutar la eliminación en segundo plano
                myExecutor.execute {
                    val api = cls_UsoApi()
                    val resultado = api.deletetask(ids[position]!!, token) // Llama a deletetask con el token

                    // Volver al hilo principal para actualizar la UI
                    myHandler.post {
                        if (resultado) {
                            Toast.makeText(context, "Tarea eliminada con éxito", Toast.LENGTH_SHORT).show()

                            // Llama al callback para recargar la lista
                            onTaskDeleted()
                        } else {
                            Toast.makeText(context, "Error al eliminar la tarea", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            builder.setNegativeButton("No", null)
            builder.show()
            true
        }

        return rowView
    }
}