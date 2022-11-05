package com.example.renn


import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import com.example.renn.utils.getCountryAndPostalCode
import com.example.renn.utils.getStreetNameAndHouseNumber
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


class AddressDetailsActivity: AppCompatActivity() {


    private lateinit var backBtn: ImageView

    private lateinit var etStreetName: TextInputEditText
    private lateinit var etStreetNameInputLayout: TextInputLayout

    private lateinit var etHouseNumber: TextInputEditText
    private lateinit var etHouseNumberInputLayout: TextInputLayout

    private lateinit var etCountry: TextInputEditText
    private lateinit var etCountryInputLayout: TextInputLayout

    private lateinit var etPostalCode: TextInputEditText
    private lateinit var etPostalCodeInputLayout: TextInputLayout

    private lateinit var fabAddressDetails: FloatingActionButton //TODO ( finish functionality )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_details)

        // Add required asterisk
        fun TextInputLayout.markRequiredInRed() {
            hint = buildSpannedString {
                append(hint)
                color(Color.RED) { append(" *") } // Mind the space prefix.
            }
        }


        backBtn = findViewById(R.id.backBtn)

        // Street name and house number
        etStreetName = findViewById(R.id.etStreetName)
        etStreetName.addTextChangedListener(textWatcher)
        etStreetNameInputLayout = findViewById(R.id.etStreetNameInputLayout)
        etStreetNameInputLayout.markRequiredInRed()

        etHouseNumber = findViewById(R.id.etHouseNumber)
        etHouseNumber.addTextChangedListener(textWatcher)
        etHouseNumberInputLayout = findViewById(R.id.etHouseNumberInputLayout)
        etHouseNumberInputLayout.markRequiredInRed()


        // Country and postal code
        etCountry = findViewById(R.id.etCountry)
        etCountry.addTextChangedListener(textWatcher)
        etCountryInputLayout = findViewById(R.id.etCountryInputLayout)
        etCountryInputLayout.markRequiredInRed()


        etPostalCode = findViewById(R.id.etPostalCode)
        etPostalCode.addTextChangedListener(textWatcher)
        etPostalCodeInputLayout = findViewById(R.id.etPostalCodeInputLayout)
        etPostalCodeInputLayout.markRequiredInRed()



        // Floating action button
        fabAddressDetails = findViewById(R.id.fabAddressDetails)

        backBtn.setOnClickListener {
            finish()
        }

        val bundle = intent.extras
        val currentPinLocation = LatLng(bundle!!.getDouble("Latitude"), bundle.getDouble("Longitude"))

        // Get street name and house number
        val (address, houseNumber) = getStreetNameAndHouseNumber(this, currentPinLocation)
        etStreetName.setText(address)
        etHouseNumber.setText(houseNumber)

        // Get country and postal code
        val (country, postalCode) = getCountryAndPostalCode(this, currentPinLocation)
        etCountry.setText(country)
        etPostalCode.setText(postalCode)


        if (etStreetName.text.toString().isNotEmpty()) {
            if (etHouseNumber.text.toString().isNotEmpty()){
                if (etCountry.text.toString().isNotEmpty()){
                    if (etPostalCode.text.toString().isNotEmpty()){
                        fabAddressDetails.visibility = View.VISIBLE
                    }
                }
            }
        }

    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (etStreetName.text.toString().isNotEmpty()) {
                if (etHouseNumber.text.toString().isNotEmpty()){
                    if (etCountry.text.toString().isNotEmpty()){
                        if (etPostalCode.text.toString().isNotEmpty()){
                            fabAddressDetails.visibility = View.VISIBLE
                        }
                        else{ fabAddressDetails.visibility = View.INVISIBLE }
                    }
                    else{ fabAddressDetails.visibility = View.INVISIBLE }
                }
                else{ fabAddressDetails.visibility = View.INVISIBLE }
            }
            else{ fabAddressDetails.visibility = View.INVISIBLE }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }
    }
}