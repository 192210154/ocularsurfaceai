package com.simats.ocularsurfaceai.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.simats.ocularsurfaceai.MainActivity
import com.simats.ocularsurfaceai.R
import com.simats.ocularsurfaceai.data.SessionManager
import com.simats.ocularsurfaceai.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = SessionManager(requireContext())

        // 2. Load the saved states
        binding.switchDark.isChecked = session.isDarkMode()
        binding.switchNotify.isChecked = session.isNotificationsEnabled()
        binding.switchAutoSave.isChecked = session.isAutoSaveEnabled()
        binding.switchConfidence.isChecked = session.isShowConfidenceEnabled()

        // 3. Dark Mode Toggle
        binding.switchDark.setOnCheckedChangeListener { _, isChecked ->
            session.setDarkMode(isChecked)
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // 4. Other Toggles
        binding.switchNotify.setOnCheckedChangeListener { _, isChecked ->
            session.setNotificationsEnabled(isChecked)
        }

        binding.switchAutoSave.setOnCheckedChangeListener { _, isChecked ->
            session.setAutoSave(isChecked)
        }

        binding.switchConfidence.setOnCheckedChangeListener { _, isChecked ->
            session.setShowConfidence(isChecked)
        }

        // 5. Delete Account
        binding.btnDeleteAccount.setOnClickListener {
            findNavController().navigate(R.id.deleteAccountFragment)
        }

        // 6. Logout
        binding.btnLogout.setOnClickListener {
            session.clear()
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
