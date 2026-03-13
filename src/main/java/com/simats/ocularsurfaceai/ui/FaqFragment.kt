package com.simats.ocularsurfaceai.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.simats.ocularsurfaceai.R
import com.simats.ocularsurfaceai.databinding.FragmentFaqBinding

class FaqFragment : Fragment(R.layout.fragment_faq) {

    private lateinit var binding: FragmentFaqBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentFaqBinding.bind(view)

        // ✅ Contact Support button → opens email app
        binding.btnContactSupport.setOnClickListener {

            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:ocularsurfaceai.support@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "Ocular Surface AI App Support")
                putExtra(Intent.EXTRA_TEXT,
                    "Hello Support Team,\n\nI need help regarding the Ocular Surface AI application.\n\n")
            }

            startActivity(intent)
        }
    }
}