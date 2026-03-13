package com.simats.ocularsurfaceai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.simats.ocularsurfaceai.api.RetrofitClient
import com.simats.ocularsurfaceai.databinding.FragmentResetPasswordBinding
import kotlinx.coroutines.launch

class ResetPasswordFragment : Fragment() {

    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!
    
    private val args: ResetPasswordFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.btnReset.setOnClickListener {
            val newPassword = binding.etNewPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword.length < 6) {
                Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val token = args.resetToken

            binding.btnReset.isEnabled = false
            binding.btnReset.text = "Resetting..."

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.api.resetPassword(token = token, newPassword = newPassword)

                    if (response.isSuccessful && response.body() != null) {
                        val apiResponse = response.body()!!

                        if (!apiResponse.error) {
                            Toast.makeText(requireContext(), apiResponse.message, Toast.LENGTH_LONG).show()
                            val action = ResetPasswordFragmentDirections.actionResetToLogin()
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
                    binding.btnReset.isEnabled = true
                    binding.btnReset.text = "Reset Password"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
