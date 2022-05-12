package com.tesseract.demo

import net.sourceforge.tess4j.ITessAPI
import net.sourceforge.tess4j.Tesseract
import net.sourceforge.tess4j.Word
import nu.pattern.OpenCV
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import java.net.URL
import javax.imageio.ImageIO

fun main() {
    val path = "https://raw.githubusercontent.com/never-sleeps/tesseract-demo/master/src/test/resources/test2.jpg"
    val words = getWordsFromImage(ImageIO.read(URL(path)), "rus+eng")
    for (word in words) {
        print(word.text + " ")
    }
}

fun getWordsFromImage(image: BufferedImage, language: String? = null): List<Word> {
    setupOpenCV()
    setupTesseract()
    // Mat - основной класс-обёртка вокруг изображения в OpenCV в jvm
    val mat = image.toMat()
        // конвертация изображения в чёрно-белый цвет
        .also { Imgproc.cvtColor(it, it, Imgproc.COLOR_BGR2GRAY) }
        // text -> white, other -> black
        .also { Imgproc.threshold(it, it, 244.0, 255.0, Imgproc.THRESH_BINARY) }
        // inverse
        .also { Core.bitwise_not(it, it) }

    val preparedImage = mat.toBufferedImage()
    return Tesseract()
        .also { it.setDatapath("/usr/local/share/tessdata/") }
        .also { it.setPageSegMode(1) }
        .also { it.setOcrEngineMode(1) }
        .also { if (language != null) it.setLanguage(language) }
        .getWords(preparedImage, ITessAPI.TessPageIteratorLevel.RIL_WORD)
}

private fun setupTesseract() {
    val libPath = "/usr/local/lib"
    val libTess = File(libPath, "libtesseract.dylib")
    if (libTess.exists()) {
        val jnaLibPath = System.getProperty("jna.library.path")
        if (jnaLibPath == null) {
            System.setProperty("jna.library.path", libPath)
        } else {
            System.setProperty("jna.library.path", libPath + File.pathSeparator + jnaLibPath)
        }
    }
}

/**
 * Подгрузить нативные библиотеки OpenCV
 */
private fun setupOpenCV() {
    OpenCV.loadLocally()
}

/**
 * Конвертация BufferedImage в Mat
 */
private fun BufferedImage.toMat(): Mat {
    val pixels = (raster.dataBuffer as DataBufferByte).data
    return Mat(height, width, CvType.CV_8UC3)
        .apply { put(0, 0, pixels) }
}

/**
 * Обратная конвертация Mat в BufferedImage
 */
private fun Mat.toBufferedImage(): BufferedImage {
    var type = BufferedImage.TYPE_BYTE_GRAY
    if (channels() > 1) {
        type = BufferedImage.TYPE_3BYTE_BGR
    }
    val bufferSize = channels() * cols() * rows()
    val b = ByteArray(bufferSize)
    this[0, 0, b] // get all the pixels
    val image = BufferedImage(cols(), rows(), type)
    val targetPixels = (image.raster.dataBuffer as DataBufferByte).data
    System.arraycopy(b, 0, targetPixels, 0, b.size)
    return image
}

