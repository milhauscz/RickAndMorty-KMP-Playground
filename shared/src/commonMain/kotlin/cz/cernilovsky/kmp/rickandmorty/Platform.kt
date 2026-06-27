package cz.cernilovsky.kmp.rickandmorty

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform