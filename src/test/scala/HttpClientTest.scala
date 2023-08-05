import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

class HttpClientTest extends AnyFlatSpec with BeforeAndAfterAll {

  private lazy val httpClient = HttpClients.createDefault()

  override def afterAll(): Unit = {
    httpClient.close()
  }

  behavior of "HttpClientTest"

  it should "return get response" in {
    val client = new HttpClient(httpClient)
    val (statusCode, body) = client.get(
      "https://5916bbd4-6d23-4347-9e5a-400395f69bd8.mock.pstmn.io/api/v1/get_data",
      Map.empty[String, String],
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

}
