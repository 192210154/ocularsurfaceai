package com.simats.ocularsurfaceai.ui

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.simats.ocularsurfaceai.R
import com.simats.ocularsurfaceai.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class AnalyzingFragment : Fragment(R.layout.fragment_analyzing) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageUriStr = arguments?.getString("imageUri")
        android.util.Log.d("AI_DEBUG", "AnalyzingFragment opened. imageUri=$imageUriStr")

        if (imageUriStr.isNullOrBlank()) {
            findNavController().popBackStack()
            return
        }

        val imageUri = Uri.parse(imageUriStr)

        lifecycleScope.launch {
            runFastApiInference(imageUri)
        }
    }

    private suspend fun runFastApiInference(imageUri: Uri) {
        try {
            // 1) Convert URI -> bitmap -> temp JPG file
            val part = withContext(Dispatchers.IO) {
                val inputStream = requireContext().contentResolver.openInputStream(imageUri)
                val bytes = inputStream?.readBytes() ?: throw Exception("Failed to read image")
                val requestBody = bytes.toRequestBody("image/jpeg".toMediaType())
                MultipartBody.Part.createFormData("file", "image.jpg", requestBody)
            }

            // 2) Call FastAPI /predict
            val res = withContext(Dispatchers.IO) {
                RetrofitClient.api.saveResult(part)
            }

            android.util.Log.d("AI_DEBUG", "FASTAPI RESULT disease=${res.disease} conf=${res.confidence} severity=${res.severity}")

            val confPercent = res.confidence * 100f

            val bundle = bundleOf(
                "imageUri" to imageUri.toString(),
                "disease" to res.disease,
                "confidence" to String.format("%.2f", confPercent),
                "severity" to (res.severity ?: "N/A")
            )

            findNavController().navigate(R.id.action_analyzing_to_result, bundle)

        } catch (e: Exception) {
            android.util.Log.e("AI_ERROR", "FastAPI inference failed: ${e.message}", e)

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    requireContext(),
                    "AI Error: ${e.message}\n\nCheck: FastAPI running + correct IP in RetrofitClient",
                    Toast.LENGTH_LONG
                ).show()
            }

            findNavController().popBackStack()
        }
    }
}