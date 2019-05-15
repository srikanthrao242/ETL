package com.etl.api

import akka.http.scaladsl.Http
import com.doc.spark_hadoop_config.SparkInit

import scala.async.Async.{async, await}

trait WebServer extends SparkInit{
  this: AkkaCoreModule
  with Router =>

  private val host = config.getString("http.host")
  private val port = config.getInt("http.port")


  private val binding = Http().bindAndHandle(routes, host, port)

  async {
    await(binding)
    log.info(s"server listening on port $port")
  }
}