(ns timi.server.components.config
  (:require
    [clojure.edn]
    [clojure.java.io]
    [com.stuartsierra.component :as component]
    [taoensso.timbre :as log]
    [trifl.java :as java]))

(def config-file (clojure.java.io/resource "config.edn"))

(def ^:private sqlite-env (System/getenv "SQLITE_DBFILE"))

(defn read-config
  ([]
    (read-config config-file))
  ([f]
    (let [config (some-> f slurp clojure.edn/read-string)]
      (cond-> config
        (:sqlite config) (update-in [:sqlite :subname]
                                    #(or sqlite-env %))))))

(defrecord ConfigManager []
  component/Lifecycle

  (start [component]
    (log/info "Starting configuration manager ...")
    (let [cfg (read-config config-file)]
      (log/trace "Using config:" cfg)
      (log/debug "Component keys:" (keys component))
      (log/debug "Successfully created manager.")
      (assoc component :cfg cfg)))

  (stop [component]
    (log/info "Stopping configuration manager ...")
    (log/debug "Component keys" (keys component))
    (assoc component :cfg nil)))

(defn new-config-manager []
  (->ConfigManager))
