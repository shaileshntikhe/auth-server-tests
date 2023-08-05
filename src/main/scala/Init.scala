import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

object Init {

  val httpclient = HttpClients.createDefault()

  def main(args: Array[String]): Unit = {
    val baseUrl = "https://5916bbd4-6d23-4347-9e5a-400395f69bd8.mock.pstmn.io"
    val getUrl = s"$baseUrl/api/v1/get_data"
    val postUrl = s"$baseUrl/api/v1/get_token"
  }



}
