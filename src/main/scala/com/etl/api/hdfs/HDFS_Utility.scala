package com.etl.api.hdfs

import java.time._
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

import com.doc.spark_hadoop_config.SparkInit
import com.typesafe.config.ConfigFactory
import org.apache.spark.rdd.RDD
import org.apache.hadoop.fs.{FileSystem, FileUtil, Path}
import org.apache.spark.sql.DataFrame

import scala.async.Async.async
import scala.concurrent.{ExecutionContext, Future}


class HDFS_Utility extends SparkInit {

  def storeInHDFS(df:DataFrame ,user_id : Int,name : String)(implicit ec: ExecutionContext):Future[Boolean] = async{
    var is_saved = false
    try{
      val timestamp = getCurrentTimeStamp()
      df.rdd.saveAsTextFile(hdfs_loc_uri(user_id,name).concat("/").concat(timestamp))
    }catch {
      case e:Exception =>
        println(e.printStackTrace())
        is_saved = false
    }
    is_saved
  }

  def hdfs_loc_uri(user_id : Int,name : String): String ={
    val hdfs_Loc = "hdfs://".concat(config.getString("hdfs.host"))
      .concat(":")
      .concat(config.getInt("hdfs.port").toString)
      .concat(config.getString("userdatabase.raw_folder_path"))
      .concat("/")
      .concat(config.getString("userdatabase.name"))
      .concat("_")
      .concat(user_id.toString)
      .concat("/").concat(name)
    hdfs_Loc
  }

  def saveAsTextFileAndMerge[T](hdfsServer: String, fileName: String, rdd: RDD[T]): Unit = {
    val sourceFile = hdfsServer + "/tmp/"
    rdd.saveAsTextFile(sourceFile)

    val dstPath = hdfsServer
   // merge(sourceFile, dstPath, fileName)
  }

 /* def merge(srcPath: String, dstPath: String, fileName: String): Unit = {
    val hdfs   = FileSystem.get(HADOOP_CONF)
    val hadoopConfig = HADOOP_CONF
    val destinationPath = new Path(dstPath)
    if (!hdfs.exists(destinationPath)) {
      hdfs.mkdirs(destinationPath)
    }
    FileUtil.copyMerge(hdfs, new Path(srcPath),
                       hdfs, new Path(dstPath + "/" + fileName),
                       false, hadoopConfig, null)
    hdfs.close()
  }
*/
  def createDatabase(user_id : String):Boolean = {
    val config = ConfigFactory.load()
    var is_create = false
    try{
      val databasesQuery = "CREATE DATABASE IF NOT EXISTS "+config.getString("userdatabase.name")+user_id
      SESSION.sql(databasesQuery)
      is_create = true
    }catch{case e : Exception => print(e.toString)}
    is_create
  }

  def getCurrentTimeStamp(): String ={
    val utcZoneId = ZoneId.of("UTC")
    val zonedDateTime = ZonedDateTime.now
    val utcDateTime: ZonedDateTime = zonedDateTime.withZoneSameInstant(utcZoneId)
    utcDateTime.toString.replace("[UTC]","").replace(".",":")
      .replace("-","")
      .replace(":","")
  }
/*
  def saveFile(filepath: String): Unit = {
    val file = new File(filepath)
    val fs   = FileSystem.get(HADOOP_CONF)
    val out = fs.create(new Path(file.getName))
    val in = new BufferedInputStream(new FileInputStream(file))
    var b = new Array[Byte](1024)
    var numBytes = in.read(b)
    while (numBytes > 0) {
      out.write(b, 0, numBytes)
      numBytes = in.read(b)
    }
    in.close()
    out.close()
    fs.close()
  }

  def removeFile(filename: String): Boolean = {
    val fs   = FileSystem.get(HADOOP_CONF)
    val path = new Path(filename)
    fs.delete(path, true)
  }

  def getFile(filename: String): InputStream = {
    val fs   = FileSystem.get(HADOOP_CONF)
    val path = new Path(filename)
    fs.open(path)
  }

  def createFolder(folderPath: String): Boolean = {
    val fs   = FileSystem.get(HADOOP_CONF)
    val path = new Path(folderPath)
    if (!fs.exists(path)) {
      val is_Created = fs.mkdirs(path)
    }
    fs.close()
    true
  }*/
}
