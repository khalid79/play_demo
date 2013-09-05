package dao

import play.api.mvc.MultipartFormData.FilePart
import play.api.libs.Files._
import play.api.Logger
import play.api.Play

import java.io.File

import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.MongoDB
import com.novus.salat._
import com.mongodb.casbah.Imports._

import com.mongodb.ServerAddress

case class MongoDBHelper(
  dbName: String, 
  servers: List[ServerAddress] = List(new ServerAddress("localhost", 27017))) {
  lazy val connection = if(servers.size > 1) { 
    MongoConnection(servers)
  } else {
    // Mongodb need to be called with MongoConnection(ServerAddress) if not
    // using replicaset
    MongoConnection(servers.head)
  }

  def db: MongoDB = connection(dbName)
  def collection(name:String): MongoCollection = db(name)
}

object Mongo {
  val DEFAULT_HOST="localhost"
  val DEFAULT_PORT=27017

  lazy val helper: MongoDBHelper = {
    Logger.info("init helper")

    val app = Play.current

    val servers = app.configuration.getConfig("mongodb.servers") match {
      case Some(config) => {
        config.keys.toList.sorted.map(_.span(_ != '.')._1).toSet.map { name :String =>
          new ServerAddress(
            config.getString(name + ".host").getOrElse(Mongo.DEFAULT_HOST),
            config.getInt(name    + ".port").getOrElse(Mongo.DEFAULT_PORT)
          )
        }
      }

      case _ => Set(
        new ServerAddress(app.configuration.getString("mongodb.host").
                              getOrElse(Mongo.DEFAULT_HOST),
                          app.configuration.getInt("mongodb.port").
                              getOrElse(Mongo.DEFAULT_PORT))
      )
    }

    val result = MongoDBHelper(app.configuration.getString("mongodb.db").get,
                               servers.toList)

    result
  }

  import com.mongodb.casbah.gridfs._

  lazy val gridfs = GridFS(helper.db)

  def connection: MongoConnection = helper.connection

  def db: MongoDB = helper.db

  def addFileWithName(file: File, filename:Option[String] = None):Option[ObjectId] = {
      val gfif = gridfs.createFile(file)
      gfif.filename = filename.getOrElse(file.getName)
      gfif.contentType = "application/octet-stream"
      gfif.save
      gfif._id
  }

  def addFile(file: FilePart[TemporaryFile], filename:Option[String] = None):Option[ObjectId] = {
      val gfif = Mongo.gridfs.createFile(file.ref.file)
      gfif.filename = filename.getOrElse(file.filename)
      gfif.contentType = file.contentType.getOrElse("")
      gfif.save
      gfif._id
  }

  def retrieve(filename: String): Option[GridFSDBFile] = {
      gridfs.findOne(filename)
  }

}

