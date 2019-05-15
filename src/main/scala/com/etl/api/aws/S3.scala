package com.etl.api.aws

import com.doc.spark_hadoop_config.SparkInit

class S3 extends SparkInit{

  def readDataFromS3(file : String): Unit ={
    val df = SESSION.read.format("csv").load(file)



    df.show()
  }

}
