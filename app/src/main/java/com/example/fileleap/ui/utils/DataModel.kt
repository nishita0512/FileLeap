package com.example.fileleap.ui.utils

enum class DataModelType{
    StartConnection, Offer, Answer, IceCandidates
}

data class DataModel(
    val type: DataModelType?=null,
    val target:String?=null,
    val data:Any?=null
)