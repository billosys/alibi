(ns timi.client.activity.graphic.core
  (:require
    [cljs-time.coerce :as cljs-coerce]
    [cljs-time.format :as cljs-format]
    [clojure.string :as string]
    [clojure.walk :refer [keywordize-keys]]
    [om.core :as om]
    [om.dom :as dom]
    [timi.client.actions :as actions]
    [timi.client.activity.graphic.draw :as draw]
    [timi.client.activity.graphic.grid :as grid]
    [timi.client.logging :refer [log log-cljs]]
    [timi.client.time.state :as state]
    [timi.client.util :refer [parse-float]]))

(def Instant (.-Instant js/JSJoda))
(def LocalDate (.-LocalDate js/JSJoda))

(defn init-data
  [project-data]
  (->> project-data
       (sort (fn [{a-from :from} {b-from :from}] (compare a-from b-from)))
       (map (fn [{:keys [from till task-id] :as project-row}]
              (assoc project-row
                     :task-id (parse-float task-id)
                     :from (.ofEpochSecond Instant from)
                     :till (.ofEpochSecond Instant till))))
       (group-by :project)
       (map (fn [[k vs]]
              {:label k
               :bars (->> vs
                          (sort (fn [{a-from :from}
                                     {b-from :from}]
                                  (.compareTo a-from b-from))))}))))

(defn tooltip-component
  [state owner]
  (let [{:keys [top width left comment]} state]
    (reify

      om/IRender
      (render [_]
        (dom/div
          #js
          {:style #js {:display (if (seq comment) "block" "none")}
           :ref "element"}
          (dom/div
            #js
            {:className "tooltip top"
             :role "tooltip"
             :style #js {:left (+ left (/ width 2))
                         :top top}}
            (dom/div #js {:className "tooltip-arrow"})
            (dom/div #js {:className "tooltip-inner"}
                     comment))))

      om/IDidUpdate
      (did-update [_ _ _]
        (let [el (aget (om/get-node owner "element") "children" 0)
              rect (draw/get-abs-bounding-client-rect el)
              px (fn [val] (str val "px"))]
          (aset el "style" "left" (px (- (+ left (/ width 2))
                                         (/ (:width rect) 2))))
          (aset el "style" "top" (px (- top (:height rect))))
          (aset el "className" "tooltip top fade in"))))))

(defn render-graphic
  [dispatch! project-data selected-date selected-entry-id]
  (let [svg (grid/render-svg
              dispatch!
              selected-date
              (init-data project-data)
              #(dispatch! (actions/time-page-change-date %))
              {:selected-entry selected-entry-id})]
    (dom/div
      nil
      (dom/div
        #js {:className "row"}
        (dom/div
          #js {:className "col-md-12"})
        (dom/div
          #js {:id "activity-svg-container"}
          svg)))))

(defn render-change-date-btns
  [dispatch! selected-date]
  (let [on-change-date #(dispatch! (actions/time-page-change-date %))
        selected-date (.parse LocalDate selected-date)]
    (dom/div
      nil
      (dom/button
        #js {:className "btn btn-default btn-prev-period"
             :onClick (fn [] (on-change-date (.plusDays selected-date -7)))}
        (dom/i #js {:className "fa fa-chevron-left fa-lg"
                    :aria-hidden "true"}))
      (dom/button
        #js {:className "btn btn-default btn-period-today"
             :onClick (fn [] (on-change-date (.now LocalDate)))}
        "Today")
      (dom/button
        #js {:className "btn btn-default btn-next-period"
             :onClick (fn [] (on-change-date (.plusDays selected-date 7)))}
        (dom/i #js {:className "fa fa-chevron-right fa-lg"
                    :aria-hidden "true"})))))

(defn render-navigator
  [{:keys [dispatch! get-state]} owner]
  (reify
    om/IRender
    (render [_]
      (let [form (om/observe owner (state/entry-screen-form-cursor (get-state)))
            selected-date (state/form-selected-date form)]
        (render-change-date-btns dispatch! selected-date)))))

(defn render-html
  [{:keys [dispatch! get-state]} owner]
  (reify
    om/IRender
    (render [_]
      (let [entries (om/observe owner (state/entries-cursor (get-state)))
            form (om/observe owner (state/entry-screen-form-cursor (get-state)))]
        (render-graphic
          dispatch!
          (state/entries-add-form-entry @entries @form)
          (state/form-selected-date form)
          (state/form-get-editing-entry-id form))))))

(defn render-tooltip
  [{:keys [get-state dispatch!]} owner]
  (reify
    om/IRender
    (render [_]
      (let [state (get-state)
            entries (om/observe owner (state/entries-cursor state))
            mouse-over-entry (om/observe owner
                                         (state/mouse-over-entry-cursor state))
            form (om/observe owner (state/entry-screen-form-cursor state))

            entry-id (:entry-id mouse-over-entry)
            {:keys [left top width]} (:pos mouse-over-entry)
            comment (->> (state/entries-add-form-entry entries form)
                         (filter #(= entry-id (:entry-id %)))
                         (first)
                         :comment)]
        (om/build tooltip-component
                  {:top top
                   :left left
                   :width width
                   :comment comment})))))
