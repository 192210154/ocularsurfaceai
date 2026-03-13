package com.simats.ocularsurfaceai.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.simats.ocularsurfaceai.R
import com.simats.ocularsurfaceai.data.SessionManager
import com.simats.ocularsurfaceai.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Load User Data
        val session = SessionManager(requireContext())
        binding.tvName.text = session.getName().ifEmpty { "User" }
        binding.tvEmail.text = session.getEmail().ifEmpty { "user@email.com" }

        // 2. Set up Button Clicks

        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_edit)
        }

        // ✅ Activated Change Password Navigation
        binding.btnChangePassword.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_change_password)
        }

        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_settings)
        }

        binding.btnFaq.setOnClickListener {
            findNavController().navigate(R.id.helpSupportFragment)
        }

        binding.btnLibrary.setOnClickListener {
            findNavController().navigate(R.id.libraryFragment)
        }

        binding.btnReminders.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_reminders)
        }

        binding.btnSpecialists.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_specialists)
        }

        binding.btnTerms.setOnClickListener {
            findNavController().navigate(R.id.privacyPolicyFragment)
        }

        binding.btnLogout.setOnClickListener {
            session.clear()
            val intent = requireActivity().packageManager.getLaunchIntentForPackage(requireActivity().packageName)
            intent?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(it)
                requireActivity().finish()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}