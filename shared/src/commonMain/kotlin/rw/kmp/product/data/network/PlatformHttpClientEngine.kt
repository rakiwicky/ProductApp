package rw.kmp.product.data.network

import io.ktor.client.engine.HttpClientEngine

expect fun createPlatformHttpClientEngine(): HttpClientEngine
