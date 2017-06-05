(ns timi.server.core
  (:require
    [clojusc.twig :as logger]
    [com.stuartsierra.component :as component]
    [timi.server.web.core :as web]
    [timi.server.components.core :as components]
    [timi.server.datasource.core :as datasource]
    [timi.server.infra.jdbc-extensions]
    [timi.server.util :as util]
    [trifl.java :as java])
  (:gen-class))

(defn get-system
  "It is often useful for other parts of the application to be able to obtain a
  system data structure (e.g., running a dev system or bringing up the
  components needed for a CLI), so this function is provided as a universal
  means of doing so."
  ([]
    (get-system #'web/app))
  ([app]
    (components/init app)))

(defn -main
  "The Tímı application's entry point."
  [& args]
  ;; Set up default logging here because the system hasn't had a chance to
  ;; it yet; once the configuration is available, the logger will re-configure.
  (logger/set-level! 'timi :info)
  (let [system (get-system)]
    (component/start system)
    ;; Setup interrupt/terminate handling
    (java/add-shutdown-handler #(component/stop system))))
