package com.simats.ocularsurfaceai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.simats.ocularsurfaceai.api.RetrofitClient
import com.simats.ocularsurfaceai.data.SessionManager
import com.simats.ocularsurfaceai.databinding.FragmentChangePasswordBinding
import kotlinx.coroutines.launch

class ChangePasswordFragment : Fragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = SessionManager(requireContext())
        val userEmail = session.getEmail()


        binding.btnUpdate.setOnClickListener {
            val current = binding.etCurrentPassword.text.toString()
            val newPass = binding.etNewPassword.text.toString()
            val confirm = binding.etConfirmPassword.text.toString()

            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass != confirm) {
                Toast.makeText(requireContext(), "New passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnUpdate.isEnabled = false
            binding.btnUpdate.text = "Updating..."

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.api.changePassword(
                        email = userEmail,
                        currentPassword = current,
                        newPassword = newPass
                    )

                    if (response.isSuccessful && response.body() != null) {
                        val apiResponse = response.body()!!

                        if (!apiResponse.error) {
                            Toast.makeText(requireContext(), apiResponse.message, Toast.LENGTH_LONG).show()
                            findNavController().popBackStack()
                        } else {
                            Toast.makeText(requireContext(), apiResponse.message, Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Server error. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    binding.btnUpdate.isEnabled = true
                    binding.btnUpdate.text = "Update Password"
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
