package com.example.dicodingstoryapp.ui.home

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.dicodingstoryapp.R
import com.example.dicodingstoryapp.data.model.StoryResponse
import com.example.dicodingstoryapp.data.repository.StoryRepository
import com.example.dicodingstoryapp.databinding.ActivityStoryDetailBinding
import com.example.dicodingstoryapp.utils.SessionManager

class StoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoryDetailBinding
    private lateinit var sessionManager: SessionManager

    private val storyDetailViewModel: StoryDetailViewModel by viewModels {
        StoryDetailViewModelFactory(StoryRepository())
    }

    companion object {
        const val EXTRA_STORY_ID = "extra_story_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.story_detail_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sessionManager = SessionManager(this)

        val storyId = intent.getStringExtra(EXTRA_STORY_ID)
        if (storyId != null) {
            fetchStoryDetail(storyId)
        } else {
            Toast.makeText(this, "Story ID is missing", Toast.LENGTH_SHORT).show()
            finish()
        }

        storyDetailViewModel.storyDetail.observe(this) { response ->
            response?.let {
                handleFetchSuccess(it)
            } ?: run {
                handleFetchFailure(null)
            }
        }

        storyDetailViewModel.detailError.observe(this) { error ->
            handleFetchFailure(error)
        }
    }

    private fun fetchStoryDetail(id: String) {
        val token = sessionManager.fetchAuthToken()
        if (token != null) {
            storyDetailViewModel.fetchStoryDetail(token, id)
        } else {
            Toast.makeText(this, "Failed to get auth token", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleFetchSuccess(response: StoryResponse) {
        val story = response.story
        binding.apply {
            tvDetailName.text = story?.name
            tvDetailDescription.text = story?.description
            Glide.with(this@StoryDetailActivity)
                .load(story?.photoUrl)
                .placeholder(R.drawable.ic_placeholder_image)
                .into(ivDetailPhoto)
        }
    }

    private fun handleFetchFailure(error: Throwable?) {
        Toast.makeText(this, "Failed to fetch story detail: ${error?.message}", Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
