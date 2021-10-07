package com.pomodoroapps.Adapter

import android.app.AlertDialog
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pomodoroapps.Database.DTO.PomodoroDTO
import com.pomodoroapps.R
import com.pomodoroapps.TimerApps

class TaskAdapter(
    private val activity: TimerApps,
    private val list: MutableList<PomodoroDTO>,
    private val backgroundColor: Int
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val backgroundTask: ConstraintLayout = itemView.findViewById(R.id.backgroundDatamodelTask)
        val priorityAccurate: ImageView = itemView.findViewById(R.id.priorityAccurate)
        val cbTask: CheckBox = itemView.findViewById(R.id.checkBoxTask)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(activity).inflate(R.layout.recyclerview_datamodel, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listPosition = list[position]
        holder.tvTitle.text = listPosition.name
        holder.cbTask.isChecked = listPosition.isCompleted
        holder.backgroundTask.background.setTint(backgroundColor)

        holder.backgroundTask.setOnClickListener {
            val dialog = AlertDialog.Builder(activity)
            dialog
                .setTitle("Apakah ingin Dihapus")
                .setCancelable(false)
                .setNegativeButton("Tidak") { _, _ ->
                    // not do anything
                }
                .setPositiveButton("Ya") { _, _ ->
                    Handler(Looper.getMainLooper()).postDelayed({
                        activity.dbHandler.deleteTask(listPosition.id)
                        activity.refreshItemTask()
                        holder.backgroundTask.visibility = View.GONE
                    }, 1000)

                    Toast.makeText(activity, "Data Terhapus", Toast.LENGTH_SHORT).show()
                }
        }


        // check Priority
        when (listPosition.priority) {

            "a" -> {

                holder.priorityAccurate.setColorFilter(
                    ContextCompat.getColor(
                        activity,
                        R.color.taskred
                    )
                )
            }
            "b" -> {

                holder.priorityAccurate.setColorFilter(
                    ContextCompat.getColor(
                        activity,
                        R.color.taskorange
                    )
                )
            }
            "c" -> {

                holder.priorityAccurate.setColorFilter(
                    ContextCompat.getColor(
                        activity,
                        R.color.tasklightgreen
                    )
                )
            }
        }


        //check checkbox value
        holder.cbTask.setOnClickListener {
            listPosition.isCompleted = !listPosition.isCompleted
            if (listPosition.isCompleted) {
                //now task.iscompleted == true
                holder.tvTitle.paintFlags =
                    holder.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                holder.tvTitle.setTextColor(ContextCompat.getColor(activity, R.color.FourthText))
                Handler(Looper.getMainLooper()).postDelayed({
                    activity.dbHandler.deleteTask(listPosition.id)
                    activity.refreshItemTask()
                    holder.backgroundTask.visibility = View.GONE
                }, 1000)
            } else {
                //now task.iscompleted == true
                holder.tvTitle.paintFlags =
                    holder.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                holder.tvTitle.setTextColor(
                    ContextCompat.getColor(
                        activity,
                        R.color.white
                    )
                )
            }
            activity.dbHandler.updateTask(listPosition)
            activity.refreshItemTask()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}