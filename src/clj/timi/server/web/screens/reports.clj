(ns timi.server.web.screens.reports
  (:require
    [ring.util.response :as response]))

(defn render
  [client-state]
  (response/response
    {:template-data client-state
     :selmer-template "templates/reports.html"}))

(defn get-page
  [request]
  (render
    {:identity (:identity request)}))
