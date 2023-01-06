package com.example.sqlitekotlinexample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.sqlitekotlinexample.adapter.TaskRecyclerAdapter
import com.example.sqlitekotlinexample.databinding.ActivityAddEditBinding
import com.example.sqlitekotlinexample.db.DatabaseHandler
import com.example.sqlitekotlinexample.models.Tasks

class AddOrEditActivity : AppCompatActivity() {

    var dbHandler: DatabaseHandler? = null
    var isEditMode = false
    var priorityAddSpinner: Spinner? = null
    private val key_value = "key_value"
    private val key_result = "key_result"
    private var priority = "NORMAL"
    private val SPEECH_REQUEST_CODE_TITLE = 0
    private val SPEECH_REQUEST_CODE_DESC = 1
    private val SPEECH_REQUEST_CODE_PRIORITY = 2
    private lateinit var binding: ActivityAddEditBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        initDB()
        initOperations()
        binding.voiceInputNameFAB.setOnClickListener(View.OnClickListener {
            displaySpeechRecognizerForTitle()
        })
        binding.voiceInputDescFAB.setOnClickListener(View.OnClickListener {
            displaySpeechRecognizerForDesc()
        })
        initAddSpinner()

    }
    private fun initAddSpinner(){
        priorityAddSpinner = findViewById(R.id.priorityAddSpinner)
        val priorityAdapter = ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            MainActivity.priorityList
        )
        priorityAddSpinner!!.adapter = priorityAdapter
        priorityAddSpinner!!.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    priority = p0?.getItemAtPosition(p2).toString()
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
    }


    private fun initDB() {
        dbHandler = DatabaseHandler(this)
        binding.btnDelete.visibility = View.INVISIBLE
        if (intent != null && intent.getStringExtra("Mode") == "Edit") {
            isEditMode = true
            val tasks: Tasks = dbHandler!!.getTask(intent.getIntExtra("Id",0))
            binding.inputName.setText(tasks.name)
            binding.inputDesc.setText(tasks.desc)
            binding.priorityAddSpinner.setSelection(MainActivity.priorityList.indexOf(tasks.priority))
            binding.swtCompleted.isChecked = tasks.completed == "Y"
            binding.btnDelete.visibility = View.VISIBLE
        }
    }
    private fun displaySpeechRecognizerForTitle() {
        //Starts an activity that will prompt the user for speech and send it through a speech recognizer.
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL, //Informs the recognizer which speech model to prefer when performing ACTION_RECOGNIZE_SPEECH.
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM //Use a language model based on free-form speech recognition.
            )
        }
        // This starts the activity and populates the intent with the speech text.
        startActivityForResult(intent, SPEECH_REQUEST_CODE_TITLE)
    }
    private fun displaySpeechRecognizerForDesc() {
        //Starts an activity that will prompt the user for speech and send it through a speech recognizer.
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL, //Informs the recognizer which speech model to prefer when performing ACTION_RECOGNIZE_SPEECH.
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM //Use a language model based on free-form speech recognition.
            )
        }
        // This starts the activity and populates the intent with the speech text.
        startActivityForResult(intent, SPEECH_REQUEST_CODE_DESC)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SPEECH_REQUEST_CODE_TITLE && resultCode == Activity.RESULT_OK) {
            val spokenText =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).let { results ->
                    results?.get(0) ?: return
                }
            Log.d("voice", spokenText)
            //setting voice text into input field
            binding.inputName?.setText(spokenText)
        }else if (requestCode == SPEECH_REQUEST_CODE_DESC && resultCode == Activity.RESULT_OK) {
            val spokenText =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).let { results ->
                    results?.get(0) ?: return
                }
            Log.d("voice", spokenText)
            //setting voice text into input field
            binding.inputDesc?.setText(spokenText)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    private fun initOperations() {
        binding.btnSave.setOnClickListener {
            var success: Boolean = false
            if (!isEditMode) {
                val tasks: Tasks = Tasks()
                tasks.name = binding.inputName.text.toString()
                tasks.desc = binding.inputDesc.text.toString()
                tasks.priority = priority
                if (binding.swtCompleted.isChecked)
                    tasks.completed = "Y"
                else
                    tasks.completed = "N"
                success = dbHandler?.addTask(tasks) as Boolean
            } else {
                val tasks: Tasks = Tasks()
                tasks.id = intent.getIntExtra("Id", 0)
                tasks.name = binding.inputName.text.toString()
                tasks.desc = binding.inputDesc.text.toString()
                tasks.priority = priority
                if (binding.swtCompleted.isChecked)
                    tasks.completed = "Y"
                else
                    tasks.completed = "N"
                success = dbHandler?.updateTask(tasks) as Boolean
            }

            if (success)
                finish()
        }

        binding.btnDelete.setOnClickListener {
            val dialog = AlertDialog.Builder(this).setTitle("Info")
                .setMessage("Click 'YES' Delete the Task.")
                .setPositiveButton("YES") { dialog, i ->
                    val success = dbHandler?.deleteTask(intent.getIntExtra("Id", 0)) as Boolean
                    if (success)
                        finish()
                    dialog.dismiss()
                }
                .setNegativeButton("NO") { dialog, i ->
                    dialog.dismiss()
                }
            dialog.show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
