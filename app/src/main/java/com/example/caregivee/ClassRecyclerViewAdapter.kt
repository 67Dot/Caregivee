package com.example.caregivee

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

//Review Code For This Page [√√√√√]

//RecyclerView EXPLANATION
//========================
//What this does is optimize long lists by ONLY grabbing the data needed to show the items that can fit onscreen.
//It recycles the item that disappears offscreen — as we scroll — and repurposes it as the new item...
//... therefore also aiding in optimization: https://stackoverflow.com/questions/37523308/when-onbindviewholder-is-called-and-how-it-works
//The Code Below Is Adapted From: https://www.geeksforgeeks.org/android-recyclerview-in-kotlin/
//GRADLE NOTE: Remember That There Are Some Special Dependencies For RecyclerView in the "Gradle Build File"!

class ClassRecyclerViewAdapter(var mvList: MutableList<ClassLineItem>, private val mvWhichActivity : Int) : RecyclerView.Adapter<ClassRecyclerViewAdapter.ViewHolder>() {
    //RecyclerView onClick
    //(Source: https://stackoverflow.com/questions/29424944/recyclerview-itemclicklistener-in-kotlin)
        var mvOnItemClick : ((ClassLineItem) -> Unit)? = null //"null" Safety Review: Should Be OK, As We Assign A Lambda Function To This Variable (E.G. In "ActivityBehestRecyclerView" And "ActivityTelephoneContactsRecyclerView") And We Also Do A "?." Check When Clicked

    //Strings 'n' Things
        private val mvClassFromHtml = ClassFromHtml()

    //Create New Views
        override fun onCreateViewHolder(mvParent : ViewGroup, mvViewType : Int): ViewHolder {
            //Inflates The "..._line_item.xml" Layout
            //That Is Used To Hold Each List Item
                val mvView = when (mvWhichActivity) {
                                0    -> LayoutInflater.from(mvParent.context).inflate(R.layout.recycler_view_behest_layout_line_item, mvParent, false)
                                else -> LayoutInflater.from(mvParent.context).inflate(R.layout.recycler_view_telephone_contacts_layout_line_item, mvParent, false)
                             }
                return ViewHolder(mvView)
        }
    //"ViewHolder" Optimizes The Number of Times findViewById Is Called.
    //If you have 1000 items and the screen can only show 10, each containing 2 Views, that means it only has to call findViewById 2*(10 Plus 2 Offscreen Items)=24 times, as opposed to let's say 2*1000 times.
    //The "Views" are essentially *recycled*: https://stackoverflow.com/questions/45534221/understanding-recyclerview-viewholder
        inner class ViewHolder(mvItemView : View) : RecyclerView.ViewHolder(mvItemView) {
            //Get The "Views" For Each "Line Item"
                val mvImageView : ImageView = mvItemView.findViewById(R.id.mxImageView)
                val mvTextView  : TextView  = mvItemView.findViewById(R.id.mxTextView)

            //RecyclerView onClick
            //(Source: https://stackoverflow.com/questions/29424944/recyclerview-itemclicklistener-in-kotlin)
                init {
                    mvItemView.setOnClickListener {
                        mvOnItemClick?.invoke(mvList[this.bindingAdapterPosition /* <-- Which "Line Item" Was Selected? */])
                    }
                }
        }
    //Populates A "Newly Scrolled To" (Or "Recycled" When Scrolling Back) View With Pertinent List Data
        override fun onBindViewHolder(mvHolder : ViewHolder, mvPosition: Int) {
            //Get The Data (E.G. Containing The Image, Name, And Mobile Number)
                val mvLineItem = mvList[mvPosition]

            //Sets The ViewHolder's ImageView To A Vector-Based Thumbnail
                mvHolder.mvImageView.setImageResource(mvLineItem.mvImage)
                mvHolder.mvImageView.setColorFilter(mvLineItem.mvColorImg)

            //Set's The ViewHolder's "TextView" To The Appropriate Info For Display
                mvHolder.mvTextView.text = mvClassFromHtml.mmFromHtml(when (mvWhichActivity) {
                                                                         0    -> "${mvLineItem.mvName}"
                                                                         else -> "${mvLineItem.mvName} ${mvLineItem.mvMobile}"})
                mvHolder.mvTextView.setTextColor(mvLineItem.mvColorText)
        }

    //List Size?
        override fun getItemCount(): Int {
            return mvList.size
        }
}