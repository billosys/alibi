(ns timi.server.cli.core
  (:require
    [clojure.string :as string]
    [clojure.tools.cli :as cli]
    [taoensso.timbre :as log]
    [timi.server.cli.commands.config :as config-cmd]
    [timi.server.cli.parser :as parser]
    [timi.server.util :as util]
    [trifl.docs :as docs]))

(def options
  [["-h" "--help"]
   ["-v" "--version"]
   ["-s" "--summary"]
   ["-l" "--log-level LOG-LEVEL" "Log level for CLI"
    :parse-fn keyword
    :validate [#(contains? #{:trace :debug :info :warn :error :fatal} %)
               "Must be one of: trace, debug, info, warn, error, or fatal"]]])

(def valid-commands
  {:help true
   :config {
     :help true
     :show true}
   :db {
     :dump true
     :init true}
   :project {
     :create true}
   :task {
     :create true}})

(defn help
  "This function generates the output for the `help` options and/or commands."
  []
  (docs/get-docstring 'timi.server.cli.core 'run))

(defn dispatch
  [config {:keys [options arguments errors data command] :as parsed}]
  (log/trace "dispatch keys:" parsed)
  (or
    (cond
      (:help options)
        (help)
      (:version options)
        data
      (:summary options)
        data
      (:unsupported parsed)
        (format "\nERROR: %s\n\n%s" data (help))
      (:completed parsed)
        data
      errors
        (do
          (log/error data)
          data))
    (case command
      :help (help)
      :config (config-cmd/run config parsed)
      :db "not implemented"
      :project "not implemented"
      :task "not implemented")))

;; Note that the option summary and commands are "hard-documented" here due
;; to the fact that we want to have the Codox-generated documentation match
;; what is sent to stdout (when using the CLI client). For this reason, we
;; offer the `--summary` option, for easily copying-and-pasting an update
;; into the docstring. The "Commands" section, on the other hand, does
;; require manual curation.
(defn run
  "
  Usage: `timi [options] command [subcommands [options]]`

  Options:
  ```
  -h, --help                  Display this help text
  -v, --version               Current version of Tímı
  -s, --summary               Get the generated summary
  -l, --log-level=LOG-LEVEL   Set the server-side CLI log level
  ```
  Commands:
  ```
  config    Perform operations relating to Tímı configuration
  db        Perform database operations
  project   Perform project-related operations
  task      Perform task-related operations
  ```

  For more information on any available options or subcommands for a given
  command, use the `help` subcommand, e.g.:
  ```
  $ timi config help
  ```
  or, e.g.:
  ```
  $ timi db help
  ```
  "
  [config msg]
  (-> msg
      (string/split #"\s")
      (cli/parse-opts options :in-order true)
      (parser/validate
        (partial util/set-log-level config :cli-server)
        (keys valid-commands))
      ((partial dispatch config))))
