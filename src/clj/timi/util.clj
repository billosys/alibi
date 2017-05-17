(ns timi.util)

(defn str->decimal [dec-str]
  (when dec-str
    (try
      (BigDecimal. dec-str)
      (catch NumberFormatException e nil))))

(defn str->int [int-str]
  (when int-str
    (try (Integer. int-str) (catch NumberFormatException e nil))))

(defn str->keyword [keyword-str]
  (when (seq keyword-str)
    (keyword (subs keyword-str 1))))
