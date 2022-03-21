/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.databinding.ListItemSleepNightBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val ITEM_VIEW_TYPE_HEADER = 0
private val ITEM_VIEW_TYPE_ITEM = 1
//The goal of this adapter is to take a list of SleepNight and adapt it to something the RecylerView can use
//to display on the screen.

//the adapter is responsible for adapting data to the RecyclerViewAPI
//viewholders are responsible for everything related to actually managing views

class SleepNightAdapter(val clickListener: SleepNightListener) : ListAdapter<SleepNightAdapter.DataItem, RecyclerView.ViewHolder>(SleepNightDifCallback()) {
    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun addHeaderAndSubmitList(list: List<SleepNight>?) {
        adapterScope.launch {
            val items = when (list) {
                null -> listOf(DataItem.Header)
                else -> listOf(DataItem.Header) + list.map { DataItem.SleepNightItem(it) }
            }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    //update the views held by this ViewHolder to show the items at the position specified
    //this is how recyclerview does recycling

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val nightItem = getItem(position) as DataItem.SleepNightItem
                holder.bind(nightItem.sleepNight, clickListener)
            }
        }
    }

    //WHAT REFACTOR DID WE JUST DO?? Because you want to be able to add mutliple viewHolders at once,
    //having all of the logic for showing OneView holder inside 'onBindViewHolder' gets unmaintainable over time
    //when adding multiple views
    //inside EACH viewHolder, define a bind function that will determine how the ViewHolder is meant
    //to be drawn on the screen.

    class ViewHolder private constructor(val binding: ListItemSleepNightBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: SleepNight,
            clickListener: SleepNightListener
        ){
            binding.clickListener = clickListener
            binding.sleep = item
            binding.executePendingBindings()
        }
        companion object { //we create a companion object because we don't want to instantiate the ViewHolder
                            //just the ViewHolder itself.
            fun from(parent: ViewGroup): ViewHolder {
                //create layout inflater based on the parent view:
                //important to use the right context
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemSleepNightBinding.inflate(layoutInflater, parent, false)

               return ViewHolder(binding)
            }
        }

    }

    //gives recycler viewholder when it asks for a new one (like asking for the first view or when number
    //items increase?
    ////this view will be added to some ViewGroup before being displayed to the screen
    //ViewGroup == RecyclerView

    //it's now the Viewholder's responsibility to know which layout to inflate
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> TextViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
            is DataItem.SleepNightItem -> ITEM_VIEW_TYPE_ITEM
        }
    }

    class SleepNightListener(val clickListener: (sleepId: Long) -> Unit){
        fun onClick(night: SleepNight) = clickListener(night.nightId)
    }

    sealed class DataItem {
        data class SleepNightItem(val sleepNight: SleepNight): DataItem() {
            override val id = sleepNight.nightId
        }

        object Header: DataItem() {
            override val id = Long.MIN_VALUE
        }

        abstract val id: Long
    }

    class TextViewHolder(view: View): RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup): TextViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.header, parent, false)
                return TextViewHolder(view)
            }
        }
    }
}

class SleepNightDifCallback : DiffUtil.ItemCallback<SleepNightAdapter.DataItem>() {
    override fun areItemsTheSame(oldItem: SleepNightAdapter.DataItem, newItem: SleepNightAdapter.DataItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SleepNightAdapter.DataItem, newItem: SleepNightAdapter.DataItem): Boolean {
        return oldItem == newItem
    }
}
