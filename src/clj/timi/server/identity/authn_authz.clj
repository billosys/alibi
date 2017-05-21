(ns timi.server.identity.authn-authz
  (:require
    [buddy.auth.middleware :as auth-middleware]
    [buddy.auth :refer [authenticated? throw-unauthorized]]
    [buddy.auth.protocols]
    [buddy.core.keys :as keys]
    [buddy.sign.jwt :as jwt]
    [ring.util.response :as response]
    [timi.server.identity.openid-connect :as openid]
    [timi.server.infra.date-time :as date-time]))

(defn get-auth-backends
  [config]
  {:openid (fn [] (openid/backend (:openid config)))
   :single-user (fn [] (get-in config [:single-user :username]
                               "anonymous"))})

(defn get-auth-backend
  [config]
  (let [auth-backend-key (:authentication config :single-user)
        auth-backends (get-auth-backends config)]
    (if-let [backend (get auth-backends auth-backend-key)]
      (backend)
      (throw (Exception. (str "Auth backend " auth-backend-key " not supported"
                              "try one of " (keys auth-backends)))))))

(defn as-user-backend [username]
  (reify
    buddy.auth.protocols/IAuthentication
    (-parse [_ _] true)
    (-authenticate [_ _ _] username)
    buddy.auth.protocols/IAuthorization
    (-handle-unauthorized [_ _ _]
      (->
        (response/response  "Permission denied")
        (response/status 403)))))

(defn wrap-authorize-all [handler]
  (fn [{:keys [uri] :as request}]
    (if (not (authenticated? request))
      (throw-unauthorized {:message "not authorized"})
      (handler request))))

(defn wrap-authn-and-authz [handler backend-or-username]
  (let [backend (if (string? backend-or-username)
                  (as-user-backend backend-or-username)
                  backend-or-username)]
    (-> handler
        (wrap-authorize-all)
        (auth-middleware/wrap-authorization backend)
        (auth-middleware/wrap-authentication backend))))
