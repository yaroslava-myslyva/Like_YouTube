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


class BigPlaylist {
    private lateinit var activity: MainActivity

    fun setActivity(activity: MainActivity) {
        this.activity = activity
    }

    fun movingFirstToEnd(bigList: MutableList<VideoIdAndTime>): MutableList<VideoIdAndTime> {
        val firstElement = bigList.removeAt(0)
        bigList.add(firstElement)
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

    fun decreasingPriority(element: VideoIdAndTime, allList: MutableList<VideoIdAndTime>) : MutableList<VideoIdAndTime> {
        allList.remove(element)
        val date = allList[99].lastListening ?: allList.last().lastListening
        var newDate = Date()
        if (date != null) {
            newDate = Date(date.time.plus(Random.nextInt(0, 101)))
        }
        element.lastListening = newDate
        allList.add(element)
        return randomize(allList)
    }

    fun randomize(list: MutableList<VideoIdAndTime>): MutableList<VideoIdAndTime> {
        val listResult = mutableListOf<VideoIdAndTime>()
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
        Log.d(TAG, "randomize: groupsNumber = $groupsNumber")
        groupsNumber = if (groupsNumber < 1) 1 else groupsNumber
        var quantityInOneGroup = list.size / groupsNumber
        quantityInOneGroup = if (quantityInOneGroup < 2) 2 else quantityInOneGroup
        Log.d(TAG, "randomize: quantityInOneGroup = $quantityInOneGroup")
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

//    fun randomize(list: MutableList<VideoIdAndLastListening>): MutableList<VideoIdAndLastListening> {
//        val listResult = mutableListOf<VideoIdAndLastListening>()
//        list.forEach { vp ->
//            val newSize = listResult.size + vp.priority
//            val stepDouble : Double = newSize / vp.priority.toDouble()
//            Log.d(TAG, "randomize: stepDouble $stepDouble")
//            val step = stepDouble.toInt()
//            Log.d(TAG, "randomize: step $step")
//            for (i in 1..vp.priority) {
//                val positionForMan = (step * i)
//                Log.d(TAG, "randomize: position $positionForMan")
//                val position = positionForMan - 1
//                    listResult.add(position, vp)
//            }
//            listResult.forEach {
//                Log.d(TAG, "randomize: ${it.videoID} ${it.priority}")
//            }
//        }
//        return listResult
//    }
}