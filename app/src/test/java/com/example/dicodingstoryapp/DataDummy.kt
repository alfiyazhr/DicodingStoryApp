package com.example.dicodingstoryapp

import com.example.dicodingstoryapp.data.model.Story

object DataDummy {
    fun generateDummyStories(): List<Story> {
        val stories = ArrayList<Story>()
        for (i in 0..10) {
            val story = Story(
                id = "story-$i",
                name = "Story $i",
                description = "Description $i",
                photoUrl = "https://example.com/photo$i.jpg",
                createdAt = "2024-12-20T00:00:00Z"
            )
            stories.add(story)
        }
        return stories
    }
}
