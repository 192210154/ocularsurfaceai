package com.simats.ocularsurfaceai.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.simats.ocularsurfaceai.R
import com.simats.ocularsurfaceai.databinding.FragmentDiseaseDetailsBinding

class DiseaseDetailsFragment : Fragment(R.layout.fragment_disease_details) {

    private lateinit var binding: FragmentDiseaseDetailsBinding
    private val args: DiseaseDetailsFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentDiseaseDetailsBinding.bind(view)

        val disease = args.disease.lowercase()

        when(disease){

            "normal" -> {
                binding.tvDiseaseName.text = "Normal Eye"
                binding.tvDiseaseDescription.text =
                    "The eye appears healthy with no visible surface disease. Maintain proper eye hygiene and avoid excessive screen exposure."
                binding.tvAdvice.text =
                    "Continue regular eye care and yearly checkups."
            }

            "blepharitis" -> {
                binding.tvDiseaseName.text = "Blepharitis"
                binding.tvDiseaseDescription.text =
                    "Blepharitis is an inflammation of the eyelid margins. It may cause itching, redness and crusting around eyelashes."
                binding.tvAdvice.text =
                    "Clean eyelids with warm compress and consult ophthalmologist if persistent."
            }

            "conjunctivitis" -> {
                binding.tvDiseaseName.text = "Conjunctivitis"
                binding.tvDiseaseDescription.text =
                    "Conjunctivitis (pink eye) is an infection or inflammation of the conjunctiva causing redness, irritation and watering."
                binding.tvAdvice.text =
                    "Avoid touching eyes and seek medical treatment. It can be contagious."
            }

            "corneal_ulcer" -> {
                binding.tvDiseaseName.text = "Corneal Ulcer"
                binding.tvDiseaseDescription.text =
                    "Corneal ulcer is a serious infection of the cornea that can lead to permanent vision loss if untreated."
                binding.tvAdvice.text =
                    "URGENT: Visit an eye hospital immediately."
            }
        }
    }
}
