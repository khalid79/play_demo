package controllers

import play.api._
import play.api.mvc._

import dao._

object Application extends Controller {

	def uploadFile = Action { request =>
		request.body.asMultipartFormData match {
			case Some(form) if (form.files.size > 0) =>
			form.files.map{ file => 
				Logger.info(" -------------> file        : " + file)
				Logger.info(" -------------> Filename    : " + file.filename)
				Logger.info(" -------------> contentType : " + file.contentType.get )
				Logger.info(" -------------> upload      : " + Mongo.addFile(file))
			}
		}
		Ok("File has been uploaded")
	}

	def allFiles = Action { request =>
		var html = "<ul>"
		Mongo.gridfs.toList.map { file =>
			val filename = file.filename.get
			html += "<li> <a href='/file/%s' > %s </a> </li>".format(filename,filename)
		}
		html += "</ul>"
		Ok(html).as(HTML)
	}

	def file(filename: String) = Action { implicit request => 
		Mongo.retrieve(filename).map { file =>
			Ok(scala.tools.nsc.io.Streamable.bytes(file.inputStream)).withHeaders(
				"Content-Type" -> file.contentType.getOrElse("")
			)
		} getOrElse NotFound
	}

}