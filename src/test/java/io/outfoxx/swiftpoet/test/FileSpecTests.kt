/*
 * Copyright 2018 Outfox, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.outfoxx.swiftpoet.test

import io.outfoxx.swiftpoet.ComposedTypeName.Companion.composed
import io.outfoxx.swiftpoet.DeclaredTypeName.Companion.typeName
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.INT
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.TypeVariableName
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.StringWriter


@DisplayName("FileSpec Tests")
class FileSpecTests {

  @Test
  @DisplayName("Generates correct imports & shortens used types")
  fun testImportsAndShortensTypes() {

    val testClass = TypeSpec.classBuilder("Test")
       .addTypeVariable(
          TypeVariableName.typeVariable("X", TypeVariableName.Bound(INT))
       )
       .addTypeVariable(
          TypeVariableName.typeVariable("Y", TypeVariableName.Bound(composed("Foundation.Test3", "Swift.Test4")))
       )
       .addTypeVariable(
          TypeVariableName.typeVariable("Z", TypeVariableName.Bound(TypeVariableName.Bound.Constraint.SAME_TYPE, "Test.Test5"))
       )
       .build()

    val testFile = FileSpec.builder("Test", "Test")
       .addType(testClass)
       .build()

    val out = StringWriter()
    testFile.writeTo(out)

    assertThat(
       out.toString(),
       CoreMatchers.equalTo(
          """
            import Foundation

            class Test<X, Y, Z> where X : Int, Y : Test3 & Test4, Z == Test5 {
            }

          """.trimIndent()
       )
    )

  }

  @Test
  @DisplayName("Generates correct imports with conflicts")
  fun testCorrectImportsWithConflicts() {

    val testClass = TypeSpec.classBuilder("Test")
       .addProperty("a", typeName("Swift.Array"))
       .addProperty("b", typeName("Foundation.Array"))
       .build()

    val testFile = FileSpec.builder("Test", "Test")
       .addType(testClass)
       .build()

    val out = StringWriter()
    testFile.writeTo(out)

    assertThat(
       out.toString(),
       CoreMatchers.equalTo(
          """
            class Test {

              let a: Array
              let b: Foundation.Array

            }

          """.trimIndent()
       )
    )

  }

}
