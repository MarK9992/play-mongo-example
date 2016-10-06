import models.Address
import org.joda.time.{DateTime, DateTimeUtils, DateTimeZone}
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

    "send 400 on bad address inputs" in new WithApplication() {
      val badAddresses = Seq(
        """
          |{}
        """.stripMargin, """
          |{
          |  "streeet": "",
          |  "town": "",
          |  "zipCode": ""
          |}
        """.stripMargin)

      badAddresses.foreach { input =>
        val request = FakeRequest(POST, "/person/foo/address/personal").withJsonBody(Json.parse(input))
        val result = route(request).get
        status(result) mustEqual BAD_REQUEST
      }
    }

    "send 404 on bad address type path" in new WithApplication() {
      val request = FakeRequest(POST, "/person/foo/address/peronal").withJsonBody(Json.toJson(Address("", "", "")))
      val result = route(request).get
      status(result) mustEqual NOT_FOUND
    }

  }

}
