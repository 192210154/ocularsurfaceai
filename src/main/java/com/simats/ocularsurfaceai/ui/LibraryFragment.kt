package com.simats.ocularsurfaceai.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.simats.ocularsurfaceai.R
import com.simats.ocularsurfaceai.databinding.FragmentLibraryBinding

class LibraryFragment : Fragment(R.layout.fragment_library) {

    private lateinit var binding: FragmentLibraryBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLibraryBinding.bind(view)
        
        binding.cardHygiene.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Eye Hygiene Tips")
                .setMessage("1. Wash hands before touching eyes.\n2. Use clean towels.\n3. Remove makeup before sleep.")
                .setPositiveButton("OK", null)
                .show()
        }

        binding.cardConditions.setOnClickListener {
            // Navigate to DiseaseDetails with a default one or a list
            val action = LibraryFragmentDirections.actionLibraryToDiseaseDetails("blepharitis")
            findNavController().navigate(action)
        }

        binding.cardExercises.setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Try the 20-20-20 rule to reduce strain!", android.widget.Toast.LENGTH_SHORT).show()
        }

        binding.cardNutrition.setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Eat carrots and leafy greens for Vitamin A!", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
}
