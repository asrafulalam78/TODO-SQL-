package com.example.sqlitekotlinexample

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.sqlitekotlinexample.adapter.TaskRecyclerAdapter
import com.example.sqlitekotlinexample.databinding.ActivityMainBinding
import com.example.sqlitekotlinexample.databinding.ListItemTasksBinding
import com.example.sqlitekotlinexample.db.DatabaseHandler
import com.example.sqlitekotlinexample.models.Tasks

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var status = "UnCompleted"
    private var priority = "HIGH"
    var taskRecyclerAdapter: TaskRecyclerAdapter? = null;
    var fab: FloatingActionButton? = null
    var recyclerView: RecyclerView? = null
    var statusSpinner: Spinner? = null
    var prioritySpinner: Spinner? = null
    var dbHandler: DatabaseHandler? = null
    var listTasks: List<Tasks> = ArrayList<Tasks>()
    var listTasksByStatus: List<Tasks> = ArrayList<Tasks>()
    var listTasksByPriority: List<Tasks> = ArrayList<Tasks>()
    var linearLayoutManager: LinearLayoutManager? = null
    companion object{
        var NORMAL = "NORMAL"
        var LOW = "LOW"
        var HIGH = "HIGH"
        val statusList = listOf("Completed", "Incompleted")
        val priorityList = listOf("NORMAL", "LOW", "HIGH","URGENT")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        initSpinner()
        initOperations()
    }

    private fun initSpinner() {
        statusSpinner = findViewById(R.id.statusSpinner)
        prioritySpinner = findViewById(R.id.prioritySpinner)
        val statusAdapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            statusList
        )
        val priorityAdapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            priorityList
        )
        statusSpinner!!.adapter = statusAdapter
        prioritySpinner!!.adapter = priorityAdapter

        statusSpinner!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    status = p0?.getItemAtPosition(p2).toString()
                    var checkStatus: String
                    if (status.equals(statusList[0])){
                        checkStatus = "Y"
                    }else{
                        checkStatus = "N"
                    }
                    Log.d("status", "Status: $checkStatus")
                    listTasksByStatus = (dbHandler as DatabaseHandler).getAllTasksByStatus(checkStatus)
                    Log.d("status", "Status: $listTasksByStatus")
                    taskRecyclerAdapter = TaskRecyclerAdapter(tasksList = listTasksByStatus, context = applicationContext)
                    taskRecyclerAdapter!!.tasksList = listTasksByStatus
                    (recyclerView as RecyclerView).adapter = taskRecyclerAdapter
                    taskRecyclerAdapter!!.notifyDataSetChanged()
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }

            }
        prioritySpinner!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    priority = p0?.getItemAtPosition(p2).toString()
                    listTasksByPriority = (dbHandler as DatabaseHandler).getAllTasksByPriority(priority.toUpperCase())
                    taskRecyclerAdapter = TaskRecyclerAdapter(tasksList = listTasksByPriority, context = applicationContext)
                    taskRecyclerAdapter!!.tasksList = listTasksByPriority
                    (recyclerView as RecyclerView).adapter = taskRecyclerAdapter
                    taskRecyclerAdapter!!.notifyDataSetChanged()
                    Log.d("TAG", "onItemSelected: ")
                    Log.d("status", "Priority: $priority")
                    Log.d("status", "Prioritylist: $listTasksByPriority")
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
    }
    fun initDB() {
        dbHandler = DatabaseHandler(this)
        listTasks = (dbHandler as DatabaseHandler).getAllTasks()
        taskRecyclerAdapter = TaskRecyclerAdapter(tasksList = listTasks, context = applicationContext)
        (recyclerView as RecyclerView).adapter = taskRecyclerAdapter
    }

    fun initViews() {
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        fab = findViewById(R.id.fab) as FloatingActionButton

        recyclerView = findViewById(R.id.recycler_view) as RecyclerView
        taskRecyclerAdapter = TaskRecyclerAdapter(tasksList = listTasks, context = applicationContext)
        linearLayoutManager = LinearLayoutManager(applicationContext)
        (recyclerView as RecyclerView).layoutManager = linearLayoutManager
    }

    fun initOperations() {
        fab?.setOnClickListener { view ->
            val i = Intent(applicationContext, AddOrEditActivity::class.java)
            i.putExtra("Mode", "A")
            startActivity(i)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_delete) {
            val dialog = AlertDialog.Builder(this).setTitle("Info").setMessage("Click 'YES' Delete All Tasks")
                    .setPositiveButton("YES") { dialog, i ->
                        dbHandler!!.deleteAllTasks()
                        initDB()
                        dialog.dismiss()
                    }
                .setNegativeButton("NO") { dialog, i ->
                    dialog.dismiss()
                }
            dialog.show()
            return true
        }
        if (id == R.id.action_add){
            val i = Intent(applicationContext, AddOrEditActivity::class.java)
            i.putExtra("Mode", "A")
            startActivity(i)
        }
        if(id == R.id.action_all){
            listTasks = (dbHandler as DatabaseHandler).getAllTasks()
            taskRecyclerAdapter = TaskRecyclerAdapter(tasksList = listTasks, context = applicationContext)
            (recyclerView as RecyclerView).adapter = taskRecyclerAdapter
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        initDB()
    }
}


