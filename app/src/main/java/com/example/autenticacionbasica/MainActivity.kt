package com.example.autenticacionbasica

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    
    private lateinit var biometricAuthManager: BiometricAuthManager
    private lateinit var statusTextView: TextView
    private lateinit var messageTextView: TextView
    private lateinit var authenticateButton: MaterialButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        // Setup window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize views
        statusTextView = findViewById(R.id.statusTextView)
        messageTextView = findViewById(R.id.messageTextView)
        authenticateButton = findViewById(R.id.authenticateButton)
        
        // Initialize biometric manager
        biometricAuthManager = BiometricAuthManager(this)
        
        // Check biometric availability on startup
        checkBiometricAvailability()
        
        // Setup button click listener
        authenticateButton.setOnClickListener {
            startBiometricAuthentication()
        }
    }
    
    /**
     * Check if biometric authentication is available on this device
     */
    private fun checkBiometricAvailability() {
        when (biometricAuthManager.isBiometricAvailable()) {
            BiometricAuthManager.BiometricAvailability.AVAILABLE -> {
                // Biometric is available, enable the button
                authenticateButton.isEnabled = true
                updateStatus(getString(R.string.auth_status_pending))
                showMessage("", Color.TRANSPARENT)
            }
            BiometricAuthManager.BiometricAvailability.NO_HARDWARE -> {
                authenticateButton.isEnabled = false
                updateStatus(getString(R.string.biometric_not_available))
                showMessage(getString(R.string.biometric_no_hardware), Color.RED)
            }
            BiometricAuthManager.BiometricAvailability.HW_UNAVAILABLE -> {
                authenticateButton.isEnabled = false
                updateStatus(getString(R.string.biometric_not_available))
                showMessage(getString(R.string.biometric_hw_unavailable), Color.RED)
            }
            BiometricAuthManager.BiometricAvailability.NONE_ENROLLED -> {
                authenticateButton.isEnabled = false
                updateStatus(getString(R.string.biometric_not_available))
                showMessage(getString(R.string.biometric_none_enrolled), Color.RED)
            }
            BiometricAuthManager.BiometricAvailability.SECURITY_UPDATE_REQUIRED -> {
                authenticateButton.isEnabled = false
                updateStatus(getString(R.string.biometric_not_available))
                showMessage(getString(R.string.biometric_security_update_required), Color.RED)
            }
            BiometricAuthManager.BiometricAvailability.UNSUPPORTED -> {
                authenticateButton.isEnabled = false
                updateStatus(getString(R.string.biometric_not_available))
                showMessage(getString(R.string.biometric_unsupported), Color.RED)
            }
            BiometricAuthManager.BiometricAvailability.STATUS_UNKNOWN -> {
                authenticateButton.isEnabled = false
                updateStatus(getString(R.string.biometric_not_available))
                showMessage(getString(R.string.biometric_status_unknown), Color.RED)
            }
        }
    }
    
    /**
     * Start the biometric authentication process
     */
    private fun startBiometricAuthentication() {
        biometricAuthManager.authenticate(
            title = getString(R.string.biometric_title),
            subtitle = getString(R.string.biometric_subtitle),
            description = getString(R.string.biometric_description),
            negativeButtonText = getString(R.string.biometric_negative_button),
            callback = object : BiometricAuthManager.AuthenticationCallback {
                override fun onAuthenticationSuccess() {
                    updateStatus(getString(R.string.auth_success))
                    showMessage(getString(R.string.auth_success_message), Color.parseColor("#4CAF50"))
                }
                
                override fun onAuthenticationError(errorCode: Int, errorMessage: String) {
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            updateStatus(getString(R.string.auth_cancelled))
                            showMessage(errorMessage, Color.parseColor("#FF9800"))
                        }
                        BiometricPrompt.ERROR_LOCKOUT,
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                            updateStatus(getString(R.string.auth_error))
                            showMessage(errorMessage, Color.RED)
                        }
                        else -> {
                            updateStatus(getString(R.string.auth_error))
                            showMessage(errorMessage, Color.RED)
                        }
                    }
                }
                
                override fun onAuthenticationFailed() {
                    updateStatus(getString(R.string.auth_failed))
                    showMessage(getString(R.string.auth_failed_message), Color.parseColor("#FF5722"))
                }
            }
        )
    }
    
    /**
     * Update the status TextView
     */
    private fun updateStatus(status: String) {
        statusTextView.text = status
    }
    
    /**
     * Show message with specified color
     */
    private fun showMessage(message: String, color: Int) {
        if (message.isEmpty()) {
            messageTextView.visibility = View.GONE
        } else {
            messageTextView.visibility = View.VISIBLE
            messageTextView.text = message
            messageTextView.setTextColor(color)
        }
    }
}