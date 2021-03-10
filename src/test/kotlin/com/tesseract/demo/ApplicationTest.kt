package com.tesseract.demo

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis

class ApplicationTest {

    @Test
    fun `getWordsFromImage should return words from image`() {
        // given
        val image = ImageIO.read(File(imagePath))
        val expectedText = "DO YOU SPEAK ENGLISH?"
        var actualText = ""

        // when
        val time = measureTimeMillis {
            val words = getWordsFromImage(image)
            for (word in words) {
                actualText = actualText + word.text + " "
            }
        }

        // then
        Assertions.assertEquals(expectedText, actualText.trim())
        println("All time: $time")
    }


    companion object {
        private val imagePath = "src/test/resources/test.jpg"
    }
}
