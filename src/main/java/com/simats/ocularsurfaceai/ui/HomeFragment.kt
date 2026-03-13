package com.simats.ocularsurfaceai.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.simats.ocularsurfaceai.R
import com.simats.ocularsurfaceai.api.RetrofitClient
import com.simats.ocularsurfaceai.data.SessionManager
import kotlinx.coroutines.launch
import java.io.File

class HomeFragment : Fragment() {

    private val adapter = RecentAnalysisAdapter { item ->
        findNavController().navigate(
            R.id.resultFragment,
            bundleOf(
                "imageUri" to item.image_url,
                "disease" to item.disease,
                "confidence" to item.confidence.toString(),
                "severity" to item.severity,
                "historyId" to item.id
            )
        )
    }

    private var selectedUri: Uri? = null

    // Views
    private var cardPreview: View? = null
    private var imgPreview: ImageView? = null
    private var btnAnalyze: Button? = null

    // Camera Permission Launcher
    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) openSystemCamera()
            else {
                Toast.makeText(requireContext(), "Camera permission required to take photo", Toast.LENGTH_SHORT).show()
            }
        }

    // Camera Result Launcher
    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                selectedUri?.let { onImageSelected(it) }
            }
        }

    // Gallery Picker Launcher
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { onImageSelected(it) }
        }

    private fun onImageSelected(uri: Uri) {
        selectedUri = uri

        // Show preview section + enable analyze
        cardPreview?.visibility = View.VISIBLE
        btnAnalyze?.apply {
            isEnabled = true
            visibility = View.VISIBLE
            alpha = 1f
        }

        imgPreview?.let {
            Glide.with(this).load(uri).into(it)
        }
    }

    private fun openSystemCamera() {
        val file = File(requireContext().cacheDir, "capture_${System.currentTimeMillis()}.jpg")

        val uri = FileProvider.getUriForFile(
            requireContext(),
            requireContext().packageName + ".fileprovider",
            file
        )

        selectedUri = uri

        // Grant URI permissions to camera apps
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val resolvedIntentActivities = requireContext().packageManager.queryIntentActivities(captureIntent, 0)
        for (resolvedIntentInfo in resolvedIntentActivities) {
            val packageName = resolvedIntentInfo.activityInfo.packageName
            requireContext().grantUriPermission(
                packageName,
                uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        takePicture.launch(uri)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = SessionManager(requireContext())
        view.findViewById<TextView>(R.id.tvUser).text = session.getName()

        // cache views once
        cardPreview = view.findViewById(R.id.cardPreview)
        imgPreview = view.findViewById(R.id.imgPreview)
        btnAnalyze = view.findViewById(R.id.btnAnalyze)

        // initial state
        cardPreview?.visibility = View.GONE
        btnAnalyze?.apply {
            isEnabled = false
            visibility = View.GONE
            alpha = 0.5f
        }

        // Buttons
        view.findViewById<View>(R.id.cardTakePhoto).setOnClickListener {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }

        view.findViewById<View>(R.id.cardUploadGallery).setOnClickListener {
            pickImage.launch("image/*")
        }

        btnAnalyze?.setOnClickListener {
            val uri = selectedUri ?: return@setOnClickListener
            findNavController().navigate(
                R.id.analyzingFragment,
                bundleOf("imageUri" to uri.toString())
            )
        }

        // Recent RecyclerView
        val rvRecent = view.findViewById<RecyclerView>(R.id.rvRecent)
        val tvEmpty = view.findViewById<View>(R.id.tvEmptyRecent)

        rvRecent.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rvRecent.adapter = adapter

        val userId = session.getUserId()

        if (userId <= 0) {
            tvEmpty.visibility = View.VISIBLE
            rvRecent.visibility = View.GONE
        } else {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val res = RetrofitClient.api.getHistory()
                    if (res.isNotEmpty()) {
                        tvEmpty.visibility = View.GONE
                        rvRecent.visibility = View.VISIBLE
                        // Only show 5 items like the php api parameter did
                        adapter.submitList(res.take(5))
                    } else {
                        rvRecent.visibility = View.GONE
                        tvEmpty.visibility = View.VISIBLE
                        adapter.submitList(emptyList())
                    }
                } catch (_: Exception) {
                    rvRecent.visibility = View.GONE
                    tvEmpty.visibility = View.VISIBLE
                }
            }
        }

        // View all -> History
        view.findViewById<View?>(R.id.tvViewAll)?.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_history)
        }

        // Avatar -> Profile
        view.findViewById<View?>(R.id.avatarCard)?.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_profile)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cardPreview = null
        imgPreview = null
        btnAnalyze = null
    }
}