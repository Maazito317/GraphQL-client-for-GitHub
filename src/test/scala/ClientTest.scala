import com.typesafe.config.ConfigFactory
import junit.framework.TestCase
import org.scalatestplus.junit.AssertionsForJUnit

class ClientTest extends TestCase with AssertionsForJUnit {
  val conf = ConfigFactory.load()
  val gqlHost = conf.getString("http-config.BASE_GHQL_URL")
  val authToken = conf.getString(("http-config.Authorization"))
  val username = "leereilly"
  val reponame = "swot"

  def test_correct_user(): Unit =  {
    val userquerent = new Querent.UserQuerent(username, gqlHost, authToken)
    assert(userquerent.search() === "The following are the fetched details:\nName: Lee Reilly\nEmail: lee@github.com\nCompany: GitHub\nBio: Fights for the users.\nURL: https://github.com/leereilly")
  }

  def test_incorrect_user(): Unit = {
    val incorrect_username = "c"
    val userquerent = new Querent.UserQuerent(incorrect_username, gqlHost, authToken)
    assert(userquerent.search() === "Try another username")
  }

  def test_most_issues(): Unit = {
    val reposquerent = new Querent.ReposQuerent(username, gqlHost, authToken)
    assert(reposquerent.search(2) === "Repository Name: games ---- Issues: 192")
  }

  def test_most_forks(): Unit = {
    val reposquerent = new Querent.ReposQuerent(username, gqlHost, authToken)
    assert(reposquerent.search(3) === "Repository Name: games ---- Forks: 1696")
  }

  def test_most_stars(): Unit = {
    val reposquerent = new Querent.ReposQuerent(username, gqlHost, authToken)
    assert(reposquerent.search(4) === "Repository Name: games ---- Stars: 13442")
  }

  def test_repo_languages(): Unit = {
    val repoquerent = new Querent.RepoQuerent(username, reponame, gqlHost, authToken)
    assert(repoquerent.search(2) === "Repository uses the following languages: Ruby")
  }

}
