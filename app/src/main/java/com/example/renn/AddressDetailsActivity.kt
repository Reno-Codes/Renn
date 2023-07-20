package com.example.renn


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.renn.utils.getCountryAndPostalCode
import com.example.renn.utils.getLatLngFromAddress
import com.example.renn.utils.getStreetNameCityAndHouseNumber
import com.example.renn.utils.markRequiredInRed
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout


class AddressDetailsActivity: AppCompatActivity() {


    private lateinit var backBtn: ImageView

    private lateinit var etStreetName: TextInputEditText
    private lateinit var etStreetNameInputLayout: TextInputLayout

    private lateinit var etCity: TextInputEditText
    private lateinit var etCityInputLayout: TextInputLayout

    private lateinit var etHouseNumber: TextInputEditText
    private lateinit var etHouseNumberInputLayout: TextInputLayout

    private lateinit var etCountry: TextInputEditText
    private lateinit var etCountryInputLayout: TextInputLayout

    private lateinit var etPostalCode: TextInputEditText
    private lateinit var etPostalCodeInputLayout: TextInputLayout

    private lateinit var fabAddressDetails: FloatingActionButton



    /* ON CREATE */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_details)

        // Back button
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


        // City
        etCity = findViewById(R.id.etCity)
        etCity.addTextChangedListener(textWatcher)
        etCityInputLayout = findViewById(R.id.etCityInputLayout)
        etCityInputLayout.markRequiredInRed()



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



        // On Floating action button click
        fabAddressDetails.setOnClickListener {
            val fullAddress = "${etStreetName.text.toString()}, ${etHouseNumber.text.toString()}, ${etPostalCode.text.toString()}, ${etCity.text.toString()}, ${etCountry.text.toString()}"
            val (isAddressValid, latLng) = getLatLngFromAddress(this, fullAddress)

            if (isAddressValid){
                val data = Intent()
                val bundle = Bundle()
                bundle.putDouble("latitudeKey", latLng.latitude)
                bundle.putDouble("longitudeKey", latLng.longitude)
                data.putExtra("streetNameKey", etStreetName.text.toString())
                data.putExtra("houseNumberKey", etHouseNumber.text.toString())
                data.putExtra("postalCodeKey", etPostalCode.text.toString())
                data.putExtra("cityKey", etCity.text.toString())
                data.putExtra("countryKey", etCountry.text.toString())
                data.putExtra("fullAddressKey", fullAddress)
                data.putExtra("latLngKey", bundle)

                setResult(Activity.RESULT_OK, data)
                finish()
            }
            else{
                Toast.makeText(this, "Invalid address", Toast.LENGTH_SHORT).show()
            }
        }
// ulica, kucni broj, postal code, grad, drzava

        val bundle = intent.extras
        val currentPinLocation = LatLng(bundle!!.getDouble("Latitude"), bundle.getDouble("Longitude"))

        // Get street name, city and house number
        val (address, city, houseNumber) = getStreetNameCityAndHouseNumber(this, currentPinLocation)
        etStreetName.setText(address)
        etCity.setText(city)
        etHouseNumber.setText(houseNumber)

        // Get country and postal code
        val (country, postalCode) = getCountryAndPostalCode(this, currentPinLocation)
        etCountry.setText(country)
        etPostalCode.setText(postalCode)


        if (etStreetName.text.toString().isNotEmpty()) {
            if (etCity.text.toString().isNotEmpty()){
                if (etHouseNumber.text.toString().isNotEmpty()){
                    if (etCountry.text.toString().isNotEmpty()){
                        if (etPostalCode.text.toString().isNotEmpty()){
                            fabAddressDetails.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }

    }

    // Text watcher
    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (etStreetName.text.toString().isNotEmpty()) {
                if (etCity.text.toString().isNotEmpty()) {
                    if (etHouseNumber.text.toString().isNotEmpty()) {
                        if (etCountry.text.toString().isNotEmpty()) {
                            if (etPostalCode.text.toString().isNotEmpty()) {
                                fabAddressDetails.visibility = View.VISIBLE
                            } else {
                                fabAddressDetails.visibility = View.INVISIBLE
                            }
                        } else {
                            fabAddressDetails.visibility = View.INVISIBLE
                        }
                    } else {
                        fabAddressDetails.visibility = View.INVISIBLE
                    }
                } else {
                    fabAddressDetails.visibility = View.INVISIBLE
                }
            } else {
                fabAddressDetails.visibility = View.INVISIBLE
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }
    }
}