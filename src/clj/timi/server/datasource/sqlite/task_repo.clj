(ns timi.server.datasource.sqlite.task-repo
  (:require
    [clojure.java.jdbc :as db]
    [timi.server.datasource.sqlite.sqlite-helpers :refer [insert-id]]
    [timi.server.domain.billing-method :refer [billing-method?]]
    [timi.server.domain.task :as task :refer [hydrate-task]]))

(defn- task->row [task]
  (let [row {:project_id (:project-id task)
             :billing_type (name (:billing-method task))
             :name (:name task)}
        task-id (:task-id task)]
    (cond-> row
      ;TODO this seems untested
      (and task-id (pos? task-id)) (assoc :id task-id))))

(defn- row->task [task-row]
  (hydrate-task {:project-id (:project_id task-row)
                 :billing-method (keyword (:billing_type task-row))
                 :name (:name task-row)
                 :task-id (:id task-row)}))

(defn- add! [db-spec task]
  (-> db-spec
      (db/insert! :tasks (task->row task))
      (insert-id)))

(defn task-exists? [db-spec task-id]
  (seq (db/query db-spec ["select id from tasks where id=?" task-id])))

(defn get-task [db-spec task-id]
  (-> (db/query db-spec
                ["select t.*,p.billing_type project_billing_type from tasks t
                 left join projects p on p.id=t.project_id where t.id=?"
                 task-id])
      (first)
      (row->task)))

(defn task-id-for-project-id  [db-spec task-id]
  (:project-id (get-task db-spec task-id)))

(defn new [db-spec]
  (reify task/TaskRepository
    (-add! [this task] (add! db-spec task))
    (-task-exists? [this task-id] (task-exists? db-spec task-id))
    (-get [this task-id] (get-task db-spec task-id))
    (-project-id-for-task-id [this task-id]
      (task-id-for-project-id db-spec task-id))))
