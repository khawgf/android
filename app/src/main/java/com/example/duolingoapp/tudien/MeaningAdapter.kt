package com.example.duolingoapp.tudien

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.duolingoapp.R


class MeaningAdapter(private var meaningList: List<Meaning>) : RecyclerView.Adapter<MeaningAdapter.MeaningViewHolder>() {

    class MeaningViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val partOfSpeechTextview: TextView = itemView.findViewById(R.id.part_of_speech_textview)
        val definitionsTextview: TextView = itemView.findViewById(R.id.definitions_textview)
        val synonymsTitleTextview: TextView = itemView.findViewById(R.id.synonyms_title_textview)
        val synonymsTextview: TextView = itemView.findViewById(R.id.synonyms_textview)
        val antonymsTitleTextview: TextView = itemView.findViewById(R.id.antonyms_title_textview)
        val antonymsTextview: TextView = itemView.findViewById(R.id.antonyms_textview)
    }

    fun updateNewData(newMeaningList: List<Meaning>) {
        meaningList = newMeaningList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeaningViewHolder {
        val context: Context = parent.context
        val inflater = LayoutInflater.from(context)
        val meaningView = inflater.inflate(R.layout.meaning_recycler_row, parent, false)
        return MeaningViewHolder(meaningView)
    }

    override fun getItemCount(): Int {
        return meaningList.size
    }

    override fun onBindViewHolder(holder: MeaningViewHolder, position: Int) {
        val meaning = meaningList[position]

        holder.partOfSpeechTextview.text = meaning.partOfSpeech
        holder.definitionsTextview.text = meaning.definitions.joinToString("\n\n") { "${meaning.definitions.indexOf(it) + 1}. ${it.definition}" }

        if (meaning.synonyms.isEmpty()) {
            holder.synonymsTitleTextview.visibility = View.GONE
            holder.synonymsTextview.visibility = View.GONE
        } else {
            holder.synonymsTitleTextview.visibility = View.VISIBLE
            holder.synonymsTextview.visibility = View.VISIBLE
            holder.synonymsTextview.text = meaning.synonyms.joinToString(", ")
        }

        if (meaning.antonyms.isEmpty()) {
            holder.antonymsTitleTextview.visibility = View.GONE
            holder.antonymsTextview.visibility = View.GONE
        } else {
            holder.antonymsTitleTextview.visibility = View.VISIBLE
            holder.antonymsTextview.visibility = View.VISIBLE
            holder.antonymsTextview.text = meaning.antonyms.joinToString(", ")
        }
    }
}