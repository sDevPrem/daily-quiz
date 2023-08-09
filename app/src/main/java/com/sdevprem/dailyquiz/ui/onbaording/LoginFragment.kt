package com.sdevprem.dailyquiz.ui.onbaording

import android.content.DialogInterface
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sdevprem.dailyquiz.AuthNavDirections
import com.sdevprem.dailyquiz.data.model.AuthUser
import com.sdevprem.dailyquiz.data.repository.UserRepository
import com.sdevprem.dailyquiz.data.util.Response
import com.sdevprem.dailyquiz.data.util.exception.LoginException
import com.sdevprem.dailyquiz.data.util.exchangeResponse
import com.sdevprem.dailyquiz.databinding.FragmentLoginBinding
import com.sdevprem.dailyquiz.uitls.launchInLifecycle
import com.sdevprem.dailyquiz.uitls.toast
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment(){
    private lateinit var binding : FragmentLoginBinding
    private val viewModel : LoginVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentLoginBinding
            .inflate(
                inflater,
                container,
                false
            ).apply { binding = this }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSignUp.setOnClickListener {
            findNavController().navigate(
                LoginFragmentDirections.actionLoginFragmentToSignUpFragment()
            )
        }
        binding.btnLogin.setOnClickListener { logIn() }

        binding.btnForgotPass.setOnClickListener {
            if (binding.email.text.toString().isEmail())
                viewModel.requestPasswordReset(binding.email.text.toString())
            else toast("Enter valid email")
        }

        launchInLifecycle {
            viewModel.loginState.collectLatest {
                when (it) {
                    is Response.Success -> handleSuccessResponse(it.data)
                    is Response.Loading -> {
                        binding.btnSignUp.isEnabled = false
                        binding.btnLogin.isEnabled = false
                        binding.progressBar.isVisible = true
                    }

                    is Response.Error -> handleError(it.e)
                    else -> {}
                }
            }
        }
    }

    private fun handleSuccessResponse(data: LoginScreenRequest) = when (data) {
        is LoginScreenRequest.PasswordResetRequest -> {
            binding.progressBar.isVisible = false
            binding.btnLogin.isEnabled = true
            showPasswordResetEmailSuccessSentDialog()
        }

        is LoginScreenRequest.LoginRequest -> findNavController().navigate(
            AuthNavDirections.actionAuthNavToHomeFragment()
        )
    }

    private fun handleError(e: Throwable?) {
        binding.btnLogin.isEnabled = true
        binding.btnSignUp.isEnabled = true
        binding.progressBar.isVisible = false
        when (e) {
            is LoginException.EmailNotVerifiedException -> showEmailVerificationDialog()
            is LoginException -> toast(e.message.toString())
            else -> toast("Something went wrong. Please try again.")
        }
    }


    private fun logIn(){
        if(
            binding.email.text.toString().isEmail().not() ||
            binding.password.text.isNullOrBlank()
        ){
            Toast.makeText(requireContext(), "Enter valid data", Toast.LENGTH_SHORT).show()
            return
        }

        launchInLifecycle {
            viewModel.login(
                binding.email.text.toString(),
                binding.password.text.toString()
            )
        }
    }

    private fun showEmailVerificationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Email Verification Needed")
            .setMessage("A link was send to your email previously. Please verify to log in.")
            .setPositiveButton("Verify Now") { _: DialogInterface?, _: Int ->
                val intent =
                    requireContext().packageManager.getLaunchIntentForPackage("com.google.android.gm")
                startActivity(intent)
                toast("Check in Spam Folder if not found")
            }.show()
    }

    private fun showPasswordResetEmailSuccessSentDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Password reset email sent")
            .setMessage("An email is sent by using which you can reset your password. Please check your inbox.")
            .setPositiveButton("Check now") { _, _ ->
                val intent =
                    requireContext().packageManager.getLaunchIntentForPackage("com.google.android.gm")
                startActivity(intent)
                toast("Check in Spam Folder if not found")
            }
            .setNegativeButton("Later") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun String.isEmail() = isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

}

@HiltViewModel
class LoginVM @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _loginState = MutableStateFlow<Response<LoginScreenRequest>>(Response.Idle)
    val loginState: StateFlow<Response<LoginScreenRequest>> = _loginState

    fun login(email: String, pass: String) {
        userRepository
            .login(AuthUser(null, email, pass))
            .onEach { response ->
                _loginState.value = response.exchangeResponse {
                    LoginScreenRequest.LoginRequest(it)
                }
            }.launchIn(viewModelScope)
    }

    fun requestPasswordReset(email: String) {
        userRepository
            .sendResetPasswordEmail(email)
            .onEach { response ->
                _loginState.value = response.exchangeResponse {
                    LoginScreenRequest.PasswordResetRequest
                }
            }.launchIn(viewModelScope)
    }
}

sealed interface LoginScreenRequest {
    object PasswordResetRequest : LoginScreenRequest

    class LoginRequest(val user: AuthUser) : LoginScreenRequest
}