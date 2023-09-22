package com.example.likeyoutube.randomizer

import android.util.Log
import com.example.likeyoutube.MainActivity.Companion.TAG
import kotlin.random.Random


class PlaylistsWorker {
    // збереження списку
    // діставання списку
    // перенесення першого елементу в кінець
    // зміна приорітету пісні зі зміною кількості цієї пісні в списку

    //а понижение приоритета, если я не хочу сейчас слушать -
    // пусть элемент посмотрит на дату и время у элемента на 100 позиций в списке ниже,
    // если такого нету - на дату и время последнего элемента и сделает себе такой же + 0-100рандомных милисекунд
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
        var groupsNumber = Random.nextInt(groupsMax.coerceAtMost(from), groupsMax.coerceAtLeast(from) + 1)
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