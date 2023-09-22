package com.example.likeyoutube.randomizer

import android.util.Log
import com.example.likeyoutube.MainActivity.Companion.TAG
import kotlin.random.Random


class PlaylistsWorker {
    // збереження списку
    // діставання списку
    // перенесення першого елементу в кінець
    // зміна приорітету пісні зі зміною кількості цієї пісні в списку


    //давай пусть налл и не налл по отдельности делится на рандомное число групп от 10 до Х=кол-во_песен_в_налл/неналл / 30)
    //(я не знаю почему 30)
    //т.е. допустим у меня неналл 1250 песен, значит будет рандомное кол-во групп от 10 до 1250/30=42
    //у каждой группы порядковый номер, мы их потом по порядку клеим
    //отрандомили группы и клеим назад
    //
    //потом склеить все группы по порядку, сначала налл все отрандомизированные группы, потом неналл всеотрандомизированные группы
    //
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
        if (groupsMax < 1) {
            groupsMax = 1
        }
        var groupsNumber = if (from < groupsMax) {
            Random.nextInt(from, groupsMax + 1)
        } else if (groupsMax < from) {
            Random.nextInt(groupsMax, from + 1)
        } else from
        Log.d(TAG, "randomize: groupsNumber = $groupsNumber")
        if(groupsNumber < 1){
            groupsNumber = 1
        }
        var quantityInOneGroup = list.size / groupsNumber
        if (quantityInOneGroup < 2) {
            quantityInOneGroup = 2
        }
        Log.d(TAG, "randomize: quantityInOneGroup = $quantityInOneGroup")
        var currentIndex = 0
        while (currentIndex < list.size) {
            val chunk =
                list.subList(currentIndex, kotlin.math.min(currentIndex + quantityInOneGroup, list.size))
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