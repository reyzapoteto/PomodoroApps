package com.pomodoroapps.Database.DTO

class PomodoroDTO {

    var id: Long = -1
    var name = ""
    var isCompleted = false
    var priority = ""

    var BY_PRIORITY: Comparator<PomodoroDTO> =
        Comparator { p0, p1 -> p0!!.priority.compareTo(p1!!.priority) }


}