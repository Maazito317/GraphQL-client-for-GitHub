package Querent

import com.typesafe.config.ConfigFactory
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.write
import org.slf4j.LoggerFactory

import scala.collection.mutable.HashMap
import scala.io.Source.fromInputStream

case class Repo[T](repository:T)
case class OneRepoDetails(name:String, description:String, url:String, createdAt:String, updatedAt:String)
case class OneRepoLanguages(languages: Edges[Language])
case class OneRepoIssues(issues: Edges[Issue])
case class Issue(author:Author, body:String)
case class Author(login:String)

/**
 * Class extends the Querent class to get information for a particular repository of a Github user
 *
 * @param userName Github username
 * @param repoName name of the repository
 * @param baseUrl for the API passed to parent Querent
 * @param authToken used to authenticate with API passed to parent Querent
 */
class RepoQuerent(userName:String, repoName: String, baseUrl: String, authToken: String) extends Querent(baseUrl, authToken) {
  val baseQuery = "{user (login:USERNAME) {repository (name:REPONAME) {QUERY_TYPE}}}"
  val queryTypes = HashMap(
    1 -> "name description url createdAt updatedAt",
    2 -> "languages (first:LANGCOUNT) {edges {node {name}}}",
    3 -> "issues (first:ISSUECOUNT) {edges {node {author {login} body}}}")

  /**
   * Map the user selection to the desired query, execute the HTTP query to get information about repositories of the user,
   * parse the response and build an output string with the information to display on console
   *
   * @return string with information about repositories to display on console
   */
  def search(qt: Int) = {
    val logger = LoggerFactory.getLogger(classOf[Querent])
    val conf = ConfigFactory.load()
    val response = this.makeQuery(
      this.baseQuery
        .replace("USERNAME", this.userName)
        .replace("REPONAME", this.repoName)
        .replace("QUERY_TYPE", this.queryTypes.getOrElse(qt, ""))
        .replace("LANGCOUNT", conf.getString("query-vars.language-count"))
        .replace("ISSUECOUNT", conf.getString("query-vars.issue-count"))
    )

    implicit val formats = this.formats
    response.getEntity match {
      case null => "Empty Response"
      case x if x != null => {
        val respJson = fromInputStream(x.getContent).getLines.mkString
        logger.debug(respJson)
        qt match {
          case qt if qt == 1 => {
            val viewer = parse(respJson).extract[Data[Repo[OneRepoDetails]]]
            val repoData = viewer.data.user.repository
            (Array(
              "==========",
              repoData.name + " (" + repoData.url + ")",
              repoData.createdAt,
              repoData.description,
              repoData.updatedAt,
              "==========").mkString("\n"))
          }
          case qt if qt == 2 => {
            val viewer = parse(respJson).extract[Data[Repo[OneRepoLanguages]]]
            val repoData = viewer.data.user.repository.languages.edges.map(
              e => parse(write(e.node)).extract[Language].name)
            ("Repository uses the following languages: " + repoData.mkString(", "))
          }
          case qt if qt == 3 => {
            val viewer = parse(respJson).extract[Data[Repo[OneRepoIssues]]]
            val repoIssues = viewer.data.user.repository.issues.edges.map(e => {
              val issue = parse(write(e.node)).extract[Issue]
              (issue.author.login, issue.body)
            })
            val toPrint = repoIssues.map(e => e._1 + "\n" + e._2)
            (toPrint.mkString("\n==========\n"))
          }
        }
      }
    }
  }
}
