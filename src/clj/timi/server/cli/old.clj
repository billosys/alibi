(ns timi.server.cli.old
  (:require
    [clojure.pprint :refer [pprint]]
    [timi.config :as config]
    [timi.server.datasource.core :as datasource]
    [timi.server.datasource.sqlite.migrations :refer [apply-migrations!]]
    [timi.server.domain.billing-method :refer [billing-method?]]
    [timi.server.domain.project :as project]
    [timi.server.domain.project-admin-app-svc :as projects]
    [timi.server.domain.task-admin-app-svc :as tasks]
    [timi.server.util :refer [str->decimal str->int str->keyword]]))

(defn args->map [args]
  (into {} (map (fn [[k v]] [(str->keyword k) v]) (partition 2 args))))

(def allowed-billing-methods #{:fixed-price :hourly :overhead})

(defn projects-create
  [& args]
  (let [{:keys [name billing-method] :as opts} (args->map args)
        billing-method (str->keyword billing-method)]
    (assert name "provide a :name")
    (assert (allowed-billing-methods billing-method)
            (str "billing method should be any of " allowed-billing-methods))
    (let [cmd {:project-name name
               :billing-method billing-method}]
      (println "Creating new project:")
      (pprint cmd)
      (let [project-id (projects/new-project! cmd)]
        (println "Done. Id for project is" project-id)))))

(defn tasks-create
  [& args]
  (let [{:keys [for-project billing-method name]
         :or {billing-method ":project"}} (args->map args)
        billing-method (str->keyword billing-method)
        for-project (str->int for-project)]
    (assert (seq name) ":name is required")
    (assert for-project ":for-project is required and should be integer")
    (assert (or (billing-method? billing-method) (= :project billing-method))
            (str billing-method " not a valid billing method"))

    (let [project (project/get for-project)]
      (assert project (str "project " for-project " not found"))

      (let [cmd {:for-project-id for-project
                 :billing-method (or (billing-method? billing-method)
                                     (:billing-method project))
                 :task-name name}]
        (println "Creating new task:")
        (pprint cmd)
        (let [task-id (tasks/new-task! cmd)]
          (println "Done. Id for task is" task-id))))))

(defn projects-help
  [& args]
  (println "Usage: lein run projects <command>")
  (println)
  (println "The following subcommands are available:")
  (println)
  (println "* create :name <project-name> :billing-method <billing-method")
  (println "  Creates a new project with the given name")
  (println "  billing-method can be any of :fixed-price, :hourly or :overhead")
  (println)
  (println "* help")
  (println "  This info"))


(defn tasks-help
  [& args]
  (println "Usage: lein run tasks")
  (println)
  (println "The following subcommands are available:")
  (println)
  (println "* create :for-project <project-id> :name <task-name> [:billing-method <billing-method]")
  (println "  Creates a new task")
  (println "  billing-method can be any of :fixed-price, :hourly, :overhead or :project Default is :project, which uses the billing method of the project.")
  (println)
  (println "* help")
  (println "  This info"))

(defn sqlite-create-db
  [& args]
  (let [filename (:filename (args->map args))]
    (assert (seq filename) ":filename is required")
    (apply-migrations! {:subprotocol "sqlite"
                        :subname filename}
                       "datasources/sqlite/migrations")
    (println "Created database" filename)))

(defn sqlite-help
  [& args]
  (println "Usage: lein run sqlite")
  (println)
  (println "The following subcommands are available:")
  (println)
  (println "* create-db :filename <filename>")
  (println "  Provisions a new sqlite db with the most recent schema")
  (println)
  (println "* help")
  (println "  This info"))

(defn cli-help []
  (println "Welcome to the Tími time tracker command line interface.")
  (println)
  (println "Usage: lein run <subcommand> [opts]")
  (println)
  (println "The following subcommands are available:")
  (println)
  (println "* projects")
  (println "* tasks")
  (println "* sqlite")
  (println)
  (println "Use lein run <subcommand> help to get more info about the subcommand"))

(def cli-structure
  {"projects" {"create" projects-create
               :default projects-help}
   "tasks" {"create" tasks-create
            :default tasks-help}
   "sqlite" {"create-db" sqlite-create-db
             :default sqlite-help}
   :default cli-help})

(defn invoke-cli-function [[cmd & more] cli]
  (let [val (or (get cli cmd) (get cli :default))]
    (if (map? val)
      (recur more val)
      (apply val more))))

(def config (timi.config/read-config))

(defn cli-main [& args]
  (invoke-cli-function args cli-structure))

(defmacro cli-clj [& args]
  `(apply cli-main '~(map str args)))

(defn -main [& args]
  (let [persistence-middleware (datasource/get-persistence-middleware config)]
    (persistence-middleware (fn [] (apply cli-main args)))))

(defmacro cli [& args]
  `(-main ~@(map str args)))
