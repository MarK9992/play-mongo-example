package models

import org.joda.time.{DateTime, DateTimeUtils, DateTimeZone}
import org.junit.runner.RunWith
import org.specs2.mutable.{BeforeAfter, Specification}
import org.specs2.runner.JUnitRunner

/**
 * @author Marc Karassev
 */
@RunWith(classOf[JUnitRunner])
class PersonSpec extends Specification with BeforeAfter {

  override def before: Any = DateTimeUtils.setCurrentMillisFixed(new DateTime(2016, 10, 1, 19, 0, DateTimeZone.UTC).getMillis)

  override def after: Any = DateTimeUtils.setCurrentMillisSystem()

  "a person born on 1955/01/30" should {

    val person = Person("", "", new DateTime(1955, 1, 30, 15, 32, DateTimeZone.UTC), male)

    "be 61 years old on 2016/10/01" in {
      person.age must equalTo(61)
    }

  }

  "a person born on 2004/10/02" should {

    val person = Person("", "", new DateTime(2004, 10, 2, 15, 32, DateTimeZone.UTC), male)

    "be 11 years old on 2016/10/01" in {
      person.age must equalTo(11)
    }

  }

}
