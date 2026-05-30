package dyds.crypto.cecoin

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform