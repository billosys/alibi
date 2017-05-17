(ns timi.post-new-entry-bar
  (:require
    [cljsjs.react]
    [timi.logging :refer [log log-cljs]]
    [clojure.string :refer [split]]
    [goog.string :as gstring]
    [goog.string.format]
    [om.core :as om]
    [om.dom :as dom]
    [timi.time-page-state :as state]))

(defn parse-float [v]
  (js/parseFloat v))

(defn parse-selected-item [v]
  (when (seq v)
    (let [[project-id task-id] (map parse-float (split v #","))]
      {:project-id project-id
       :task-id task-id})))

(defn selectize-score-fn [options search]
  (let [default-score 0
        search-results (. js/fuzzy filter
                          search (clj->js options)
                          #js {:extract #(aget % "text")})
        score-by-option-value
        (into {}
              (map #(vector (.. % -original -value)
                            (.-score %)) search-results))]
    (fn [item]
      (get score-by-option-value (.-value item) default-score))))

(defn selectize-render-option
  [options-by-id item escape-fn css-classes]
  (let [item-data (options-by-id (.-value item))
        billing-method (:billing-method item-data)]
    (str (gstring/format "<div class='option %s'>" (or css-classes ""))
         (escape-fn (.-text item))
         (gstring/format "<span class='pull-right billing-method-badge %s'>"
                         (str "billing-method-badge-"
                              (.toLowerCase billing-method)))
         (escape-fn billing-method)
         "</span></div>")))

(defn entry-bar-form [{:keys [dispatch! get-state]} owner]
  (let [get-selectize (fn [] (.. (js/$ (om/get-node owner "the-form"))
                                 (find "select")
                                 (get 0)
                                 -selectize))
        post-new-entry-bar-state  (state/post-new-entry-bar-cursor (get-state))]
    (reify
      om/IDidMount
      (did-mount [_]
        (let [$form (js/$ (om/get-node owner "the-form"))
              $select (.find $form "select")]
          (.selectize
            $select #js
            {:highlight false
             :selectOnTab true
             :score (partial selectize-score-fn (:options post-new-entry-bar-state))
             :render #js {:option (partial selectize-render-option
                                           (:options-by-id post-new-entry-bar-state))
                          :item (fn [i e] (selectize-render-option
                                            (:options-by-id post-new-entry-bar-state)
                                            i e "option-selected"))}})
          (let [selectize (aget $select 0 "selectize")]
            (.on selectize "dropdown_close"
                 (fn []
                   (let [value (.getValue selectize)]
                     (.setTimeout ; updating the state will trigger a destroy
                                  ; of selectize, which in turn will prevent
                                  ; the dropdown_close event handlers from
                                  ; complete correctly, so we schedule
                                  ; updating the state for a later moment
                                  js/window
                                  (fn [] (if (seq value)
                                           (dispatch!
                                             {:action :select-task
                                              :task (parse-selected-item value)})
                                           (dispatch! {:action :cancel-entry})))
                                  0)))))))
      om/IWillUnmount
      (will-unmount [_]
        ;(log "will unmount")
        (.destroy (get-selectize)))

      om/IDidUpdate
      (did-update [_ _ _]
        (let [{:keys [project-id task-id]} (state/selected-task-cursor (get-state))
              selectize (get-selectize)
              current-val (.getValue selectize)]
          (if (and project-id task-id)
            (let [new-val (str project-id "," task-id)]
              (when-not (= new-val current-val)
                (.clear selectize)
                (.addItem selectize new-val)))
            (.clear selectize))))

      om/IRender
      (render [_]
        (let [post-new-entry-bar-state
              (om/observe owner (state/post-new-entry-bar-cursor (get-state)))

              options (:options post-new-entry-bar-state)

              {:keys [project-id task-id]}
              (om/observe owner (state/selected-task-cursor (get-state)))

              select-value (if (and project-id task-id)
                             (str project-id "," task-id) "")]
          (dom/form
            #js {:id "post-new-entry-bar"
                 :className "navbar-form navbar-form-post-new-entry-bar"
                 :ref "the-form"}
            (dom/select
              #js {:name "post-new-entry-bar"
                   :placeholder "Post new entry..."
                   :defaultValue select-value}
              (dom/option #js {:value ""} "")
              (map #(let [value (str (:project-id %) "," (:task-id %))]
                      (dom/option
                        #js {:key value
                             :data-project-id (:project-id %)
                             :data-task-id (:task-id %)
                             :data-billing-method (:billing-method %)
                             :value value}
                        (:text %))) options))))))))
