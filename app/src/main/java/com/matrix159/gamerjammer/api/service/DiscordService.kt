package com.matrix159.gamerjammer.api.service

import com.matrix159.gamerjammer.api.model.AccessToken
import io.reactivex.rxjava3.core.Observable
import retrofit2.Response
import retrofit2.http.*

interface DiscordService {

    @FormUrlEncoded
    @Headers("Accept: application/json")
    @POST("oauth2/token")
    fun getToken(@Field("client_id") clientID: String,
                 //@Field("client_secret") clientSecret: String,
                 @Field("grant_type") grantType: String,
                 @Field("code") code: String,
                 @Field("redirect_uri") redirectURI: String,
                 @Field("scope") scope: String,
                 @Field("code_verifier") codeVerifier: String): Observable<AccessToken>
}