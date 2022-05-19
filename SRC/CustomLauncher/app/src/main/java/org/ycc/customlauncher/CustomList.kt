package org.ycc.customlauncher

import android.R
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream


/**
 * CustomLauncherアプリケーションカスタマイズクラス
 *  Edit masami
 *  2021.12.24
 */
class CustomList {
    /** AppConfig */
    var appConfig = AppConfig()
    /** AppInfo */
    var appList: List<AppInfo> = listOf<AppInfo>()
    /** StringContents */
    var contents: String = ""
    /**
     * デフォルトデータの取得
     */
    fun setCustomList(context: Context, appList: AppInfoList): Boolean {
        try {
            /** 設定ファイル /storage/self/primary/Android/data/org.ycc.customlauncher/files/custom.json */
            val filename = context.getString(org.ycc.customlauncher.R.string.custom_file)
            // Todo File Directory /storage/self/primary/Android/data/org.ycc.customlauncher/files
            val filedir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
//            val filedir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val readFile = File(filedir, filename)
            Log.d("CustomLauncher", "${readFile.absoluteFile}")
            if(!readFile.exists()){
                /** 設定ファイルが存在しない場合 */
                Log.d("CustomLauncher", "FileNameRead NG")
                appConfig = appList.appConfig.copy()
                if(!saveContents(context, appList.contents)){
                    /** 設定ファイルが保存できない場合 */
                    throw Exception("saveContents Error")
                }
                if(saveDefaultImage(context)){
                    /** 背景画像保存エラー */
                    throw Exception("saveDefaultImage Error")
                }
            }
            else {
                /** 設定ファイルが存在 */
                contents = readFile.bufferedReader().use(BufferedReader::readText)
                Log.d("CustomLauncher", "FileNameRead OK")
                /** JSON */
                val jsonObject = JSONObject(contents)
//            Log.d("CustomLauncher", "JSONObject OK")
                /** AppConfig */
                val json = jsonObject.getJSONObject("AppConfig")
//            Log.d("CustomLauncher", "AppConfig OK")
                /** welcome */
                appConfig.welcome = json.getString("welcome")
                val jsonArray = json.getJSONArray("app")
                /** group_logo */
                appConfig.group_logo = json.getString("group_logo")
                /** back_image */
                appConfig.back_image = json.getString("back_image")
                /** help_1 */
                appConfig.help_1 = json.getString("help_1")
                /** help_2 */
                appConfig.help_2 = json.getString("help_2")
                /** help_3 */
                appConfig.help_3 = json.getString("help_3")
                /** help_4 */
                appConfig.help_4 = json.getString("help_4")
                /** help_5 */
                appConfig.help_5 = json.getString("help_5")
//            Log.d("CustomLauncher", "JsonArray OK")
                for (i in 0 until jsonArray.length()) {
                    val app = App()
                    val jsonData = jsonArray.getJSONObject(i)
//                Log.d("CustomLauncher", "$i : ${jsonData.getString("name")}")
                    /** name検索用 */
                    app.name = jsonData.getString("name")
                    /** label表示用 */
                    app.label = jsonData.getString("label")
                    /** shortcut */
                    app.shortcut = jsonData.getString("shortcut")
                    /** icon */
                    app.icon = jsonData.getString("icon")
                    appConfig.app = appConfig.app + app
                }
            }
            return true
        } catch (e: JSONException) {
            Log.e("CustomLauncher setCustomList: ", e.toString())
            e.printStackTrace()
        } catch (e1: FileNotFoundException) {
            Log.e("CustomLauncher setCustomList: ", e1.toString())
        } catch (ex: Exception) {
            Log.e("CustomLauncher setCustomList: ", ex.toString())
            ex.printStackTrace()
        }
        return false
    }
    /**
     * デフォルトデータの取得
     */
    fun loadContents(context: Context): Boolean {
        try {
            /** 設定ファイル /data/data/org.ycc.customlauncher/files/custom.json */
            val filename = context.getString(org.ycc.customlauncher.R.string.custom_file)
            // Todo File Directory
            val filedir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
//            val filedir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val readFile = File(filedir, filename)
            Log.d("CustomLauncher", "${readFile.absoluteFile}")
            if(readFile.exists()){
                contents = readFile.bufferedReader().use(BufferedReader::readText)
                Log.d("CustomLauncher", "FileNameRead OK")
                return true
            }
            return false
        } catch (e: JSONException) {
            Log.e("CustomLauncher loadContents: ", e.toString())
            e.printStackTrace()
        } catch (e1: FileNotFoundException) {
            Log.e("CustomLauncher loadContents: ", e1.toString())
        } catch (ex: Exception) {
            Log.e("CustomLauncher loadContents: ", ex.toString())
            ex.printStackTrace()
        }
        return false
    }
    /**
     * JSONデータ確認
     */
    fun checkContents(context: Context, newText: String): Boolean {
        try {
            val jsonObject = JSONObject(newText)
            /** AppConfig */
            val json = jsonObject.getJSONObject("AppConfig")
//            Log.d("CustomLauncher", "AppConfig OK")
            /** welcome */
            val welcome = json.getString("welcome")
            val jsonArray = json.getJSONArray("app")
            /** group_logo */
            val group_logo = json.getString("group_logo")
            /** back_image */
            val back_image = json.getString("back_image")
            /** help_1 */
            val help_1 = json.getString("help_1")
            /** help_2 */
            val help_2 = json.getString("help_2")
            /** help_3 */
            val help_3 = json.getString("help_3")
            /** help_4 */
            val help_4 = json.getString("help_4")
            /** help_5 */
            val help_5 = json.getString("help_5")
            for (i in 0 until jsonArray.length()) {
                val app = App()
                val jsonData = jsonArray.getJSONObject(i)
                /** name検索用 */
                app.name = jsonData.getString("name")
                /** label表示用 */
                app.label = jsonData.getString("label")
                /** shortcut */
                app.shortcut = jsonData.getString("shortcut")
                /** icon */
                app.icon = jsonData.getString("icon")
            }
            if(pictureFileExists(context, back_image, Configuration.ORIENTATION_PORTRAIT)){
                if(pictureFileExists(context, back_image, Configuration.ORIENTATION_LANDSCAPE)){
                    return true
                }
            }
            return false
        } catch (e: JSONException) {
            Log.e("CustomLauncher checkContents: ", e.toString())
            e.printStackTrace()
        } catch (ex: Exception) {
            Log.e("CustomLauncher checkContents: ", ex.toString())
            ex.printStackTrace()
        }
        return false
    }
    /**
     * デフォルト背景保存(JPG)
     */
    private fun saveDefaultImage(context: Context): Boolean {
        try {
            val filedir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            /** PORTRAIT */
            val bmp = BitmapFactory.decodeResource(context.resources, org.ycc.customlauncher.R.drawable.back4)
            var fos: FileOutputStream? = null
            fos = FileOutputStream(File(filedir, appConfig.back_image))
            bmp.compress(CompressFormat.JPEG, 100, fos)
            fos.close()
            /** LANDSCAPE */
            val bmp_l = BitmapFactory.decodeResource(context.resources, org.ycc.customlauncher.R.drawable.back4_l)
            val filename = appConfig.back_image.replace(".", "_l.")
            fos = FileOutputStream(File(filedir, filename))
            bmp_l.compress(CompressFormat.JPEG, 100, fos)
            fos.close()
            return true
        } catch (ex: Exception) {
            Log.e("CustomLauncher saveDefaultImage: ", ex.toString())
            ex.printStackTrace()
        }
        return false
    }
    /**
     * JSONデータ保存
     */
    fun saveContents(context: Context, newText: String): Boolean {
        try {
            /** 設定ファイル /data/data/org.ycc.customlauncher/files/custom.json */
            val filename = context.getString(org.ycc.customlauncher.R.string.custom_file)
            // Todo File Directory
            val filedir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            /** JSON */
            val jsonObject = JSONObject(newText)
            /** AppConfig */
            val json = jsonObject.getJSONObject("AppConfig")
            /** welcome */
            appConfig.welcome = json.getString("welcome")
            val jsonArray = json.getJSONArray("app")
            /** group_logo */
            appConfig.group_logo = json.getString("group_logo")
            /** back_image */
            appConfig.back_image = json.getString("back_image")
            /** help_1 */
            appConfig.help_1 = json.getString("help_1")
            /** help_2 */
            appConfig.help_2 = json.getString("help_2")
            /** help_3 */
            appConfig.help_3 = json.getString("help_3")
            /** help_4 */
            appConfig.help_4 = json.getString("help_4")
            /** help_5 */
            appConfig.help_5 = json.getString("help_5")
//            Log.d("CustomLauncher", "JsonArray OK")
            for (i in 0 until jsonArray.length()) {
                val app = App()
                val jsonData = jsonArray.getJSONObject(i)
//                Log.d("CustomLauncher", "$i : ${jsonData.getString("name")}")
                /** name検索用 */
                app.name = jsonData.getString("name")
                /** label表示用 */
                app.label = jsonData.getString("label")
                /** shortcut */
                app.shortcut = jsonData.getString("shortcut")
                /** icon */
                app.icon = jsonData.getString("icon")
                appConfig.app = appConfig.app + app
            }
            File(filedir, filename).writer().use {
                it.write(newText)
            }
            return true
        } catch (e: JSONException) {
            Log.e("CustomLauncher saveContents: ", e.toString())
            e.printStackTrace()
        } catch (ex: Exception) {
            Log.e("CustomLauncher saveContents: ", ex.toString())
            ex.printStackTrace()
        }
        return false
    }
    /**
     * 背景画像絶対パス取得
     */
    fun getBackGroundFilename(context: Context, orientation: Int): String {
        Log.d("CustomLauncher", "CustomList: getBackGroundFilename start")
        var filename = ""
        try {
            val filedir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

            if(orientation == Configuration.ORIENTATION_PORTRAIT){
                /** ORIENTATION_PORTRAIT */
                filename = appConfig.back_image
            }
            else {
                /** ORIENTATION_LANDSCAPE */
                filename = appConfig.back_image.replace(".", "_l.")
            }
            Log.d("CustomLauncher", "CustomList: getBackGroundDrawable ${filename}")
            val readFile = File(filedir, filename)
            return readFile.absolutePath
        }
        catch (e: Exception) {
            Log.e("CustomLauncher", e.toString())
            e.printStackTrace()
        }
        return ""
    }
    /**
     * 背景画像存在確認
     */
    fun pictureFileExists(context: Context, portFilename: String, orientation: Int): Boolean {
        Log.d("CustomLauncher", "CustomList: pictureFileExists portFilename: ${portFilename}")
        try {
            val filedir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            if(orientation==Configuration.ORIENTATION_PORTRAIT){
                /** ORIENTATION_PORTRAIT */
                val readFile = File(filedir, portFilename)
                if(readFile.exists()==false) {
                    Log.d("CustomLauncher", "CustomList: pictureFileExists PORTRAIT No Exists")
                    return false
                }
                return true
            }
            /** ORIENTATION_LANDSCAPE */
            val filename = portFilename.replace(".", "_l.")
            val readFile_l = File(filedir, filename)
            if(readFile_l.exists()==false) {
                Log.d("CustomLauncher", "CustomList: pictureFileExists LANDSCAPE No Exists")
                return false
            }
            return true
        }
        catch (e: Exception) {
            Log.e("CustomLauncher", e.toString())
            e.printStackTrace()
        }
        return false
    }
    /**
     * 外部ストレージ使用確認
     */
    fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }
}