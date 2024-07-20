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



