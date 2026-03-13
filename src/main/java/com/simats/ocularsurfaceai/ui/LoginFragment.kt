package com.simats.ocularsurfaceai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.simats.ocularsurfaceai.R
import com.simats.ocularsurfaceai.api.LoginRequest
import com.simats.ocularsurfaceai.api.RetrofitClient
import com.simats.ocularsurfaceai.data.SessionManager
import com.simats.ocularsurfaceai.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Enter email & password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val loginRequest = LoginRequest(email, password)
            
            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.api.login(loginRequest) // Use RetrofitClient.api
                    if (response.token.isNotEmpty()) {
                        val session = SessionManager(requireContext())
                        session.saveLogin(
                            userId = response.user.id,
                            name = response.user.name,
                            email = response.user.email,
                            token = response.token
                        )

                        Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_login_to_home)
                    } else {
                        Toast.makeText(requireContext(), "Login failed: No token received", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    if (e is retrofit2.HttpException) {
                        Toast.makeText(requireContext(), "Login failed: Incorrect credentials", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.tvCreateAccount.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_signup)
        }

        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.forgotPasswordFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}