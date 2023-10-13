package com.example.likeyoutube2.randomizer

import android.content.Context
import android.util.Log
import com.example.likeyoutube2.Constants
import com.example.likeyoutube2.MainActivity
import com.example.likeyoutube2.MainActivity.Companion.TAG
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date
import kotlin.random.Random


class WorkerBigPlaylist {
    private lateinit var activity: MainActivity

    fun setActivity(activity: MainActivity) {
        this.activity = activity
    }

    fun movingFirstToEnd(bigList: MutableList<VideoIdAndTime>): MutableList<VideoIdAndTime> {
        Log.d(TAG, "movingFirstToEnd: ")
        val firstElement = bigList.removeAt(0)
        bigList.add(firstElement)
        return bigList
    }

    fun movingLastInFront(bigList: MutableList<VideoIdAndTime>): MutableList<VideoIdAndTime> {
        Log.d(TAG, "movingLastInFront: ")
        val lastElement = bigList.removeLast()
        bigList.add(0, lastElement)
        return bigList
    }

    fun getBigPlaylistFromShared(): MutableList<VideoIdAndTime>? {
        val jsonString = activity.application.getSharedPreferences(
            Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE
        ).getString(Constants.DATA_BIG_LIST, null) ?: return null
        val gson = Gson()
        val listType = object : TypeToken<MutableList<VideoIdAndTime>>() {}.type
        return gson.fromJson(jsonString, listType)
    }


    fun saveBigPlaylist(bigList: MutableList<VideoIdAndTime>) {
        val gson = Gson()
        val jsonString = gson.toJson(bigList)
        activity.application
            .getSharedPreferences(
                Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE
            )
            .edit().putString(Constants.DATA_BIG_LIST, jsonString).apply()
    }

    fun decreasingPriority(
        element: VideoIdAndTime,
        allList: MutableList<VideoIdAndTime>
    ): MutableList<VideoIdAndTime> {
        allList.remove(element)
        val date = if(allList.size > 99) allList[99].lastListening else allList.last().lastListening
        var newDate = Date()
        if (date != null) {
            newDate = Date(date.time.plus(Random.nextInt(0, 101)))
        }
        element.lastListening = newDate
        allList.add(element)
        return randomize(allList)
    }

    fun randomize(list: MutableList<VideoIdAndTime>): MutableList<VideoIdAndTime> {
        Log.d(TAG, "randomize: ")
        val listResult = mutableListOf<VideoIdAndTime>()
        val first = list.removeAt(0)
        listResult.add(first)
        val listNulls = mutableListOf<VideoIdAndTime>()
        val listNotNulls = mutableListOf<VideoIdAndTime>()
        list.forEach {
            if (it.lastListening != null) {
                listNotNulls.add(it)
            } else {
                listNulls.add(it)
            }
        }
        listNotNulls.sortBy { it.lastListening }
        val listListsNulls = splitAndShuffleList(listNulls)
        val listListsNotNulls = splitAndShuffleList(listNotNulls)
        listListsNulls.forEach { listN -> listN.forEach { listResult.add(it) } }
        listListsNotNulls.forEach { listN -> listN.forEach { listResult.add(it) } }
        return listResult
    }

    private fun splitAndShuffleList(
        list: MutableList<VideoIdAndTime>
    ): MutableList<MutableList<VideoIdAndTime>> {
        val result = mutableListOf<MutableList<VideoIdAndTime>>()
        val from = 10
        val otFonaria = 30
        var groupsMax = list.size / otFonaria
        groupsMax = if (groupsMax < 1) 1 else groupsMax
        var groupsNumber =
            Random.nextInt(groupsMax.coerceAtMost(from), groupsMax.coerceAtLeast(from) + 1)
        groupsNumber = if (groupsNumber < 1) 1 else groupsNumber
        var quantityInOneGroup = list.size / groupsNumber
        quantityInOneGroup = if (quantityInOneGroup < 2) 2 else quantityInOneGroup
        var currentIndex = 0
        while (currentIndex < list.size) {
            val chunk =
                list.subList(
                    currentIndex,
                    kotlin.math.min(currentIndex + quantityInOneGroup, list.size)
                )
            chunk.shuffle()
            result.add(chunk)
            currentIndex += quantityInOneGroup
        }
        return result
    }
}
