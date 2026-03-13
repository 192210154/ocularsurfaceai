package com.simats.ocularsurfaceai.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.simats.ocularsurfaceai.MainActivity
import com.simats.ocularsurfaceai.api.RetrofitClient
import com.simats.ocularsurfaceai.data.SessionManager
import com.simats.ocularsurfaceai.databinding.FragmentDeleteAccountBinding
import kotlinx.coroutines.launch

class DeleteAccountFragment : Fragment() {

    private var _binding: FragmentDeleteAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeleteAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = SessionManager(requireContext())
        val userEmail = session.getEmail()


        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnConfirmDelete.setOnClickListener {
            if (userEmail.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Error: User email not found.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Disable buttons to prevent double-clicking
            binding.btnConfirmDelete.isEnabled = false
            binding.btnConfirmDelete.text = "Deleting..."
            binding.btnCancel.isEnabled = false

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.api.deleteAccount(email = userEmail)

                    if (response.isSuccessful && response.body() != null) {
                        val apiResponse = response.body()!!

                        if (!apiResponse.error) {
                            // Success! Account is deleted in MySQL.
                            Toast.makeText(requireContext(), apiResponse.message, Toast.LENGTH_LONG).show()

                            // Clear the local session data
                            session.clear()

                            // Kick the user back out to the splash/login screen
                            val intent = Intent(requireContext(), MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            requireActivity().finish()
                        } else {
                            Toast.makeText(requireContext(), apiResponse.message, Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Server error. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    // Re-enable in case of failure
                    binding.btnConfirmDelete.isEnabled = true
                    binding.btnConfirmDelete.text = "Permanently Delete Account"
                    binding.btnCancel.isEnabled = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}