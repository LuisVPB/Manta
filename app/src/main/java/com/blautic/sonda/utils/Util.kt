package com.blautic.sonda.utils

import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.widget.Toast
import kotlinx.datetime.*
import timber.log.Timber
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class Util {

    companion object {
        fun generatedExcel(context: Context, uri: Uri, vararg tables: String?) {
            if (tables.size == 0) return
            generarExcel(context, uri, *tables)
        }

        private fun generarExcel(
            context: Context,
            uri: Uri,
            vararg tables: String?
        ) {
            val progressDialog = ProgressDialog(context)
            progressDialog.setCancelable(false)
            progressDialog.setMessage("Exporting data, please wait..")
            progressDialog.show()

            val builder = ArrayToExcel.Builder(context)
            builder.setOutputPath(context.filesDir.path)
            builder.setOutputFileName("monitor.xls")
            builder.start(object : ArrayToExcel.ExportListener {

                override fun onStart() {
                    progressDialog.show()
                    Timber.d("onStart")
                }

                override fun onCompleted(filePath: String?) {
                    Timber.d("onCompleted %s", filePath)
                    progressDialog.dismiss()
                    try {
                        val outputStream = context.contentResolver.openOutputStream(uri)
                        outputStream?.let {
                            copy(File(filePath), outputStream)
                            showMessage(context, uri.path)
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onError(e: Exception?) {
                    progressDialog.dismiss()
                    Timber.e(e)
                }
            })
        }

        fun showMessage(context: Context?, msn: String?) {
            Toast.makeText(context, msn, Toast.LENGTH_SHORT).show()
        }

        @Throws(IOException::class)
        fun copy(src: File?, out: OutputStream) {
            FileInputStream(src).use { `in` ->
                val buf = ByteArray(1024)
                var len: Int
                while (`in`.read(buf).also { len = it } > 0) {
                    out.write(buf, 0, len)
                }
                out.close()
            }
        }
    }





}



