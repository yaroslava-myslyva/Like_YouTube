package com.example.likeyoutube

class Constants {
    companion object {
        val SHARED_PREFERENCES_NAME = "AUTH_STATE_PREFERENCE"
        val AUTH_STATE = "AUTH_STATE"

        val SCOPE_PROFILE = "profile"
        val SCOPE_EMAIL = "email"
        val SCOPE_OPENID = "openid"
        val SCOPE_YOUTUBE = "https://www.googleapis.com/auth/youtube"

        val DATA_PICTURE = "picture"
        val DATA_FIRST_NAME = "given_name"
        val DATA_LAST_NAME = "family_name"
        val DATA_EMAIL = "email"
        val DATA_PLAYLISTS_TITLES = "playlists_titles"
        val DATA_PLAYLISTS_TITLES_AND_IDS = "playlists_titles_and_ids"
        val DATA_BIG_LIST = "big_list"

        val CLIENT_ID = "812820590609-t2kgrbk4esfncnimvghmc1ah41222fpl.apps.googleusercontent.com" //android
        val CODE_VERIFIER_CHALLENGE_METHOD = "S256"
        val MESSAGE_DIGEST_ALGORITHM = "SHA-256"

        val URL_AUTHORIZATION = "https://accounts.google.com/o/oauth2/v2/auth"
        val URL_TOKEN_EXCHANGE = "https://www.googleapis.com/oauth2/v4/token"
        val URL_AUTH_REDIRECT = "com.example.likeyoutube:/oauth2redirect"
        val URL_API_CALL = "https://www.googleapis.com/youtube"

        val URL_LOGOUT = "https://accounts.google.com/o/oauth2/revoke?token="

        val URL_LOGOUT_REDIRECT = "com.example.likeyoutube:/logout"

        val RC_SIGN_IN = 100
    }
}