package com.example.android.guesstheword.screens.score

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ScoreViewModel(finalScore: Int): ViewModel() {
    //Setup Live Data:
    private var _eventPlayAgain = MutableLiveData<Boolean>()
            val eventPlayAgain : LiveData<Boolean>
                    get() = _eventPlayAgain

    private var _score = MutableLiveData<Int>()
            val score : LiveData<Int>
                get() = _score

        //initialize it if necessary:
    init{
        _score.value = finalScore
    }

    //functions that manipulate Live Data go here:
    fun onPlayAgain(){
        _eventPlayAgain.value = true
    }

    fun onPlayAgainComplete(){
        _eventPlayAgain.value = false
    }



}