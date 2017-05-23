(ns timi.server.web.screens.docs
  (:require
    [clojure.java.io :as io]
    [markdown.core :as markdown]
    [ring.util.response :as response]))

(defn get-markdown
  [docs-path]
  (->> docs-path
       (format "docs/%s.md")
       (io/resource)
       (slurp)
       (markdown/md-to-html-string)))

(defn render
  [client-state docs-path]
  (response/response
    {:template-data (assoc client-state :markdown (get-markdown docs-path))
     :selmer-template "templates/docs.html"}))

(defn get-page
  ([request]
    (get-page request "overview"))
  ([request docs-path]
    (render
      {:identity
        (merge (get-in request [:session :identity-data])
               (:identity request))}
      docs-path)))
