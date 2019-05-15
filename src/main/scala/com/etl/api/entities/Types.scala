package com.etl.api.entities

case class LoadFiles(filePath: String, tablename: String, delimiter : String, userid:Int,file_format : String)
case class LoadFromDB(db_type:String,host: String, port:Int,database:String,tablename: String,tablename1: String,username:String,password:String,user_id:Int)
case class ReadFromHive(user_id:Int,tablename:String)