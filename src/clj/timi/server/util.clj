(ns timi.server.util
  (:require
    [clojure.java.io :as io]
    [clojusc.twig :as logger]))

(defn str->decimal
  [dec-str]
  (when dec-str
    (try
      (BigDecimal. dec-str)
      (catch NumberFormatException e nil))))

(defn str->int
  [value]
  (when (string? value)
    (try
      (Integer/parseInt value)
      (catch NumberFormatException e nil))))

(defn str->keyword
  [keyword-str]
  (when (seq keyword-str)
    (keyword (subs keyword-str 1))))

(defn get-version
  []
  "[add version info]")

(defn exit
  ([status]
    (exit status nil))
  ([status msg]
    (when msg (println msg))
    (System/exit status)))

(defn set-log-level-type
  [config config-keys level]
  (logger/set-level! (get-in config (conj config-keys :ns))
                     (or level
                         (get-in config (conj config-keys :level)))))

(defn set-log-level
  ([config type]
    (set-log-level config type nil))
  ([config type level]
    (case type
      :app (set-log-level-type config [:log] level)
      :cli-client (set-log-level-type config [:cli :client :log] level)
      :cli-server (set-log-level-type config [:cli :server :log] level)
      :repl (set-log-level-type config [:repl :log] level))))

(defn get-banner
  []
  (slurp (io/resource "text/banner.txt")))
