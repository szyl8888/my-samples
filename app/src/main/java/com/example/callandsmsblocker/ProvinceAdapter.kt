package com.example.callandsmsblocker

data class ProvinceItem(val name: String, val prefixes: List<String>, var selected: Boolean = false)

class ProvinceAdapter(private val allItems: MutableList<ProvinceItem>) : RecyclerView.Adapter<ProvinceViewHolder>() {
    private val items = allItems.toMutableList()

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ProvinceViewHolder {
        val cb = android.widget.CheckBox(parent.context)
        val lp = RecyclerView.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT)
        cb.layoutParams = lp
        return ProvinceViewHolder(cb)
    }

    override fun onBindViewHolder(holder: ProvinceViewHolder, position: Int) {
        val item = items[position]
        holder.checkBox.text = "${item.name} (${item.prefixes.size})"
        holder.checkBox.isChecked = item.selected
        holder.checkBox.setOnCheckedChangeListener { _, isChecked -> item.selected = isChecked }
    }

    override fun getItemCount(): Int = items.size

    fun getSelectedProvinces(): List<ProvinceItem> = items.filter { it.selected }

    fun filter(q: String) {
        val s = q.trim().lowercase()
        items.clear()
        if (s.isEmpty()) { items.addAll(allItems) }
        else { items.addAll(allItems.filter { it.name.lowercase().contains(s) }) }
        notifyDataSetChanged()
    }
}

class ProvinceViewHolder(val checkBox: android.widget.CheckBox) : RecyclerView.ViewHolder(checkBox)
