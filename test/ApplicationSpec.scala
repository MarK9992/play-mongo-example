import controllers.PersonController
import org.joda.time.{DateTimeZone, DateTime, DateTimeUtils}
import org.junit.runner.RunWith
import org.specs2.mutable._
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification with BeforeAfter {

  override def before: Any = DateTimeUtils.setCurrentMillisFixed(new DateTime(2016, 10, 1, 19, 0, DateTimeZone.UTC).getMillis)

  override def after: Any = DateTimeUtils.setCurrentMillisSystem()

  "Application" should {

    "send 404 on a bad request" in new WithApplication {
      route(FakeRequest(GET, "/boum")) must beNone
    }

    "render the index page" in new WithApplication {
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("Your new application is ready.")
    }

    "send 400 on bad person creation inputs" in new WithApplication() {
      val badCreateInputs = Seq(
        """
          |{}
        """.stripMargin, """
          |{
          |  "name": "az",
          |  "lastName": "lastName",
          |  "sex": "male",
          |  "birthDate": "1990-01-01"
          |}
        """.stripMargin, """
          |{
          |  "name": "name",
          |  "lastName": "az",
          |  "sex": "male",
          |  "birthDate": "1990-01-01"
          |}
        """.stripMargin, """
          |{
          |  "name": "name",
          |  "lastName": "lastName",
          |  "sex": "male",
          |  "birthDate": "2000-01-01"
          |}
        """.stripMargin, """
          |{
          |  "name": "name",
          |  "lastName": "lastName",
          |  "sex": "male",
          |  "birthDate": "1900-01-01"
          |}
        """.stripMargin)

      badCreateInputs.foreach { input =>
        val request = FakeRequest(POST, "/person").withJsonBody(Json.parse(input))
        val result = call(PersonController.create, request)
        status(result) mustEqual BAD_REQUEST
      }
    }

  }

}
