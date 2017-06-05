(ns timi.server.components.cli
  (:require
    [com.stuartsierra.component :as component]
    [taoensso.timbre :as log]
    [timi.server.cli.udp :as cli-server]))

(defrecord CLIServer []
  component/Lifecycle

  (start [component]
    (log/info "Starting CLI server ...")
    (let [cfg (get-in component [:cfg-mgr :cfg])
          server (cli-server/serve cfg)]
      (log/trace "Using config:" cfg)
      (log/trace "Component keys:" (keys component))
      (log/debug "Successfully created server:" server)
      (assoc component :cli server)))

  (stop [component]
    (log/info "Stopping CLI server ...")
    (log/trace "Component keys" (keys component))
    (when-let [server (:cli component)]
      (log/debug "Using server object:" server)
      (server))
    (assoc component :cli nil)))

(defn new-server []
  (->CLIServer))
