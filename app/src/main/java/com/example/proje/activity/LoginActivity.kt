package com.example.proje.activity


import android.Manifest
import android.app.AppOpsManager
import android.app.KeyguardManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.proje.R
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    lateinit var biometricPrompt : BiometricPrompt
    private val TAG = "UYGULAMA:"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        val biometricManager = BiometricManager.from(this)

        if(!checkForBiometrics()){

            loginButton.setOnClickListener {
                var intent = Intent(applicationContext, DashboardActivity::class.java)
                finish()
                startActivity(intent)
            }
        }


        biometricPrompt = createBiometricPrompt()
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Login")
            .setDescription("Giriş için parmak izinizi kullanın.")
            .setNegativeButtonText("çıkış")
            .build()

        loginButton.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }


        //biometricPrompt = createBiometricPrompt()
    }

    override fun onResume() {
        super.onResume()
        if (!getGrantStatus()) {
          //  Thread.sleep(3000)
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }
    }

    private fun checkForBiometrics() : Boolean{
        Log.d(TAG, "checkForBiometrics started")
        var canAuthenticate = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT < 29) {
                val keyguardManager : KeyguardManager = applicationContext.getSystemService(
                    KEYGUARD_SERVICE
                ) as KeyguardManager
                val packageManager : PackageManager   = applicationContext.packageManager
                if(!packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
                    Log.w(TAG, "checkForBiometrics, Fingerprint Sensor not supported")
                    canAuthenticate = false
                }
                if (!keyguardManager.isKeyguardSecure) {
                    Log.w(TAG, "checkForBiometrics, Lock screen security not enabled in Settings")
                    canAuthenticate = false
                }
            } else {
                val biometricManager : BiometricManager = this.getSystemService(BiometricManager::class.java)
                if(biometricManager.canAuthenticate() != BiometricManager.BIOMETRIC_SUCCESS){
                    Log.w(TAG, "checkForBiometrics, biometrics not supported")
                    canAuthenticate = false
                }
            }
        }else{
            canAuthenticate = false
        }
        Log.d(TAG, "checkForBiometrics ended, canAuthenticate=$canAuthenticate ")
        return canAuthenticate
    }

    private fun createBiometricPrompt(): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(this)

            val callback = object : BiometricPrompt.AuthenticationCallback() {

                //bir hata oldumu bu metot çalışçak.
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.d(TAG, "$errorCode :: $errString")

                }

                //auth gerçekleşmezse bu çalışacak
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.d(TAG, "Authentication failed for an unknown reason")
                }


                //auth gerçekleşirse
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.d(TAG, "Authentication was successful")
                    //başarılı olursa ana aktiviteye geçiş sağla. intent ile diğerine geçiş yap.
                    var intent = Intent(applicationContext, DashboardActivity::class.java)

                    finish()
                    startActivity(intent)
                }
            }
            return BiometricPrompt(this, executor, callback)


    }

    private fun getGrantStatus(): Boolean {
        val appOps = applicationContext
            .getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(), applicationContext.packageName
        )
        return if (mode == AppOpsManager.MODE_DEFAULT) {
            applicationContext.checkCallingOrSelfPermission(Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
        } else {
            mode == AppOpsManager.MODE_ALLOWED
        }
    }

}