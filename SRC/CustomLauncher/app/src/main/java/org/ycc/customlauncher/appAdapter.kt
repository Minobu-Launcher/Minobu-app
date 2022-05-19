package org.ycc.customlauncher

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Interface
 */
interface AppInfoListener {
    fun buttonTapped(appInfo: AppInfo)
}

/**
 * CustomLauncherRecyclerViewアダプタクラス
 *  Edit masami
 *  2021.12.15
 */
class AppAdapter(private val appList: List<AppInfo>, private val listener: AppInfoListener): RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.image_view)
        val name: TextView = view.findViewById(R.id.text_view)

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        Log.d("CustomLauncher", "AppAdapter Event: onCreateViewHolder")
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.list_item, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        Log.d("CustomLauncher", "AppAdapter Event: onBindViewHolder")
        val appInfo = appList[position]
        viewHolder.image.setImageDrawable(appInfo.icon)
        viewHolder.name.text = appInfo.label
        viewHolder.image.setOnClickListener {
            listener.buttonTapped(appInfo)
        }
    }

    override fun getItemCount() = appList.size
}
