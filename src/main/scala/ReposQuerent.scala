package Querent

import com.typesafe.config.ConfigFactory
import org.json4s.jackson.JsonMethods.parse
import org.json4s.jackson.Serialization.write
import org.slf4j.LoggerFactory

import scala.collection.mutable.HashMap
import scala.io.Source.fromInputStream

case class AllRepoDetails[T](repositories:Edges[T])
case class Count(totalCount:Int)
case class Repos(name:String)
case class ReposIssues(name:String, issues:Count)
case class ReposForks(name:String, forks:Count)
case class ReposStargazers(name:String, stargazers:Count)
case class ReposLanguages(languages:Edges[Language])

/**
 * Class extends the Querent class to get information for repositories of a particular Github user
 *
 * @param userName Github username
 * @param baseUrl for the API passed to parent Querent
 * @param authToken used to authenticate with API passed to parent Querent
 */
class ReposQuerent(userName: String, baseUrl: String, authToken: String) extends Querent(baseUrl, authToken) {
  val baseQuery = "{user (login:USERNAME) {repositories (first:REPONUM) {edges {node {QUERY_TYPE}}}}}"
  val queryTypes = HashMap(1 -> "name", 2 -> "name issues {totalCount}", 3 -> "name forks {totalCount}", 4 -> "name stargazers {totalCount}")

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
      this.baseQuery.replace("USERNAME", this.userName)
        .replace("REPONUM", conf.getString("query-vars.repo-count"))
        .replace("QUERY_TYPE", this.queryTypes.getOrElse(qt, "name")))
    implicit val formats = this.formats
    response.getEntity match {
      case null => "Empty Response"
      case x if x != null => {
        val respJson = fromInputStream(x.getContent).getLines.mkString
        logger.debug(respJson)
        qt match {
          case _ if qt == 1 => {
            val viewer = parse(respJson).extract[Data[AllRepoDetails[Repos]]]
            val names = viewer.data.user.repositories.edges.map(e => parse(write(e.node)).extract[Repos].name)
            (names.mkString("\n"))
          }
          case _ if qt == 2 => {
            val viewer = parse(respJson).extract[Data[AllRepoDetails[ReposIssues]]]
            val repoNameIssues = viewer.data.user.repositories.edges.map(e => {
              val ri = parse(write(e.node)).extract[ReposIssues]
              (ri.name, ri.issues.totalCount)
            })
            val repoWithMax = repoNameIssues.maxBy(e => e._2)
            ("Repository Name: " + repoWithMax._1 + " ---- Issues: " + repoWithMax._2)
          }
          case _ if qt == 3 => {
            val viewer = parse(respJson).extract[Data[AllRepoDetails[ReposForks]]]
            val repoNameForks = viewer.data.user.repositories.edges.map(e => {
              val ri = parse(write(e.node)).extract[ReposForks]
              (ri.name, ri.forks.totalCount)
            })
            val repoWithMax = repoNameForks.maxBy(e => e._2)
            ("Repository Name: " + repoWithMax._1 + " ---- Forks: " + repoWithMax._2)
          }
          case _ if qt == 4 => {
            val viewer = parse(respJson).extract[Data[AllRepoDetails[ReposStargazers]]]
            val repoNameStars = viewer.data.user.repositories.edges.map(e => {
              val ri = parse(write(e.node)).extract[ReposStargazers]
              (ri.name, ri.stargazers.totalCount)
            })
            val repoWithMax = repoNameStars.maxBy(e => e._2)
            ("Repository Name: " + repoWithMax._1 + " ---- Stars: " + repoWithMax._2)
          }

        }
      }
    }
  }
}