(ns timi.client.cli
  (:require
    [billo.udp.client.core :as udp]
    [billo.udp.client.util :as util]
    [cljs.nodejs :as node]
    [clojure.string :as string]
    [clojusc.twig :as logger]
    [taoensso.timbre :as log]
    [timi.client.config :as config]))

;;; CLI setup and functions

(node/enable-util-print!)

(logger/set-level! (get-in config/data [:cli :client :log :ns])
                   (get-in config/data [:cli :client :log :level]))


;;; UDP Callback

(defn handle-receive
  [client data]
  (let [buffer (js/Buffer. data)]
    (log/debug "Received data:" data)
    (udp/close client)
    (log/debug "Disconnected.")
    (println (str data))
    (.exit node/process)))

;;; Main

(defn -main
  [& args]
  (log/debug "Got args:" args)
  (let [client (udp/client)
        port (get-in config/data [:cli :server :port])
        data (util/args->str args)]
    (udp/on-receive client #(handle-receive client %))
    (udp/send client port data)
    (util/wait)))

(set! *main-cli-fn* -main)
