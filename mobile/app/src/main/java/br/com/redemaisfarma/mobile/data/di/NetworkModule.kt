package br.com.redemaisfarma.mobile.data.di

import br.com.redemaisfarma.mobile.BuildConfig
import br.com.redemaisfarma.mobile.data.network.AuthApiService
import br.com.redemaisfarma.mobile.data.network.AuthHeaderInterceptor
import br.com.redemaisfarma.mobile.data.network.ClienteApiService
import br.com.redemaisfarma.mobile.data.network.ProdutosPublicApiService
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().build()

    @Provides
    @Singleton
    fun provideOkHttpClient(authHeaderInterceptor: AuthHeaderInterceptor): OkHttpClient {
        val logger = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
            else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(authHeaderInterceptor)
            .addInterceptor(logger)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(moshi: Moshi, okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    @Provides
    @Singleton
    fun provideClienteApiService(retrofit: Retrofit): ClienteApiService =
        retrofit.create(ClienteApiService::class.java)

    @Provides
    @Singleton
    fun provideProdutosPublicApiService(retrofit: Retrofit): ProdutosPublicApiService =
        retrofit.create(ProdutosPublicApiService::class.java)
}
