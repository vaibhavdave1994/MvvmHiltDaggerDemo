package com.example.mvvmhiltdaggerdemo.api
import com.example.demoapp.other.Constants
import com.example.demoapp.other.Constants.contentType
import com.example.mvvmhiltdaggerdemo.repository.Repository
import com.example.mvvmhiltdaggerdemo.util.LogUtil
import com.google.gson.Gson
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.internal.EverythingIsNonNull
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    private val TAG = ApiModule::class.java.simpleName

    @Provides
    fun provideBaseUrl() = Constants.BASE_URL

    @Singleton
    @Provides
    fun providesHttpLoggingInterceptor() = HttpLoggingInterceptor()
        .apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Singleton
    @Provides
    fun providesOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        val httpClient = OkHttpClient.Builder()
        if (LogUtil.isEnableLogs) { //dont show logs from here
            val interceptor = HttpLoggingInterceptor()
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
            httpClient.addInterceptor(interceptor)
        }
        try {
            httpClient.addInterceptor(object : Interceptor {
                @EverythingIsNonNull
                @Throws(IOException::class)
                override fun intercept(chain: Interceptor.Chain): Response {
                    val original: Request = chain.request()
                    var request: Request? = null
                    var response: Response? = null
                    val url = original.url.toString()
                    return if (original.method.equals("POST", ignoreCase = true)
                        || original.method.equals("PUT", ignoreCase = true)
                    ) { //if request method is post or put then encrypt body

                        //get requested body
                        val oldBody = original.body
                        val buffer = Buffer()
                        oldBody!!.writeTo(buffer)
                        val strBody = buffer.readUtf8()
                        val mediaType = "application/json;charset=utf-8".toMediaTypeOrNull()
                        val newEncryptedBody = RequestBody.create(mediaType, strBody)
                        val requestBuilder: Request.Builder = original.newBuilder()
                        requestBuilder.method(original.method, newEncryptedBody)
                        request = requestBuilder.build()
                        response = chain.proceed(request)
                        val strResponse = String(response.body!!.bytes(), StandardCharsets.UTF_8)
                        LogUtil.printLog(TAG, "HEADER Content-Type => " + contentType)
                        LogUtil.printLog(TAG, "URL => $url")
                        LogUtil.printLog(TAG, "REQUEST => $strBody")
                        LogUtil.printLog(TAG, "RESPONSE => " + strResponse.trim { it <= ' ' })
                        val contentType = response.body!!.contentType()
                        val bodyRes = ResponseBody.create(contentType, strResponse)
                        response.newBuilder().body(bodyRes).build()
                    } else {
                        //GET request
                        val requestBuilder: Request.Builder = original.newBuilder()
                        requestBuilder.method(original.method, original.body)
                        request = requestBuilder.build()
                        response = chain.proceed(request)
                        val strResponse = String(response.body!!.bytes(), StandardCharsets.UTF_8)
                        LogUtil.printLog(TAG, "HEADER Content-Type => " + contentType)
                        LogUtil.printLog(TAG, "URL => $url")
                        LogUtil.printLog(TAG, "RESPONSE => " + strResponse.trim { it <= ' ' })
                        val contentType = response.body!!.contentType()
                        val bodyRes = ResponseBody.create(contentType, strResponse)
                        response.newBuilder().body(bodyRes).build()
                    }
                }
            })
                .connectTimeout(100, TimeUnit.SECONDS)
                .writeTimeout(100, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS)
        } catch (e: Exception) {
            e.printStackTrace()
        }
       return httpClient
           .build()
    }


    @Singleton
    @Provides
    fun provideRetrofit(BASE_URL: String,gson: Gson, okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()

    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

    @Singleton
    @Provides
    fun providesRepository(apiService: ApiService) = Repository(apiService)

    @Singleton
    @Provides
    fun providesgson() :Gson = Gson()

}


