package com.example.autenticacionbasica

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
        
        // Configurar edge-to-edge para manejar correctamente los insets del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Views
        statusTextView = findViewById(R.id.statusTextView)
        messageTextView = findViewById(R.id.messageTextView)
        authenticateButton = findViewById(R.id.authenticateButton)
        
        // biometric manager
        biometricAuthManager = BiometricAuthManager(this)

        checkBiometricAvailability()

        authenticateButton.setOnClickListener {
            startBiometricAuthentication()
        }
    }
    
    /**
     * Comprobar la disponibilidad de la autenticación biométrica y actualizar la UI en consecuencia
     */
    private fun checkBiometricAvailability() {
        when (biometricAuthManager.isBiometricAvailable()) {
            BiometricAuthManager.BiometricAvailability.AVAILABLE -> {
                // Biometric is available, enable the button
                authenticateButton.isEnabled = true
                updateStatus(getString(R.string.auth_status_pending))
                clearMessage()
            }
            BiometricAuthManager.BiometricAvailability.NO_HARDWARE -> {
                authenticateButton.isEnabled = false
                updateStatus(getString(R.string.biometric_not_available))
                showMessage(getString(R.string.biometric_no_hardware), getColor(R.color.auth_error))
            }
            BiometricAuthManager.BiometricAvailability.HW_UNAVAILABLE -> {
                authenticateButton.isEnabled = false
                updateStatus(getString(R.string.biometric_not_available))
                showMessage(getString(R.string.biometric_hw_unavailable), getColor(R.color.auth_error))
            }
            BiometricAuthManager.BiometricAvailability.NONE_ENROLLED -> {
                authenticateButton.isEnabled = false
                updateStatus(getString(R.string.biometric_not_available))
                showMessage(getString(R.string.biometric_none_enrolled), getColor(R.color.auth_error))
            }
            BiometricAuthManager.BiometricAvailability.SECURITY_UPDATE_REQUIRED -> {
                authenticateButton.isEnabled = false
                updateStatus(getString(R.string.biometric_not_available))
                showMessage(getString(R.string.biometric_security_update_required), getColor(R.color.auth_error))
            }
            BiometricAuthManager.BiometricAvailability.UNSUPPORTED -> {
                authenticateButton.isEnabled = false
                updateStatus(getString(R.string.biometric_not_available))
                showMessage(getString(R.string.biometric_unsupported), getColor(R.color.auth_error))
            }
            BiometricAuthManager.BiometricAvailability.STATUS_UNKNOWN -> {
                authenticateButton.isEnabled = false
                updateStatus(getString(R.string.biometric_not_available))
                showMessage(getString(R.string.biometric_status_unknown), getColor(R.color.auth_error))
            }
        }
    }
    
    /**
     * Empiezar el proceso de autenticación biométrica mostrando el prompt correspondiente
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
                    showMessage(getString(R.string.auth_success_message), getColor(R.color.auth_success))
                }
                
                override fun onAuthenticationError(errorCode: Int, errorMessage: String) {
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            updateStatus(getString(R.string.auth_cancelled))
                            showMessage(errorMessage, getColor(R.color.auth_warning))
                        }
                        BiometricPrompt.ERROR_LOCKOUT,
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                            updateStatus(getString(R.string.auth_error))
                            showMessage(errorMessage, getColor(R.color.auth_error))
                        }
                        else -> {
                            updateStatus(getString(R.string.auth_error))
                            showMessage(errorMessage, getColor(R.color.auth_error))
                        }
                    }
                }
                
                override fun onAuthenticationFailed() {
                    updateStatus(getString(R.string.auth_failed))
                    showMessage(getString(R.string.auth_failed_message), getColor(R.color.auth_failed))
                }
            }
        )
    }
    
    /**
     * Actualizar el TextView de estado con el mensaje proporcionado
     */
    private fun updateStatus(status: String) {
        statusTextView.text = status
    }
    
    /**
     * Limpiar el mensaje de error o éxito y ocultar el TextView correspondiente
     */
    private fun clearMessage() {
        messageTextView.visibility = View.GONE
        messageTextView.text = ""
    }
    
    /**
     * Mostrar un mensaje de error o éxito con el color especificado y hacer visible el TextView correspondiente
     */
    private fun showMessage(message: String, color: Int) {
        messageTextView.visibility = View.VISIBLE
        messageTextView.text = message
        messageTextView.setTextColor(color)
    }
}