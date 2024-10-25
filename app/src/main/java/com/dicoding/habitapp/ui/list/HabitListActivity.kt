package com.dicoding.habitapp.ui.list

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.dicoding.habitapp.R
import com.dicoding.habitapp.data.Habit
import com.dicoding.habitapp.setting.SettingsActivity
import com.dicoding.habitapp.ui.ViewModelFactory
import com.dicoding.habitapp.ui.add.AddHabitActivity
import com.dicoding.habitapp.ui.detail.DetailHabitActivity
import com.dicoding.habitapp.ui.random.RandomHabitActivity
import com.dicoding.habitapp.utils.Event
import com.dicoding.habitapp.utils.HABIT_ID
import com.dicoding.habitapp.utils.HabitSortType
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class HabitListActivity : AppCompatActivity() {

    private lateinit var rvHabit: RecyclerView
    private lateinit var habitListViewModel: HabitListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_habit_list)
        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            val addIntent = Intent(this, AddHabitActivity::class.java)
            startActivity(addIntent)
        }

        //TODO 6 : Initiate RecyclerView with LayoutManager
        rvHabit = findViewById(R.id.rv_habit)
        rvHabit.apply {
            setHasFixedSize(true)
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            rvHabit.layoutManager = layoutManager
        }
        //END T0D0

        initAction()

        val factory = ViewModelFactory.getInstance(this)
        habitListViewModel = ViewModelProvider(this, factory)[HabitListViewModel::class.java]

        //TODO 7 : Submit pagedList to adapter and add intent to detail
        habitListViewModel.habits.observe(this, Observer(this::recyclerView))
        habitListViewModel.snackbarText.observe(this, Observer(this::showSnackBar))


    }

    private fun recyclerView(habit: PagedList<Habit>) {
        val adapter = HabitAdapter { dataHabit ->
            val intent = Intent(this@HabitListActivity, DetailHabitActivity::class.java)
            intent.putExtra(HABIT_ID, dataHabit.id)
            startActivity(intent)
        }

        adapter.submitList(habit)
        rvHabit.adapter = adapter
    }

    //TODO 15 : Fixing bug : Menu not show and SnackBar not show when list is deleted using swipe
    private fun showSnackBar(eventMessage: Event<Int>) {
        val message = eventMessage.getContentIfNotHandled() ?: return
        Snackbar.make(
            findViewById(R.id.coordinator_layout),
            getString(message),
            Snackbar.LENGTH_SHORT
        ).setAction("Undo"){
            habitListViewModel.insert(habitListViewModel.undo.value?.getContentIfNotHandled() as Habit)
        }.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                showFilteringPopUpMenu()
                true
            }
            R.id.action_random -> {
                val intentRandom = Intent(applicationContext, RandomHabitActivity::class.java)
                startActivity(intentRandom)
                true
            }
            R.id.action_settings -> {
                val intentSettings = Intent(applicationContext, SettingsActivity::class.java)
                startActivity(intentSettings)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showFilteringPopUpMenu() {
        val view = findViewById<View>(R.id.action_filter) ?: return
        PopupMenu(this, view).run {
            menuInflater.inflate(R.menu.filter_habits, menu)

            setOnMenuItemClickListener {
                habitListViewModel.filter(
                    when (it.itemId) {
                        R.id.minutes_focus -> HabitSortType.MINUTES_FOCUS
                        R.id.title_name -> HabitSortType.TITLE_NAME
                        else -> HabitSortType.START_TIME
                    }
                )
                true
            }
            show()
        }
    }

    private fun initAction() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return makeMovementFlags(0, ItemTouchHelper.RIGHT)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val habit = (viewHolder as HabitAdapter.HabitViewHolder).getHabit
                habitListViewModel.deleteHabit(habit)
            }

        })
        itemTouchHelper.attachToRecyclerView(rvHabit)
    }
}
