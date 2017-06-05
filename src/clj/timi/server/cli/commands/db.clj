(ns timi.server.cli.commands.db
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.string :as string]
    [clojure.tools.cli :as cli]
    [taoensso.timbre :as log]
    [timi.server.cli.parser :as parser]
    [timi.server.datasource.sqlite.migrations :as db-migrator]
    [timi.server.util :as util]
    [trifl.docs :as docs])
  (:import
    (clojure.lang PersistentHashMap)
    (java.lang Object String)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Supporting Constants/Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; XXX this will be generalized later (and moved) when different db backends
;;      are supported
(defmulti get-connection-data
  (fn [x & ys]
    (mapv class (into [x] ys))))

(defmethod get-connection-data [PersistentHashMap]
  [config]
  (log/debug "Getting connection data from config ...")
  (-> config
      :persistence
      (config)))

(defmethod get-connection-data [String String]
  [db-type filename]
  (log/debug "Creating connection data from db-type & filename ...")
  {:subprotocol db-type
   :subname filename})

(defmethod get-connection-data [String]
  [filename]
  (log/debug "Creating connection data from filename ...")
  (get-connection-data "sqlite" filename))

(defn get-migration-path
  [db-type]
  (log/debug "Getting db migration path ...")
  (format "datasources/%s/migrations" db-type))

;; XXX this will be generalized later (and moved) when different db backends
;;      are supported
(defn init-db
  ([connection-data]
    (log/info "Creating initial database ...")
    (log/debug "connection-data:" connection-data)
    (log/debug "connection-data:" (:subprotocol connection-data))
    (db-migrator/apply-migrations!
      connection-data
      (-> connection-data
          :subprotocol
          (get-migration-path)))
    (log/debug "Database created.")
    :ok)
  ([config filename]
    (log/debug "Checking for init type ...")
    (log/debug "Config type:" (type config))
    (log/debug "Filename type:" (type filename))
    (log/debug "Filename:" filename)
    (if (nil? filename)
      (init-db (get-connection-data config))
      (init-db (get-connection-data filename)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Tímı CLI API   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def options
  ;; Note that any options added here need to be named differently than those
  ;; in timi.server.cli.core/options.
  [])

(defn help
  "This function generates the output for the `help` options and/or commands."
  []
  (docs/get-docstring 'timi.server.cli.commands.db 'run))

(defn dispatch
  "Dispatch on the db subcommands."
  [config valid-subcommands
   {:keys [options arguments errors data subcommands]}]
  (let [subcommand (parser/get-default-subcommand valid-subcommands
                                                  (first subcommands))]
    (log/infof "Running '%s' subcommand ..." subcommand)
    (log/trace "Using config:" config)
    (log/debug "dispatch subcommands:" subcommands)
    (log/debug "dispatch arguments:" arguments)
    (case subcommand
      :help (help)
      :dump :not-implemented
      :init (init-db config (nth arguments 2 nil))
      (parser/handle-unknown-subcommand subcommand help))))

(defn run
  "
  Usage: `timi db [subcommands [options]]`

  Subommands:
  ```
  help                      Display this help text
  dump                      Dump the schema and inserts to stdout (not implemented)
  init CONNECTION-STRING    Initialize new app storage with the given
                            connection string (for sqlite this is just
                            a filename)
  ```"
  [config valid-subcommands parsed]
  (dispatch config (keys valid-subcommands) parsed))
