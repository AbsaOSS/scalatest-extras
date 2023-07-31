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

import scala.annotation.tailrec
import scala.reflect.ClassTag
import scala.reflect.runtime.universe.{Mirror, runtimeMirror}

/**
 * Reflection utils
 */
object ReflectionUtils {

  /**
   * Extract a value from a given field or parameterless method regardless of its visibility.
   * This method utilizes a mix of Java and Scala reflection mechanisms,
   * and can extract from a compiler generated fields as well.
   * Note: if the field has an associated Scala accessor one will be called.
   * Consequently if the filed is lazy it will be initialized.
   *
   * @param o         target object
   * @param fieldName field name to extract value from
   * @tparam A type in which the given field is declared
   * @tparam B expected type of the field value to return
   * @return a field value
   */
  @deprecated(message = "Use extractValue instead.")
  def extractFieldValue[A: ClassTag, B](o: AnyRef, fieldName: String): B =
    extractValue[A, B](o, fieldName)

  /**
   * Extract a value from a given field or parameterless method regardless of its visibility.
   * This method utilizes a mix of Java and Scala reflection mechanisms,
   * and can extract from a compiler generated fields as well.
   * Note: if the field has an associated Scala accessor one will be called.
   * Consequently if the filed is lazy it will be initialized.
   *
   * @param o         target object
   * @param fieldName field name to extract value from
   * @tparam A type in which the given field is declared
   * @tparam B expected type of the field value to return
   * @return a field value
   */
  def extractValue[A: ClassTag, B](o: AnyRef, fieldName: String): B =
    new ValueExtractor[A, B](o, fieldName).extract()

  /**
   * A single type parameter alternative to {{{extractValue[A, B](a, ...)}}} where {{{a.getClass == classOf[A]}}}
   */
  def extractValue[T](o: AnyRef, fieldName: String): T = {
    extractValue[AnyRef, T](o, fieldName)(ClassTag(o.getClass))
  }


  private class ValueExtractor[A: ClassTag, B](o: AnyRef, fieldName: String) {

    private val mirror: Mirror = runtimeMirror(getClass.getClassLoader)

    def extract(): B = {
      val declaringClass = implicitly[ClassTag[A]].runtimeClass
      reflectClassHierarchy(declaringClass)
        .getOrElse(
          throw new NoSuchFieldException(s"${declaringClass.getName}.$fieldName")
        )
        .asInstanceOf[B]
    }

    @tailrec
    private def reflectClassHierarchy(c: Class[_]): Option[_] =
      if (c == classOf[AnyRef]) None
      else {
        val maybeValue: Option[Any] = reflectClass(c)
        if (maybeValue.isDefined) maybeValue
        else {
          val superClass = c.getSuperclass
          if (superClass == null) None
          else reflectClassHierarchy(superClass)
        }
      }

    private def reflectClass(c: Class[_]): Option[Any] =
      scalaReflectClass(c).orElse(javaReflectClass(c))

    /**
     * may return None because:
     *  - `Symbols#CyclicReference` (Scala bug #12190)
     *  - `RuntimeException("scala signature")` (#80, #82)
     */
    private def scalaReflectClass(c: Class[_]): Option[Any] = util.Try {
      val members = mirror.classSymbol(c).toType.decls
      val m = members
        .filter(smb => (
          smb.toString.endsWith(s" $fieldName")
            && smb.isTerm
            && !smb.isConstructor
            && (!smb.isMethod || smb.asMethod.paramLists.forall(_.isEmpty))
          ))
        .minBy(!_.isMethod)

      val im = mirror.reflect(o)
      if (m.isMethod) im.reflectMethod(m.asMethod).apply()
      else im.reflectField(m.asTerm).get
    }.toOption

    private def javaReflectClass(c: Class[_]): Option[Any] =
      c.getDeclaredFields.collectFirst {
        case f if f.getName == fieldName =>
          f.setAccessible(true)
          f.get(o)
      } orElse {
        c.getDeclaredMethods.collectFirst {
          case m if m.getName == fieldName && m.getParameterCount == 0 =>
            m.setAccessible(true)
            m.invoke(o)
        }
      }

    private def reflectInterfaces(c: Class[_]) = {
      val altNames = allInterfacesOf(c)
        .map(_.getName.replace('.', '$') + "$$" + fieldName)

      c.getDeclaredFields.collectFirst {
        case f if altNames contains f.getName =>
          f.setAccessible(true)
          f.get(o)
      }
    }

    /**
     * Return all interfaces that the given class implements included inherited ones
     *
     * @param c a class
     * @return
     */
    private def allInterfacesOf(c: Class[_]): Set[Class[_]] = {
      @tailrec
      def collect(ifs: Set[Class[_]], cs: Set[Class[_]]): Set[Class[_]] =
        if (cs.isEmpty) ifs
        else {
          val c0 = cs.head
          val cN = cs.tail
          val ifsUpd = if (c0.isInterface) ifs + c0 else ifs
          val csUpd = cN ++ (c0.getInterfaces filterNot ifsUpd) ++ Option(c0.getSuperclass)
          collect(ifsUpd, csUpd)
        }

      collect(Set.empty, Set(c))
    }


  }
}
