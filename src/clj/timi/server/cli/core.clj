(ns timi.server.cli.core
  (:require
    [clojure.string :as string]
    [clojure.tools.cli :as cli]
    [taoensso.timbre :as log]
    [timi.server.cli.parser :as parser]
    [timi.server.util :as util]
    [trifl.docs :as docs]))

(def options
  [["-h" "--help"]
   ["-v" "--version"]
   ["-s" "--summary"]
   ["-l" "--log-level LOG-LEVEL" "Log level for CLI"
    :parse-fn #(read-string %)
    :validate [#(contains? #{:debug :info :warn :error :fatal} %)
               "Must be one of :debug :info :warn :error :fatal"]]])

(def valid-commands
  {:help nil
   :config [
     :show]
   :db [
     :dump
     :init]
   :project [
     :create]
   :task [
     :create]})

(defn help
  "This function generates the output for the `help` options and/or commands."
  []
  (docs/get-docstring 'timi.server.cli.core 'run))

(defn dispatch
  [{:keys [command options data]}]
  (log/debug "dispatch command:" command)
  (log/debug "dispatch options:" options)
  (log/debug "dispatch data:" data)
  (or
    data
    (case command
      :help (do
              (help))
      :config "not implemented"
      :db "not implemented"
      :project "not implemented"
      :task "not implemented")))

(defn run
  "
  Usage: `timi [options] command [subcommands [options]]`

  Options:
  ```
  -h, --help                  Display this help text
  -v, --version               Current version of Tímı
  -s, --summary               Get the generated summary
  -l, --log-level LOG-LEVEL   Set the server-side CLI log level
  ```
  Commands:
  ```
  config    XXX
  db        XXX
  project   XXX
  task      XXX
  ```

  For more information on any available options or subcommands for a given
  command, use the `--help` option, e.g.:
  ```
  $ timi config --help
  ```
  "
  [config msg]
  (-> msg
      (string/split #"\s")
      (cli/parse-opts options :in-order true)
      (parser/validate-args
        (partial util/set-log-level config :cli-server)
        #'help
        (keys valid-commands))
      (dispatch)))
