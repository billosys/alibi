(ns timi.server.cli.tcp
  (:require
    [clojure.tools.logging :as log]
    [net.tcp :as tcp]
    [net.ty.channel :as channel]
    [net.ty.pipeline :as pipeline]
    [timi.config :as config]
    [timi.server.cli.core : as cli]))

(def config (config/read-config))

(defn parse-commands
  [cmds]
  (log/info "In parse-commands, got:" cmds)
  cmds)

(defn run-commands
  [cmds]
  (log/info "In run-commands, got:" cmds)
  cmds)

(defn pipeline
  []
  (pipeline/channel-initializer
   [(pipeline/line-based-frame-decoder)
    pipeline/string-decoder
    pipeline/string-encoder
    pipeline/line-frame-encoder
    (pipeline/with-input [ctx msg]
      (channel/write-and-flush! ctx (-> msg
                                        (parse-commands)
                                        (run-commands))))]))

(defn serve
  ([]
    (serve config))
  ([config]
    (tcp/server
      {:handler (pipeline)}
      (get-in config [:cli :server :host])
      (get-in config [:cli :server :port]))))

(defn -main
  [& _]
  (serve))
