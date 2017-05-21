(ns timi.server.domain.entry
  (:require
    [clojure.set :refer [rename-keys]]
    [timi.server.domain.billing-method :as billing-method]
    [timi.server.domain.project :refer [project?]]
    [timi.server.domain.task :refer [task?]]
    [timi.server.infra.date-time :refer [local-time? before? local-date?]]))

(defn- valid-time-interval? [{:keys [start-time end-time]}]
  (and
    (local-time? start-time)
    (local-time? end-time)
    (before? start-time end-time)))


(defrecord Entry [entry-id
                  task-id
                  for-date
                  start-time
                  end-time
                  user-id
                  comment
                  billable?
                  billed?])

(defn valid-billable?
  [value for-task]
  (if-not value
    true
    (billing-method/billable? (:billing-method for-task))))

(defn- assert-valid-billable?
  [value for-task]
  (assert (valid-billable? value for-task)
          (str "entry can only be billable when task billing method in "
               "#{:hourly :hourly}")))

(defn hydrate-entry
  [{:keys [task-id
           for-date
           start-time
           end-time
           user-id
           billable?
           comment
           billed?] :as cmd}]
  {:pre [(integer? task-id)
         (valid-time-interval? cmd)
         (local-date? for-date)
         (instance? Boolean billable?)
         (or (nil? billed?) (instance? Boolean billed?))]}
  (map->Entry (assoc (rename-keys cmd { })
                     :billed? (boolean billed?))))

(defn new-entry
  [{:keys [billable?] :as cmd}
   & {:keys [for-task for-project]}]
  {:pre [(task? for-task)
         (project? for-project)
         (not (integer? (:entry-id cmd)))
         (not (find cmd :billed?))]}
  (assert-valid-billable? billable? for-task)
  (hydrate-entry cmd))


(defn validation-errs [m validators]
  (reduce (fn [errs [k validator]]
            (let [field-val (get m k)]
              (if (and field-val (not (validator field-val)))
                (conj errs k)
                errs))) [] validators))

(defn update-entry
   [entry {:keys [as-identity old-task task-id new-task] :as cmd}]
  {:pre [(empty? (validation-errs cmd {:start-time local-time?
                                       :end-time local-time?
                                       :for-date local-date?
                                       :as-identity integer?}))
         (task? old-task)
         (or (not task-id) (task? new-task))]}
  (assert (= as-identity (:user-id entry))
          "can only updates entries for yourself")
  (assert (not (:billed? entry))
          "can not change an entry when it's already billed")
  (assert (not (find cmd :user-id)) "you can't update the user-id for an hour entry")
  (let [update-field (fn [entry k]
                       (if (find cmd k)
                         (assoc entry k (k cmd))
                         entry))
        entry' (reduce update-field entry
                       #{:start-time :end-time :for-date :task-id :comment
                         :billable?})]
    (assert (not (before? (:end-time entry') (:start-time entry')))
            "start time can't come after end time")
    (assert (valid-billable? (:billable? entry') (or new-task old-task)))
    entry'))


(defn delete-entry [entry {:keys [as-identity] :as cmd}]
  {:pre [(integer? as-identity)]}
  (assert (= as-identity (:user-id entry))
          "can only updates entries for yourself")
  (assert (not (:billed? entry)) "Can't delete an already billed entry")
  entry)


(defprotocol EntryRepository
  (-add-entry! [this entry])
  (-find-entry [this entry-id])
  (-save-entry! [this entry])
  (-delete-entry! [this entry-id]))

(def ^:private ^:dynamic *impl*)

(defmacro with-impl [impl & body]
  `(binding [*impl* ~impl]
     ~@body))

(defn add-entry! [entry]
  (-add-entry! *impl* entry))

(defn find-entry [entry-id]
  (-find-entry *impl* entry-id))

(defn delete-entry! [entry]
  (-delete-entry! *impl* (:entry-id entry)))

(defn save!
  [entry]
  (-save-entry! *impl* entry))
