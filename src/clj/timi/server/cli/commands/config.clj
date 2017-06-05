(ns timi.server.cli.commands.config
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.string :as string]
    [clojure.tools.cli :as cli]
    [taoensso.timbre :as log]
    [timi.server.cli.parser :as parser]
    [timi.server.util :as util]
    [trifl.docs :as docs]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;   Supporting Constants/Functions   ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn show-config
  [config]
  (-> config
      (pprint)
      (with-out-str)))

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
  (docs/get-docstring 'timi.server.cli.commands.config 'run))

(defn dispatch
  "Dispatch on the config subcommands."
  [config valid-subcommands
   {:keys [options arguments errors data subcommands]}]
  (log/trace "Valid subcommands:" valid-subcommands)
  (let [subcommand (parser/get-default-subcommand valid-subcommands
                                                  (first subcommands)
                                                  :show)]
    (log/infof "Running '%s' subcommand ..." subcommand)
    (log/debug "dispatch subcommands:" subcommands)
    (case subcommand
      :help (help)
      :show (show-config config)
      (parser/handle-unknown-subcommand subcommand help))))

(defn run
  "
  Usage: `timi config [subcommands [options]]`

  Subommands:
  ```
  help    Display this help text
  show    Display the complete current configuration values
  ```"
  [config valid-subcommands parsed]
  (dispatch config (keys valid-subcommands) parsed))
