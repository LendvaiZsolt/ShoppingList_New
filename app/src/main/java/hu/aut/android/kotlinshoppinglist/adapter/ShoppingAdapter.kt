package hu.aut.android.kotlinshoppinglist.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import hu.aut.android.kotlinshoppinglist.MainActivity
import hu.aut.android.kotlinshoppinglist.adapter.ShoppingAdapter.ViewHolder
import hu.aut.android.kotlinshoppinglist.data.AppDatabase
import hu.aut.android.kotlinshoppinglist.data.ShoppingItem
import hu.aut.android.kotlinshoppinglist.databinding.RowItemBinding
import hu.aut.android.kotlinshoppinglist.touch.ShoppingTouchHelperAdapter
import kotlinx.android.synthetic.main.row_item.view.*
import java.util.*
import kotlin.concurrent.thread
/*Az Adapter objektum hídként működik az AdapterView és az adott nézet mögöttes adatai között.
 Az Adapter hozzáférést biztosít az adatelemekhez.
  Az Adapter felelős azért is, hogy nézetet készítsen az adatkészlet minden eleméhez.

 */
class ShoppingAdapter(var context: Context) :
    ListAdapter<ShoppingItem, ViewHolder>(ShopDiffCallback()), ShoppingTouchHelperAdapter {
    /*új elem megjelenítésekor hívódik meg*/
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
    /*adott pozíciójú elem frissítése*/
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        /*adott pozíciójú elem lekérése*/
        val item = getItem(position)
        holder.bind(item)

        with(holder) {
            /*delete gomb eseménykezelő*/
            binding.btnDelete.setOnClickListener {
                thread {
                    /*delete meghívása*/
                    AppDatabase.getInstance(context).shoppingItemDao().deleteItem(item)
                }
            }
            /*edit gomb eseménykezelő, mainactivity-ben edit dialógus meghívása*/

            binding.btnEdit.setOnClickListener {
                (holder.itemView.context as MainActivity).showEditItemDialog(
                    item
                )
            }
            /*checkbox eseménykezelő, lekédezzük, hogy be van-e ikszelve, és firssítjük a db-t a dao-val*/

            binding.cbBought.setOnClickListener {
                item.bought = binding.cbBought.isChecked
                thread {
                    AppDatabase.getInstance(context).shoppingItemDao().updateItem(item)
                }
            }

            binding.cbOnSale.setOnClickListener {
                item.onSale = binding.cbOnSale.isChecked
                thread {
                    AppDatabase.getInstance(context).shoppingItemDao().updateItem(item)
                }
            }
        }
    }

    override fun onItemDismissed(position: Int) {
        thread {
            AppDatabase.getInstance(context).shoppingItemDao().deleteItem(getItem(position))
        }
    }

    override fun onItemMoved(fromPosition: Int, toPosition: Int) {
        notifyItemMoved(fromPosition, toPosition)
    }

    inner class ViewHolder(val binding: RowItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ShoppingItem) {
            binding.item = item
        }
    }

}

class ShopDiffCallback : DiffUtil.ItemCallback<ShoppingItem>() {
    override fun areItemsTheSame(oldItem: ShoppingItem, newItem: ShoppingItem): Boolean {
        return oldItem.itemId == newItem.itemId
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: ShoppingItem, newItem: ShoppingItem): Boolean {
        return oldItem == newItem
    }
}