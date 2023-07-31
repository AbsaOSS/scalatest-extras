ScalaTest extras
===

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/za.co.absa.commons/scalatest-extras_2.12/badge.svg)](https://search.maven.org/search?q=g:za.co.absa.commons&a=scalatest-extras_2.11)

[//]: # ([![TeamCity build]&#40;https://teamcity.jetbrains.com/app/rest/builds/aggregated/strob:%28locator:%28buildType:%28id:OpenSourceProjects_AbsaOSS_Commons_AutoBuildWithScala212%29,branch:master%29%29/statusIcon.svg&#41;]&#40;https://teamcity.jetbrains.com/viewType.html?buildTypeId=OpenSourceProjects_AbsaOSSSpline_AutomaticBuildsWithTests_Spark24&branch=develop&tab=buildTypeStatusDiv&#41;)

---

# Building

### Switch the codebase to the required Scala version.
By default, Scala 2.11 is used. To build it for another Scala version, switch to the required Scala version first. 
```shell
# E.g. to switch to Scala 2.13 use
mvn scala-cross-build:change-version -Pscala-2.13
```

### Build the project
When building the project activate a Scala profile corresponding to the Scala version of the codebase.
```shell
# E.g. for Scala 2.13 use
mvn clean install -Pscala-2.13
```

### Building for all supported Scala versions
```shell
./build-all.sh
```

### Measuring code coverage
```shell
./mvn clean verify -Pcode-coverage 
```
Code coverage will be generated on path:
```
{project-root}\target\site\jacoco
```

# Usage

## `ConditionalTestTags` -  runs tests conditionally

    ```scala
      it should "test that new Spark feature" taggedAs ignoreIf(ver"$SPARK_VERSION" < ver"2.4") in  {
        ...
      }
    
      it should "test some DAO" taggedAs ignoreIf(!isDatabaseAvailable) in  {
        ...
      }
    ```

## `ConsoleStubs` - stubs Console API
    ```scala
      captureStdOut(Console.out.print("foo")) should be("foo")
    ```

## `SystemExitFixture` intercept `System.exit()` and asserts status
    ```scala
      captureExitStatus(System.exit(42)) should be(42)
    
      // OR
    
      assertingExitStatus(be > 0 and be < 5) {
          // run some code that calls System.exit(...)
      }
    ```
   
## `EnvFixture` - adds an API to set an environment variable for the scope of a single test method
    ```scala
    class MySpec ... with EnvFixture {
      it should "set FOO variable for this test body only" in {
        setEnv("FOO", 42)
        // execute some test code that reads FOO environment variable
        // via the standard Java API like System.getenv("FOO")
      }
    }
    ```

## `WhitespaceNormalizations` - extends Scalatest DSL with some whitespace treatment methods
    ```scala
      (
        """
          {
            a: 111,
            b: {
              v: 42
            }
          }
        """
        should equal ("{ a: 111, b: { v: 42 } }")
        (after being trimmed and whitespaceNormalized)
      )
    ```

## `CommonMatchers` - provides matchers, currently only URI matcher
    ```scala
      class MySpec ... with CommonMatchers {
        it should "produce the correct URI" in {
          val uri: String = ???
   
          uri should equalToUri("file:///foo.txt")
          // compares using java.net.URI`s equals method
        }
      }
    ```

---

    Copyright 2019 ABSA Group Limited
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
