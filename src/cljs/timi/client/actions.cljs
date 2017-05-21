(ns timi.client.actions
  (:require
    [time.core :as time]
    [timi.client.activity.graphic.data :as activity-data]
    [timi.client.time.state :as state]))

(defn change-view-period [for-date data]
  {:action :change-view-period
   :for-date for-date
   :data data})

(defn entries-load-cache [for-date entries]
  {:action :entries-load-cache
   :for-date for-date
   :entries entries})

(defn entries-loading-cache [for-date]
  {:action :entries-loading-cache
   :for-date for-date})

(defn entries-load-data [new-date]
  (fn [dispatch! state]
    (let [date-str (.toString new-date)
          monday-before (.toString (time/find-monday-before date-str))]
      (activity-data/fetch-data
        monday-before
        (state/entries-cache state)
        {:on-fetching #(dispatch! (entries-loading-cache monday-before))
         :on-fetched #(do (dispatch! (entries-load-cache monday-before %))
                          (dispatch! (change-view-period date-str %)))}))))

(def time-page-change-date entries-load-data)
