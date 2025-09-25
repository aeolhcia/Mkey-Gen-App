package xyz.aeolhcia.mkeygen // IMPORTANT: Change this to match your project's package name

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var selectedDateTextView: TextView
    private lateinit var monthToPass: String
    private lateinit var dayToPass: String
    private lateinit var generateButton: Button
    private lateinit var additionalDataLayout: LinearLayout
    private lateinit var splashTextView: TextView

    @SuppressLint("DefaultLocale", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get references to your UI elements
        val deviceSpinner: Spinner = findViewById(R.id.device_spinner)
        val datePickerButton: Button = findViewById(R.id.date_picker_button)
        selectedDateTextView = findViewById(R.id.selected_date_text_view)
        val additionalDataEditText: EditText = findViewById(R.id.additional_data_edit_text)
        val inquiryNumberEditText: EditText = findViewById(R.id.inquiry_number_edit_text)
        val masterKeyTextView: TextView = findViewById(R.id.master_key_text_view)
        splashTextView = findViewById(R.id.splash_text_view)
        generateButton = findViewById(R.id.generate_button)
        additionalDataLayout = findViewById(R.id.additional_data_layout)

        // Populate the device spinner
        val deviceOptions = listOf("Wii", "WiiU", "DSi", "3DS", "Switch")
        val deviceAdapter = ArrayAdapter(this, R.layout.spinner_item, deviceOptions)
        deviceSpinner.adapter = deviceAdapter

        deviceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedDevice = parent?.getItemAtPosition(position).toString()
                if (selectedDevice == "Switch") {
                    // Show the layout if Switch is selected
                    additionalDataLayout.visibility = View.VISIBLE
                } else {
                    // Hide the layout for all other devices
                    additionalDataLayout.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        // Set up the date picker
        val calendar = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

        // Set initial date to current date
        monthToPass = String.format("%02d", calendar.get(Calendar.MONTH) + 1)
        dayToPass = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))
        selectedDateTextView.text = "Current Date: " + simpleDateFormat.format(calendar.time)

        datePickerButton.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    calendar.set(selectedYear, selectedMonth, selectedDay)
                    selectedDateTextView.text =
                        "Selected Date: " + simpleDateFormat.format(calendar.time)
                    monthToPass = String.format("%02d", selectedMonth + 1)
                    dayToPass = String.format("%02d", selectedDay)
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        inquiryNumberEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Trigger a click on the generateButton
                generateButton.performClick()
                // Hide the keyboard
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(inquiryNumberEditText.windowToken, 0)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        // Set the initial text and size
        masterKeyTextView.text = "Waiting for input..."
        masterKeyTextView.textSize = 42f // Note: textSize in code uses floats

        // Set up the button click listener
        generateButton.setOnClickListener {

            // 1. Give the user feedback
            generateButton.text = "Generating..."
            generateButton.isEnabled = false

            // 2. Hide the keyboard
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

            // 4. Retrieve and prepare the data
            val selectedDevice = deviceSpinner.selectedItem.toString()
            val additionalData = additionalDataEditText.text.toString()
            val inquiryNumber = inquiryNumberEditText.text.toString()
            val deviceMap = mapOf(
                "3DS" to "CTR",
                "DSi" to "TWL",
                "Wii" to "RVL",
                "WiiU" to "WUP",
                "Switch" to "HAC"
            )
            val deviceCode = deviceMap[selectedDevice] ?: ""

            // 5. Make the POST request
            fetchMasterKey(
                deviceCode,
                monthToPass,
                dayToPass,
                additionalData,
                inquiryNumber,
                masterKeyTextView
            )
        }


    }

    private fun fetchMasterKey(
        deviceCode: String,
        month: String,
        day: String,
        aux: String,
        inquiry: String,
        textView: TextView
    ) {
        val client = OkHttpClient()
        val url = "https://mkey.salthax.org/"

        val formBody = FormBody.Builder()
            .add("device", deviceCode)
            .add("month", month)
            .add("day", day)
            .add("aux", aux)
            .add("inquiry", inquiry)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            @SuppressLint("SetTextI18n")
            override fun onFailure(call: Call, e: IOException) {
                val masterKeyTextView: TextView = findViewById(R.id.master_key_text_view)
                val generateButton: Button = findViewById(R.id.generate_button)

                runOnUiThread {
                    if (e is UnknownHostException) {
                        masterKeyTextView.text = "Error: No Internet connection."
                        textView.textSize = 36f // Revert to 32sp on failure
                    } else {
                        masterKeyTextView.text = "Error: Network request failed."
                        textView.textSize = 36f // Revert to 32sp on failure
                    }
                    generateButton.text = "Generate Key"
                    generateButton.isEnabled = true
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response) {
                val masterKeyTextView: TextView = findViewById(R.id.master_key_text_view)
                val generateButton: Button = findViewById(R.id.generate_button)

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val masterKey = parseMasterKey(responseBody)
                    runOnUiThread {
                        if (masterKey != null) {
                            textView.text = masterKey
                            textView.textSize = 64f // Change to 48sp for the key
                            splashTextView.text = getSplashText()

                        } else {
                            textView.text = "Failed to find master key."
                            textView.textSize = 42f // Revert to 32sp on failure
                        }
                        generateButton.text = "Generate Key"
                        generateButton.isEnabled = true
                    }
                } else {
                    runOnUiThread {
                        masterKeyTextView.text = "Error: Server returned ${response.code}."
                        textView.textSize = 36f // Revert to 32sp on failure
                        generateButton.text = "Generate Key"
                        generateButton.isEnabled = true
                    }
                }
            }
        }
        )
    }

    private fun parseMasterKey(html: String?): String? {
        if (html == null) return null
        val startTag = "<h3>Your master key is: "
        val endTag = ".</h3>"
        val startIndex = html.indexOf(startTag)
        if (startIndex == -1) return null
        val endIndex = html.indexOf(endTag, startIndex)
        if (endIndex == -1) return null
        return html.substring(startIndex + startTag.length, endIndex)
    }

    // In your MainActivity class
    @SuppressLint("DiscouragedApi")
    private fun getSplashText(): String {
        // Get the current date and format it as "MM_dd" to match string resource names
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MM_dd", Locale.getDefault())
        val currentDate = dateFormat.format(calendar.time)

        // Check for a specific date splash text in the resources
        val resourceId = resources.getIdentifier("splash_$currentDate", "string", packageName)

        return if (resourceId != 0) {
            // If a resource with that name exists, return its value
            getString(resourceId)
        } else {
            // Otherwise, get a random splash text from the string array
            val generalSplashes = resources.getStringArray(R.array.general_splashes)
            generalSplashes.random()
        }
    }
}