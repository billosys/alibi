(ns timi.server.cli.parser
  (:require
    [clojure.string :as string]
    [taoensso.timbre :as log]
    [timi.server.util :as util]))

(defn clean-arg
  [arg]
  (->> arg
       (remove #(= % (char 0)))
       (apply str)))

(defn args->commands
  [args]
  (->> args
       (map clean-arg)
       (remove empty?)
       (map string/trim)
       (map string/trim-newline)
       (map keyword)
       (vec)))

(defn error-msg
  [errors]
  (->> errors
       (map #(str "\t" %))
       (string/join \newline)
       (str "\nThe following error(s) occurred while attempting "
            "to parse your command:\n\n")))

(defn handle-unknown-subcommand
  [subcommand help-fn]
  (let [msg (format "The subcommand '%s' is not supported."
                    (name subcommand))]
    (log/error msg)
    (format "\nERROR: %s\n\n%s" msg (help-fn))))

(defn validate-membership
  "This function takes keywords that form a set and a single keyword which may
  or may not be a member of the set. If it is, the keyword is returned; if it
  is not, a nil is returned."
  [valid-members member]
  ((set valid-members) member))

(defn validate-command
  [valid-commands command]
  (log/info "Validating command ...")
  (log/trace "Valid commands:" valid-commands)
  (log/trace "Command:" command)
  (validate-membership valid-commands command))

(defn validate-subcommand
  [valid-subcommands subcommand]
  (log/info "Validating subcommand ...")
  (log/trace "Valid subcommands:" valid-subcommands)
  (log/trace "Subcommand:" subcommand)
  (validate-membership valid-subcommands subcommand))

(defn get-default-subcommand
  ([valid-subcommands subcommand]
    (get-default-subcommand valid-subcommands subcommand :help))
  ([valid-subcommands subcommand default]
    (log/tracef "Got subcommand %s and default %s" subcommand default)
    (log/trace "Validation result:" (validate-subcommand valid-subcommands subcommand))
    (if (validate-subcommand valid-subcommands subcommand)
      subcommand
      default)))

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
      (:help options)
        parsed
      (:banner options)
        (assoc parsed :data (util/get-banner))
      (:log-level options)
        (let [log-level (:log-level options)]
          (log/warn "Setting log level to " log-level)
          (log-fn log-level)
          (assoc parsed :completed true
                        :data :ok))
      errors
        (assoc parsed :data (error-msg errors))
      (validate-command valid-commands command)
        (let [[command & subcommands] (args->commands (:arguments parsed))]
          (assoc parsed :command command :subcommands subcommands))
      :else
        (assoc parsed :unsupported true
                      :data (format "The command '%s' is not supported."
                                    (name command))))))
