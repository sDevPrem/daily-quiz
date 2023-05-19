package com.sdevprem.dailyquiz.ui.onbaording

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
import com.sdevprem.dailyquiz.data.model.User
import com.sdevprem.dailyquiz.data.repository.UserRepository
import com.sdevprem.dailyquiz.data.util.Response
import com.sdevprem.dailyquiz.data.util.exception.LoginException
import com.sdevprem.dailyquiz.databinding.FragmentLoginBinding
import com.sdevprem.dailyquiz.ui.LoginFragmentDirections
import com.sdevprem.dailyquiz.uitls.launchInLifecycle
import com.sdevprem.dailyquiz.uitls.toast
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
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
        binding.btnLogin.setOnClickListener{
            logIn()
        }

        launchInLifecycle {
            viewModel.loginState.collectLatest {
                when(it){
                    is Response.Success -> {
                        findNavController().navigate(
                            LoginFragmentDirections.actionLoginFragmentToHomeFragment()
                        )
                    }
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

    private fun handleError(e : Throwable?){
        binding.btnLogin.isEnabled = true
        binding.btnSignUp.isEnabled = true
        binding.progressBar.isVisible = false
        when(e){
            is LoginException.InvalidCredentialException -> toast(e.message.toString())
            else -> toast("Something went wrong. Please try again.")
        }
    }


    private fun logIn(){
        if(
            binding.email.text.isNullOrBlank() ||
            !Patterns.EMAIL_ADDRESS.matcher(binding.email.text.toString()).matches() ||
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
}

@HiltViewModel
class LoginVM @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel(){
    private val _loginState = MutableStateFlow<Response<User>>(Response.Idle)
    val loginState : StateFlow<Response<User>> = _loginState

    fun login(email : String, pass : String){
        userRepository
            .login(User(email, pass))
            .onStart {
                emit(Response.Loading)
            }.onEach {
                _loginState.value = it
            }.launchIn(viewModelScope)
    }
}