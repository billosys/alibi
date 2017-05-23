(ns timi.server.web.screens.user
  (:require
    [ring.util.response :as response]))

(defn render
  [client-state]
  (response/response
    {:template-data (assoc client-state
                           :settings (seq (:identity client-state)))
     :selmer-template "templates/user.html"}))

(defn get-settings
  [request]
  (render
      {:identity
        (merge (get-in request [:session :identity-data])
               (:identity request))}))
