package com.matrix159.gamerjammer.api.model

import com.squareup.moshi.Json

data class AccessToken (
    @field:Json(name = "access_token") val accessToken: String,
    @field:Json(name = "token_type") val tokenType: String,
    @field:Json(name = "expires_in") val expiresIn: Long,
    @field:Json(name = "refresh_token") val refreshToken: String,
    @field:Json(name = "scope") val scope: String
)

data class AccessTokenRequest (
    @field:Json(name = "client_id") val clientID: String,
    @field:Json(name = "client_secret") val clientSecret: String,
    @field:Json(name = "grant_type") val grantType: String,
    @field:Json(name = "code") val code: String,
    @field:Json(name = "redirect_uri") val redirectURI: String,
    @field:Json(name = "scope") val scope: String,
    @field:Json(name = "code_verifier") val codeVerifier: String
)