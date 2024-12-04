import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import kotlin.random.Random
import kotlin.random.nextInt

class MainActivity : AppCompatActivity() {

    private lateinit var timerTextView: TextView
    private lateinit var questionTextView: TextView
    private lateinit var answerEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var scoreTextView: TextView
    private lateinit var playAgainButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var highScoreTextView: TextView

    private var score = 0
    private var correctAnswer = 0
    private var questionTimer: CountDownTimer? = null // Timer for each question
    private lateinit var mainTimer: CountDownTimer // Overall game timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerTextView = findViewById(R.id.timerTextView)
        questionTextView = findViewById(R.id.questionTextView)
        answerEditText = findViewById(R.id.answerEditText)
        submitButton = findViewById(R.id.submitButton)
        scoreTextView = findViewById(R.id.scoreTextView)
        playAgainButton = findViewById(R.id.playAgainButton)

        sharedPreferences = getSharedPreferences("MyGamePrefs", Context.MODE_PRIVATE)

        startMainTimer()
        generateQuestion()

        submitButton.setOnClickListener {
            val userAnswer = answerEditText.text.toString().toIntOrNull()
            if (userAnswer == correctAnswer) {
                score++
                scoreTextView.text = "Score: $score"
            }
            generateQuestion()
            answerEditText.text.clear()
        }

        playAgainButton.setOnClickListener {
            // Reset game state
            score = 0
            scoreTextView.text = "Score: $score"
            timerTextView.text = "60"
            submitButton.isEnabled = true
            answerEditText.isEnabled = true
            playAgainButton.visibility = View.GONE // Hide Play Again button
            startMainTimer() // Restart main timer
            generateQuestion()
        }
    }

    private fun startMainTimer() {
        mainTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerTextView.text = (millisUntilFinished / 1000).toString()
            }

            override fun onFinish() {
                timerTextView.text = "Time's up!"
                endGame()
            }
        }.start()
    }

    private fun startQuestionTimer() {
        questionTimer?.cancel() // Cancel any ongoing timer
        questionTimer = object : CountDownTimer(10000, 1000) { // 10-second timer
            override fun onTick(millisUntilFinished: Long) {
                timerTextView.text = "Time Left: ${(millisUntilFinished / 1000)}s"
            }

            override fun onFinish() {
                timerTextView.text = "Question time over!"
                generateQuestion() // Move to the next question
            }
        }.start()
    }

    private fun generateQuestion() {
        startQuestionTimer() // Start the timer for the new question

        val selectedDifficulty = when (findViewById<RadioGroup>(R.id.difficultyRadioGroup).checkedRadioButtonId) {
            R.id.easyRadioButton -> "easy"
            R.id.mediumRadioButton -> "medium"
            R.id.hardRadioButton -> "hard"
            else -> "easy" // Default to easy
        }

        val (num1Range, num2Range, operators) = when (selectedDifficulty) {
            "easy" -> Triple(1..10, 1..10, listOf("+", "-"))
            "medium" -> Triple(1..20, 1..20, listOf("+", "-", "*"))
            "hard" -> Triple(1..50, 1..50, listOf("+", "-", "*", "/"))
            else -> Triple(1..10, 1..10, listOf("+", "-"))
        }

        val num1 = Random.nextInt(num1Range)
        val num2 = Random.nextInt(num2Range)
        val operator = operators.random()

        val question = "$num1 $operator $num2"
        questionTextView.text = question

        correctAnswer = when (operator) {
            "+" -> num1 + num2
            "-" -> num1 - num2
            "*" -> num1 * num2
            "/" -> if (num2 != 0) num1 / num2 else 0 // Handle division by zero
            else -> num1 + num2
        }

        // Ensure valid integer division for "hard" difficulty
        if (selectedDifficulty == "hard" && operator == "/" && num1 % num2 != 0) {
            generateQuestion()
        }
    }

    private fun endGame() {
        submitButton.isEnabled = false
        answerEditText.isEnabled = false
        playAgainButton.visibility = View.VISIBLE
        questionTimer?.cancel() // Stop question timer

        val highScore = sharedPreferences.getInt("highScore", 0)
        if (score > highScore) {
            with(sharedPreferences.edit()) {
                putInt("highScore", score)
                apply()
            }
        }
        highScoreTextView.text = "High Score: ${sharedPreferences.getInt("highScore", 0)}"
    }

    override fun onDestroy() {
        super.onDestroy()
        mainTimer.cancel()
        questionTimer?.cancel()
    }
}
