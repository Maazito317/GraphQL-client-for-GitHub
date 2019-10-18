package Querent

import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.write
import org.slf4j.LoggerFactory

import scala.io.Source.fromInputStream

case class UserAllDetails(email:String, login:String, url:String, location:String, avatarUrl:String, bio:String, company:String, name:String)

/**
 * Class extends the Querent class to get information for a particular Github user
 *
 * @param username Github username
 * @param baseUrl for the API passed to parent Querent
 * @param authToken used to authenticate with API passed to parent Querent
 */
class UserQuerent(username: String, baseUrl: String, authToken: String) extends Querent(baseUrl, authToken) {
  //Base query string with common fields where username is replaced by the input from console
  val baseQuery = "{user (login:USERNAME) {email login url location avatarUrl bio company name}}"

  /**
   * Execute the HTTP query to get information about provided username, parse the response and build an output string with
   * the information to display on console
   *
   * @return string with information about user to display on console
   */
  def search(): String = {
    val logger = LoggerFactory.getLogger(classOf[Querent])

    //Replace username with provided input and get the response of the executed query
    val response = this.makeQuery(this.baseQuery.replace("USERNAME", this.username))
    implicit val formats = this.formats
    response.getEntity match {
      case null => "Empty Response"
      case x if x != null => {
        val respJson = fromInputStream(x.getContent).getLines.mkString
        logger.debug(respJson)
        //Extract user data from Json Response
        val viewer = parse(respJson).extract[Data[UserAllDetails]]
        val userDetails = viewer.data.user
        try {
          //Build console output from the parsed user data
          Array("The following are the fetched details:\nName: ", userDetails.name, "\nEmail: ", userDetails.email, "\nCompany: ", userDetails.company, "\nBio: ", userDetails.bio, "\nURL: ", userDetails.url).mkString("")
        } catch {
          case e: NullPointerException => {
            logger.error("Username " + username + " does not exist.")
            "Try another username"
          }
        }
      }
    }
  }
}
