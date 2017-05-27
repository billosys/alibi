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

(def options
  ;; Note that any options added here need to be named differently than those
  ;; in timi.server.cli.core/options.
  [])

(defn validate-subcommand
  [subcommand]
  (log/info "Validating subcommand ...")
  (log/trace "Command:" subcommand)
  (#{:help :list :create} subcommand))

(defn validate-billing-method
  [method]
  (log/info "Validating billing method ...")
  (log/trace "method:" method)
  (#{:fixed-price :hourly :overhead} method))

(defn help
  "This function generates the output for the `help` options and/or commands."
  []
  (docs/get-docstring 'timi.server.cli.commands.project 'run))

(defn handle-unknown-subcommand
  [subcommand]
  (let [msg (format "The subcommand '%s' is not supported."
                    (name subcommand))]
    (log/error msg)
    (format "\nERROR: %s\n\n%s" msg (help))))

(defn list-projects
  [config]
  (log/info "Listing projects ...")
  (projects/get))

(defn create-project
  [config project-name billing-method]
  (log/info "Creating new project %s with billing method %s ..."
            project-name billing-method)
  ;; XXX use the validate-billing-method function above
  (let [cmd {:project-name project-name
             :billing-method billing-method}
        project-id (projects/new-project! cmd)]
    (log/debug "Created project.")
    (str ":project-id " project-id)))

(defn dispatch
  [config {:keys [options arguments errors data subcommands]}]
  (let [subcommand (or (first subcommands) :help)]
    (log/infof "Running '%s' subcommand ..." subcommand)
    (log/trace "Using config:" config)
    (log/debug "dispatch subcommands:" subcommands)
    (log/debug "dispatch arguments:" arguments)
    (case (validate-subcommand subcommand)
      :help (help)
      :list (list-projects config)
      :create (create-project config
                (nth arguments 2 nil)
                (nth subcommands 2 :hourly))
      (handle-unknown-subcommand subcommand))))

(defn run
  "
  Usage: `timi project [subcommands [options]]`

  Subommands:
  ```
  help                          Display this help text
  list                          List all projects
  create NAME [BILLING-METHOD]  Create a new project to track
  ```
  "
  [config parsed]
  (dispatch config parsed))
