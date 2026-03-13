package com.simats.ocularsurfaceai.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.simats.ocularsurfaceai.R
import com.simats.ocularsurfaceai.data.SessionManager
import com.simats.ocularsurfaceai.databinding.FragmentSplashBinding
import com.simats.ocularsurfaceai.utils.Prefs

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imgLogo.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.splash_fade_scale))

        val session = SessionManager(requireContext())

        Handler(Looper.getMainLooper()).postDelayed({
            if (!isAdded) return@postDelayed

            val onboardingDone = Prefs.isOnboardingDone(requireContext())
            val isLoggedIn = session.isLoggedIn()

            if (onboardingDone) {
                if (isLoggedIn) {
                    safeNavigate(R.id.action_splash_to_home)
                } else {
                    safeNavigate(R.id.action_splash_to_login)
                }
            } else {
                safeNavigate(R.id.action_splash_to_onboarding)
            }
        }, 1500)
    }

    private fun safeNavigate(actionId: Int) {
        val navController = findNavController()
        val currentDestination = navController.currentDestination
        if (currentDestination?.id == R.id.splashFragment) {
            navController.navigate(actionId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
