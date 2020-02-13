/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.TestCase
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewCodeGenTests: BaseComposeTest() {
    @After
    fun teardown() {
        Compose.clearRoots()
    }

    @get:Rule
    override val activityRule = makeTestActivityRule()

    @Test
    fun testStaticComposition() {
        val tv1Id = 100
        val tv2Id = 200

        compose {
            TextView(id=tv1Id, text="Hello world!")
            LinearLayout(orientation = LinearLayout.HORIZONTAL) {
                TextView(id=tv2Id, text="Yellow world")
            }
        }.then { activity ->
            val helloText = activity.findViewById(tv1Id) as TextView
            TestCase.assertEquals("Hello world!", helloText.text)
            val yellowText = activity.findViewById(tv2Id) as TextView
            TestCase.assertEquals(
                "Yellow world",
                yellowText.text
            )
        }
    }

    @Test
    fun testUpdatedComposition() {
        val tv1Id = 100
        val tv2Id = 200

        var text1 by mutableStateOf("Hello world!")
        var text2 by mutableStateOf("Yellow world")

        compose {
            TextView(id=tv1Id, text=text1)
            LinearLayout(orientation = LinearLayout.HORIZONTAL) {
                TextView(id=tv2Id, text=text2)
            }
        }.then { activity ->
            val helloText = activity.findViewById(tv1Id) as TextView
            TestCase.assertEquals(text1, helloText.text)
            val yellowText = activity.findViewById(tv2Id) as TextView
            TestCase.assertEquals(text2, yellowText.text)

            // Modify the composed state
            text1 = "$text1 (changed)"
            text2 = "$text2 (changed)"
        }.then { activity ->
            val helloText = activity.findViewById(tv1Id) as TextView
            TestCase.assertEquals(text1, helloText.text)
            val yellowText = activity.findViewById(tv2Id) as TextView
            TestCase.assertEquals(text2, yellowText.text)
        }
    }

    @Test
    fun testSingleView() {
        val tvId = 237
        var text by mutableStateOf("Hello world")

        compose {
            // <TextView text id=tvId />
            TextView(id=tvId, text=text)
        }.then { activity ->
            val tv = activity.findViewById(tvId) as TextView
            TestCase.assertEquals("Hello world", tv.text)

            text = "Salutations!"
        }.then { activity ->
            val tv = activity.findViewById(tvId) as TextView
            TestCase.assertEquals("Salutations!", tv.text)
        }
    }

    @Test
    fun testViewGroup() {
        val tvId = 258
        val llId = 260
        var text by mutableStateOf("Hello world")
        var orientation by mutableStateOf(LinearLayout.HORIZONTAL)

        compose {
            // <LinearLayout>
            //  <TextView text />
            // </LinearLayout
            LinearLayout(id = llId, orientation=orientation) {
                TextView(id=tvId, text=text)
            }
        }.then { activity ->
            val tv = activity.findViewById(tvId) as TextView
            TestCase.assertEquals("Hello world", tv.text)

            text = "Salutations!"
            orientation = LinearLayout.VERTICAL
        }.then { activity ->
            val tv = activity.findViewById(tvId) as TextView
            TestCase.assertEquals("Salutations!", tv.text)

            val ll = activity.findViewById(llId) as LinearLayout
            TestCase.assertEquals(
                LinearLayout.VERTICAL,
                ll.orientation
            )
        }
    }

    @Test
    fun testComposableFunctionInvocationOneParameter() {
        data class Phone(val area: String, val prefix: String, val number: String)

        var phone by mutableStateOf(Phone("123", "456", "7890"))
        var phoneCalled = 0
        compose {

            // Composition function
            //  @Composable
            //  fun PhoneView(phone: Phone) {
            //    phoneCalled++
            //   TextView(text="...")
            //  }
            @Composable fun PhoneView(phone: Phone) {
                phoneCalled++
                TextView(
                    text="${if (phone.area.isBlank()) ""
                    else "(${phone.area}) "}${phone.prefix}-${phone.number}"
                )
            }

            // <PhoneView phone />
            PhoneView(phone)
        }.then { _ ->
            TestCase.assertEquals(1, phoneCalled)
        }.recomposeRoot().then { _ ->
            TestCase.assertEquals(1, phoneCalled)

            phone = Phone("124", "456", "7890")
        }.then { _ ->
            TestCase.assertEquals(2, phoneCalled)
        }
    }

    @Test
    fun testComposableFunctionInvocationTwoParameters() {
        val tvId = 279
        var left by mutableStateOf(0)
        var right by mutableStateOf(1)
        var addCalled = 0
        compose {

            // Composition function
            //  @Composable
            //  fun AddView(left: Int, right: Int) {
            //    addCalled++
            //   <TextView text="$left + $right = ${left + right}" />
            //  }
            @Composable fun AddView(left: Int, right: Int) {
                addCalled++
                TextView(id=tvId, text="$left + $right = ${left + right}")
            }

            // <AddView left right />
            AddView(left, right)
        }.then { activity ->
            TestCase.assertEquals(1, addCalled)
            val tv = activity.findViewById(tvId) as TextView
            TestCase.assertEquals(
                "$left + $right = ${left + right}",
                tv.text
            )
        }.recomposeRoot().then { activity ->
            TestCase.assertEquals(1, addCalled)
            val tv = activity.findViewById(tvId) as TextView
            TestCase.assertEquals(
                "$left + $right = ${left + right}",
                tv.text
            )

            left = 1
        }.then { activity ->
            TestCase.assertEquals(2, addCalled)

            val tv = activity.findViewById(tvId) as TextView
            TestCase.assertEquals(
                "$left + $right = ${left + right}",
                tv.text
            )
        }.recomposeRoot().then { activity ->
            TestCase.assertEquals(2, addCalled)

            val tv = activity.findViewById(tvId) as TextView
            TestCase.assertEquals(
                "$left + $right = ${left + right}",
                tv.text
            )

            right = 41
        }.then { activity ->
            TestCase.assertEquals(3, addCalled)

            val tv = activity.findViewById(tvId) as TextView
            TestCase.assertEquals(
                "$left + $right = ${left + right}",
                tv.text
            )
        }
    }

    @Test
    fun testMoveComponents() {
        val data = mutableListOf(1, 2, 3, 4, 5)
        compose {
            for (item in data) {
                key(item) {
                    TextView(text="$item View")
                }
            }
        }.then {
            data.add(data.removeAt(0))
        }.recomposeRoot().then { activity ->
            val root = activity.root
            for (index in 0 until data.size) {
                val textView = root.getChildAt(index) as TextView
                TestCase.assertEquals(
                    "${data[index]} View",
                    textView.text
                )
            }
        }
    }

    @Test
    fun testViewClassWithCtorParametersInvocation() {
        val tvId = 749

        class MyTextView(context: Context) : TextView(context) {
            constructor(context: Context, someText: String) : this(context) {
                text = someText
            }
        }

        var hello by mutableStateOf("Hello world!")
        compose {
            // <MyTextView someText=hello />
            key(hello) {
                MyTextView(id=tvId, someText=hello)
            }
        }.then { activity ->
            val tv = activity.findViewById(tvId) as TextView
            TestCase.assertEquals("Hello world!", tv.text)

            hello = "Salutations!"
        }.then { activity ->
            val tv = activity.findViewById(tvId) as TextView
            TestCase.assertEquals("Salutations!", tv.text)
        }
    }

    @Test
    fun testViewClassWithMutableCtorParameter() {
        val tvId = 749

        class MyTextView(context: Context, var someValue: String) : TextView(context)

        var hello by mutableStateOf("Hello world!")
        var value by mutableStateOf("Unmodified")
        compose {
            // <MyTextView someText=hello />
            MyTextView(id=tvId, someValue=value, text=hello)
        }.then { activity ->
            val tv = activity.findViewById(tvId) as MyTextView
            TestCase.assertEquals("Hello world!", tv.text)
            TestCase.assertEquals("Unmodified", tv.someValue)

            hello = "Salutations!"
            value = "Modified"
        }.then { activity ->
            val tv = activity.findViewById(tvId) as MyTextView
            TestCase.assertEquals("Salutations!", tv.text)
            TestCase.assertEquals("Modified", tv.someValue)
        }
    }
}