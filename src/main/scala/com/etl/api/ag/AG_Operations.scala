package com.etl.api.ag

import com.hiddime.etl.api.util.Util
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.{DataFrame, Row}
import org.apache.spark.sql.types.StructType

import scala.async.Async.async
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.immutable.HashMap
import scala.collection.mutable.ArrayBuffer
import org.apache.spark.sql.functions._

class AG_Operations extends java.io.Serializable {

  private val xsd = "http://www.w3.org/2001/XMLSchema#"
  val config = ConfigFactory.load()

  def createQuadsUsingDF(graphName: String,
                         user_id : Int,
                         outputFile: String,
                         df:DataFrame,
                         distinct_field1 :String,
                         field_uris : Map[String,String],
                         pre_field_uris : Map[String,String])(implicit ec: ExecutionContext) : Future[Boolean] =async{
    var is_Complete = false
    println("in createQuadsUsingDF")

    try{
      val schema = df.schema
      val dataTypes = getDataTypes(schema)
      //val dist_col = getDistinct_col(df)
      var dataFrame = df
      var distinct_field = "marketbasket_header_key"
      val regex = "/(\\s)+/g".r

      df.printSchema()

      var byteArr = new ArrayBuffer[Byte]()
      var resp : Map[String,String] = HashMap[String,String]()

     // if (dist_col.length == 0) {
       // dataFrame = df.withColumn("cid", monotonically_increasing_id)
     // }else distinct_field = distinct_field1

      val columns = dataFrame.columns
      val nQuads = dataFrame.rdd.map(row=>{
        val g_name = "<"+config.getString("agGraph.graph_url")+"graph_name/"+ regex.replaceAllIn(graphName.toString.trim, "_") +">"
        var triple = ""
        columns.foreach(col => {
          if (!col.toString.equalsIgnoreCase(distinct_field) && !col.equalsIgnoreCase("cid")) {
            val obj = convertObject_toString(dataTypes, col, row)
            if (!obj.equalsIgnoreCase("") && obj.trim.nonEmpty) {
              val obj_str = regex.replaceAllIn(row.getAs(distinct_field).toString.trim, "")
              var subject = "<" + config.getString("agGraph.graph_url") + "subject/" + graphName + "/" + obj_str + ">"
              if (field_uris.contains(distinct_field)) {
                subject = "<" + field_uris(col) + obj_str + ">"
              }
              var predicate = "<" + config.getString("agGraph.graph_url") + "predicate/" + regex.replaceAllIn(col.toString.trim, "") + ">"
              if (pre_field_uris.contains(col)) {
                predicate = "<" + pre_field_uris(col) + regex.replaceAllIn(col.toString.trim, "") + ">"
              }
              var object_val = "\"" + obj + "\""
              if (field_uris.contains(distinct_field)) {
                object_val = "<" + field_uris(col) + regex.replaceAllIn(obj.toString.trim, "") + ">"
              }
              triple = triple + subject + " " + predicate + " " + object_val + " " + g_name + " .\n"
            }
          }
        })
        triple
      })
      val util = new Util()
      nQuads.coalesce(1,true).saveAsTextFile("./temp/"+graphName)

      /*val aG_Config = new AG_Config()
      val isloaded = aG_Config.loadQuads_or_Triples(user_id,"./temp/"+graphName+"/part-00000")
      if(isloaded == 0){
        is_Complete = false
      }else{
        is_Complete = true
      }*/
    }catch {
      case e:Exception=>e.printStackTrace(); is_Complete = false
    }
    is_Complete
  }


  def convertObject_toString(dataTypes : Map[String,String],col:String,row : Row): String ={
    var obj = ""
    if(dataTypes.contains(col) && dataTypes(col).equalsIgnoreCase("date")){
      val format = new java.text.SimpleDateFormat("yyyy-MM-dd")
      obj = format.parse(row.getAs(col)).toString
    }else if(dataTypes.contains(col) && dataTypes(col).equalsIgnoreCase("dateTime")){
      val format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
      obj = format.parse(row.getAs(col)).toString
    }else{
      obj = row.getAs(col).toString
    }
    obj
  }

  def getDistinct_col(df: DataFrame) : Array[String] ={
    val response = new ArrayBuffer[String]()
    df.columns.foreach { cname =>
      if(df.select(cname).collect().distinct.length == df.count()){
        response.append(cname)
      }
    }
    response.distinct.toArray
  }

  def getDataTypes(schema : StructType): Map[String,String] ={
    var resp : Map[String,String] = HashMap[String,String]()
    schema.foreach(st_field =>{
      val typeString = st_field.dataType.simpleString
      if(typeString.equalsIgnoreCase("datetime") || typeString.equalsIgnoreCase("timestamp")){
        resp += (st_field.name -> "<".concat(xsd.concat("dateTime>")))
      }else if(typeString.equalsIgnoreCase("date")){
        resp += (st_field.name -> "<".concat(xsd.concat("date>")))
      }else if(typeString.equalsIgnoreCase("int")||
        typeString.equalsIgnoreCase("bigint")||
        typeString.equalsIgnoreCase("tinyint")||
        typeString.equalsIgnoreCase("smallint")||
        typeString.equalsIgnoreCase("mediumint")){
        resp += (st_field.name -> "<".concat(xsd.concat("integer>")))
      }else if(typeString.equalsIgnoreCase("bigint")){
        resp += (st_field.name -> "<".concat(xsd.concat("long>")))
      }else if(typeString.equalsIgnoreCase("float")){
        resp += (st_field.name -> "<".concat(xsd.concat("float>")))
      }else if(typeString.equalsIgnoreCase("decimal")){
        resp += (st_field.name -> "<".concat(xsd.concat("decimal>")))
      }else{
        resp += (st_field.name -> "<".concat(xsd.concat("string>")))
      }
    })
    resp
  }

}
