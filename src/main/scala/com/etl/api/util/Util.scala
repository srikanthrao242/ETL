package com.etl.api.util

import java.io.File
import org.apache.commons.io.FileUtils

//import scalax.file.Path
class Util {
  def createfile(filePath : String){
    /*val path: Path = Path (filePath)
    path.createFile()*/

    /*// force create a file will fail if it is a directory which
    // contain children
    path.createFile(failIfExists=false)

    // create a directory at the path location
    path.createDirectory()
    path.createDirectory(failIfExists=false)*/
  }

  /**
    * Delete files and directories
    */

  def detectDelemiter(file_ext:String): Unit ={
    try{
      val d_file_ext = Array("nq","nquads","nt","ntriples")
      if(d_file_ext.contains(file_ext)){

      }

    }catch {
      case e:Exception=>print(e.toString)
    }

  }
  def emptyTemp(): Unit ={
    val list_files = getListOfFiles("./temp")
    val file = new File("./temp")
    import org.apache.commons.io.FileUtils
    FileUtils.deleteDirectory(file)
  }
  def getListOfFiles(dir: String):List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }
  def split[A](xs: List[A], n: Int): List[List[A]] = {
    if (xs.size <= n) xs :: Nil;
    else (xs take n) :: split(xs drop n, n);
  }

  def delete(filePath : String) {
    /*val path: Path = Path.fromString(filePath)
    //path.delete()
    path.deleteIfExists()*/
    /*path.deleteRecursively()
    path.deleteRecursively(true)
    path.deleteRecursively(continueOnFailure=true)*/
  }

}
