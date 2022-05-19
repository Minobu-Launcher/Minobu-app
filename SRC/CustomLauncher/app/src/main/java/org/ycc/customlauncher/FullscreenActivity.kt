package org.ycc.customlauncher

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView


/**
 * CustomLauncherメインアクティビティクラス
 *  Edit masami
 *  2021.12.08
 */
class FullscreenActivity : AppCompatActivity(), AppInfoListener {
    /** Welcome TextView */
    private lateinit var fullscreenContent: TextView
    /** Help TextView */
    private lateinit var helpContent: TextView
    /** Controls Layout */
    private lateinit var fullscreenContentControls: LinearLayout
    /** RecyclerView */
//    private final var recyclerView: RecyclerView = findViewById<RecyclerView>(R.id.recycler_view)
    /** GridLayoutManager */
    private var gridLayoutManager: GridLayoutManager? = null
    /** MainLayout */
    private lateinit var mainContent: FrameLayout

    /** TextView Tap Count */
    private var tapCount: Int = 0
    /** TextView Tap Count */
    private var editCount: Int = 0

    /** 背景画像パス */
    private var backImagePath: String = ""
    private var backImagePath_l: String = ""
    private var portFilename: String = ""

    /** Handler */
//    private val hideHandler = Handler()
    private val hideHandler = Handler(Looper.getMainLooper())

    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {
        Log.d("CustomLauncher", "Event: hidePart2Runnable")
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        /** Todo API-Level & ステータスバーを表示させるとステータスバーのタップイベントを拾ってしまう */
        fullscreenContent.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val showPart2Runnable = Runnable {
        Log.d("CustomLauncher", "Event: showPart2Runnable")
        // Delayed display of UI elements
        /** show init */
        fullscreenContent.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        supportActionBar?.show()
        fullscreenContentControls.visibility = View.VISIBLE
    }
    /** FullScreenFlag */
    private var isFullscreen: Boolean = false
    /** Hide */
    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val delayHideTouchListener = View.OnTouchListener { view, motionEvent ->
        Log.d("CustomLauncher", "Event: delayHideTouchListener")
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS)
            }
            MotionEvent.ACTION_UP -> view.performClick()
            else -> {
            }
        }
        false
    }

    /**
     * 設定ファイル編集画面 EditActivity コールバック
     */
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        // 呼び出し先のActivityを閉じた時に呼び出されるコールバック
        if(result.resultCode == Activity.RESULT_OK) {
            // RESULT_OK時の処理
            val intent = result.data
            val status = intent?.getStringExtra(EditActivity.EDIT_KEY)
            Log.d("CustomLauncher", "Event: editCheck result: ${status}")
            if(status == EditActivity.EDIT_NORMAL) {
                AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle(getString(R.string.edit_title))
                    .setMessage(getString(R.string.edit_message))
                    .setPositiveButton("OK", { dialog, which ->
                        /** Yesが押された時 */
                        Log.d("CustomLauncher", "Event: startForResult EditNarmal")
                    })
                    .show()
            }
            else if(status == EditActivity.EDIT_ERROR) {
                AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle(getString(R.string.error_title))
                    .setMessage(getString(R.string.error_message))
                    .setPositiveButton("OK", { dialog, which ->
                        /** Yesが押された時 */
                        Log.d("CustomLauncher", "Event: startForResult EditError")
                    })
                    .show()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("CustomLauncher", "Event: onCreate")
        super.onCreate(savedInstanceState)
        try{
            setContentView(R.layout.activity_fullscreen)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)

            isFullscreen = true

            // Set up the user interaction to manually show or hide the system UI.
            fullscreenContent = findViewById(R.id.fullscreen_content)
            fullscreenContent.setOnClickListener { homeCheck() }

            fullscreenContentControls = findViewById(R.id.fullscreen_content_controls)

            mainContent = findViewById(R.id.main_content)

            // Upon interacting with UI controls, delay any scheduled hide()
            // operations to prevent the jarring behavior of controls going away
            // while interacting with the UI.
//        findViewById<Button>(R.id.dummy_button).setOnTouchListener(delayHideTouchListener)

            /** HelpMessage */
            helpContent = findViewById(R.id.help)
            helpContent.setOnClickListener { editCheck() }

            /** RecyclerView */
            val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)

            if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
                /** ORIENTATION_PORTRAIT */
                gridLayoutManager = GridLayoutManager(
                        this,
                        PORTRAIT_COUNT,
                        RecyclerView.VERTICAL,
                        false
                )
//                mainContent.setBackgroundResource(R.drawable.back4)
            }
            else {
                /** ORIENTATION_LANDSCAPE */
                gridLayoutManager = GridLayoutManager(
                        this,
                        LANDSCAPE_COUNT,
                        RecyclerView.VERTICAL,
                        false
                )
//                mainContent.setBackgroundResource(R.drawable.back4_l)
            }
            recyclerView.layoutManager = gridLayoutManager

            /** Default Data */
            val appInfoList = AppInfoList()
            appInfoList.setDefault(this)

            /** アプリ内部ファイルを検索 */
            val customList = CustomList()
            /** CustomFile */
            if(customList.setCustomList(this, appInfoList)) {
                /** Read OK */
                Log.d("CustomLauncher", "CustomFile!!")
                appInfoList.appConfig = customList.appConfig.copy()
                appInfoList.contents = customList.contents
            }
            else {
                /** DefaultFile */
                Log.d("CustomLauncher", "DefaultFile!!")
                appInfoList.setDefault(this)
            }

            Log.d("CustomLauncher", "AppConfig Set OK")
            /** アプリ内部ファイルを検索 */
            appInfoList.setAppInfo(this)
            /** ランチャーアプリデータの取得 */
            appInfoList.setLauncherList()

            show()
            /** タイトル */
            fullscreenContent.setText(appInfoList.appConfig.welcome)
            /**  背景画像 */
            portFilename = customList.appConfig.back_image
            backImagePath = customList.getBackGroundFilename(this, Configuration.ORIENTATION_PORTRAIT)
            Log.d("CustomLauncher", "BackGround absolutePath PORTRAIT ${backImagePath}")
            backImagePath_l = customList.getBackGroundFilename(this, Configuration.ORIENTATION_LANDSCAPE)
            Log.d("CustomLauncher", "BackGround absolutePath LANDSCAPE ${backImagePath_l}")
            setBackImage(resources.configuration.orientation)
            /* 上記処理に変更
            val resId = getResourceId(appInfoList.appConfig.back_image, "drawable")
            if(resId != null) {
                /** Todo 変更 */
