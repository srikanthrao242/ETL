package com.etl.api

import java.io.{PrintWriter, StringWriter}

import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route, RouteConcatenation}
import com.doc.entities.JsonSupport
import com.doc.spark_hadoop_config.FileOperations
import com.hiddime.etl.api.entities.JsonSupport
import com.hiddime.etl.api.entities._
import com.hiddime.etl.api.aws.AWS
//import com.hiddime.etl.api.util.{CirceSupportForAkkaHttp, ErrorResponse}
import io.circe.generic.auto._
import io.circe.Json
/*
* author srikanth
*
* */
sealed trait SuperTrait

trait Router extends SuperTrait with JsonSupport with RouteConcatenation {

  this: AkkaCoreModule =>

  val fo = new FileOperations()
  val exceptionHandler = ExceptionHandler {
    case exception: Exception =>
      val sw = new StringWriter
      exception.printStackTrace(new PrintWriter(sw))
      complete(s"uncaught exception: $sw")
    //complete(InternalServerError, ErrorResponse(InternalServerError.intValue, s"uncaught exception: $sw"))
  }

  val routes: Route = pathPrefix("etlservices"){
    
     path("loadfiles") {
        post {
          entity(as[LoadFiles]){loadfiles=>
            fo.readFile_store(loadfiles)(executionContext)
          }
        }
      } ~
      path("readFromHive"){
         post{
           entity(as[ReadFromHive]) { readfromHive =>
             fo.read_Table(readfromHive)
             complete(readfromHive.toString)
           }
         }
       }~
       path("loadFromDB"){
         post{
           entity(as[LoadFromDB]) { loadFromDB =>
             try{
               println(loadFromDB)
               fo.readDB_store(loadFromDB)(executionContext)
             }catch{
               case e :Exception=>complete(e.toString)
             }

           }
         }
       }~ new AWS().routes
  }
   

}
