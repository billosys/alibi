(ns timi.server.util)

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
