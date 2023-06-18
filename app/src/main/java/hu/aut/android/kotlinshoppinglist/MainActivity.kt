package hu.aut.android.kotlinshoppinglist

import android.os.Bundle
import hu.aut.android.kotlinshoppinglist.adapter.ShoppingAdapter
import kotlinx.android.synthetic.main.activity_main.*
import android.preference.PreferenceManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import hu.aut.android.kotlinshoppinglist.data.AppDatabase
import hu.aut.android.kotlinshoppinglist.data.ShoppingItem
import hu.aut.android.kotlinshoppinglist.touch.ShoppingTouchHelperCallback
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity(), ShoppingItemDialog.ShoppingItemHandler {
    companion object {
        val KEY_FIRST = "KEY_FIRST"
        val KEY_ITEM_TO_EDIT = "KEY_ITEM_TO_EDIT"
    }
    /*saját ShippingAdapter*/
    private lateinit var adapter: ShoppingAdapter
    /*activity létrehozásakor*/
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*activity_main.xml a view*/
        setContentView(R.layout.activity_main)
        /*toolbar beállítása acyvity_main-ből*/
        setSupportActionBar(toolbar)
        /*gomb eseménykezelő (rózsaszín levél ikonos), a dialógust hozza fel*/
        fab.setOnClickListener { view ->
            ShoppingItemDialog().show(supportFragmentManager, "TAG_ITEM")
        }

        initRecyclerView()
        /*első futáskor*/
        if (isFirstRun()) {
            /*New shopping item szöveg kiírása*/
            MaterialTapTargetPrompt.Builder(this@MainActivity)
                .setTarget(findViewById<View>(R.id.fab))
                .setPrimaryText("New Shopping Item")
                .setSecondaryText("Tap here to create new shopping item")
                .show()
        }

        saveThatItWasStarted()
    }
    /*lekérjük, hogy első futás-e, tehát hogy léterhozás és nem módisítás*/
    private fun isFirstRun(): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
            KEY_FIRST, true
        )
    }
    /*beállítjuk, hogy nem az első futás lesz ez*/
    private fun saveThatItWasStarted() {
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        sp.edit()
            .putBoolean(KEY_FIRST, false)
            .apply()
    }

    private fun initRecyclerView() {
        /*saját ShoppingAdapter létrehozása*/
        adapter = ShoppingAdapter(this)
        recyclerShopping.adapter = adapter
        /*dao-ból lekérjük az összes elemet*/
        AppDatabase.getInstance(this).shoppingItemDao().findAllItems()
            .observe(this, Observer { items ->
                adapter.submitList(items)
            })

        val callback = ShoppingTouchHelperCallback(adapter)
        val touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(recyclerShopping)
    }
    /*edit dialógus*/
    fun showEditItemDialog(itemToEdit: ShoppingItem) {
        /*edit dialógus lérrehozása, megjelenítése*/
        val editDialog = ShoppingItemDialog()

        val bundle = Bundle()
        bundle.putSerializable(KEY_ITEM_TO_EDIT, itemToEdit)
        editDialog.arguments = bundle

        editDialog.show(supportFragmentManager, "TAG_ITEM_EDIT")
    }

    /*shoppig item elem beszúrása a db-be*/
    override fun shoppingItemCreated(item: ShoppingItem) {
        thread {
            AppDatabase.getInstance(this@MainActivity).shoppingItemDao().insertItem(item)
        }
    }
    /*shopping item update dao-t hívja meg*/
    override fun shoppingItemUpdated(item: ShoppingItem) {
        thread {
            AppDatabase.getInstance(this@MainActivity).shoppingItemDao().updateItem(item)
        }
    }
}
