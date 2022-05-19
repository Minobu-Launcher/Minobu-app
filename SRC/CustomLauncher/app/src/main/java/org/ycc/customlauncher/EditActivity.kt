package org.ycc.customlauncher

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

/**
 * CustomLauncher設定ファイル編集アクティビティクラス
 *  Edit masami
 *  2022.02.18
 */
class EditActivity : AppCompatActivity() {
    /** EditText */
    private var editText: EditText? = null
    /** OrgText */
    private var orgText: String = ""

    /**
     * onCreate
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("CustomLauncher", "EditActivity: onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        val frame: FrameLayout = findViewById<FrameLayout>(R.id.main_content)
        /** actionbar */
//        val toolbar: Toolbar = findViewById<Toolbar>(R.id.toolbar)
//        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        try {
            /* Todo 3Button Hidden
            frame.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
             */

            val intent = Intent(application, EditActivity::class.java)
            /** 編集ファイル */
            val customList = CustomList()
            orgText = loadCustomList()
            if (orgText == "") {
                /** 編集ファイルがない場合 */
                AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle(getString(R.string.error_title))
                    .setMessage(getString(R.string.undef_message))
                    .setPositiveButton("OK", { dialog, which ->
                        /** Yesが押された時 */
                        intent.putExtra(EditActivity.EDIT_KEY, EditActivity.EDIT_NO_EDIT)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    })
                    .show()
            }
            /** 設定ファイル内容表示 */
            editText = findViewById<EditText>(R.id.custom_list)
            editText!!.setText(orgText, TextView.BufferType.NORMAL)
        } catch (e: Exception) {
            Log.e("CustomLauncher", e.toString())
        }
    }

    /**
     * onOptionsItemSelected
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("CustomLauncher", "EditActivity: onOptionsItemSelected")
        /** パラメータチェック */
        if(item.itemId != android.R.id.home) {
            Log.d("CustomLauncher", "EditActivity: Paramater Error")
            return super.onOptionsItemSelected(item)
        }
        /** 編集内容をチェック */
        val contents = editText!!.getText().toString()
        if(checkOrgText(contents)) {
            /** 同じ内容 */
            Log.d("CustomLauncher", "EditActivity: No Edit")
            intent.putExtra(EditActivity.EDIT_KEY, EditActivity.EDIT_NO_EDIT)
            setResult(Activity.RESULT_OK, intent)
            finish()
            return super.onOptionsItemSelected(item)
        }
        Log.d("CustomLauncher", "EditActivity: Edit Data Select")
        AlertDialog.Builder(this)
            .setCancelable(false)
            .setTitle(getString(R.string.edit_title))
            .setMessage(getString(R.string.save_message))
            .setPositiveButton("YES", { dialog, which ->
                // TODO:Yesが押された時の挙動
                Log.d("CustomLauncher", "EditActivity: Select YES")
                /** 保存 */
                if (saveCustomList(contents)) {
                    intent.putExtra(EditActivity.EDIT_KEY, EditActivity.EDIT_NORMAL)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                } else {
                    /** 編集Error */
                    intent.putExtra(EditActivity.EDIT_KEY, EditActivity.EDIT_ERROR)
                    setResult(Activity.RESULT_OK, intent)
                    finish()
                }
            })
            .setNegativeButton("No", { dialog, which ->
                Log.d("CustomLauncher", "EditActivity: Select NO")
                // 保存しない
                intent.putExtra(EditActivity.EDIT_KEY, EditActivity.EDIT_NO_SAVE)
                setResult(Activity.RESULT_OK, intent)
                finish()
            })
            .show()
        return super.onOptionsItemSelected(item)
    }
    /**
     * Event
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("CustomLauncher", "EditActivity: onKeyDown")
        /** パラメータチェック */
        if(keyCode != KeyEvent.KEYCODE_BACK) {
            Log.d("CustomLauncher", "EditActivity: onKeyDow KEYCODE_OTHER")
            return super.onKeyDown(keyCode, event)
        }
        /** 編集内容をチェック */
        Log.d("CustomLauncher", "EditActivity: onKeyDow KEYCODE_BACK")
        val contents = editText!!.getText().toString()
        if(checkOrgText(contents)) {
            /** 同じ内容 */
            Log.d("CustomLauncher", "EditActivity: No Edit")
            intent.putExtra(EditActivity.EDIT_KEY, EditActivity.EDIT_NO_EDIT)
            setResult(Activity.RESULT_OK, intent)
            finish()
            return super.onKeyDown(keyCode, event)
        }
        Log.d("CustomLauncher", "EditActivity: Edit Data Select")
        return super.onKeyDown(keyCode, event)
    }
    override fun onResume() {
        super.onResume()
        Log.d("CustomLauncher", "EditActivity: onResume()")
    }
    override fun onRestart() {
        super.onRestart()
        Log.d("CustomLauncher", "EditActivity: onRestart()")
    }
    override fun onPause() {
        super.onPause()
        Log.d("CustomLauncher", "EditActivity: onPause()")
    }
    override fun onStop() {
        super.onStop()
        Log.d("CustomLauncher", "EditActivity: onStop()")
    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d("CustomLauncher", "EditActivity: onDestroy()")
    }
    override fun onPostCreate(savedInstanceState: Bundle?) {
        Log.d("CustomLauncher", "EditActivity: onPostCreate")
        super.onPostCreate(savedInstanceState)
    }
    /**
     * ORIENTATION Change
     */
    override fun onConfigurationChanged(config: Configuration) {
        Log.d("CustomLauncher", "EditActivity: onConfigurationChanged")
        super.onConfigurationChanged(config)
    }

    /**
     * LOAD
     */
    fun loadCustomList(): String {
        var contents = ""
        try {
            val customList = CustomList()
            /** CustomFile */
            if(customList.loadContents(this)) {
                /** Read OK */
                Log.d("CustomLauncher", "EditActivity: loadCustomList!!")
                contents = customList.contents
            }
        } catch (e: Exception) {
            Log.e("CustomLauncher", e.toString())
            e.printStackTrace()
        }
        return contents
    }

    /**
     * SAVE
     */
    fun saveCustomList(newText: String): Boolean {
        try {
            val customList = CustomList()
            /** JSON */
            if(!checkCustomList(newText)){
                throw Exception("checkCustomList Error")
            }
            /** CustomFile */
            if(customList.saveContents(this, newText)) {
                /** Save OK */
                Log.d("CustomLauncher", "EditActivity: saveCustomList!!")
                return true
            }

        } catch (e: Exception) {
            Log.e("CustomLauncher", e.toString())
            e.printStackTrace()
        }
        return false
    }

    /**
     * CHECK orgText
     */
    fun checkOrgText(newText: String): Boolean {
        try {
            if(orgText.equals(newText)) {
                return true
            }
        } catch (e: Exception) {
            Log.e("CustomLauncher", e.toString())
            e.printStackTrace()
        }
        return false
    }

    /**
     * CHECK JSON
     */
    fun checkCustomList(newText: String): Boolean {
        try {
            val customList = CustomList()
            return customList.checkContents(this, newText)
        } catch (e: Exception) {
            Log.e("CustomLauncher", e.toString())
            e.printStackTrace()
        }
        return false
    }

    companion object {

        /**
         * EDIT STATUS
         */
        const val EDIT_KEY = "edit_key"

        /**
         * EDIT STATUS NORMAL
         */
        const val EDIT_NORMAL = "1"

        /**
         * EDIT STATUS NO EDIT
         */
        const val EDIT_NO_EDIT = "0"

        /**
         * EDIT STATUS NO SAVE
         */
        const val EDIT_NO_SAVE = "2"

        /**
         * EDIT STATUS EDIT ERROR
         */
        const val EDIT_ERROR = "9"

        /**
         * Activity Intent
         */
        fun createIntent(context: Context): Intent {
            return Intent(context, EditActivity::class.java)
        }
    }

    /**
     * ActivityResultContract Extra
     */
    class GetString : ActivityResultContract<Unit, String?>() {
        override fun createIntent(context: Context, input: Unit?): Intent {
            return createIntent(context)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): String? {
            return if (resultCode == RESULT_OK) {
                intent?.getStringExtra("Result")
            } else {
                null
            }
        }
    }
}
