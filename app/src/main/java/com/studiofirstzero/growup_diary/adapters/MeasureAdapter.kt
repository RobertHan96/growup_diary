package com.studiofirstzero.growup_diary.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.studiofirstzero.growup_diary.R
import com.studiofirstzero.growup_diary.datas.Measure

class MeasureAdapter (context : Context, resID:Int, list:ArrayList<Measure>)  : ArrayAdapter<Measure>(context, resID, list)  {
    val mContext = context
    val mList = list
    val inf = LayoutInflater.from(mContext)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var tempRow = convertView
        tempRow.let {

        }.let {
            tempRow = inf.inflate(R.layout.measure_list_item, null)
        }

        val row = tempRow!!
        val data = mList.get(position)

        val measureDate = row.findViewById<TextView>(R.id.measureDate)
        val measureValue = row.findViewById<TextView>(R.id.measureValue)

        measureDate.text = data.measureDate
        measureValue.text = data.measureValue.toString()
        return  row
    }


}