(ns timi.server.core
  (:require
    [clojusc.twig :as logger]
    [com.stuartsierra.component :as component]
    [taoensso.timbre :as log]
    [timi.config :as config]
    [timi.server.web.core :as web]
    [timi.server.components.core :as components]
    [timi.server.datasource.core :as datasource]
    [timi.server.infra.jdbc-extensions]
    [trifl.java :as java])
  (:gen-class))

(def config (config/read-config))

(logger/set-level! (get-in config [:log :ns])
                   (get-in config [:log :level]))

(def app
  "Used by the ring handler configuration in project.clj."
  (web/app config))

(defn get-system
  ([]
    (get-system #'app config))
  ([config]
    (get-system (web/app config) config))
  ([app config]
    (components/init app config)))

(defn -main
  [& args]
  (let [system (get-system)]
    (log/info "Starting Tímı ...")
    (component/start system)
    ;; Setup interrupt/terminate handling
    (java/add-shutdown-handler #(component/stop system))))
