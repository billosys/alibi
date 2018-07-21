(ns timi.server.components.cli
  (:require
    [billo.udp.server.core :as cli-server]
    [com.stuartsierra.component :as component]
    [taoensso.timbre :as log]
    [timi.server.cli.core :as cli]))

(defn cli-parser
  [data options]
  (cli/run (:config options) data))

(defrecord CLIServer []
  component/Lifecycle

  (start [component]
    (log/info "Starting CLI server ...")
    (let [cfg (get-in component [:cfg-mgr :cfg])
          options {:port (get-in cfg [:cli :server :port])
                   :parser-fn cli-parser
                   :parser-opts {:config cfg}}
          server (cli-server/run options)]
      (log/trace "Using config:" cfg)
      (log/trace "Using server options:" options)
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
