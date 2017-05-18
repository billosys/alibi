(ns timi.components
  (:require [com.stuartsierra.component :as component]
            [timi.components.httpd :as httpd]))

(defn init [app config]
  (component/system-map
    :cfg config
    :httpd (component/using
             (httpd/new-server app)
             [:cfg])))

(defn stop [system component-key]
  (->> system
       (component-key)
       (component/stop)
       (assoc system component-key)))

(defn start [system component-key]
  (->> system
       (component-key)
       (component/start)
       (assoc system component-key)))

(defn restart [system component-key]
  (-> system
      (stop component-key)
      (start component-key)))
