package com.harry.scprprograms.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.scprprograms.util.Status
import com.harry.scprprograms.adapter.ProgramsAdapter
import com.harry.scprprograms.databinding.ActivityMainBinding
import com.harry.scprprograms.model.Programs
import com.harry.scprprograms.repository.SCPRProgramsRepository
import com.harry.scprprograms.viewmodel.MainViewModel
import com.harry.scprprograms.viewmodel.MainViewModelProviderFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val scprProgramsRepository = SCPRProgramsRepository()
        val mainViewModelProviderFactory = MainViewModelProviderFactory(scprProgramsRepository)
        mainViewModel =
            ViewModelProvider(this, mainViewModelProviderFactory)[MainViewModel::class.java]
        mainViewModel.getPrograms()
        lifecycleScope.launchWhenStarted {
            mainViewModel.programs.collect {
                when (it.status) {
                    Status.EMPTY -> Unit
                    Status.SUCCESS ->
                        displayPrograms(it.data?.body()!!)
                    Status.ERROR -> Toast.makeText(
                        this@MainActivity,
                        "Something went wrong.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun displayPrograms(programs: Programs) {
        val programAdapter = ProgramsAdapter()
        programAdapter.differ.submitList(programs.Programs)
        binding.programsRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.programsRecyclerView.adapter = programAdapter
    }
}