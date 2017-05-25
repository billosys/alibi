(ns timi.server.components.cli
  (:require
    [com.stuartsierra.component :as component]
    [taoensso.timbre :as log]
    [timi.server.cli.tcp :as cli-server]))

(defrecord CLIServer []
  component/Lifecycle

  (start [component]
    (log/info "Starting CLI server ...")
    (let [cfg (get-in component [:cfg])
          server (cli-server/serve cfg)]
      (log/trace "Using config:" cfg)
      (log/debug "Component keys:" (keys component))
      (log/debug "Successfully created server:" server)
      (assoc component :cli server)))

  (stop [component]
    (log/info "Stopping CLI server ...")
    (log/debug "Component keys" (keys component))
    (if-let [server (:cli component)]
      (do (log/warn "XXX Implement CLI server shutdown function")
          (log/debug "Using server object:" server)
          ;; XXX make shutdown call here
          ))
    (assoc component :cli nil)))

(defn new-server []
  (->CLIServer))
