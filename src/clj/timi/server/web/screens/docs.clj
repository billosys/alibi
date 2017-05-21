(ns timi.server.web.screens.docs
  (:require
    [ring.util.response :as response]))

(defn render
  [client-state]
  (response/response
    {:template-data client-state
     :selmer-template "templates/docs.html"}))

(defn get-page
  [request]
  (render
    {:identity (:identity request)}))
