package com.simats.ocularsurfaceai.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.simats.ocularsurfaceai.R

class ReviewImageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_review_image, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imgPreview = view.findViewById<ImageView>(R.id.imgPreview)
        val btnAnalyze = view.findViewById<Button>(R.id.btnAnalyze)
        val btnRetake = view.findViewById<Button>(R.id.btnRetake)

        val uriString = arguments?.getString("imageUri").orEmpty()
        if (uriString.isNotBlank()) {
            imgPreview.setImageURI(Uri.parse(uriString))
        }

        btnRetake.setOnClickListener {
            findNavController().popBackStack() // back to home/camera
        }

        btnAnalyze.setOnClickListener {
            // go to analyzing with the same image
            findNavController().navigate(
                R.id.action_review_to_analyzing,
                bundleOf("imageUri" to uriString)
            )
        }
    }
}
