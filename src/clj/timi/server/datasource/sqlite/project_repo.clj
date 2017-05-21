(ns timi.server.datasource.sqlite.project-repo
  (:require
    [clojure.java.jdbc :as db]
    [timi.server.datasource.sqlite.sqlite-helpers :refer [insert-id]]
    [timi.server.domain.project :as project :refer [hydrate-project]]))

(defn- row->project [row]
  (when row
    (hydrate-project {:project-id (:id row)
                      :billing-method (keyword (:billing_type row))
                      :project-name (:name row)})))

(defn- project->row [project]
  (let [row {:billing_type (name (:billing-method project))
             :name (:project-name project)}
        project-id (:project-id project)]
    (cond-> row
      ;; TODO this seems unused/not tested

      (and project-id (pos? project-id)) (assoc :id project-id))))

(defn- get-project [db-spec project-id]
  (->
    (db/query db-spec ["select * from projects where id=?" project-id])
    first
    row->project))

(defn- add! [db-spec project]
  (-> db-spec
      (db/insert! :projects (project->row project))
      (insert-id)))

(defn exists? [db-spec project-id]
  (seq (db/query db-spec ["select id from projects where id=?" project-id])))

(defn new [db-spec]
  (reify
    project/ProjectRepository
    (-get [this project-id] (get-project db-spec project-id))
    (-add! [this project] (add! db-spec project))
    (-exists? [this project-id] (exists? db-spec project-id))))

