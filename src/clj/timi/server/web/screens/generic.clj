(ns timi.server.web.screens.generic
  (:require
    [ring.util.response :as response]))

(defn render
  [client-state]
  (response/response
    {:template-data client-state
     :selmer-template "templates/generic.html"}))

(defn get-page
  [request page-data]
  (render page-data))
