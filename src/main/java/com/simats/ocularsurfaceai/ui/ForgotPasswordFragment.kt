package com.simats.ocularsurfaceai.ui

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.simats.ocularsurfaceai.api.RetrofitClient
import com.simats.ocularsurfaceai.databinding.FragmentForgotPasswordBinding
import kotlinx.coroutines.launch

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.btnSendResetLink.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(requireContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnSendResetLink.isEnabled = false
            binding.btnSendResetLink.text = "Sending..."

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.api.forgotPassword(email = email)

                    if (response.isSuccessful && response.body() != null) {
                        val apiResponse = response.body()!!

                        if (!apiResponse.error) {
                            Toast.makeText(requireContext(), apiResponse.message, Toast.LENGTH_LONG).show()
                            val action = ForgotPasswordFragmentDirections.actionForgotToVerify(email)
                            findNavController().navigate(action)
                        } else {
                            Toast.makeText(requireContext(), apiResponse.message, Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Server error. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    binding.btnSendResetLink.isEnabled = true
                    binding.btnSendResetLink.text = "Send Reset Link"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}