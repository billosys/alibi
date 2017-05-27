(ns timi.server.components.core
  (:require
    [com.stuartsierra.component :as component]
    [taoensso.timbre :as log]
    [timi.server.components.cli :as cli]
    [timi.server.components.config :as config]
    [timi.server.components.httpd :as httpd]
    [timi.server.components.logging :as logging]))

(defn init [app]
  (component/system-map
    :cfg-mgr (component/using
             (config/new-config-manager)
             [])
    :logger (component/using
             (logging/new-logger)
             [:cfg-mgr])
    :cli (component/using
             (cli/new-server)
             [:cfg-mgr :logger])
    :httpd (component/using
             (httpd/new-server app)
             [:cfg-mgr :logger])))

(defn stop [system component-key]
  (->> system
       (component-key)
       (component/stop)
       (assoc system component-key)))

(defn start [system component-key]
  (log/info "Starting Tímı ...")
  (->> system
       (component-key)
       (component/start)
       (assoc system component-key)))

(defn restart [system component-key]
  (-> system
      (stop component-key)
      (start component-key)))
