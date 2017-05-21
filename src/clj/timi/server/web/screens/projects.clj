(ns timi.server.web.screens.projects
  (:require
    [ring.util.response :as response]
    [timi.server.domain.task :refer [project-id-for-task-id]]
    [timi.server.util :refer [str->int]]))

(defn- default-client-state
  [id selected-task-id]
  (cond-> {:identity id
           :selected-project-id ""
           :selected-task-id  ""}
    selected-task-id
    (assoc :selected-task-id selected-task-id
           :selected-project-id (project-id-for-task-id selected-task-id))))

(defn render
  [client-state]
  (response/response
    {:template-data (assoc client-state
                           :identity (:identity client-state))
     :selmer-template "templates/projects.html"}))

(defn get-page
  [{{selected-project-id "selected-project-id"} :params :as request}]
  (render
    (default-client-state
      (:identity request)
      (str->int selected-project-id))))
