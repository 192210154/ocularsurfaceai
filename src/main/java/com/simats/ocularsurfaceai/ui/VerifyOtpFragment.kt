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
import com.simats.ocularsurfaceai.databinding.FragmentVerifyOtpBinding
import kotlinx.coroutines.launch

class VerifyOtpFragment : Fragment() {

    private var _binding: FragmentVerifyOtpBinding? = null
    private val binding get() = _binding!!
    
    private val args: VerifyOtpFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerifyOtpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        
        val email = args.email
        binding.tvInstructions.text = "Enter the 6-digit code sent to $email."

        binding.btnVerify.setOnClickListener {
            val otp = binding.etOtp.text.toString().trim()

            if (otp.isEmpty() || otp.length < 6) {
                Toast.makeText(requireContext(), "Please enter the 6-digit OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnVerify.isEnabled = false
            binding.btnVerify.text = "Verifying..."

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.api.verifyOtp(email = email, otp = otp)

                    if (response.isSuccessful && response.body() != null) {
                        val apiResponse = response.body()!!

                        if (!apiResponse.error && apiResponse.reset_token != null) {
                            Toast.makeText(requireContext(), apiResponse.message, Toast.LENGTH_SHORT).show()
                            val action = VerifyOtpFragmentDirections.actionVerifyToReset(apiResponse.reset_token)
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
                    binding.btnVerify.isEnabled = true
                    binding.btnVerify.text = "Verify OTP"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
