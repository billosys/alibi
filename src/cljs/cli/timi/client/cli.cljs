(ns timi.client.cli
  (:require
    [cljs.nodejs :as node]
    [clojure.string :as string]
    [clojusc.twig :as logger]
    [taoensso.timbre :as log]
    [timi.client.config :as config]
    [timi.client.tcp :as tcp]))

;;; CLI setup and functions

(node/enable-util-print!)

(logger/set-level! (get-in config/data [:cli :client :log :ns])
                   (get-in config/data [:cli :client :log :level]))

(defn args->str
  [args]
  (str (string/join " "args) "\n"))

;;; Callbacks

(defn handle-connect
  [client data]
  (log/debug "Connected.")
  (tcp/send client data))

(defn handle-receive
  [client data]
  (let [buffer (js/Buffer. data)]
    (log/debug "Received data:" data)
    (tcp/disconnect client)
    (log/debug "Disconnected.")
    (println (str data))))

;;; Main

(defn -main
  [& args]
  (log/debug "Got args:" args)
  (let [client (tcp/connect config/data)
        data (args->str args)]
    (tcp/on-connect client #(handle-connect client data))
    (tcp/on-receive client #(handle-receive client %))))

(set! *main-cli-fn* -main)
