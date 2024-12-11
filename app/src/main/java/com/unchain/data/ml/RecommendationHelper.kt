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
    private val inputSize = 1376
    
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

    fun getRecommendations(dailyIntake: Float): FloatArray {
        val interpreter = interpreter ?: throw IllegalStateException("TFLite interpreter not initialized")
        
        try {

            val inputArray = Array(1) { FloatArray(inputSize) { 0f } }

            inputArray[0][inputSize - 1] = dailyIntake
            

            val outputArray = Array(1) { FloatArray(3) }
            

            interpreter.run(inputArray, outputArray)
            Log.d("RecommendationHelper", "Inference successful. Output: ${outputArray[0].contentToString()}")
            

            val finalOutput = FloatArray(4)
            System.arraycopy(outputArray[0], 0, finalOutput, 0, 3)
            finalOutput[3] = dailyIntake
            
            return finalOutput
        } catch (e: Exception) {
            Log.e("RecommendationHelper", "Error during inference", e)
            throw e
        }
    }

    companion object {
        private const val DAILY_SUGAR_LIMIT = 50f
        private const val WEEKLY_SUGAR_LIMIT = 350f

        fun processRecommendations(output: FloatArray): List<RecommendationItem> {
            val sugarLevels = listOf("Low", "Normal", "High")
            val dailyIntake = output[output.size - 1]
            

            val (sugarLevel, confidence) = when {
                dailyIntake >= WEEKLY_SUGAR_LIMIT -> Pair("High", 95.0f)
                dailyIntake >= 20f -> Pair("Normal", 90.0f)
                else -> Pair("Low", 85.0f)
            }
            

            val recommendation = when (sugarLevel) {
                "High" -> "Your weekly sugar intake is above recommended levels. Consider reducing consumption."
                "Normal" -> "Your weekly sugar intake is within healthy limits, but monitor it closely"
                else -> "Your weekly sugar intake is below recommended levels. Consider balanced nutrition."
            }
            
            return listOf(
                RecommendationItem(
                    id = sugarLevels.indexOf(sugarLevel) + 1,
                    sugarLevel = sugarLevel,
                    score = confidence.coerceIn(0f, 100f),
                    recommendation = recommendation
                )
            )
        }
    }
    
    fun close() {
        interpreter?.close()
    }
}
