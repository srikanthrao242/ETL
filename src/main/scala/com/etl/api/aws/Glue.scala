package com.etl.api.aws

import java.util
import java.util.concurrent.Future

import com.amazonaws.auth.{AWSCredentials, AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.Regions
import com.amazonaws.services.ec2.model.{DescribeSecurityGroupsRequest, DescribeVpcsRequest}
import com.amazonaws.services.glue
import com.amazonaws.services.glue.model._
import com.amazonaws.services.guardduty.model.SecurityGroup
import com.amazonaws.services.rds.model.Filter
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._
import scala.util.control.Breaks._

class Glue{

  println("in Glue")

  val config: Config = ConfigFactory.load()
  val bucket = "pathtobucet"
  val filepath = "pathtocsv"
  val credentials : AWSCredentials= new BasicAWSCredentials(
    config.getString("awss3.accessKey"),
    config.getString("awss3.secretAccessKey")
  )

  val glueClient = glue.AWSGlueClientBuilder.standard()
                      .withCredentials(new AWSStaticCredentialsProvider(credentials))
                      .withRegion(Regions.US_EAST_1)
                      .build()


 /* //creating database
  var createDatabaseRequest = new CreateDatabaseRequest
  //createDatabaseRequest.setCatalogId("sampleDB")
  var databaseInput = new DatabaseInput
  databaseInput.setDescription("testing from scala creation of database")
  databaseInput.setName("sampledbi")
  createDatabaseRequest.setDatabaseInput(databaseInput)
  glueClient.createDatabase(createDatabaseRequest)

  //creating table with existing database
  var createTableRequest = new CreateTableRequest
  createTableRequest.withDatabaseName("sampledbi")
  var tableInput = new TableInput
  tableInput.setName("sampletestscala")
  tableInput.setDescription("testing from scala table creation")
  createTableRequest.setTableInput(tableInput)
  glueClient.createTable(createTableRequest)*/

  //creating connections
  var createConnectionRequest = new CreateConnectionRequest
  var connectionInput = new ConnectionInput

  connectionInput.setName("scalaToRDS")
  connectionInput.setConnectionType("JDBC")
  connectionInput.setDescription("testing creating connection from scala")

  var connectionProperties = new util.HashMap[String,String]()
  connectionProperties.put("HOST","host")
  connectionProperties.put("USERNAME","etluser")
  connectionProperties.put("PASSWORD","etluser")
  connectionProperties.put("DATABASE","db")
  connectionInput.setConnectionProperties(connectionProperties)



  var physicalConnectionRequirements = new PhysicalConnectionRequirements
  physicalConnectionRequirements.withSubnetId("subnetid")
  var securityGoupIdList = new util.ArrayList[String]()
  securityGoupIdList.add("subgrouplist")
  physicalConnectionRequirements.setSecurityGroupIdList(securityGoupIdList)
  physicalConnectionRequirements.withAvailabilityZone("vpc")


  var vpcRequest = new DescribeVpcsRequest
  vpcRequest.withVpcIds("vpcid")
  val secGroupName = "grupname"
  var mySG :SecurityGroup = _
  var vpcID = "vpcid"

  var vpcFilter :Filter = new Filter()
  var dsgRequest = new DescribeSecurityGroupsRequest();
  connectionInput.setPhysicalConnectionRequirements(new PhysicalConnectionRequirements)


  createConnectionRequest.setConnectionInput(connectionInput)

 // glueClient.createConnection(createConnectionRequest)
  val  getPlanRequest = new GetPlanRequest
  getPlanRequest.setLanguage("SCALA")

  val catalogEntry_source = new CatalogEntry
  catalogEntry_source.setDatabaseName("db")
  catalogEntry_source.setTableName("tablename")
  getPlanRequest.setSource(catalogEntry_source)

  /*val catalogEntry_sink = new CatalogEntry
  catalogEntry_sink.setDatabaseName("***")
  catalogEntry_sink.setTableName("gluetest_folder")
  var sink = new java.util.ArrayList[CatalogEntry]()
  sink.add(catalogEntry_sink)*/
  //getPlanRequest.setSinks(sink)

  val location = new Location
  var codeGenNodeArg_arr = new util.ArrayList[CodeGenNodeArg]()
  var codeGenNodeArg1 = new CodeGenNodeArg
  codeGenNodeArg1.setName("catalog_connection")
  codeGenNodeArg1.setValue("RdsDBConnection")
  codeGenNodeArg_arr.add(codeGenNodeArg1)
  var codeGenNodeArg2 = new CodeGenNodeArg
  codeGenNodeArg2.setName("database")
  codeGenNodeArg2.setValue("")
  codeGenNodeArg_arr.add(codeGenNodeArg2)
  var codeGenNodeArg3 = new CodeGenNodeArg
  codeGenNodeArg3.setName("dbtable")
  codeGenNodeArg3.setValue("gluetest_folder")
  codeGenNodeArg_arr.add(codeGenNodeArg3)
  location.setJdbc(codeGenNodeArg_arr)
 /* var codeGenNodeArg_arr_s3 = new util.ArrayList[CodeGenNodeArg]()
  var codeGenNodeArgS3 = new CodeGenNodeArg
  codeGenNodeArgS3.setName("LOCATION")
  codeGenNodeArgS3.setValue("s3://glue/folder/")
  codeGenNodeArg_arr_s3.add(codeGenNodeArgS3)
  location.setS3(codeGenNodeArg_arr_s3)*/
  getPlanRequest.setLocation(location)

  var getMappingRequest = new GetMappingRequest

  getMappingRequest.setLocation(location)
  getMappingRequest.setSource(catalogEntry_source)

  var mapp: GetMappingResult = glueClient.getMapping(getMappingRequest)



 /* val mappingEntry = new MappingEntry
  mappingEntry.setSourceTable("sales_folder")
  mappingEntry.setSourcePath("s3://-glue/_folder/db")
  mappingEntry.setSourceType("S3")
  mappingEntry.setTargetTable("gluetest_folder")
  mappingEntry.setTargetPath("RdsDBConnection")
  mappingEntry.setTargetType("JDBC")*/
  //var mapping = new java.util.ArrayList[MappingEntry]()
  //mapping.add(mappingEntry)
  getPlanRequest.setMapping(mapp.getMapping)




  var res: GetPlanResult = glueClient.getPlan(getPlanRequest)

  var scalacode: String = res.getScalaCode

  println(scalacode)

 /* //creating Crawlwer
  var s3Target : S3Target = new S3Target
  s3Target.setPath("s3://-glue/_folder")
  var targets :CrawlerTargets = new CrawlerTargets
  var target_arr = new java.util.ArrayList[S3Target]()
  target_arr.add(s3Target)
  targets.setS3Targets(target_arr)



  var req : CreateCrawlerRequest = new CreateCrawlerRequest
  req.setDatabaseName("sampledbi")
  req.setDescription("Trying to read data from s3 and create table in hive to move data to rds db")
  req.setName("testfromscala")
  req.setRole("aws-glue")
  req.setTablePrefix("glueTest")
  req.setTargets(targets)
  val crawlers = glueClient.getCrawlers(new GetCrawlersRequest).getCrawlers.asScala
  var isCrawlerExists = false
  breakable{
    crawlers.foreach(craw =>{
      if(craw.getName.equals("testfromscala")){
        isCrawlerExists = true
        break()
      }
    })
  }
  if(!isCrawlerExists){
    glueClient.createCrawler(req)
  }
  var startCrawlerRequest = new StartCrawlerRequest
  startCrawlerRequest.setName("testfromscala")
  val startCrawlerRequest_ = glueClient.startCrawler(startCrawlerRequest)*/

   println("successfull")


  //println("crawler created successfully"+request.toString)

  //creating job
 /* var jobRequest = new CreateJobRequest
  jobRequest.setName("sampleJob")
  jobRequest.setRole("aws-glue")
  jobRequest.setDescription("simple job testing from scala")
  jobRequest.setConnections(new ConnectionsList)
  val jobCommand = new JobCommand
  jobCommand.setName("TestGlue")
  jobCommand.setScriptLocation("s3://aws-glue-scripts-600053779239-us-east-1//TestGlue")


  jobRequest.setCommand(jobCommand)


  val job = glueClient.createJob(jobRequest)

*/


}
