package com.iyxan23.sketch.collab

import android.os.Environment
import android.util.Base64
import android.util.Log
import com.iyxan23.sketch.collab.models.SketchwareProject
import com.iyxan23.sketch.collab.models.SketchwareProjectMeta
import org.json.JSONObject
import java.io.File
import java.io.RandomAccessFile
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList

object Util {
    var key2name = Hashtable<String, String>()
    private const val TAG = "Util"

    fun sha512(s: String): String {
        try {
            // Create MD5 Hash
            val digest = MessageDigest
                .getInstance("SHA-512")
            digest.update(s.toByteArray())
            val messageDigest = digest.digest()

            // Create Hex String
            val hexString = StringBuilder()
            for (aMessageDigest in messageDigest) {
                var h = Integer.toHexString(0xFF and aMessageDigest.toInt())
                while (h.length < 2) h = "0$h"
                hexString.append(h)
            }
            return hexString.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ""
    }

    fun base64encode(txt: String): String {
        val data = txt.toByteArray()
        return Base64.encodeToString(data, Base64.DEFAULT)
    }

    fun base64decode(base64: String?): String {
        val data = Base64.decode(base64, Base64.DEFAULT)
        return String(data)
    }


    fun getSketchwareProjects(): ArrayList<SketchwareProject> {
        val sketchwareProjects: ArrayList<SketchwareProject> = ArrayList()

        val path: String = Environment.getExternalStorageDirectory().absolutePath + "/.sketchware/mysc/list/"
        val pathFiles: String = Environment.getExternalStorageDirectory().absolutePath + "/.sketchware/data/"

        Log.d(TAG, "getSketchwareProjects: $path");
        Log.d(TAG, "getSketchwareProjects: $pathFiles");

        val projectData: ArrayList<String> = listDir(pathFiles)

        for ((index, pat) in listDir(path).withIndex()) {
            val project = JSONObject(decrypt("$pat/project"));

            /*
            Log.d(TAG, "getSketchwareProjects PROJECT: " + decrypt(pat + "/project"));
            Log.d(TAG, "getSketchwareProjects LOGIC: " + decrypt(Environment.getExternalStorageDirectory().getAbsolutePath() + "/.sketchware/data/" + project.getString("sc_id") + "/logic"));
            Log.d(TAG, "getSketchwareProjects VIEW: " + decrypt(Environment.getExternalStorageDirectory().getAbsolutePath() + "/.sketchware/data/" + project.getString("sc_id") + "/view"));
            Log.d(TAG, "getSketchwareProjects FILE: " + decrypt(Environment.getExternalStorageDirectory().getAbsolutePath() + "/.sketchware/data/" + project.getString("sc_id") + "/file"));
             */

            val dataPath: String = projectData[index]
            val swProjMeta = SketchwareProjectMeta(
                    project.getString("my_app_name"),
                    project.getString("sc_ver_name"),
                    project.getString("my_sc_pkg_name"),
                    project.getString("my_ws_name"),
                    project.getString("sc_id").toInt()
            )

            val swProj = SketchwareProject(
                    decrypt("$dataPath/project"),
                    decrypt("$dataPath/logic"),
                    decrypt("$dataPath/view"),
                    decrypt("$dataPath/resource"),
                    decrypt("$dataPath/library"),
                    decrypt("$dataPath/file"),
                    swProjMeta
            )

            sketchwareProjects.add(0, swProj)

        }
        return sketchwareProjects
    }

    fun decrypt(path: String): String {
        try {
            val instance = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val bytes = "sketchwaresecure".toByteArray()
            instance.init(2, SecretKeySpec(bytes, "AES"), IvParameterSpec(bytes))
            val randomAccessFile = RandomAccessFile(path, "r")
            val bArr = ByteArray(randomAccessFile.length().toInt())
            randomAccessFile.readFully(bArr)
            return String(instance.doFinal(bArr))
        } catch (e: Exception) {
            Log.e("Util", "Error while decrypting, at path: $path")
            e.printStackTrace()
        }
        return "ERROR WHILE DECRYPTING"
    }

    fun encrypt(path: String, str: String) {
        try {
            val instance = Cipher.getInstance("AES/CBC/PKCS5Padding")
            val bytes = "sketchwaresecure".toByteArray()
            instance.init(1, SecretKeySpec(bytes, "AES"), IvParameterSpec(bytes))
            val doFinal = instance.doFinal(str.toByteArray())
            val randomAccessFile = RandomAccessFile(path, "rw")
            randomAccessFile.setLength(0L)
            randomAccessFile.write(doFinal)
            // return new String(doFinal);
        } catch (e: Exception) {
            Log.e("Util", "Error while encrypting: $str, at path: $path")
            e.printStackTrace()
        }
    }

    fun listDir(str: String?): ArrayList<String> {
        val arrayList = ArrayList<String>()
        val file = File(str)
        if (file.exists() && !file.isFile) {
            val listFiles = file.listFiles()
            if (listFiles != null && listFiles.isNotEmpty()) {
                arrayList.clear()
                for (absolutePath in listFiles) {
                    arrayList.add(absolutePath.absolutePath)
                }
            }
        }
        return arrayList
    }

    fun getSketchwareProject(id: Int): SketchwareProject? {
        val dataPath: String = Environment.getExternalStorageDirectory().absolutePath + "/.sketchware/data/$id"
        val myscProjectDirectory: String = Environment.getExternalStorageDirectory().absolutePath + "/.sketchware/mysc/list/$id/project"
        val dataDirectoryFile = File(dataPath)

        if (!dataDirectoryFile.exists() or !dataDirectoryFile.isDirectory)
            return null;

        val project = JSONObject(decrypt(myscProjectDirectory))

        val swProjMeta = SketchwareProjectMeta(
                project.getString("my_app_name"),
                project.getString("sc_ver_name"),
                project.getString("my_sc_pkg_name"),
                project.getString("my_ws_name"),
                project.getString("sc_id").toInt()
        )

        return SketchwareProject(
                decrypt("$dataPath/project"),
                decrypt("$dataPath/logic"),
                decrypt("$dataPath/view"),
                decrypt("$dataPath/resource"),
                decrypt("$dataPath/library"),
                decrypt("$dataPath/file"),
                swProjMeta
        )
    }
}