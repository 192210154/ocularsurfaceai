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
import com.simats.ocularsurfaceai.api.RetrofitClient
import com.simats.ocularsurfaceai.api.SignupRequest
import com.simats.ocularsurfaceai.data.SessionManager
import com.simats.ocularsurfaceai.databinding.FragmentSignupBinding
import kotlinx.coroutines.launch

class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSignup.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val gender = binding.etGender.text.toString().trim()
            val qualification = binding.etQualification.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirm = binding.etConfirm.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || gender.isEmpty() || 
                qualification.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!binding.cbDisclaimer.isChecked) {
                Toast.makeText(requireContext(), "Please accept the disclaimer", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(requireContext(), "Password must be 6+ characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirm) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val signupRequest = SignupRequest(name, email, password, gender, qualification)
            
            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.api.signup(signupRequest)
                    
                    if (response.token.isNotEmpty()) {
                        Toast.makeText(requireContext(), "Account Created!", Toast.LENGTH_SHORT).show()
                        
                        val session = SessionManager(requireContext())
                        session.saveLogin(
                            userId = response.user.id,
                            name = response.user.name,
                            email = response.user.email,
                            token = response.token
                        )
                        
                        // Navigate to Login after success and prevent back to Signup
                        findNavController().navigate(R.id.action_signup_to_login)
                    } else {
                        Toast.makeText(requireContext(), "Signup failed", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    if (e is retrofit2.HttpException && e.code() == 409) {
                        Toast.makeText(requireContext(), "Email already exists", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}