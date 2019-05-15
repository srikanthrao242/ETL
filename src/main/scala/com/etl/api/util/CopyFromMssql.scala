package com.etl.api.util

/*import com.amazonaws.services.glue.GlueContext
import com.amazonaws.services.glue.util.GlueArgParser
import com.amazonaws.services.glue.util.Job*/
import org.apache.spark.SparkContext
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}

import java.util.Properties

import org.apache.log4j.{Level, LogManager, Logger}
import java.net.URLDecoder

import scala.util.parsing.json._
import scala.collection.immutable.HashMap
import scala.collection.JavaConverters._

object CopyFromMssql {
  def main(/*sysArgs: Array[String]*/) {
    val spark: SparkContext = new SparkContext()
    //val glueContext: GlueContext = new GlueContext(spark)
    // @params: [JOB_NAME]
    //val args = GlueArgParser.getResolvedOptions(sysArgs, Seq("JOB_NAME").toArray)
   // Job.init(args("JOB_NAME"), glueContext, args.asJava)
    // User Code

    val sysArgs :Array[String] = Array("argObj","codedstring to glue")

    implicit val log: Logger = LogManager.getRootLogger
    var userparams : Map[String,String] = HashMap[String,String]()
    for((x,i) <- sysArgs.view.zipWithIndex){
      if(i+1 < sysArgs.length)
        if(x.equalsIgnoreCase("argObj")){
          userparams += ("argObj" -> URLDecoder.decode(sysArgs(i+1),"UTF-8"))
        }
    }

    val jsonString = userparams("argObj")
    val json_parse = JSON.parseFull(jsonString)
    val user_data = json_parse.get.asInstanceOf[Map[String, String]]

    val SESSION = SparkSession.builder.getOrCreate()

    val sDf = getDataFromSource(user_data,SESSION)

    import SESSION.sqlContext.implicits._



    sDf.show()

    log.info("========================================")

    log.info("rdd count: "+sDf.rdd.count())

    log.info("========================================")

    writeToDestination(sDf,user_data)


    //Job.commit()
  }
  def getDataFromSource(user_data:Map[String,String],session:SparkSession): DataFrame ={

    val sourceDbType = "mssql"
    val sourcehost = user_data("sourcehost")
    val sourceport = user_data("sourceport")
    val sourceusername = user_data("sourceusername")
    val sourcepassword = user_data("sourcepassword")
    var sourcequery = user_data("sourcequery")
    var limit = user_data("limit")
    var limitcolumn = user_data("limitcolumn")
    sourcequery = sourcequery + ""

    import session.sqlContext.implicits._

    var jdbcURL = ""
    val connectionProperties = new Properties()
    connectionProperties.put("user", sourceusername)
    connectionProperties.put("password", sourcepassword)
    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance
    if(sourceDbType.equalsIgnoreCase("mssql")){
      Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance
      jdbcURL = "jdbc:sqlserver://"+sourcehost+":"+sourceport
    }else {
      Class.forName("com.mysql.cj.jdbc.Driver").newInstance
      jdbcURL  = "jdbc:mysql://"+sourcehost+":"+sourceport
    }
    session.read.jdbc(jdbcURL,"("+sourcequery+") tmp",connectionProperties)
  }

  def writeToDestination(df:DataFrame,user_data:Map[String,String]): Unit ={

    val destDbType = "mssql"
    val desthost = user_data("sourcehost")
    val destport = user_data("sourceport")
    val destusername = user_data("sourceusername")
    val destpassword = user_data("sourcepassword")
    val destdb = user_data("destinationdb")
    val desttable = user_data("desttable")
    //var sourcequery = user_data("sourcequery")
    val url = "jdbc:sqlserver://"+desthost+":"+destport+";database="+destdb+";user="+destusername+";password="+destpassword
    //val url = "jdbc:sqlserver://"+desthost+":"+destport

    val connectionProperties = new Properties()
    connectionProperties.put("user", destusername)
    connectionProperties.put("password", destpassword)
    // log.info("=======================Connection Information============================")
    //log.info("url: ")
    //log.info(url)
    //log.info(" desttable: ")
    //log.ingo(desttable)
    df.write.mode(SaveMode.Append).jdbc(url,desttable,connectionProperties)
    //df.write.mode(SaveMode.Append).jdbc(url,"411_01.dbo.calendar_1",connectionProperties)
  }
}
