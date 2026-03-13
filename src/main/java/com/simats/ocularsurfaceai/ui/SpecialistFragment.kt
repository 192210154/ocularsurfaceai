package com.simats.ocularsurfaceai.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.ocularsurfaceai.R
import com.simats.ocularsurfaceai.databinding.FragmentSpecialistBinding
import com.simats.ocularsurfaceai.databinding.ItemSpecialistBinding

data class Specialist(
    val name: String,
    val title: String,
    val distance: String,
    val rating: String
)

class SpecialistFragment : Fragment(R.layout.fragment_specialist) {

    private lateinit var binding: FragmentSpecialistBinding
    private lateinit var adapter: SpecialistAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSpecialistBinding.bind(view)

        setupRecyclerView()

        binding.cardMaps.setOnClickListener {
            val gmmIntentUri = Uri.parse("geo:0,0?q=ophthalmologist+nearby")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(mapIntent)
            } else {
                // Fallback for when Google Maps app is not installed
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/ophthalmologist+nearby"))
                startActivity(webIntent)
            }
        }
    }

    private fun setupRecyclerView() {
        val specialists = listOf(
            Specialist("Dr. Sarah Johnson", "Senior Ophthalmologist", "2.5 km away", "4.9"),
            Specialist("Dr. Michael Chen", "Corneal Specialist", "4.2 km away", "4.8"),
            Specialist("Dr. Emma Wilson", "Eye Surgeon", "5.1 km away", "4.7"),
            Specialist("City Eye Hospital", "Specialist Clinic", "3.0 km away", "4.6")
        )

        adapter = SpecialistAdapter(specialists)
        binding.rvSpecialists.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSpecialists.adapter = adapter
    }

    private class SpecialistAdapter(private val list: List<Specialist>) :
        RecyclerView.Adapter<SpecialistAdapter.VH>() {

        inner class VH(val binding: ItemSpecialistBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val binding = ItemSpecialistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(binding)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = list[position]
            holder.binding.tvName.text = item.name
            holder.binding.tvTitle.text = item.title
            holder.binding.tvDistance.text = item.distance
            holder.binding.tvRating.text = "★ ${item.rating}"
            
            holder.binding.btnBook.setOnClickListener {
                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:0123456789")
                }
                holder.itemView.context.startActivity(dialIntent)
            }
        }

        override fun getItemCount() = list.size
    }
}
