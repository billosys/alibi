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
  (log/info "Validating command ...")
  (log/trace "Valid commands:" valid-commands)
  (log/trace "Command:" command)
  ((into #{} valid-commands) command))

(defn validate
  [{:keys [options arguments errors summary] :as parsed}
   log-fn valid-commands]
  (log/trace "Parsed main args as" parsed)
  (log/info "Validating main args ...")
  (let [command (keyword (first arguments))]
    (cond
      (:summary options)
        (assoc parsed :data summary)
      (:version options)
        (assoc parsed :data (util/get-version))
      (:log-level options)
        (let [log-level (:log-level options)]
          (log/warn "Setting log level to " log-level)
          (log-fn log-level)
          (assoc parsed :completed true
                        :data :ok))
      errors
        (assoc parsed :data (error-msg errors))
      (validate-command valid-commands command)
        (let [[command & subcommands] (vec (map keyword (:arguments parsed)))]
          (assoc parsed :command command :subcommands (vec subcommands)))
      :else
        (assoc parsed :unsupported true
                      :data (format "The command '%s' is not supported."
                                    (name command))))))
