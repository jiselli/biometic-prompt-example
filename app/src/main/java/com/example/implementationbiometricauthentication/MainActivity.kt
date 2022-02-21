package com.example.implementationbiometricauthentication

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.implementationbiometricauthentication.databinding.ActivityMainBinding
import com.example.implementationbiometricauthentication.room.database.UserDatabase
import com.example.implementationbiometricauthentication.room.database.UserRepository
import com.example.implementationbiometricauthentication.room.entitie.User
import com.example.implementationbiometricauthentication.util.UtilAES
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var userRepository: UserRepository
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userDao = UserDatabase.getInstance(this).userDao()
        userRepository = UserRepository(userDao)

        setupOnClickListener()
    }

    private fun setupOnClickListener() {
        binding.apply {
            loginButton.setOnClickListener {
                refreshUser()
            }
            signInWithBiometricTextView.setOnClickListener {
                setupBiometric()
            }
        }
    }

    private fun refreshUser() {
        val email = binding.editTextEmail.text.toString()
        val password = binding.editTextPassword.text.toString()
        if (simulateLoginTrueCallback(email, password)) {
            Toast.makeText(applicationContext, "Login successfully!", Toast.LENGTH_SHORT).show()
            val user = UtilAES.encrypt(password)?.let { User(email, it) }
            user?.let { user ->
                lifecycleScope.launch(Dispatchers.IO) {
                    userRepository.removeAllUser()
                    userRepository.saveUser(user)
                }
            }
        }
    }


    private fun readingUser() {
        val refThis = this

        lifecycleScope.launch(Dispatchers.IO) {
            val user = userRepository.read()
            if (user.isNotEmpty()) {
                refThis.runOnUiThread {
                    binding.editTextEmail.setText(user[0].email)
                    UtilAES.decrypt(user[0].password)?.let {
                        binding.editTextPassword.setText(it)
                    }
                }
            }
        }
    }

    private fun simulateLoginTrueCallback(email: String, password: String): Boolean {
        if (email == "" || password == "") {
            return false
        }
//        Here would be the communication implementation with api
        return true
    }

    private fun setupBiometric() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    readingUser()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verify your identify")
            .setSubtitle("Authentication is required")
            .setDescription("Tap confirm to complete")
            .setNegativeButtonText("Use app password")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
            .build()

        if (biometricAvailable()) {
            biometricPrompt.authenticate(promptInfo)
        } else {
            binding.signInWithBiometricTextView.visibility = View.GONE
        }
    }

    private fun biometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Log.d("LOG", "App can authenticate using biometrics")
                true
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.d("LOG", "No biometric features available on this device.")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.d("LOG", "Biometric features are currently unavailable")
                false
            }
            else -> false
        }
    }

}