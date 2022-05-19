package org.ycc.customlauncher

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Drawable
import android.content.Intent
import android.util.Log

/**
 * CustomLauncherアプリケーションデータクラス
 *  Edit masami
 *  2021.12.08
 */
data class AppInfo(
    val icon: Drawable? = null,
    var label: String = "",
    val componentName: ComponentName? = null){

    fun launch(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                it.addCategory(Intent.CATEGORY_LAUNCHER)
                it.component = componentName
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Log.e("CustomLauncher", e.toString())
        }
    }

}
