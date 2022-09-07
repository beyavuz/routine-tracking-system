package com.example.proje.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.proje.R
import kotlinx.android.synthetic.main.activity_lock.*

class LockActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_v2)

        var intent = intent
        var packageName = intent.getStringExtra("packageName").orEmpty()
        var isim = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)) as String
        uygulama_icon.setImageDrawable(packageManager.getApplicationIcon(packageName))
       // textView2.text = "$isim uygulaması kilit altında."


        var button = findViewById<Button>(R.id.button_lock_acti)
        button.setOnClickListener {
            var login_intent =  Intent(applicationContext,LoginActivity::class.java)
            startActivity(login_intent)
        }

    }
}