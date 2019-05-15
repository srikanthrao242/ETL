package com.etl.api.ag

import com.franz.agraph.http.AGProtocol._
import com.franz.agraph.repository.{AGCatalog, AGRepository, AGRepositoryConnection, AGServer}
import com.typesafe.config.ConfigFactory
import org.eclipse.rdf4j.repository.RepositoryConnection

import scala.collection.mutable.ArrayBuffer


class AG_Config {

  val config = ConfigFactory.load()

  def this(catalogName : String, repository_id : String) {
    this()
    AG_Config.server = AG_Server()
    AG_Config.catalogName = catalogName
    AG_Config.repository_id = repository_id
    AG_Config.catalogType = AG_Config.NAMED_CATALOG
    AG_Config.catalog = getCatalog_ifnot_create()
    AG_Config.catalogURL = getNamedCatalogLocation(AG_Config.server.getServerURL, catalogName)
    AG_Config.repositoriesURL = getNamedCatalogRepositoriesLocation(AG_Config.catalogURL)
  }

  def getServer_Url(): String ={
    val SERVER_URL = "http://"
                      .concat(config.getString("agGraph.host"))
                      .concat(":")
                      .concat(config.getString("agGraph.port"))
    SERVER_URL
  }

  def getCatalog_ifnot_create(): AGCatalog ={
    var catalog: AGCatalog = null
    if(!AG_Config.server.listCatalogs().contains(AG_Config.catalogName)){
      catalog = AG_Config.server.createCatalog(AG_Config.catalogName)
    }else{
      catalog = AG_Config.server.getCatalog(AG_Config.catalogName)
    }
    catalog
  }

  def AG_Server(): AGServer ={
    val SERVER_URL = getServer_Url()
    val USERNAME = config.getString("agGraph.user")
    val PASSWORD = config.getString("agGraph.password")
    var server :AGServer = null
    try{
      server =  new AGServer(SERVER_URL, USERNAME, PASSWORD)
    }catch {
      case e:Exception=> e.printStackTrace()
    }
    server
  }
  def AGRepositoryConnection(close: Boolean): AGRepositoryConnection ={

    var conn: AGRepositoryConnection = null
    try{
      val catalog = AG_Config.server.getCatalog(AG_Config.catalogName)
      println("AGRepositoryConnections")
      if (catalog == null) throw new Exception("Catalog " + AG_Config.catalogName
        + " does not exist. Either "
        + "define this catalog in your agraph.cfg or modify the CATALOG_ID "
        + "in this tutorial to name an existing catalog.")
      //AG_Config.closeAll()

      val repository: AGRepository = AG_Config.catalog.createRepository(AG_Config.repository_id)
      repository.initialize()
      conn = repository.getConnection()
      AG_Config.closeBeforeExit(conn)

      if (close) { // tidy up
        conn.close()
        repository.shutDown()
        AG_Config.server.close()
      }
    }catch{
      case e:Exception=>
        e.printStackTrace()
    }
    conn
  }
  def loadQuads_or_Triples(user_id:Int,path:String): Int ={
    var result = 0
    try{
      import sys.process._
      val ag_bin = config.getString("agGraph.ag_bin")+"/agload"
      val process_str = ag_bin+" -p "+config.getString("agGraph.port")+
        " --catalog "+config.getString("agGraph.CATALOG_ID")+
        " --input nquads catalyst_ds_"+user_id+" "+path
      val pb: ProcessBuilder = Process(process_str)
      val p = pb.run()
      result = p.exitValue()
    }catch {
      case e:Exception=>{e.printStackTrace()}
    }
    result
  }

/*  def AGRepositoryConnection(close: Boolean): AGRepositoryConnection = {
    var conn: AGRepositoryConnection = null
    try{
      val repository: AGRepository = AG_Config.catalog.createRepository(AG_Config.repository_id)
      repository.initialize()
      conn = repository.getConnection()
      AG_Config.closeBeforeExit(conn)
      if (close) { // tidy up
        conn.close()
        repository.shutDown()
        AG_Config.server.close()
      }
    }catch{
      case e:Exception=>
        e.printStackTrace()
    }
    conn
  }*/
  def stringTest1(s:String) : String = {
    val re =  stringTest(s,0)
    re
  }
def stringTest(s:String,ind :Int): String ={
  var result : String = null
  var rs = s
  var charArr = s.toCharArray
  if(charArr.indexOf('[') != -1)
  for (i <- ind until charArr.length) {
    println(s"$i is ${charArr(i)}")
    if(charArr(i).equals('[')){
      if(ind != charArr.length)
        stringTest(s,i)
      else return s
    }else if(charArr(i).equals(']')){
      var slic = s.slice(ind-1,i)
      var repeat = s.slice(ind+1,i-1)
      var r_String = ""
      val repeat_time = charArr(ind-1).toInt
      for(j <- 0 until  repeat_time){
        r_String = r_String+repeat
      }
      rs= s.replaceAll(slic,r_String)
      stringTest(rs,i)
    }
  }else return s
  println(s)
  s
}
}
object AG_Config{

  val ROOT_CATALOG = 0
  val NAMED_CATALOG = 2
  private var catalogName : String = _
  private var catalogType = 0
  private var catalogURL :String = _
  private var repositoriesURL :String = _
  private var server: AGServer = _
  private var catalog: AGCatalog = _
  private var repository_id: String = _

  def toClose = new ArrayBuffer[RepositoryConnection]()

  def close(conn : RepositoryConnection): Unit ={
    try
      conn.close()
    catch {
      case e: Exception =>
        System.err.println("Error closing repository connection: " + e)
        e.printStackTrace()
    }
  }

  def closeBeforeExit(conn:RepositoryConnection): Unit ={
    toClose += conn
  }

  def closeAll(): Unit ={
    while (!toClose.isEmpty){
      val conn: RepositoryConnection = toClose(0)
      close(conn)
      toClose.remove(0)
    }
  }
}
