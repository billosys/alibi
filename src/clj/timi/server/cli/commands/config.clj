(ns timi.server.cli.commands.config
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.string :as string]
    [clojure.tools.cli :as cli]
    [taoensso.timbre :as log]
    [timi.server.cli.parser :as parser]
    [timi.server.util :as util]
    [trifl.docs :as docs]))

(def options
  ;; Note that any options added here need to be named differently than those
  ;; in timi.server.cli.core/options.
  [])

(defn validate-subcommand
  [subcommand]
  (log/info "Validating subcommand ...")
  (log/trace "Command:" subcommand)
  (#{:help :show} subcommand))

(defn help
  "This function generates the output for the `help` options and/or commands."
  []
  (docs/get-docstring 'timi.server.cli.commands.config 'run))

(defn show-config
  [config]
  (-> config
      (pprint)
      (with-out-str)))

(defn handle-unknown-subcommand
  [subcommand]
  (let [msg (format "The subcommand '%s' is not supported."
                    (name subcommand))]
    (log/error msg)
    (format "\nERROR: %s\n\n%s" msg (help))))

(defn dispatch
  [config {:keys [options arguments errors data subcommands]}]
  (let [subcommand (or (first subcommands) :show)]
    (log/infof "Running '%s' subcommand ..." subcommand)
    (log/debug "dispatch subcommands:" subcommands)
    (case (validate-subcommand subcommand)
      :help (help)
      :show (show-config config)
      (handle-unknown-subcommand subcommand))))

(defn run
  "
  Usage: `timi config [subcommands [options]]`

  Subommands:
  ```
  help    Display this help text
  show    Display the complete current configuration values
  ```
  "
  [config parsed]
  (dispatch config parsed))
