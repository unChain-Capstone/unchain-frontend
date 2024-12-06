package com.unchain.data.ml

import android.content.Context
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import android.util.Log
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class RecommendationItem(
    val id: Int,
    val sugarLevel: String,
    val score: Float,
    val recommendation: String
)

class RecommendationHelper(private val context: Context) {
    private var interpreter: Interpreter? = null
    private val modelPath = "recommendation_system.tflite"
    private val inputSize = 1376 // 1375 for TF-IDF vector + 1 for sugar intake
    
    init {
        try {
            val model = FileUtil.loadMappedFile(context, modelPath)
            val options = Interpreter.Options()
            interpreter = Interpreter(model, options)
            Log.d("RecommendationHelper", "Model loaded successfully")
            
            // Log model input/output details
            interpreter?.let { interp ->
                val inputTensor = interp.getInputTensor(0)
                val outputTensor = interp.getOutputTensor(0)
                Log.d("RecommendationHelper", "Input shape: ${inputTensor.shape().contentToString()}")
                Log.d("RecommendationHelper", "Output shape: ${outputTensor.shape().contentToString()}")
            }
        } catch (e: Exception) {
            Log.e("RecommendationHelper", "Error loading model", e)
        }
    }

    fun getRecommendations(weeklyIntake: Float): FloatArray {
        val interpreter = interpreter ?: throw IllegalStateException("TFLite interpreter not initialized")
        
        try {
            // Create input array with zeros for TF-IDF vector and weeklyIntake at the end
            val inputArray = Array(1) { FloatArray(inputSize) { 0f } }
            inputArray[0][inputSize - 1] = weeklyIntake
            
            // Create output array for 3 classes (Low, Normal, High)
            val outputArray = Array(1) { FloatArray(3) }
            
            // Run inference
            interpreter.run(inputArray, outputArray)
            Log.d("RecommendationHelper", "Inference successful. Output: ${outputArray[0].contentToString()}")
            
            return outputArray[0]
        } catch (e: Exception) {
            Log.e("RecommendationHelper", "Error during inference", e)
            throw e
        }
    }

    companion object {
        fun processRecommendations(output: FloatArray): List<RecommendationItem> {
            val sugarLevels = listOf("Low", "Normal", "High")
            return output.mapIndexed { index, score ->
                val recommendation = when (sugarLevels[index]) {
                    "Low" -> "Consider increasing your sugar intake slightly for balanced nutrition"
                    "Normal" -> "Great job! Your sugar intake is within healthy limits"
                    "High" -> "Try to reduce your sugar intake to maintain better health"
                    else -> "Keep monitoring your sugar intake"
                }
                RecommendationItem(
                    id = index + 1,
                    sugarLevel = sugarLevels[index],
                    score = score,
                    recommendation = recommendation
                )
            }.sortedByDescending { it.score }
        }
    }
    
    fun close() {
        interpreter?.close()
    }
}
