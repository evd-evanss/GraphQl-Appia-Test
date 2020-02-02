package com.sayhitoiot.graphqlappiatest.ui.activityes.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.example.graphql.MeasurementsQuery
import com.sayhitoiot.graphqlappiatest.R
import kotlinx.android.synthetic.main.item_measure.view.*

class MeasuresAdapter(private val measures: MutableList<MeasurementsQuery.Measurement> = mutableListOf()):
    RecyclerView.Adapter<MeasuresAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(parent.inflate(R.layout.item_measure))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(measures[position])
    }

    override fun getItemCount() = measures.size

    fun updateList(character: MutableList<MeasurementsQuery.Measurement>){
        this.measures.clear()
        this.measures.addAll(character)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        fun bind(result: MeasurementsQuery.Measurement){
            var data_raw = result.measuredAt().toString().subSequence(0,10).toString()
            var data = data_raw.split("-")
            itemView.text_measures.text = "Medição: "+ result.measurement()+" mg/dl"
            itemView.text_status.text = "Status: " + result.status()
            itemView.text_date.text = "Data: "+ data[2]+"/"+data[1]+"/"+data[0]
        }

    }
}

fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
}