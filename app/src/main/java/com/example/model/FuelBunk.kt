package com.example.model

/**
 * Represents a petrol/fuel station (fuel bunk) in the geographical chunk around the driver.
 */
data class FuelBunk(
    val id: String,
    val name: String,
    val brand: String,
    val lat: Double,
    val lon: Double,
    val address: String,
    val fuelsAvailable: List<String> = listOf("Petrol (E20)", "Diesel", "XP95"),
    val pricePerLiter: Double = 101.5,
    val isOpen24Hours: Boolean = true,
    val hasAirService: Boolean = true,
    val hasRestroom: Boolean = true
)

/**
 * Pre-defined regional fuel bunks around the loaded GPS coordinate sector (Mangalore / NH169 / Vamanjoor corridor).
 */
object RegionalFuelBunksRepository {
    val regionalBunks: List<FuelBunk> = listOf(
        FuelBunk(
            id = "bunk-ioc-vamanjoor",
            name = "Indian Oil - Vamanjoor Junction",
            brand = "Indian Oil",
            lat = 12.92150,
            lon = 74.89520,
            address = "NH 169, Vamanjoor Main Rd, Mangaluru",
            fuelsAvailable = listOf("Petrol (E20)", "Diesel", "XP95 Power"),
            pricePerLiter = 101.35,
            isOpen24Hours = true,
            hasAirService = true,
            hasRestroom = true
        ),
        FuelBunk(
            id = "bunk-bpcl-kudupu",
            name = "Bharat Petroleum - Kudupu Highway",
            brand = "Bharat Petroleum",
            lat = 12.89920,
            lon = 74.88250,
            address = "Solapur-Mangalore Highway, Kudupu",
            fuelsAvailable = listOf("Speed Petrol", "Diesel", "CNG"),
            pricePerLiter = 101.50,
            isOpen24Hours = true,
            hasAirService = true,
            hasRestroom = true
        ),
        FuelBunk(
            id = "bunk-hpcl-kulshekar",
            name = "Hindustan Petroleum - Kulshekar Chowki",
            brand = "HPCL",
            lat = 12.88410,
            lon = 74.86980,
            address = "Kulshekar Kaikamba Cross, Mangaluru",
            fuelsAvailable = listOf("Power Petrol", "Diesel", "AutoGas"),
            pricePerLiter = 101.42,
            isOpen24Hours = true,
            hasAirService = true,
            hasRestroom = true
        ),
        FuelBunk(
            id = "bunk-shell-bikarnakatte",
            name = "Shell AutoCare - Bikarnakatte",
            brand = "Shell",
            lat = 12.87850,
            lon = 74.85820,
            address = "Bikarnakatte Bypass, Mangaluru",
            fuelsAvailable = listOf("Shell V-Power", "Shell Unleaded", "V-Power Diesel"),
            pricePerLiter = 106.80,
            isOpen24Hours = true,
            hasAirService = true,
            hasRestroom = true
        ),
        FuelBunk(
            id = "bunk-ioc-gurupura",
            name = "Indian Oil Kisan Seva - Gurupura",
            brand = "Indian Oil",
            lat = 12.94650,
            lon = 74.93620,
            address = "Gurupura Kaikamba, Mangaluru Rural",
            fuelsAvailable = listOf("Petrol", "High-Flow Diesel"),
            pricePerLiter = 101.20,
            isOpen24Hours = false,
            hasAirService = true,
            hasRestroom = false
        ),
        FuelBunk(
            id = "bunk-bpcl-kadri",
            name = "BPCL COCO Station - Kadri Hills",
            brand = "Bharat Petroleum",
            lat = 12.87120,
            lon = 74.85050,
            address = "Kadri Temple Rd, Mangaluru",
            fuelsAvailable = listOf("Speed97", "Diesel", "EV Fast Charging"),
            pricePerLiter = 101.55,
            isOpen24Hours = true,
            hasAirService = true,
            hasRestroom = true
        ),
        FuelBunk(
            id = "bunk-nayara-moodbidri",
            name = "Nayara Energy - Moodbidri Road",
            brand = "Nayara",
            lat = 12.97820,
            lon = 74.96250,
            address = "State Highway 67, Ganjimutt",
            fuelsAvailable = listOf("Petrol", "Diesel"),
            pricePerLiter = 101.10,
            isOpen24Hours = true,
            hasAirService = true,
            hasRestroom = true
        ),
        FuelBunk(
            id = "bunk-hpcl-surathkal",
            name = "HPCL Highway Express - Surathkal",
            brand = "HPCL",
            lat = 12.99850,
            lon = 74.80500,
            address = "NH 66 Bypass, Surathkal",
            fuelsAvailable = listOf("Power Petrol", "Turbo Diesel", "CNG"),
            pricePerLiter = 101.40,
            isOpen24Hours = true,
            hasAirService = true,
            hasRestroom = true
        ),
        FuelBunk(
            id = "bunk-ioc-bantwal",
            name = "Indian Oil Hub - Bantwal Cross",
            brand = "Indian Oil",
            lat = 12.89500,
            lon = 75.02500,
            address = "NH 73, BC Road, Bantwal",
            fuelsAvailable = listOf("Petrol", "Diesel", "XP95"),
            pricePerLiter = 101.30,
            isOpen24Hours = true,
            hasAirService = true,
            hasRestroom = true
        ),
        FuelBunk(
            id = "bunk-shell-mulki",
            name = "Shell Express - Mulki Toll Plaza",
            brand = "Shell",
            lat = 13.08500,
            lon = 74.79200,
            address = "NH 66, Mulki Coastal Highway",
            fuelsAvailable = listOf("V-Power", "Unleaded", "Diesel"),
            pricePerLiter = 106.50,
            isOpen24Hours = true,
            hasAirService = true,
            hasRestroom = true
        ),
        FuelBunk(
            id = "bunk-hpcl-belthangady",
            name = "HPCL Outpost - Belthangady",
            brand = "HPCL",
            lat = 12.99800,
            lon = 75.24500,
            address = "Ujire-Belthangady Highway",
            fuelsAvailable = listOf("Petrol", "Diesel"),
            pricePerLiter = 101.45,
            isOpen24Hours = false,
            hasAirService = true,
            hasRestroom = false
        ),
        FuelBunk(
            id = "bunk-bpcl-udupi",
            name = "Bharat Petroleum - Udupi Highway",
            brand = "Bharat Petroleum",
            lat = 13.24500,
            lon = 74.74800,
            address = "NH 66, Kaup-Udupi Corridor",
            fuelsAvailable = listOf("Speed Petrol", "Diesel", "Speed97"),
            pricePerLiter = 101.60,
            isOpen24Hours = true,
            hasAirService = true,
            hasRestroom = true
        )
    )

    /**
     * Calculates great circle distance between two points using the Haversine formula in kilometers.
     */
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radius of Earth in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return r * c
    }
}
