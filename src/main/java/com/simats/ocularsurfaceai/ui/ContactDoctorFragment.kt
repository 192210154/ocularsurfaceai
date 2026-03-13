package com.simats.ocularsurfaceai.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.simats.ocularsurfaceai.R
import com.simats.ocularsurfaceai.databinding.FragmentContactDoctorBinding

class ContactDoctorFragment : Fragment(R.layout.fragment_contact_doctor) {

    private lateinit var binding: FragmentContactDoctorBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentContactDoctorBinding.bind(view)

        binding.btnSendMail.setOnClickListener {

            val subject = binding.etSubject.text.toString()
            val message = binding.etMessage.text.toString()

            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:eyeclinic@gmail.com") // change later
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            intent.putExtra(Intent.EXTRA_TEXT, message)

            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(),
                    "No email app installed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}