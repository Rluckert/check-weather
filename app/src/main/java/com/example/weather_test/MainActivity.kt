package com.example.weather_test

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weather_test.adapters.RecentSearchAdapter
import com.example.weather_test.clients.RetrofitClient
import com.example.weather_test.databinding.ActivityMainBinding
import com.example.weather_test.models.RecentSearch
import com.example.weather_test.services.WeatherService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var weatherApi: WeatherService
    private lateinit var recentSearchAdapter: RecentSearchAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        weatherApi = RetrofitClient.instance.create(WeatherService::class.java)
        recentSearchAdapter = RecentSearchAdapter(
            emptyList(),
            onItemClick = { selectedSearch ->
                binding.inputText.setText(selectedSearch)
                getWeatherByCity(selectedSearch)
            }
        )
        binding.previousResult.adapter = recentSearchAdapter
        binding.previousResult.layoutManager = LinearLayoutManager(this)

        loadRecentSearches()

        binding.btnCheck.setOnClickListener {
            val findText = binding.inputText.text.toString().trim()
            getWeatherByCity(findText, true)
        }

        binding.btnClearSearches.setOnClickListener {
            lifecycleScope.launch {
                val db = AppDatabase.getDatabase(applicationContext)
                withContext(Dispatchers.IO) {
                    db.recentSearchOperations().clearAll()
                }
                loadRecentSearches()
            }
        }
    }

    private fun getWeatherByCity(cityName: String, saveInDb: Boolean = false) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    weatherApi.getWeatherByCityName(cityName)
                }
                if (response.isSuccessful) {
                    val weather = response.body()
                    weather?.let {
                        val resultado = """
                            Ciudad: ${it.name}
                            Temperatura: ${it.main.temp}°C
                            Clima: ${it.weather[0].description}
                        """.trimIndent()
                        val recentSearch = RecentSearch(
                            cityName = it.name,
                            temperature = it.main.temp,
                            description = it.weather[0].description
                        )
                        if(saveInDb){
                            saveRecentSearchToDatabase(recentSearch)
                        }
                        loadRecentSearches()
                        binding.weatherInformation.text = resultado
                    }
                } else {
                    handleErrorResponse(response.code())
                }
            } catch (e: Exception) {
                Log.e("ERROR", "Fallo: ${e.message}", e)
                val message = when (e) {
                    is java.net.UnknownHostException -> "No hay conexión a internet."
                    is java.net.SocketTimeoutException -> "La solicitud tardó demasiado, intenta de nuevo."
                    else -> "Ocurrió un error inesperado."
                }
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                binding.weatherInformation.text = message
            }

        }
    }

    private suspend fun saveRecentSearchToDatabase(recentSearch: RecentSearch) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(applicationContext)
            db.recentSearchOperations().insert(recentSearch)
        }
    }

    private fun loadRecentSearches() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val recentSearches = withContext(Dispatchers.IO) {
                db.recentSearchOperations().getAll()
            }
            recentSearchAdapter.updateData(recentSearches)
        }
    }

    private fun handleErrorResponse(code: Int) {
        val message = when (code) {
            404 -> "Ciudad no encontrada"
            in 500..599 -> "Error del servidor. Intenta más tarde."
            else -> "Error ($code) al realizar la búsqueda"
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        binding.weatherInformation.text = message
    }
}
