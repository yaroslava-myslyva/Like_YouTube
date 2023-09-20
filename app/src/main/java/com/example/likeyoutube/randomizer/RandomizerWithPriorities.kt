package com.example.likeyoutube.randomizer

import android.util.Log
import com.example.likeyoutube.MainActivity.Companion.TAG

class RandomizerWithPriorities {
    fun randomize(list: MutableList<VideoIdAndPriority>): MutableList<VideoIdAndPriority> {
        val listResult = mutableListOf<VideoIdAndPriority>()
        list.forEach { vp ->
            val newSize = listResult.size + vp.priority
            val stepDouble : Double = newSize / vp.priority.toDouble()
            Log.d(TAG, "randomize: stepDouble $stepDouble")
            var step = stepDouble.toInt()
            if (stepDouble % 1.0 == 0.5) {
                step += 1
            }
            Log.d(TAG, "randomize: step $step")
            for (i in 1..vp.priority) {
                val positionForMan = (step * i)
                Log.d(TAG, "randomize: position $positionForMan")
                val position = positionForMan - 1
                try {
                    listResult.add(position, vp)
                } catch (e:IndexOutOfBoundsException){
                    listResult.add(vp)
                }
            }
           // Log.d(TAG, "randomize: new")
            listResult.forEach {
                Log.d(TAG, "randomize: ${it.videoID} ${it.priority}")
            }
        }
        return listResult
    }
}