package com.nabila.storyappdicoding

import com.nabila.storyappdicoding.data.model.Story

object DataDummy {
    fun generateDummyStories(): List<Story> {
        val items: MutableList<Story> = arrayListOf()
        for (i in 0..10) {
            val story = Story(
                id = i.toString(),
                name = "User $i",
                description = "Description for story $i",
                photoUrl = "https://dummyimage.com/300x200/000/fff&text=Story+$i",
                createdAt = "2024-12-17T00:00:00Z",
                lat = null,
                lon = null
            )
            items.add(story)
        }
        return items
    }
}