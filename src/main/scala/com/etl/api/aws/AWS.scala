package com.etl.api.aws

import akka.http.scaladsl.server.{ Route, RouteConcatenation}
import akka.http.scaladsl.server.Directives._

class AWS extends RouteConcatenation{
  val routes: Route = {
    get {
      path("reads3") {
        val s3 = new S3()
        complete("")
      }
    }
  }
}
