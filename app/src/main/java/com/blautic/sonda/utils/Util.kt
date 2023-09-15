package com.blautic.sonda.utils

import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import kotlinx.datetime.*
import timber.log.Timber
import java.io.File
import java.util.*

class Util {

    companion object {
        fun generatedExcel(context: Context, uri: Uri, vararg tables: String?) {
            if (tables.size == 0) return
            generarExcel(context, uri, null, *tables)
        }

        private fun generarExcel(
            context: Context,
            uri: Uri,
            sqls: Map<String, String>?,
            vararg tables: String?
        ) {
            val progressDialog = ProgressDialog(context)
            progressDialog.setCancelable(false)
            progressDialog.setMessage("Exporting data, please wait..")
            progressDialog.show()
            val builder = ArrayToExcel.Builder(context)
                .setDataBase(context.getDatabasePath("bioMonitor.db").path)

            if (sqls != null) {
                for ((key, value): Map.Entry<String, String> in sqls) {
                    builder.addSQL(key, value)
                }
            } else if (tables != null) {
                builder.setTables(tables)
            }
            builder.setOutputPath(context.filesDir.path)
            builder.setOutputFileName("monitor.xls")
            builder.start(object : ExportListener() {
                fun onStart() {
                    progressDialog.show()
                    Timber.d("onStart")
                }

                fun onCompleted(filePath: String?) {
                    Timber.d("onCompleted %s", filePath)
                    progressDialog.dismiss()
                    try {
                        val outputStream = context.contentResolver.openOutputStream(uri)
                        com.blautic.myomove.utils.Utils.copy(File(filePath), outputStream)
                        com.blautic.myomove.utils.Utils.showMessage(context, uri.path)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                fun onError(e: Exception?) {
                    progressDialog.dismiss()
                    Timber.e(e)
                }
            })
        }
    }



}



