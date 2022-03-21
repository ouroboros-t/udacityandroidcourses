/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

       private var viewModelJob = Job()

        //define scope the coroutine will run on
        private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

        private var tonight = MutableLiveData<SleepNight?>()

        private val nights = database.getAllNights()

        val nightString = Transformations.map(nights){
            //give us access to our strings
            nights -> formatNights(nights, application.resources)
        }

        //whenever state of tonight changes, stop/start buttons become visible/invisible
        //tonight begins as null, so we want start button to be visible and stop to not be
        val startButtonVisible = Transformations.map(tonight) {
            null == it
        }
        //when tonight is no longer null, we want stop to be visible
        val stopButtonVisible = Transformations.map(tonight){
            null != it
        }
        //clear button should be visible if there are nights to clear:
        val clearButtonVisible = Transformations.map(nights){
            it?.isNotEmpty()
        }

        private var _showSnackBarEvent = MutableLiveData<Boolean>()

        val showSnackBarEvent: LiveData<Boolean>
            get() = _showSnackBarEvent


        fun doneShowingSnackBar(){
            _showSnackBarEvent.value = false
        }

        private val _navigateToSleepQuality = MutableLiveData<SleepNight>()
                val navigateToSleepQuality: LiveData<SleepNight> //<-- encapsulate Live Data
                    get() = _navigateToSleepQuality

        fun doneNavigating() {
            _navigateToSleepQuality.value = null
        }

        init {
                initializeTonight()
        }

        private fun initializeTonight(){
                //launches a coroutine that does not block the main thread
                uiScope.launch {
                        tonight.value = getTonightFromDatabase()
                }
        }

        //suspend function so that it runs within a coroutine
        private suspend fun getTonightFromDatabase(): SleepNight? {
                return withContext(Dispatchers.IO) {
                        var night = database.getTonight()
                        if(night?.endTimeMilli != night?.startTimeMilli){
                                night = null
                        }
                        return@withContext night
                }
        }

        fun onStartTracking() {
                //create coroutine because this process will take a while
                uiScope.launch{
                        val newNight = SleepNight()

                        insert(newNight)

                        tonight.value = getTonightFromDatabase()
                }
        }

        private suspend fun insert(night: SleepNight) {
                withContext(Dispatchers.IO) {
                        database.insert(night)
                }
        }

        fun onStopTracking() {
                uiScope.launch {
                        //check to see if oldNight is null, return result from check?
                        val oldNight = tonight.value ?: return@launch
                        oldNight.endTimeMilli = System.currentTimeMillis()

                        update(oldNight)
                    _navigateToSleepQuality.value = oldNight
                }
        }

        private suspend fun update(night: SleepNight){
                withContext(Dispatchers.IO){
                        database.update(night)
                }
        }


        fun onClear() {
            uiScope.launch {
                clear()
                tonight.value = null
                _showSnackBarEvent.value = true
            }
        }
    private suspend fun clear(){
        withContext(Dispatchers.IO){
            database.clear()
        }
    }

        //tell the job to cancel all coroutines:
        override fun onCleared() {
                super.onCleared()
                viewModelJob.cancel()
        }





//        //for notes only:
//        fun someWorkToDo (){
//                uiScope.launch {
//                        suspendFunction()
//                        //run this coroutine within this scope
//                }
//        }
//
//        fun suspendFunction() {
//                withContext(Dispatchers.IO){
//                        longrunningFunction()
//                        //will take a long time so use coroutines
//                }
//        }
}

