package com.example.hoangcv2_test.repositories

import com.example.hoangcv2_test.database.Run
import com.example.hoangcv2_test.database.RunDAO
import javax.inject.Inject

class MainRepository @Inject constructor(
    val runDAO: RunDAO

) {
    suspend fun insertRun(run: Run)=runDAO.insertRun(run)
    suspend fun deleteRun(run:Run)=runDAO.deleteRun(run)
    fun getAllRunsSortedByDate()=runDAO.getAllRunsSortedByDate()

    fun getAllRunsSortedByDistance()=runDAO.getAllRunsSortedByDistance()

    fun getAllRunsSortedByTimeInMillis()=runDAO.getAllRunsSortedByTimeMillis()

    fun getAllRunsSortedByAvgSpeed()=runDAO.getAllRunsSortedByAvgSpeed()

    fun getAllRunsSortedByCaloriesBurned()=runDAO.getAllRunsSortedByCaloriesBurned()

    fun getTotalAvgSpeed()=runDAO.getTotalAvgSpeed()

    fun getTotalDistance()=runDAO.getTotalDistance()

    fun getTotalCaloriesBurned()=runDAO.getTotalCaloriesBurned()

    fun getTotalTimeInMillis()=runDAO.getTotalTimeInMillis()
}
