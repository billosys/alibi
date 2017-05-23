(ns timi.server.identity.github
  (:require
    [buddy.auth.backends :as backends]
    [clj-http.client :as client]
    [clojure.data.json :as json]
    [clojure.java.io :refer [resource]]
    [clojure.tools.logging :as log]
    [crypto.random :refer [base64]]
    [ring.util.codec :refer [form-encode]]
    [ring.util.response :refer [response response? redirect status] :as response]
    [timi.server.infra.date-time :as date-time]))

(def time-tolerance [60 :seconds])
(def authorization-endpoint "https://github.com/login/oauth/authorize")
(def token-endpoint "https://github.com/login/oauth/access_token")
(def identity-endpoint "https://api.github.com/user")

(defn backend
  [{:keys [client-id
           client-secret] :as oauth-config}]
  {:pre [client-id client-secret]}
  (letfn
    [(fetch-token [authorization-code]
       (client/post
         token-endpoint
         {:basic-auth [client-id client-secret]
          :form-params {"grant_type" "authorization_code"
                        "code" authorization-code
                        "scope" "user"}
          :accept "application/json"
          :throw-exceptions false}))

     (fetch-identity [access-token]
       (log/debug "Getting identity data ...")
       (client/get
         identity-endpoint
         {:query-params {:access_token access-token}
          :accept "application/json"
          :throw-exceptions false}))

     (parse-token [response-body]
       (log/debug "Parsing fetch-token response ...")
       (log/debug "Response:" response-body)
       (-> response-body
           (json/read-str :key-fn keyword)
           :access_token))

     (parse-identity [response-body]
       (log/debug "Parsing fetch-identity response ...")
       (log/debug "Response:" response-body)
       (json/read-str response-body :key-fn keyword))

     (request-identity [access-token]
       (log/debug "Requesting user identity ...")
       (log/debug "Access token:" access-token)
       (let [{:keys [body] fetch-identity-status :status}
             (fetch-identity access-token)]
         (if (not= fetch-identity-status 200)
           (-> body
               (response)
               (status 400))
           (let [ident (parse-identity body)]
             (log/debug "Parsed oauth response:" ident)
             ident))))

     (request-openid-token [authorization-code]
       (log/debug "Requesting oauth token ...")
       (log/debug "Authoization code:" authorization-code)
       (let [{:keys [body] fetch-token-status :status}
             (fetch-token authorization-code)]
         (if (not= fetch-token-status 200)
           (-> body
               (response)
               (status 400))
           (let [access-token (parse-token body)]
             (log/debug "Parsed oauth response:" access-token)
             (request-identity access-token)))))

     (verify-oauth-authorization-response
       [{{:keys [oauth-state]} :session
         {request-state "state" authorization-code "code"} :params
         :as request}]
       {:pre [oauth-state request-state authorization-code]}
       (log/debug "Verifying authorization ...")
       (if (not= request-state oauth-state)
         (-> "Invalid state token"
             ((fn [x] (log/error x) x))
             (response)
             (status 400))
         (request-openid-token authorization-code)))

     (redirect-to-oauth []
       (let [csrf-token (base64 32)
             query-params {"client_id" client-id
                           "state" csrf-token
                           "scope" "user"}]
         (-> "%s?%s"
             (format authorization-endpoint (form-encode query-params))
             (redirect)
             (assoc-in [:session :oauth-state] csrf-token))))

     (get-username [identity-data]
       (or (:name identity-data)
           (:login identity-data)
           (:id identity-data)))

     (login-and-redirect-to-home [{:keys [session]} identity-data]
       (log/debug "Login and redirect")
       (log/debug "Identity data:" identity-data)
       (log/debug "Session:" session)
       (-> "/"
           (redirect)
           (assoc :session (assoc session
                                  :identity (get-username identity-data)
                                  :identity-data identity-data))))

     (handle-unauthorized [request & _]
       (if (= (:uri request) "/authorize")
         (let [verify-result (verify-oauth-authorization-response request)]
           (if (response? verify-result)
             verify-result
             (login-and-redirect-to-home request verify-result)))
         (redirect-to-oauth)))]
    (backends/session
      {:unauthorized-handler handle-unauthorized})))
