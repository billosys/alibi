(ns timi.server.components.db
  (:require
    [com.stuartsierra.component :as component]
    [clojure.java.jdbc :as jdbc]
    [taoensso.timbre :as log]))

(defn get-db-spec
  [config]
  (-> config
      :persistence
      (config)))

(defrecord DBManager []
  component/Lifecycle

  (start [component]
    (log/info "Starting database manager ...")
    (let [cfg (get-in component [:cfg-mgr :cfg])
          db-spec (get-db-spec cfg)
          conn (jdbc/get-connection db-spec)]
      (log/trace "Using config:" cfg)
      (log/debug "Using db spec:" db-spec)
      (log/trace "Component keys:" (keys component))
      (log/debug "Successfully created database manager.")
      (assoc component
             :db-spec db-spec
             :conn conn)))

  (stop [component]
    (log/info "Stopping database manager ...")
    (log/trace "Component keys" (keys component))
    component))

(defn new-db-manager []
  (->DBManager))
