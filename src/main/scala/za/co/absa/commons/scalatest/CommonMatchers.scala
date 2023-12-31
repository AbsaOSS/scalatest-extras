/*
 * Copyright 2021 ABSA Group Limited
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

import org.scalatest.matchers.{MatchResult, Matcher}

import java.net.URI

trait CommonMatchers {

  class EqualToUri(expectedUri: String) extends Matcher[String] {

    def apply(left: String): MatchResult = {
      URI.create(left) == URI.create(expectedUri)

      MatchResult(
        URI.create(left) == URI.create(expectedUri),
        s"URI $left was not the same as expected $expectedUri",
        s"URI $left was the same as expected $expectedUri"
      )
    }
  }

  def equalToUri(expectedUri: String) = new EqualToUri(expectedUri)
}
