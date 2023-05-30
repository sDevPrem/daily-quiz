package com.sdevprem.dailyquiz.ui.onbaording

import android.os.Bundle
import android.util.Patterns.EMAIL_ADDRESS
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
import com.sdevprem.dailyquiz.R
import com.sdevprem.dailyquiz.data.model.AuthUser
import com.sdevprem.dailyquiz.data.repository.UserRepository
import com.sdevprem.dailyquiz.data.util.Response
import com.sdevprem.dailyquiz.data.util.exception.SignupException
import com.sdevprem.dailyquiz.databinding.FragmentSignUpBinding
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
class SignUpFragment : Fragment(R.layout.fragment_sign_up){
    lateinit var binding : FragmentSignUpBinding
    private val viewModel : SignUpVM by viewModels()

    @Inject
    lateinit var userRepository: UserRepository
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return FragmentSignUpBinding.inflate(
            inflater,
            container,
            false
        ).apply {
            binding = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSignUp.setOnClickListener{
            registerUser()
        }
        binding.btnLogIn.setOnClickListener {
            findNavController().navigate(
                SignUpFragmentDirections.actionSignUpFragmentToLoginFragment()
            )
        }

        launchInLifecycle {
            viewModel.signUpState.collectLatest {
                when(it){
                    is Response.Loading -> {
                        binding.btnLogIn.isEnabled = false
                        binding.btnSignUp.isEnabled = false
                        binding.progressBar.isVisible = true
                    }
                    is Response.Error -> handleError(it.e)
                    is Response.Success -> {
                        findNavController().navigate(
                            SignUpFragmentDirections.actionSignUpFragmentToHomeFragment()
                        )
                    }
                    else -> {
                        binding.progressBar.isVisible = false
                    }
                }
            }
        }

    }

    private fun handleError(e : Throwable?){
        binding.btnLogIn.isEnabled = true
        binding.btnSignUp.isEnabled = true
        binding.progressBar.isVisible = false
        when(e){
            is SignupException.EmailAlreadyInUseException -> toast("The email is already in use. Please use another email.")
            is SignupException.WeakPasswordException -> toast("Invalid Password. Please use different password.")
            is SignupException.InvalidCredentialException -> toast("Enter valid data.")
            else -> toast("Something went wrong. Please try again.")
        }
    }

    private fun registerUser(){
        if(
            binding.email.text.isNullOrBlank() ||
            !EMAIL_ADDRESS.matcher(binding.email.text.toString()).matches() ||
            binding.password.text.isNullOrBlank() ||
            binding.confirmPassword.text.isNullOrBlank() ||
            binding.password.text.toString() != binding.confirmPassword.text.toString()
        ){
            Toast.makeText(requireContext(), "Enter valid data", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.registerUser(
            binding.email.text.toString(),
            binding.confirmPassword.text.toString()
        )
    }
}

@HiltViewModel
class SignUpVM @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    private val _signUpState = MutableStateFlow<Response<AuthUser>>(Response.Idle)
    val signUpState: StateFlow<Response<AuthUser>> = _signUpState

    fun registerUser(email: String, pass: String) {
        userRepository
            .signUp(AuthUser(null, email, pass))
            .onStart {
                emit(Response.Loading)
            }.onEach {
                _signUpState.value = it
            }.launchIn(viewModelScope)
    }
}