package Querent;

import com.typesafe.config.ConfigFactory
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.json4s.DefaultFormats
import org.slf4j.LoggerFactory


//Common case classes used by Querent child classes

//All outputs are contained in key data and user
case class Data[S](data:User[S])
case class User[S](user:S)
//Edge and node classes to implement all Connections (IssueConnection, LanguageConnection, etc)
case class Edges[T](edges:Array[Node[T]])
case class Node[T](node:T)
//Case class for language of repository
case class Language(name:String)

/**
 * Class that implements the common base functionality of building an HTTP Client and Request and receiving an HTTP response
 *
 * All classes that need to make HTTP queries should extend this class
 *
 * @constructor create a new Querent object with the provided configs
 * @param baseUrl for the API
 * @param authToken used to authenticate with API
 */
class Querent(baseUrl: String, authToken: String) {
  private val client = HttpClientBuilder.create.build()
  protected val formats = DefaultFormats

  def getFormats: DefaultFormats = formats

  /**
   * Function takes as input the query string, builds the HTTP client and creates an HTTP request
   * and returns the HTTP response
   *
   * @param query string specifying the information required
   * @return HttpResponse of HTTP query
   */
  def makeQuery(query: String): HttpResponse = {
    val logger = LoggerFactory.getLogger(classOf[Querent])
    logger.debug("Query string: " + query)
    val httpUriRequest = new HttpPost(this.baseUrl)
    httpUriRequest.addHeader("Authorization", this.authToken)
    val conf = ConfigFactory.load()
    httpUriRequest.addHeader("Accept", conf.getString("http-config.Accept_Type"))
    val gqlReq = new StringEntity("{\"query\":\"" + query + "\"}" )
    httpUriRequest.setEntity(gqlReq)
    client.execute(httpUriRequest)
  }
}