//                mainContent.setBackgroundResource(this.getDrawable(resId!))
            }
             */
            /** Todo Color */
            fullscreenContent.setTextColor(R.color.black)
            /** フッター */
            helpContent.setText(appInfoList.appConfig.help_1)
            helpContent.setTextColor(R.color.white)
            /** 各ボタン */
            recyclerView.adapter = AppAdapter(appInfoList.appList, this)

        }
        catch (e: Exception) {
            Log.e("CustomLauncher", e.toString())
        }
    }
    override fun onStart() {
        super.onStart()
        Log.d("CustomLauncher", "Event: onStart()")
    }

    /**
     * ExtAPP -> APP
     */
    override fun onResume() {
        Log.d("CustomLauncher", "Event: onResume()")
        super.onResume()
        setBackImage(resources.configuration.orientation)
        if(gridLayoutManager != null) {
            if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
                /** ORIENTATION_PORTRAIT */
                gridLayoutManager!!.spanCount = PORTRAIT_COUNT
//                mainContent.setBackgroundResource(R.drawable.back4)
            }
            else {
                /** ORIENTATION_LANDSCAPE */
                gridLayoutManager!!.spanCount = LANDSCAPE_COUNT
//                mainContent.setBackgroundResource(R.drawable.back4_l)

            }
            gridLayoutManager!!.requestLayout()
        }
    }
    override fun onRestart() {
        super.onRestart()
        Log.d("CustomLauncher", "Event: onRestart()")
    }

    override fun onPause() {
        super.onPause()
        Log.d("CustomLauncher", "Event: onPause()")
    }

    override fun onStop() {
        super.onStop()
        Log.d("CustomLauncher", "Event: onStop()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("CustomLauncher", "Event: onDestroy()")
    }
    override fun onPostCreate(savedInstanceState: Bundle?) {
        Log.d("CustomLauncher", "Event: onPostCreate")
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        /** show init */
//        delayedHide(100)
    }

    /**
     * ORIENTATION Change
     */
    override fun onConfigurationChanged(config: Configuration) {
        Log.d("CustomLauncher", "Event: onConfigurationChanged")
        super.onConfigurationChanged(config)
        setBackImage(resources.configuration.orientation)
        if(gridLayoutManager != null) {
            if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
                /** ORIENTATION_PORTRAIT */
                gridLayoutManager!!.spanCount = PORTRAIT_COUNT
//                mainContent.setBackgroundResource(R.drawable.back4)
            }
            else {
                /** ORIENTATION_LANDSCAPE */
                gridLayoutManager!!.spanCount = LANDSCAPE_COUNT
//                mainContent.setBackgroundResource(R.drawable.back4_l)
            }
            gridLayoutManager!!.requestLayout()
        }
    }
    /*
    fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig!!)
    }
     */
    /*
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
     */

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        Log.d("CustomLauncher", "Event: onCreateView")
        /*
        if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
            /** ORIENTATION_PORTRAIT */
            recyclerView.layoutManager = GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        }
        else {
            /** ORIENTATION_LANDSCAPE */
            recyclerView.layoutManager = GridLayoutManager(this, 4, RecyclerView.VERTICAL, false)
        }
        */
        return super.onCreateView(name, context, attrs)
    }

    /**
     * Button Tapped
     */
    override fun buttonTapped(appInfo: AppInfo) {
        Log.d("CustomLauncher", "Event: buttonTapped")
        // リストにタップされたアプリを実行
        appInfo.launch(this)
        tapCount = 0
        editCount = 0
    }

    /**
     * fullscreenContent TextViewタップイベント
     */
    private fun homeCheck() {
        Log.d("CustomLauncher", "Event: homeCheck")
        editCount = 0
        tapCount = tapCount + 1
        if(tapCount < UI_TAP_COUNT){
            return
        }
        tapCount = 0
        /** アプリ設定 */
        val intent = Intent()
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        startActivity(intent)
    }

    /**
     * helpContent TextViewタップイベント
     */
    private fun editCheck() {
        Log.d("CustomLauncher", "Event: editCheck")
        tapCount = 0
        editCount = editCount + 1
        // Todo Count 3?
        if(editCount < UI_TAP_COUNT){
            return
        }
        editCount = 0
        Log.d("CustomLauncher", "Event: editCheck OK")
        /** JSON EDIT Activity */
        try {
            val intent = Intent(application, EditActivity::class.java)
            // Activityを起動
            startForResult.launch(intent)
            /*
            startActivity(intent)
            */
        } catch (e: Exception) {
            Log.e("CustomLauncher", e.toString())
        }
    }

    /**
     * Resource Id get
     */
    /*
    private fun getResourceId(text: String, content: String): Int?{
        var resultId: Int? = null
        val res: Resources = resources
        try{
            resultId = res.getIdentifier(text, content, packageName)
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        return resultId
    }
     */

    /**
     * hide <-> show
     */
    private fun toggle() {
        if (isFullscreen) {
            hide()
        } else {
            show()
        }
    }

    /**
     * fullscreenContent Hide
     */
    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        fullscreenContentControls.visibility = View.GONE
        isFullscreen = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * fullscreenContent Show
     */
    private fun show() {
        // Show the system bar
        fullscreenContent.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        isFullscreen = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        Log.d("CustomLauncher", "Event: delayedHide")
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    /**
     * 背景画像の設定
     */
    private fun setBackImage(orientation: Int) {
        Log.d("CustomLauncher", "setBackImage: start ${orientation}")
        try {
            val customList = CustomList()
            /** 設定ファイル画像 /storage/self/primary/Android/data/org.ycc.customlauncher/files/Pictures */
            if(customList.pictureFileExists(this, portFilename, orientation)){
                /** ファイル存在 */
                if(orientation == Configuration.ORIENTATION_PORTRAIT){
                    val d = Drawable.createFromPath(backImagePath)
                    mainContent.setBackground(d)
                }
                else{
                    val d = Drawable.createFromPath(backImagePath_l)
                    mainContent.setBackground(d)
                }
                Log.d("CustomLauncher", "setBackImage: PORTRAIT/LANDSCAPE SET")
                return
            }
        }
        catch (e: Exception) {
            Log.e("CustomLauncher", e.toString())
            e.printStackTrace()
        }
        /** 設定ファイルで指定された画像が開けない場合標準の画像をセット */
        if(orientation == Configuration.ORIENTATION_PORTRAIT){
            /** ORIENTATION_PORTRAIT */
            mainContent.setBackgroundResource(R.drawable.back4)
            Log.d("CustomLauncher", "setBackImage: PORTRAIT DEFAULT SET")
        }
        else {
            /** ORIENTATION_LANDSCAPE */
            mainContent.setBackgroundResource(R.drawable.back4_l)
            Log.d("CustomLauncher", "setBackImage: LANDSCAPE DEFAULT SET")
        }
        return
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300

        /**
         * TapCount
         */
        private const val UI_TAP_COUNT = 7

        /**
         * PORTRAIT SpanCount（縦画面）
         */
        private const val PORTRAIT_COUNT = 3

        /**
         * LANDSCAPE SpanCount（横画面）
         */
        private const val LANDSCAPE_COUNT = 5

        /**
         * Extra Message
         */
        private const val EXTRA_MESSAGE = "org.ycc.customlauncher.MESSAGE"

    }
}