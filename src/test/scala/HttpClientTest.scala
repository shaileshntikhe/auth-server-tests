import org.apache.http.{HttpStatus, ProtocolVersion}
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.{CloseableHttpClient, HttpClients}
import org.apache.http.message.BasicStatusLine
import org.apache.http.util.EntityUtils
import org.json4s.native.JsonParser
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import org.json4s._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
//import org.json4s.jackson.JsonMethods._

class HttpClientITest extends AnyFlatSpec with BeforeAndAfterAll {

  behavior of "HttpClientTest"

  it should "return get response" in {
    val mockedHttpClient = mock(classOf[CloseableHttpClient])
    val mockedResponse = mock(classOf[CloseableHttpResponse])
    val client = new HttpClient(mockedHttpClient)
    val mockedResponseBody = new StringEntity("mock response")
    val mockedStatus = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_OK, null)

    when(mockedResponse.getStatusLine).thenReturn(mockedStatus)
    when(mockedResponse.getEntity).thenReturn(mockedResponseBody)
    when(mockedHttpClient.execute(any(classOf[HttpGet]))).thenReturn(mockedResponse)

    val (statusCode, body) = client.get(
      "http://localhost/api/v1/get_data",
      Map("Authorization" -> "Bearer some_token"),
      EntityUtils.toString
    )
    statusCode shouldBe 200
    body shouldBe "ABCD\\nPQRS"
  }

  it should "return post response" in {
    val client = new HttpClient(HttpClients.createDefault())
    val (statusCode, body) = client.post(
      "https://5916bbd4-6d23-4347-9e5a-400395f69bd8.mock.pstmn.io/api/v1/get_token",
      Map.empty[String, String],
      Seq(
        "username" -> "username",
        "password" -> "password",
        "client_id" -> "client_id",
        "client_secret" -> "client_secret",
        "grant_type" -> "grant_type",
        "scope" -> "scope"
      ),
      EntityUtils.toString
    )
    statusCode shouldBe 200
    body shouldBe """{"auth_token": "abcd"}"""
  }

  it should "create resources" in {
    implicit val formats = DefaultFormats
    val client = new HttpClient(HttpClients.createDefault())

    def createResource: Option[Resource] = {
      val (statusCode, body) = client.postJson(
        "http://localhost:8181/bpocore/market/api/v1/resources",
        Map("Content-Type" -> "application/json"),
        """
          |{
          |   "resourceTypeId": "example.resourceTypes.ChildResource1",
          |   "title": "Child resource 1",
          |   "active": true,
          |   "domainId": "built-in",
          |   "productId": "856e6dd0-3a77-11ee-83a7-29b374b0cfce",
          |   "providerData": {
          |       "template": "example.serviceTemplates.ChildResource1"
          |   }
          |}
          |""".stripMargin,
        EntityUtils.toString
      )
      val json = JsonParser.parse(body)
      for {
        id <- (json \ "id").extractOpt[String]
        productId <- (json \ "productId").extractOpt[String]
        resourceTypeId <- (json \ "resourceTypeId").extractOpt[String]
      } yield Resource(id, productId, resourceTypeId)
    }

    val resources = (1 to 100).flatMap { _ => createResource }
    println(s"Created resources:")
    resources.foreach(r => println(r.toString))

  }

}

case class Resource(id: String, productId: String, resourceTypeId: String) {
  override def toString: String = {
    s"""{ resourceId: "$id", productId: "$productId", resourceTypeId: "$resourceTypeId"}"""
  }
}