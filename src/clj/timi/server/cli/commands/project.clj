(ns timi.server.cli.commands.project
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.string :as string]
    [clojure.tools.cli :as cli]
    [taoensso.timbre :as log]
    [timi.server.cli.parser :as parser]
    [timi.server.datasource.sqlite.migrations :as db-migrator]
    [timi.server.domain.project :as project]
    [timi.server.domain.project-admin-app-svc :as projects]
    [timi.server.util :as util]
    [trifl.docs :as docs])
  (:import
    (clojure.lang PersistentHashMap)
    (java.lang Object String)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Supporting Constants/Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def billing-methods
  {:fixed-price true
   :hourly true
   :overhead true})

(defn list-projects
  [config]
  (log/info "Listing projects ...")
  (projects/get))

(defn create-project
  [config project-name billing-method]
  (log/info "Creating new project %s with billing method %s ..."
            project-name billing-method)
  ;; XXX use validation for billing methods ...
  ;; (parser/validate-member billing-methods billing-method)
  (let [cmd {:project-name project-name
             :billing-method billing-method}
        project-id (projects/new-project! cmd)]
    (log/debug "Created project.")
    (str ":project-id " project-id)))

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
  (docs/get-docstring 'timi.server.cli.commands.project 'run))

(defn dispatch
  "Dispatch on the project subcommands."
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
      :list (list-projects config)
      :create (create-project config
                (nth arguments 2 nil)
                (nth subcommands 2 :hourly))
      (parser/handle-unknown-subcommand subcommand help))))

(defn run
  "
  Usage: `timi project [subcommands [options]]`

  Subommands:
  ```
  help                          Display this help text
  list                          List all projects
  create NAME [BILLING-METHOD]  Create a new project to track
  ```"
  [config valid-subcommands parsed]
  (dispatch config (keys valid-subcommands) parsed))
