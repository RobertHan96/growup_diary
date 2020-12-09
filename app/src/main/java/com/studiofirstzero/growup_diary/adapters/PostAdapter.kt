package com.studiofirstzero.growup_diary.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.studiofirstzero.growup_diary.R
import com.studiofirstzero.growup_diary.datas.Post
import kotlin.math.floor
import kotlin.math.round

class PostAdapter(context: Context, resID:Int, list: ArrayList<Post>)  : ArrayAdapter<Post>(context, resID, list)  {
    val mContext = context
    val mList = list
    val inf = LayoutInflater.from(mContext)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var tempRow = convertView
        tempRow.let {

        }.let {
            tempRow = inf.inflate(R.layout.post_list_item, null)
        }

        val row = tempRow!!
        val data = mList.get(position)

        val createdAt = row.findViewById<TextView>(R.id.createdAtText)
        val title = row.findViewById<TextView>(R.id.titleText)
        val measureValue = row.findViewById<TextView>(R.id.measureValueText)

        title.text = data.title
        measureValue.text = "${data.measureValue?.toInt()}cm"
        createdAt.text = data.createdAt?.subSequence(0,10)
        return  row
    }


}