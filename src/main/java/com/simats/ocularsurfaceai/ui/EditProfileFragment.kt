package com.simats.ocularsurfaceai.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.simats.ocularsurfaceai.R
import com.simats.ocularsurfaceai.api.RetrofitClient
import com.simats.ocularsurfaceai.data.SessionManager
import com.simats.ocularsurfaceai.databinding.FragmentEditProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private lateinit var binding: FragmentEditProfileBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentEditProfileBinding.bind(view)

        val session = SessionManager(requireContext())

        binding.etName.setText(session.getName())
        binding.etEmail.setText(session.getEmail())

        binding.btnSave.setOnClickListener {

            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        RetrofitClient.api.updateProfile(
                            session.getUserId(),
                            name,
                            email,
                            "" // Password not handled in this redesigned screen
                        )
                    }

                    if (!response.error) {
                        Toast.makeText(requireContext(), "Profile Updated", Toast.LENGTH_SHORT).show()
                        // Update local session
                        session.saveLogin(
                            session.getUserId(),
                            name,
                            email,
                            session.getToken() ?: ""
                        )
                        findNavController().navigateUp()
                    } else {
                        Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                    }

                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Server Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}