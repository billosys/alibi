(ns timi.server.cli.core
  (:require
    [clojure.string :as string]
    [clojure.tools.cli :as cli]
    [timi.server.cli.parser :as parser]
    [timi.server.util :as util]
    [trifl.docs :as docs]))

(def options
  [["-h" "--help"]
   ["-v" "--version"]
   ["-s" "--summary"]
   ["-l" "--log-level LOG-LEVEL" "Log level for CLI"
    :default :warn
    :parse-fn #(read-string %)
    :validate [#(contains? #{:debug :info :warn :error :fatal} %)
               "Must be one of :debug :info :warn :error :fatal"]]])

(def valid-commands
  {:help nil
   :config [:show]
   :db [:init :dump]
   :project [:create]
   :task [:create]})

(defn help
  "This function generates the output for the `help` options and/or commands."
  []
  (docs/print-docstring 'timi.server.cli.core 'run))

(defn dispatch
  [{:keys [command options exit-message ok?]}]
  (println command)
  (println options)
  (println exit-message)
  (println ok?)
  (if exit-message
    (util/exit (if ok? 0 1) exit-message)
    (case command
      :help (do
              (help)
              (util/exit 0))
      :config "not implemented"
      :db "not implemented"
      :project "not implemented"
      :task "not implemented")))

(defn run
  "
  Usage: `timi [options] command [subcommands [options]]`

  Options:
  ```
  -h, --help
  -v, --version
  -s, --summary
  -l, --log-level LOG-LEVEL  :warn  Log level for CLI
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
  timi config --help
  ```
  "
  [msg]
  (-> msg
      (string/split #"\s")
      ((fn [x] (println x) x))
      (cli/parse-opts options :in-order true)
      ((fn [x] (println x) x))
      (parser/validate-args #'help (keys valid-commands))
      (dispatch)))
