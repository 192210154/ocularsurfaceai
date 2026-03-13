package com.simats.ocularsurfaceai.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simats.ocularsurfaceai.R
import com.simats.ocularsurfaceai.databinding.FragmentReminderBinding
import com.simats.ocularsurfaceai.databinding.ItemReminderBinding
import com.simats.ocularsurfaceai.models.Reminder
import com.simats.ocularsurfaceai.utils.ReminderReceiver
import java.util.*

class ReminderFragment : Fragment(R.layout.fragment_reminder) {

    private lateinit var binding: FragmentReminderBinding
    private lateinit var adapter: ReminderAdapter
    private val reminders = mutableListOf<Reminder>()
    private val gson = Gson()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentReminderBinding.bind(view)

        setupRecyclerView()
        loadReminders()

        binding.btnAddReminder.setOnClickListener {
            showAddReminderDialog()
        }
    }

    private fun setupRecyclerView() {
        adapter = ReminderAdapter(reminders) { reminder ->
            deleteReminder(reminder)
        }
        binding.rvReminders.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReminders.adapter = adapter
    }

    private fun loadReminders() {
        val prefs = requireContext().getSharedPreferences("reminders_pref", Context.MODE_PRIVATE)
        val json = prefs.getString("reminders_list", null)
        if (json != null) {
            val type = object : TypeToken<List<Reminder>>() {}.type
            val list: List<Reminder> = gson.fromJson(json, type)
            reminders.clear()
            reminders.addAll(list)
            updateUI()
        }
    }

    private fun saveReminders() {
        val prefs = requireContext().getSharedPreferences("reminders_pref", Context.MODE_PRIVATE)
        val json = gson.toJson(reminders)
        prefs.edit().putString("reminders_list", json).apply()
        updateUI()
    }

    private fun updateUI() {
        if (reminders.isEmpty()) {
            binding.emptyState.visibility = View.VISIBLE
            binding.rvReminders.visibility = View.GONE
        } else {
            binding.emptyState.visibility = View.GONE
            binding.rvReminders.visibility = View.VISIBLE
            adapter.notifyDataSetChanged()
        }
    }

    private fun showAddReminderDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val input = EditText(requireContext())
        input.hint = "Medicine Name (e.g. Eye Drops)"
        builder.setTitle("New Reminder")
        builder.setView(input)
        builder.setPositiveButton("Set Time") { _, _ ->
            val name = input.text.toString()
            if (name.isNotBlank()) {
                pickTime(name)
            } else {
                Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun pickTime(name: String) {
        val calendar = Calendar.getInstance()
        val timePicker = TimePickerDialog(requireContext(), { _, hour, minute ->
            addReminder(name, hour, minute)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)
        timePicker.show()
    }

    private fun addReminder(name: String, hour: Int, minute: Int) {
        val id = System.currentTimeMillis()
        val reminder = Reminder(id, name, hour, minute)
        reminders.add(reminder)
        saveReminders()
        scheduleAlarm(reminder)
        Toast.makeText(requireContext(), "Reminder set for ${"%02d:%02d".format(hour, minute)}", Toast.LENGTH_SHORT).show()
    }

    private fun deleteReminder(reminder: Reminder) {
        cancelAlarm(reminder)
        reminders.remove(reminder)
        saveReminders()
    }

    private fun scheduleAlarm(reminder: Reminder) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), ReminderReceiver::class.java).apply {
            putExtra("medicine_name", reminder.medicineName)
            putExtra("reminder_id", reminder.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    private fun cancelAlarm(reminder: Reminder) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    // --- Inner Adapter Class ---
    private class ReminderAdapter(
        private val list: List<Reminder>,
        private val onDelete: (Reminder) -> Unit
    ) : RecyclerView.Adapter<ReminderAdapter.VH>() {

        inner class VH(val binding: ItemReminderBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val binding = ItemReminderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(binding)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = list[position]
            holder.binding.tvMedicineName.text = item.medicineName
            holder.binding.tvReminderTime.text = "Daily at ${"%02d:%02d".format(item.hour, item.minute)}"
            holder.binding.btnDelete.setOnClickListener { onDelete(item) }
        }

        override fun getItemCount() = list.size
    }
}
