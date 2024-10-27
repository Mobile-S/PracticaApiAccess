package com.example.clase07_apiaccess.api

import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class cls_UsoApi {
    //Declaring base path to api-rest service and external connection variables
    private val urlPath = "http://192.168.1.21:5000/"
    private var urlAPI: URL? = null
    private var conex: HttpURLConnection? = null

    private var conectado = true

    private val TAG = "Uso de API"

    private fun prepara(metodo: String, ruta2: String) {
        try {
            //Establishing access at aws api-rest service
            urlAPI = URL(urlPath + ruta2)
            Log.d(TAG, "Despues de asignar URL: " + urlPath + ruta2)
            conex = urlAPI!!.openConnection() as HttpURLConnection
            Log.d(TAG, "Despues de abrir la URL: " + urlPath + ruta2)

            //Add headers to connection variable
            conex!!.setRequestProperty("Accept", "application/json")
            conex!!.setRequestProperty("Content-Type", "application/json")
            Log.d(
                TAG,
                "Despues de asignar Request Properties: " + conex!!.requestProperties.toString()
            )

            //Declaring time out connection
            conex!!.connectTimeout = 5000
            conex!!.readTimeout = 5000
            Log.d(TAG, "Despues de asignar tiempo")

            //Set active connection form bidirectional way (read and write)
            conex!!.doInput = true
            conex!!.doOutput = metodo=="POST" || metodo=="PUT" || metodo=="DELETE"
            Log.d(TAG, "Despues de asignar i/o")

            //Declare the access method on the aws api
            conex!!.requestMethod = metodo
            Log.d(TAG, "Despues de asignar metodo: $metodo")

        } catch (err: Exception) {
            conectado = false
        }
    } //end of constructor function


    //Funcion para registrar un usuario
    fun signup(datos: cls_Usuario): cls_Usuario? {
        var registro: cls_Usuario? = null
        prepara("POST", "signup")
        try {
            val salidaJSON = JSONObject()

            //Put de external data into json object
            salidaJSON.put("name", datos.nombre)
            salidaJSON.put("email", datos.email)
            salidaJSON.put("passwd", datos.contrasena)

            //Create sentences to write json object into de aws api-rest service
            val wr = OutputStreamWriter(conex!!.outputStream)
            wr.write(salidaJSON.toString())
            wr.flush()

            //Validate the write instruction has successful return
            if (conex!!.responseCode == HttpURLConnection.HTTP_CREATED) {
                val br = BufferedReader(
                    InputStreamReader(
                        conex!!.inputStream,
                        "UTF-8"
                    )
                )
                registro = cls_Usuario()

                //Acquire both parts of json object, status data and body data
                val auxGen = JSONObject(br.readText().toString())
                val auxDat = JSONObject(auxGen["data"].toString())

                //Put data into the local user object
                registro.token  = (auxDat["user"] as JSONObject).get("token").toString()
                registro.nombre = (auxDat["user"] as JSONObject).get("name").toString()
                registro.email  = (auxDat["user"] as JSONObject).get("email").toString()

                //Close buffer reader object
                br.close()
            } //end

            //Close stream writer object
            wr.close()
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        //return a user object (null or full filled)
        return registro
    }

    //Funcion para registrar una tarea
    fun addtask(tarea: String?, token: String): cls_Nota? {
        var registro: cls_Nota? = null
        prepara("POST", "$token/task")
        try {
            val salidaJSON = JSONObject()

            // Coloca los datos externos en el objeto JSON
            salidaJSON.put("task", tarea)

            // Crea el flujo de escritura para enviar el objeto JSON a la API
            val wr = OutputStreamWriter(conex!!.outputStream)
            wr.write(salidaJSON.toString())
            wr.flush()

            // Valida que la instrucción de escritura haya sido exitosa
            if (conex!!.responseCode == HttpURLConnection.HTTP_OK) {
                val br = BufferedReader(
                    InputStreamReader(
                        conex!!.inputStream,
                        "UTF-8"
                    )
                )
                registro = cls_Nota()

                // Adquiere ambos partes del objeto JSON, datos de estado y cuerpo
                val auxGen = JSONObject(br.readLine().toString())
                val auxDat = JSONObject(auxGen["data"].toString())

                // Coloca los datos en el objeto local
                registro.id = auxDat["_id"].toString()
                registro.tarea = auxDat["task"].toString()
                registro.fecha = auxDat["date"].toString()

                // Cierra el buffer reader
                br.close()
            }

            // Cierra el stream writer
            wr.close()
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return registro
    }

    fun gettask(token: String?): Array<cls_Nota?>? {
        prepara("GET", "$token/task")
        Log.i("Datos3", "Despues de prepara")
        try {
            if (conex!!.responseCode == HttpURLConnection.HTTP_OK) {
                val br = BufferedReader(
                    InputStreamReader(
                        conex!!.inputStream,
                        "UTF-8"
                    )
                )
                val auxGen = JSONObject(br.readLine().toString())

                var registro =  arrayOfNulls<cls_Nota?>((auxGen["data"] as JSONArray).length())

                for (i in 0 until (auxGen["data"] as JSONArray).length()) {
                    val aux = (auxGen["data"] as JSONArray)[i] as JSONObject
                    var nota = cls_Nota()
                    nota.id = aux["id"].toString()
                    nota.tarea = aux["task"].toString()
                    nota.fecha = aux["date"].toString()

                    registro.set(i,nota)
                }

                //Close buffer reader object
                br.close()

                return registro
            } //end
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e("Datos 3", "Error in GetData", e)
        }
        return null
    }

    fun deletetask(id: String, token: String): Boolean {
        prepara("DELETE", "$token/task/$id")
        return try {
            // Enviar la solicitud DELETE y leer la respuesta
            val responseCode = conex!!.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val br = BufferedReader(InputStreamReader(conex!!.inputStream, "UTF-8"))
                val response = br.readLine()
                val jsonResponse = JSONObject(response)

                // Verificar si el resultado es verdadero en la respuesta JSON
                val result = jsonResponse.optBoolean("result", false)
                br.close()

                if (result) {
                    Log.d("Uso de API", "Tarea eliminada con éxito")
                    return true
                } else {
                    Log.d("Uso de API", "Error en la eliminación de la tarea")
                    return false
                }
            } else {
                Log.d("Uso de API", "Error al eliminar la tarea: Código de respuesta $responseCode")
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}