package org.ycc.customlauncher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.lang.Exception

/**
 * CustomLauncherインストール済みアプリケーションクラス
 *  Edit masami
 *  2021.12.08
 */
class AppInfoList {
    /** AppConfig */
    var appConfig = AppConfig()
    /** AppInfo */
    var appList: List<AppInfo> = listOf<AppInfo>()
    /** DefaultIcon */
    var defaultIcon: Drawable? = null
    /** StringContents */
    var contents: String = ""
    /**
     * デフォルトデータの取得
     */
    fun setDefault(context: Context) {
        try {
            /** アセット呼び出し String */
//            val assetManager = Resources.getSystem().assets
            val assetManager = context.resources.assets
            val inputStream = assetManager.open(context.getString(R.string.assets_file))
//            Log.d("CustomLauncher", "Load OK")
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
//            Log.d("CustomLauncher", "bufferedReader OK")
            contents = bufferedReader.readText()
//            Log.d("CustomLauncher", "String OK")
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
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e1: FileNotFoundException) {
            Log.e("CustomLauncher", e1.toString())
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
    /**
     * アプリデータの取得
     */
    fun setAppInfo(context: Context) {
        Log.d("CustomLauncher", "setAppInfo START")
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN)
                .also { it.addCategory(Intent.CATEGORY_LAUNCHER) }
        /** パッケージ情報の取得 */
        Log.d("CustomLauncher", "context.pakagename : ${context.packageName}")
        var activityList: List<AppInfo> = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
                .asSequence()
                .mapNotNull { it.activityInfo }
                .filter { it.packageName != context.packageName }
                .map {
                    AppInfo(
                            it.loadIcon(pm) ?: getDefaultIcon(context),
                            it.loadLabel(pm).toString(),
                            ComponentName(it.packageName, it.name)
                    )
                }
                .sortedBy { it.label }
                .toList()
        Log.d("CustomLauncher", "setAppInfo activityList Length : ${activityList.size}")
        /** ランチャーリストへセット */
        for (i in 0 until appConfig.app.size) {
            val app = appConfig.app.get(i)
            for (l in 0 until activityList.size) {
                val activity = activityList.get(l)
                if(activity.label == app.name) {
                    /** リストへ追加 label->name */
                    activity.label = app.label
                    appList = appList + activity
                    break
                }
            }
        }
        /** Todo インストールされたアプリ一覧を出す場合 */
        /*
        for (i in 0 until activityList.size) {
            val activity = activityList.get(i)
            Log.d("CustomLauncher", "$i : ${activity.label}")
        }
         */
    }
    /**
     * デフォルトアイコンの取得
     */
    fun getDefaultIcon(context: Context): Drawable {
        return defaultIcon
                ?: AppCompatResources.getDrawable(context, R.mipmap.ic_launcher)
                        ?.also { defaultIcon = it }!!
    }
    /**
     * ランチャーアプリデータの取得
     */
    fun setLauncherList() {
        for (i in 0 until appList.size) {
            val app = appList.get(i)
            Log.d("CustomLauncher", "$i : ${app.label}")
        }
    }
}