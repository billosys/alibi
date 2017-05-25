(ns timi.server.cli.parser
  (:require
    [clojure.string :as string]
    [taoensso.timbre :as log]
    [timi.server.util :as util]))

(defn error-msg [errors]
  (->> errors
       (string/join \newline)
       (str "The following error(s) occurred while attempting "
            "to parse your command:\n\n")))

(defn validate-command
  [valid-commands command]
  ((into #{} valid-commands) command))

(defn validate-args
  [{:keys [options arguments errors summary]} log-fn help-fn valid-commands]
  (let [command (keyword (first arguments))]
    (cond
      (:help options)
        {:data (help-fn) :options options}
      (:summary options)
        {:data summary :options options}
      (:version options)
        {:data (util/get-version) :options options}
      (:log-level options)
        (let [log-level (:log-level options)]
          (log/warn "Setting log level to " log-level)
          (log-fn log-level)
          {:data :ok :options options})
      errors
        {:data (error-msg errors)}
      ;; custom validation on arguments
      (validate-command valid-commands command)
        {:command command :options options}
      :else
        {:data (help-fn) :options options})))
