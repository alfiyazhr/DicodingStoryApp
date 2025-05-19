package com.example.dicodingstoryapp.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dicodingstoryapp.R
import com.example.dicodingstoryapp.data.paging.StoryPagingAdapter
import com.example.dicodingstoryapp.data.repository.StoryRepository
import com.example.dicodingstoryapp.databinding.ActivityHomeBinding
import com.example.dicodingstoryapp.ui.auth.LoginActivity
import com.example.dicodingstoryapp.ui.maps.MapsActivity
import com.example.dicodingstoryapp.utils.SessionManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var storyPagingAdapter: StoryPagingAdapter

    private val storyViewModel: StoryViewModel by viewModels {
        StoryViewModelFactory(StoryRepository())
    }

    private val addStoryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            refreshStories()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_name)

        sessionManager = SessionManager(this)

        setupRecyclerView()
        setupListeners()
        setupSwipeRefresh()
        loadStories()
        observePagingData()
    }

    override fun onResume() {
        super.onResume()
        refreshStories()
    }

    private fun setupRecyclerView() {
        storyPagingAdapter = StoryPagingAdapter { story ->
            val intent = Intent(this, StoryDetailActivity::class.java)
            intent.putExtra(StoryDetailActivity.EXTRA_STORY_ID, story.id)
            startActivity(intent)
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = storyPagingAdapter
            setHasFixedSize(true)
        }

        storyPagingAdapter.addLoadStateListener { loadState ->
            binding.swipeRefreshLayout.isRefreshing = loadState.refresh is LoadState.Loading

            when (loadState.refresh) {
                is LoadState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                }
                is LoadState.NotLoading -> {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE


                    if (storyPagingAdapter.itemCount < 1) {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                }
                is LoadState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE

                    val error = (loadState.refresh as LoadState.Error).error
                    Toast.makeText(
                        this,
                        "Error: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            val errorState = loadState.append as? LoadState.Error
                ?: loadState.prepend as? LoadState.Error

            errorState?.let { error ->
                Toast.makeText(
                    this,
                    "Pagination Error: ${error.error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupListeners() {
        binding.fabAddStory.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            addStoryLauncher.launch(intent)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            storyPagingAdapter.refresh()
            refreshStories()
        }
    }

    private fun loadStories() {
        val token = sessionManager.fetchAuthToken()
        if (token != null) {
            Log.d("HomeActivity", "Token: $token")
            storyViewModel.getStoriesWithPaging(token)
        } else {
            Toast.makeText(this, "Failed to get auth token", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }


    private fun refreshStories() {
        val token = sessionManager.fetchAuthToken()
        if (token != null) {
            storyViewModel.refresh(token)
            storyPagingAdapter.refresh()
            binding.recyclerView.scrollToPosition(0)
        }
    }

    private fun observePagingData() {
        lifecycleScope.launch {
            storyViewModel.pagingDataFlow.collectLatest { pagingData ->
                pagingData?.let {
                    storyPagingAdapter.submitData(lifecycle, it)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                sessionManager.clearSession()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            R.id.action_maps -> {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
