import com.typesafe.config.Config
import org.apache.http.util.EntityUtils
import org.json4s._
import org.json4s.native.JsonMethods._

import java.io.PrintWriter

class CinchyService() {

  def fetchData(httpClient: HttpClient, config: Config): Boolean = {
    val outputPath = config.getString("")
    getAuthToken(httpClient, config) match {
      case Some(token) =>
        writeData(httpClient, outputPath, token, "")
      case _ =>
        // replace with log.error
        println(s"Could not get auth token")
        false
    }
  }

  def getAuthToken(httpClient: HttpClient, config: Config): Option[String] = {
    val loginEndpoint = config.getString("")
    val username = config.getString("")
    val password = config.getString("")
    val clientId = config.getString("")
    val clientSecret = config.getString("")
    val grantType = config.getString("")
    val scope = config.getString("")
    val (statusCode, body) = httpClient.post(
      loginEndpoint,
      Map.empty[String, String],
      Seq("username" -> username, "password" -> password, "client_id" -> clientId, "client_secret" -> clientSecret, "grant_type" -> grantType, "scope" -> scope),
      EntityUtils.toString
    )
    if(statusCode == 200) {
      implicit val formats = DefaultFormats
      val parsedJson = parse(body)
      val token = (parsedJson \ "access_token").extractOpt[String]
      if(token.isEmpty) {
        // replace with log.warn
        println(s"Could not extract token from body: $body")
      }
      token
    } else {
      // replace with log.warn
      println(s"Received $statusCode, response body is: [$body]")
      None
    }
  }

  def writeData(httpClient: HttpClient, path: String, token: String, url: String): Boolean = {
    val (status, body) = httpClient.get(url, Map("Authorization" -> s"Bearer $token"), EntityUtils.toString)
    if (status == 200) {
      val printWriter = new PrintWriter(path)
      printWriter.write(body)
      printWriter.flush()
      printWriter.close()
      true
    } else {
      // replace with log.warn
      println(s"Got $status, response body: [$body]")
      false
    }
  }

}
