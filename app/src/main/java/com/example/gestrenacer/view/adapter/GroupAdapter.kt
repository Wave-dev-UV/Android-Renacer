package com.example.gestrenacer.view.adapter

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.example.gestrenacer.models.Group

class GroupAdapter(
    context: Context,
    private val allGroups: List<Group>,
    private val autoCompleteTextView: AutoCompleteTextView,
    private val showDetailsDialog: (Group) -> Unit
) : ArrayAdapter<Group>(context, android.R.layout.simple_dropdown_item_1line, allGroups),
    Filterable {

    private var filteredGroups: List<Group> = allGroups

    init {
        autoCompleteTextView.threshold = 0
        autoCompleteTextView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                autoCompleteTextView.showDropDown()
            }
        }
        autoCompleteTextView.setOnClickListener {
            autoCompleteTextView.showDropDown()
        }
    }

    override fun getCount(): Int {
        return filteredGroups.size
    }

    override fun getItem(position: Int): Group? {
        return filteredGroups[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val group = getItem(position)

        (view as? TextView)?.text = group?.nombre ?: ""

        val typedValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, typedValue, true)
        view.setBackgroundResource(typedValue.resourceId)

        view.setOnClickListener {
            group?.let {
                autoCompleteTextView.setText(it.nombre, false)
                autoCompleteTextView.dismissDropDown()
                autoCompleteTextView.setSelection(it.nombre.length)
            }
        }
        view.setOnLongClickListener {
            group?.let { showDetailsDialog(it) }
            autoCompleteTextView.dismissDropDown()
            true
        }

        return view
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase() ?: ""
                val results = FilterResults()
                val matchingGroups = if (query.isNotEmpty()) {
                    allGroups.filter { it.nombre.lowercase().contains(query) }
                } else {
                    allGroups
                }

                results.values = matchingGroups
                results.count = matchingGroups.size
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredGroups = results?.values as? List<Group> ?: listOf()
                notifyDataSetChanged()
            }
        }
    }
}