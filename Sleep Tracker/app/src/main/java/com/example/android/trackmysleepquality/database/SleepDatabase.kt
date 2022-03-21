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

package com.example.android.trackmysleepquality.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//exportSchema is default to true, it saves the schema versions of database
//whenever changing the schema, must update the version number of the app will not work
@Database(entities = [SleepNight::class], version = 1, exportSchema = false)
abstract class SleepDatabase : RoomDatabase(){
    abstract val sleepDatabaseDao: SleepDatabaseDao

    companion object { //this allows clients to access the methods for creating/getting the database
        //without instantiating the class.Since we only want a database from this class, there's no reason to instantiate it.

        @Volatile //any writes to this field/property is visible to other threads - value is always up to date immediately
        private var INSTANCE: SleepDatabase? = null //avoids repeated opening connections to database

        fun getInstance(context: Context) : SleepDatabase {
            synchronized(this){
                var instance = INSTANCE

                if(instance == null){
                    //create the database:
                    instance = Room.databaseBuilder(context.applicationContext, SleepDatabase::class.java, "sleep_history_database")
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }


                return instance
            }
        }


    }

}