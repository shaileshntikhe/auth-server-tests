import com.typesafe.config.ConfigFactory
import org.apache.http.impl.client.HttpClients

object Init {



  def main(args: Array[String]): Unit = {
    val httpclient = HttpClients.createDefault()
    val wrapper = new HttpClient(httpclient)
    val config = ConfigFactory.load()
    val cinchyService = new CinchyService()
    val written = cinchyService.fetchData(wrapper, config)
    if(written) {
      println(s"Data fetch successful")
    } else {
      println(s"Failed to fetch data")
      System.exit(1)
    }
    wrapper.close()
  }



}
