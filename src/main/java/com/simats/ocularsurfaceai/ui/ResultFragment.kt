package com.simats.ocularsurfaceai.ui

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.simats.ocularsurfaceai.R
import com.simats.ocularsurfaceai.api.RetrofitClient
import com.simats.ocularsurfaceai.data.SessionManager
import com.simats.ocularsurfaceai.databinding.FragmentResultBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!
    private var historyId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Intercept Hardware Back Button
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack(R.id.homeFragment, false)
            }
        })


        // Set Today's Date dynamically
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        binding.tvDate.text = sdf.format(Date())

        // Retrieve Arguments
        val uriString = arguments?.getString("imageUri").orEmpty()
        val disease = arguments?.getString("disease") ?: "Normal"
        val confidenceString = arguments?.getString("confidence") ?: "0"
        val severity = arguments?.getString("severity") ?: "Mild"
        historyId = arguments?.getString("historyId") ?: historyId

        // Set UI based on Detection Result
        val diseaseLower = disease.lowercase()
        val confPercent = confidenceString.toFloatOrNull() ?: 0f
        val confInt = confPercent.toInt().coerceIn(0, 100)

        // --- DYNAMIC CONTENT UPDATE ---
        binding.tvLabel.text = disease
        binding.tvConfidence.text = "Confidence: ${"%.2f".format(confPercent)}%"
        binding.progress.progress = confInt

        // Update "About This Condition" text dynamically
        binding.tvDesc.text = getConditionDescription(disease)

        // Setup visibility and buttons
        setupDisplayLogic(uriString, diseaseLower, confPercent, severity)

        // Button Actions
        setupButtonActions(uriString, disease, confidenceString, severity)
    }

    /**
     * Helper function to provide specific descriptions based on the AI result.
     */
    private fun getConditionDescription(condition: String): String {
        return when (condition.lowercase().trim()) {
            "blepharitis" -> "Blepharitis is an inflammation of the eyelids that usually affects both eyes along the edges of the eyelids. It commonly occurs when tiny oil glands near the base of the eyelashes become clogged."
            "conjunctivitis" -> "Conjunctivitis (Pink Eye) is an inflammation or infection of the transparent membrane that lines your eyelid and covers the white part of your eyeball."
            "corneal_ulcer" -> "A corneal ulcer is an open sore on the cornea, the clear front layer of the eye. It is often caused by infections (bacterial, viral, or fungal) or severe dry eye and requires immediate medical attention to prevent vision loss."
            "normal" -> "The eye appears healthy with no signs of inflammation, infection, or structural abnormalities."
            "no_eye" -> "The system could not identify a human eye in this image. Please ensure the photo is well-lit and focused on the ocular surface."
            else -> "The AI has detected signs of $condition. While this provides a preliminary analysis, please consult an ophthalmologist for a definitive diagnosis and treatment plan."
        }
    }

    private fun setupDisplayLogic(uriString: String, diseaseLower: String, confPercent: Float, severity: String) {
        if (uriString.isNotBlank()) {
            if (uriString.startsWith("http", true)) {
                Glide.with(this).load(uriString).into(binding.imgResult)
            } else {
                binding.imgResult.setImageURI(Uri.parse(uriString))
            }
        }

        val blockDetailsAndSave = diseaseLower == "no_eye" || confPercent < 60f

        if (diseaseLower == "no_eye") {
            binding.tvSeverity.text = "Invalid Image"
        } else if (confPercent < 60f) {
            binding.tvSeverity.text = "Low Confidence"
        } else {
            binding.tvSeverity.text = severity
        }

        if (blockDetailsAndSave) {
            binding.tvFullDetails.visibility = View.GONE
            binding.btnSaveResult.visibility = View.GONE
            binding.btnShareResult.visibility = View.GONE
        } else {
            binding.tvFullDetails.visibility = View.VISIBLE
            updateSaveDownloadVisibility()
        }
    }

    private fun updateSaveDownloadVisibility() {
        if (!historyId.isNullOrBlank()) {
            binding.btnShareResult.visibility = View.VISIBLE
            binding.btnSaveResult.visibility = View.GONE
        } else {
            binding.btnShareResult.visibility = View.VISIBLE
            binding.btnSaveResult.visibility = View.VISIBLE
        }
    }

    private fun setupButtonActions(uriString: String, disease: String, confidenceString: String, severity: String) {
        val diseaseLower = disease.lowercase()
        val confPercent = confidenceString.toFloatOrNull() ?: 0f

        binding.tvFullDetails.setOnClickListener {
            if (diseaseLower == "no_eye" || confPercent < 60f) {
                Toast.makeText(requireContext(), "Please retake a clear eye image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val action = ResultFragmentDirections.actionResultFragmentToDiseaseDetailsFragment(disease)
            findNavController().navigate(action)
        }

        binding.btnSaveResult.setOnClickListener {
            handleSaveResult(uriString, disease, confidenceString, severity)
        }

        binding.btnShareResult.setOnClickListener {
            handleShareResult(disease, confidenceString, severity)
        }

        binding.btnNewScan.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }
    }

    private fun handleSaveResult(uriString: String, disease: String, confidenceString: String, severity: String) {
        if (uriString.isBlank()) return

        val userId = SessionManager(requireContext()).getUserId()
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.btnSaveResult.isEnabled = false
                binding.btnSaveResult.text = "Saving..."

                val part = withContext(Dispatchers.IO) {
                    val uri = Uri.parse(uriString)
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes() ?: throw Exception("Failed to read image content")
                    val requestBody = bytes.toRequestBody("image/jpeg".toMediaType())
                    MultipartBody.Part.createFormData("file", "image.jpg", requestBody)
                }

                val res = RetrofitClient.api.saveResult(part) // PredictResponse doesn't need explicit ID and confidence since python calculates it

                if (res.history_id > 0) {
                    Toast.makeText(requireContext(), "Result saved", Toast.LENGTH_SHORT).show()
                    historyId = res.history_id.toString()
                    updateSaveDownloadVisibility()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnSaveResult.isEnabled = true
                binding.btnSaveResult.text = "Save Result"
            }
        }
    }

    private fun handleShareResult(disease: String, confidence: String, severity: String) {
        val shareText = """
            Ocular Surface AI Screening Report
            ----------------------------------
            Detected Condition: $disease
            Confidence Score: $confidence
            Severity Level: $severity
            
            Note: This is an AI-generated screening for research purposes. Please consult a specialist for clinical diagnosis.
        """.trimIndent()

        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Eye screening Report - $disease")
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
        
        startActivity(Intent.createChooser(shareIntent, "Share Report via"))
    }

    private fun handleDownloadReport() {
        val id = historyId ?: return
        val url = "http://172.23.50.66:8000/ocular/api/report.php?history_id=$id"

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Ocular Report")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Ocular_Report_$id.pdf")

        val dm = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
        Toast.makeText(requireContext(), "Downloading...", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}