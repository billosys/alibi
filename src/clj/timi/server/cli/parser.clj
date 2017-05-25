(ns timi.server.cli.parser
  (:require
    [clojure.string :as string]
    [timi.server.util :as util]))

(defn error-msg [errors]
  (str "The following error(s) occurred while attempting to parse your "
       "command:\n\n"
       (string/join \newline errors)))

(defn validate-command
  [valid-commands command]
  ((into #{} valid-commands) command))

(defn validate-args
  [{:keys [options arguments errors summary]} help-fn valid-commands]
  (let [command (keyword (first arguments))]
    (cond
      (:help options)
        {:exit-message (help-fn) :ok? true}
      (:summary options)
        {:exit-message (println summary) :ok? true}
      (:version options)
        {:exit-message (util/get-version) :ok? true}
      errors
        {:exit-message (error-msg errors)}
      ;; custom validation on arguments
      (validate-command valid-commands command)
        {:command command :options options}
      :else
        {:exit-message (help-fn)})))
