package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface TmdbApiService {

    @GET("3/trending/movie/day")
    suspend fun getTrendingMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): TmdbMovieResponse

    @GET("3/movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): TmdbMovieResponse

    @GET("3/discover/movie")
    suspend fun discoverMoviesByGenre(
        @Query("api_key") apiKey: String,
        @Query("with_genres") genreId: Int,
        @Query("page") page: Int = 1,
        @Query("sort_by") sortBy: String = "popularity.desc"
    ): TmdbMovieResponse

    @GET("3/trending/tv/day")
    suspend fun getPopularTvShows(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): TmdbMovieResponse

    @GET("3/search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbMovieResponse

    @GET("3/search/tv")
    suspend fun searchTv(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbMovieResponse

    @GET("3/search/multi")
    suspend fun searchMulti(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false
    ): TmdbMovieResponse

    @GET("3/movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("append_to_response") appendToResponse: String = "videos,credits"
    ): TmdbMovieDetailsDto

    @GET("3/tv/{tv_id}")
    suspend fun getTvDetails(
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String,
        @Query("append_to_response") appendToResponse: String = "videos,credits"
    ): TmdbMovieDetailsDto

    @GET("3/movie/{movie_id}/credits")
    suspend fun getMovieCredits(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): TmdbCreditsResponse

    @GET("3/movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): TmdbVideosResponse

    @GET("3/tv/{tv_id}/videos")
    suspend fun getTvVideos(
        @Path("tv_id") tvId: Int,
        @Query("api_key") apiKey: String
    ): TmdbVideosResponse

    @GET("3/tv/{tv_id}/season/{season_number}")
    suspend fun getTvSeasonDetails(
        @Path("tv_id") tvId: Int,
        @Path("season_number") seasonNumber: Int,
        @Query("api_key") apiKey: String
    ): TmdbSeasonDetailDto

    companion object {
        private const val BASE_URL = "https://api.themoviedb.org/"

        fun create(): TmdbApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(TmdbApiService::class.java)
        }
    }
}
