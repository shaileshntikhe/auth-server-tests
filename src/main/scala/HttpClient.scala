import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.message.BasicNameValuePair
import org.apache.http.{HttpEntity, NameValuePair}

import scala.collection.JavaConverters._

class HttpClient(client: CloseableHttpClient) {

  def get[T](url: String, headers: Map[String, String], fn: HttpEntity => T): (Int, T) = {
    val request = new HttpGet(url)
    headers.foreach { case (k, v) => request.addHeader(k, v) }
    val response = client.execute(request)
    val statusCode = response.getStatusLine.getStatusCode
    val body = fn(response.getEntity)
    response.close()
    (statusCode, body)
  }

  def post[T](url: String, headers: Map[String, String], keyValues: Seq[(String, String)], fn: HttpEntity => T): (Int, T) = {
    val request = new HttpPost(url)
    headers.foreach { case (k, v) => request.addHeader(k, v) }
    val keyValuePairs: Seq[NameValuePair] = keyValues.map { case (k, v) => new BasicNameValuePair(k, v) }
    val requestBody = new UrlEncodedFormEntity(keyValuePairs.asJava, "UTF-8")
    request.setEntity(requestBody)
    val response = client.execute(request)
    val statusCode = response.getStatusLine.getStatusCode
    val body = fn(response.getEntity)
    response.close()
    (statusCode, body)
  }

  def close(): Unit = {
    client.close()
  }

}
