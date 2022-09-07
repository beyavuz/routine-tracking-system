package com.example.proje.denemeler

class deneme1(var len:Int) {
}


fun main(){
    var n1 = deneme1(12)
    var n2 = deneme1(123)
    var liste = ArrayList<deneme1>()
    liste.add(n1)
    liste.add(n2)

    liste.sortBy {
        it.len
    }

    liste.sortByDescending {
        it.len
    }

    liste.forEach {
        println(it.len)
    }
}