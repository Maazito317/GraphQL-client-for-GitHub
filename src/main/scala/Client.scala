import scala.io.StdIn
import util.control.Breaks._
import Querent._
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

object Client {
  def main(args: Array[String]): Unit = {
    //Instance of logger created
    val logger = LoggerFactory.getLogger(classOf[Querent])

    //Config file loaded and API URL and authorization key extracted
    val conf = ConfigFactory.load()
    val gqlHost = conf.getString("http-config.BASE_GHQL_URL")
    logger.info("API URL loaded from config file: " + gqlHost)
    val authToken = conf.getString(("http-config.Authorization"))
    logger.info("Authorization key loaded from config file: " + authToken)

    //Get username of Github user as input from console
    print("Enter User Name: ")
    val userName = StdIn.readLine().trim()

    //Create a Querent object for this Github user
    val userquerent = new Querent.UserQuerent(userName, gqlHost, authToken)
    //Query and print details for this Github user
    println(userquerent.search())

    breakable {
      while (true) {
        print("\n\nEnter 1 to run Query on One Repository of this user\n")
        print("Enter 2 to run query on All Repositories of this user\n")
        print("Enter 0 to exit\n")
        val selection = StdIn.readInt()
        selection match {
          case _ if selection == 1 => {
            print("Enter Repository Name: ")
            val repoName = StdIn.readLine().trim()
            logger.debug("Repository name: " + repoName)
            breakable{
              while(true) {
                print("\n\nEnter 1 to view Details of this repository\n")
                print("Enter 2 to view Languages of this repository\n")
                print("Enter 3 to view Issues of this repository\n")
                print("Enter 0 to go back to previous menu\n")
                val selection = StdIn.readInt()
                selection match {
                  case _ if selection == 0 => {
                    break()
                  }
                  case _ => {
                    val repoquerent = new Querent.RepoQuerent(userName, repoName, gqlHost, authToken)
                    logger.debug("Selection: " + selection)
                    println(repoquerent.search(selection))
                  }
                }
              }
            }
          }
          case _ if selection == 2 => {
            breakable{
              while(true) {
                print("\n\nEnter 1 to view Names of Repositories\n")
                print("Enter 2 to view Repository with Most Issues\n")
                print("Enter 3 to view Repository with Most Forks\n")
                print("Enter 4 to view Repository with Most Stargazers\n")
                print("Enter 0 to go back to previous menu\n")
                val selection = StdIn.readInt()
                selection match {
                  case _ if selection == 0 => {
                    break()
                  }
                  case _ => {
                    val reposquerent = new Querent.ReposQuerent(userName, gqlHost, authToken)
                    logger.debug("Selection: " + selection)
                    println(reposquerent.search(selection))
                  }
                }
              }
            }
          }
          case _ if selection == 0 => {
            break()
          }
        }
      }
    }
  }
}
