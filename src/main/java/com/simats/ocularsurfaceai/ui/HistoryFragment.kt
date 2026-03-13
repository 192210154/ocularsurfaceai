package com.simats.ocularsurfaceai.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.ocularsurfaceai.R
import com.simats.ocularsurfaceai.api.RetrofitClient
import com.simats.ocularsurfaceai.data.SessionManager
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    // Adapter with click listener
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rv = view.findViewById<RecyclerView>(R.id.rvHistory)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmptyHistory)


        // RecyclerView setup
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        val userId = SessionManager(requireContext()).getUserId()

        if (userId <= 0) {
            tvEmpty.visibility = View.VISIBLE
            rv.visibility = View.GONE
            return
        }

        // Load history
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val resList = RetrofitClient.api.getHistory()

                if (resList.isNotEmpty()) {
                    rv.visibility = View.VISIBLE
                    tvEmpty.visibility = View.GONE
                    adapter.submitList(resList)
                } else {
                    rv.visibility = View.GONE
                    tvEmpty.visibility = View.VISIBLE
                    adapter.submitList(emptyList())
                }

            } catch (e: Exception) {
                rv.visibility = View.GONE
                tvEmpty.visibility = View.VISIBLE
                adapter.submitList(emptyList())
            }
        }
    }
}