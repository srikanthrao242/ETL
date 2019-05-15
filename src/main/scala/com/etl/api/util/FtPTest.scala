package com.etl.api.util

import com.hiddime.etl.api.spark_hadoop_config.SparkInit

object FtPTest with SparkInit{

  /*val dataSource = "ftp://pathtofile"
  val df = SC.wholeTextFiles(dataSource)
  import SESSION.sqlContext.implicits._
  df.toDF().show()
  df.toDF().write.mode("append").save("s3://db/ftpfile")*/
 //   df.saveAsTextFile("s3://db/ftpfile")



  def main(args: Array[String]) {
    val sc = new java.util.Scanner (System.in)
    var n = sc.nextInt()
    val s = n.toBinaryString
    var maxLength = 0
    var tempLength = 0
    s.indices.foreach(f=>{
      if(s.charAt(f) == '1'){
        tempLength += 1
      }else{
        tempLength = 0
      }
      if (tempLength > maxLength) maxLength = tempLength
    })
    println(maxLength)
  }

}
