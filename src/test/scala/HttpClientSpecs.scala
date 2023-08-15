import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet, HttpPost}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.message.{BasicNameValuePair, BasicStatusLine}
import org.apache.http.util.EntityUtils
import org.apache.http.{HttpEntity, HttpStatus, NameValuePair, ProtocolVersion}
import org.mockito.ArgumentMatcher
import org.mockito.ArgumentMatchers.argThat
import org.mockito.Mockito.{mock, when}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import scala.collection.JavaConverters.seqAsJavaListConverter

class HttpClientSpecs extends AnyFlatSpec with BeforeAndAfterAll {

  behavior of "HttpClientTest"

  it should "return data when passed correct bearer token" in {
    val requestUrl = "http://localhost/api/v1/get_data"
    val requestHeaders = Map("Authorization" -> "Bearer some_token")
    val mockedResponseBody = new StringEntity("mocked response body")
    val mockedStatus = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_OK, null)

    val mockedHttpClient = mock(classOf[CloseableHttpClient])
    val mockedResponse = mock(classOf[CloseableHttpResponse])
    val client = new HttpClient(mockedHttpClient)

    when(mockedResponse.getStatusLine).thenReturn(mockedStatus)
    when(mockedResponse.getEntity).thenReturn(mockedResponseBody)

    val request = new HttpGet(requestUrl)
    requestHeaders.foreach { case (k, v) => request.addHeader(k, v) }

    class HttpGetMatcher(expected: HttpGet) extends ArgumentMatcher[HttpGet] {
      override def matches(argument: HttpGet): Boolean = {
        val sameUrl = argument.getURI == expected.getURI
        val headersFromFirstRequest = argument.getAllHeaders.map(h => s"${h.getName}:${h.getValue}").toSet
        val headersFromSecondRequest = expected.getAllHeaders.map(h => s"${h.getName}:${h.getValue}").toSet
        val sameHeaders = headersFromFirstRequest == headersFromSecondRequest
        sameUrl && sameHeaders
      }
    }

    when(mockedHttpClient.execute(argThat(new HttpGetMatcher(request)))).thenReturn(mockedResponse)

    val (statusCode, body) = client.get(requestUrl, requestHeaders, EntityUtils.toString)
    statusCode shouldBe 200
    body shouldBe "mocked response body"
  }

  it should "return access_token when passed correct auth data" in {
    val requestUrl = "https://localhost/api/v1/generate_token"
    val mockedResponseBody = """{"access_token": "some_access_token"}"""
    val mockedEntity = new StringEntity(mockedResponseBody)
    val data = Seq(
      "username" -> "username",
      "Password" -> "Password",
      "client_id" -> "client_id",
      "client_secret" -> "client_secret",
      "grant_type" -> "grant_type",
      "scope" -> "scope"
    )
    val charSet = "UTF-8"
    val requestHeaders = Map.empty[String, String]
    val mockedHttpClient = mock(classOf[CloseableHttpClient])
    val mockedResponse = mock(classOf[CloseableHttpResponse])
    val mockedHttpStatus = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), HttpStatus.SC_OK, null)

    when(mockedResponse.getStatusLine).thenReturn(mockedHttpStatus)
    when(mockedResponse.getEntity).thenReturn(mockedEntity)

    class HttpPostMatcher(expected: HttpPost) extends ArgumentMatcher[HttpPost] {
      override def matches(argument: HttpPost): Boolean = {
        val sameUrl = argument.getURI == expected.getURI
        val headersFromFirstRequest = argument.getAllHeaders.map(h => s"${h.getName}:${h.getValue}").toSet
        val headersFromSecondRequest = expected.getAllHeaders.map(h => s"${h.getName}:${h.getValue}").toSet
        val bodyMatches = argument.getEntity.toString == expected.getEntity.toString
        val sameHeaders = headersFromFirstRequest == headersFromSecondRequest
        sameUrl && sameHeaders && bodyMatches
      }
    }

    val request = new HttpPost(requestUrl)
    requestHeaders.foreach { case (k, v) => request.addHeader(k, v) }
    val keyValuePairs: Seq[NameValuePair] = data.map { case (k, v) => new BasicNameValuePair(k, v) }
    val requestBody = new UrlEncodedFormEntity(keyValuePairs.asJava, charSet)
    request.setEntity(requestBody)

    when(mockedHttpClient.execute(argThat(new HttpPostMatcher(request)))).thenReturn(mockedResponse)

    val httpClient = new HttpClient(mockedHttpClient)
    val (statusCode, body) = httpClient.post(
      requestUrl,
      requestHeaders,
      data,
      (entity: HttpEntity) => scala.io.Source.fromInputStream(entity.getContent).mkString
    )

    statusCode shouldBe 200
    body shouldBe mockedResponseBody
  }

}
