import com.typesafe.config.Config
import org.apache.http.HttpEntity
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{mock, when}
import org.scalatest.FreeSpec
import org.scalatest.Matchers.convertToAnyShouldWrapper

import java.nio.file.{Files, Paths}
import scala.util.Random

class CinchyServiceTest extends FreeSpec {

  "CinchyServiceTest" - {

    "fetchData should write data to file" in {
      val mockedHttpClient = mock(classOf[HttpClient])
      val mockedConfig = mock(classOf[Config])

      when(mockedHttpClient.post(anyString(), any[Map[String, String]](), any[Seq[(String, String)]], any[Function1[HttpEntity, String]]()))
        .thenReturn(200 -> """{"access_token": "some_token"}""")

      val tempPath = s"""/tmp/${Random.nextInt(10000)}.txt"""
      when(mockedConfig.getString(anyString())).thenReturn(tempPath)

      when(mockedHttpClient.get(anyString(), any[Map[String, String]](), any[Function1[HttpEntity, String]]()))
        .thenReturn(200 -> """ABCD\nPQRS""")

      val cinchyService = new CinchyService()
      cinchyService.fetchData(mockedHttpClient, mockedConfig) shouldBe true
      Files.deleteIfExists(Paths.get(tempPath))
    }

    "fetchData should not write data to file" in {
      val mockedHttpClient = mock(classOf[HttpClient])
      val mockedConfig = mock(classOf[Config])

      when(mockedHttpClient.post(anyString(), any[Map[String, String]](), any[Seq[(String, String)]], any[Function1[HttpEntity, String]]()))
        .thenReturn(400 -> """{"message": "invalid auth data"}""")
      when(mockedConfig.getString(anyString())).thenReturn("")

      val cinchyService = new CinchyService()
      cinchyService.fetchData(mockedHttpClient, mockedConfig) shouldBe false
    }

    "getAuthToken should return token" in {
      val mockedHttpClient = mock(classOf[HttpClient])
      val mockedConfig = mock(classOf[Config])

      when(mockedHttpClient.post(anyString(), any[Map[String, String]](), any[Seq[(String, String)]], any[Function1[HttpEntity, String]]()))
        .thenReturn(200 -> """{"access_token": "some_token"}""")
      when(mockedConfig.getString(anyString())).thenReturn("")

      val cinchyService = new CinchyService()
      cinchyService.getAuthToken(mockedHttpClient, mockedConfig) shouldBe Some("some_token")
    }

    "getAuthToken should not return token" in {
      val mockedHttpClient = mock(classOf[HttpClient])
      val mockedConfig = mock(classOf[Config])

      when(mockedHttpClient.post(anyString(), any[Map[String, String]](), any[Seq[(String, String)]], any[Function1[HttpEntity, String]]()))
        .thenReturn(400 -> """{"message": "invalid auth data"}""")
      when(mockedConfig.getString(anyString())).thenReturn("")

      val cinchyService = new CinchyService()
      cinchyService.getAuthToken(mockedHttpClient, mockedConfig) shouldBe None
    }

    "writeData should write to file" in {
      val mockedHttpClient = mock(classOf[HttpClient])
      val mockedConfig = mock(classOf[Config])

      when(mockedHttpClient.get(anyString(), any[Map[String, String]](), any[Function1[HttpEntity, String]]()))
        .thenReturn(200 -> """ABCD\nPQRS""")
      when(mockedConfig.getString(anyString())).thenReturn("")

      val cinchyService = new CinchyService()
      val tempPath = s"""/tmp/${Random.nextInt(10000)}.txt"""
      cinchyService.writeData(mockedHttpClient, tempPath, "test_token", "test_url") shouldBe true
      Files.deleteIfExists(Paths.get(tempPath))
    }

    "writeData should not write to file" in {
      val mockedHttpClient = mock(classOf[HttpClient])
      val mockedConfig = mock(classOf[Config])

      when(mockedHttpClient.get(anyString(), any[Map[String, String]](), any[Function1[HttpEntity, String]]()))
        .thenReturn(400 -> """{"message": "incorrect auth token"}""")
      when(mockedConfig.getString(anyString())).thenReturn("")

      val cinchyService = new CinchyService()
      val tempPath = s"""/tmp/${Random.nextInt(10000)}.txt"""
      cinchyService.writeData(mockedHttpClient, tempPath, "test_token", "test_url") shouldBe false
      Files.deleteIfExists(Paths.get(tempPath))
    }

  }
}
