package cz.cernilovsky.android.rickandmorty.di

import cz.cernilovsky.android.rickandmorty.core.network.HttpClientFactory
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.module

expect val platformModule: Module

val sharedModule = module {
    single<HttpClient> {
        HttpClientFactory.create(get())
    }
}