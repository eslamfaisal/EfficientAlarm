package com.eslam.efficientalarm.listeners

interface OnTimeSet {
    fun pickedTime(hoursOfDay: Int, minutes: Int)
}