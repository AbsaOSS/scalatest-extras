/*
 * Copyright 2020 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.commons.scalatest

import org.scalatest.matchers.Matcher
import org.scalatest.matchers.should.Matchers._

import java.io.{ByteArrayOutputStream, PrintStream, StringReader}

trait ConsoleStubs {

  protected def withStdIn[A](str: String)(body: => A): A = {
    Console.withIn(new StringReader(str))(body)
  }

  protected def assertingStdOut[T](matchCriteria: Matcher[String])(body: => T): T = {
    assertingPrintStream(matchCriteria)(Console.withOut(_)(body))
  }

  protected def assertingStdErr[T](matchCriteria: Matcher[String])(body: => T): T = {
    assertingPrintStream(matchCriteria)(Console.withErr(_)(body))
  }

  protected def captureStdOut(body: => Any): String =
    withPrintStreamToString(Console.withOut(_)(body))._2

  protected def captureStdErr(body: => Any): String =
    withPrintStreamToString(Console.withErr(_)(body))._2

  private def assertingPrintStream[T](matchCriteria: Matcher[String])(fn: PrintStream => T): T = {
    val (retVal, str) = withPrintStreamToString(fn)
    str should matchCriteria
    retVal
  }

  private def withPrintStreamToString[T](fn: PrintStream => T): (T, String) = {
    val baos = new ByteArrayOutputStream
    val retVal = {
      val res = new PrintStream(baos)
      try fn(res)
      finally res.close()
    }

    (retVal, baos.toString)
  }
}
