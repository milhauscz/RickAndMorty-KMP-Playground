package cz.cernilovsky.android.rickandmorty

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform