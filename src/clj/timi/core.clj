(ns timi.core
  (:require
    [clojusc.twig :as logger]
    [com.stuartsierra.component :as component]
    [taoensso.timbre :as log]
    [timi.config :as config]
    [timi.web.core :as web]
    [timi.cli :refer [cli-main cli-clj]]
    [timi.components :as components]
    [timi.datasource.core :as datasource]
    [timi.infra.jdbc-extensions]
    [trifl.java :as java])
  (:gen-class))

(def config (timi.config/read-config))

(logger/set-level! (get-in config [:log :ns])
                   (get-in config [:log :level]))

(def app
  "Used by the ring handler configuration in project.clj."
  (web/app config))

(defn -main [& args]
  (if (seq args)
    (let [persistence-middleware (datasource/get-persistence-middleware config)]
      (persistence-middleware (fn [] (apply cli-main args))))
    (let [system (components/init #'app config)]
      (log/info "Starting Tímı ...")
      (component/start system)
      ;; Setup interrupt/terminate handling
      (java/add-shutdown-handler #(component/stop system)))))

(defmacro cli [& args]
  `(-main ~@(map str args)))
