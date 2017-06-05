(ns timi.server.components.logging
  (:require
    [com.stuartsierra.component :as component]
    [taoensso.timbre :as log]
    [timi.server.util :as util]))

(defrecord Logger []
  component/Lifecycle

  (start [component]
    (log/info "Starting logger ...")
    (let [cfg (get-in component [:cfg-mgr :cfg])]
      (util/set-log-level cfg :app)
      (log/trace "Using config:" cfg)
      (log/trace "Component keys:" (keys component))
      (log/debug "Successfully created logger.")
      component))

  (stop [component]
    (log/info "Stopping logger ...")
    (log/trace "Component keys" (keys component))
    component))

(defn new-logger []
  (->Logger))
