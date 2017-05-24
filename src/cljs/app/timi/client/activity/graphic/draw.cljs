(ns timi.client.activity.graphic.draw
  (:require
    [om.core :as om]
    [om.dom :as dom]))

(defn get-abs-bounding-client-rect
  [dom-el]
  (let [rect (.getBoundingClientRect dom-el)
        scroll-x (.-scrollX js/window)
        scroll-y (.-scrollY js/window)]
    {:top (+ (.-top rect) scroll-y)
     :left (+ (.-left rect) scroll-x)
     :right (+ (.-right rect) scroll-x)
     :bottom (+ (.-bottom rect) scroll-y)
     :width (.-width rect)
     :height (.-height rect)}))

(def constants
  {:pixels-per-minute 0.2
   :pixels-per-day 144
   :left-axis-px 100
   :project-label-offset-px 5
   :project-label-vert-offset-px 4
   :projects {:vertical-offset-px 25}
   :hour-entry {:height-px 8
                :active {:color "rgb(240, 153, 45)",
                         :radius 3
                         :std-dev 2}}
   :row-spacing-px 14
   :day-spacing-px 20
   :project-spacing-px 20
   :project-heading-x-px 10
   :heading-to-rows-px 15
   :grid-time-label {:x-offset-px 5
                     :height-px 15}
   :grid-day-label {:x-offset-px 5
                    :height-px 20}
   :day-summary  {:bar {:y-offset-px  8}
                  :label {:x-offset-px 5
                          :y-offset-px 15}}
   :canvas {:bottom-padding-px 10}
   :no-data {:y-offset-px 50
             :x-offset-px 540
             :bottom-padding-px 50}
   :time-line  {:width-px 8}})

(defn const
  [keys & default]
  (get-in constants keys default))

(defn result-empty
  []
  {:y-offset 0 :els []})

(defn result-append-el
  [el draw-result]
  (update draw-result :els conj el))

(defn result-add-to-y
  [inc draw-result]
  (update draw-result :y-offset + inc))

(defn result-set-y-offset
  [y-offset draw-result]
  (assoc draw-result :y-offset y-offset))

(defn no-data-msg
  [draw-result]
  (->> draw-result
       (result-append-el
         (dom/text
           #js
           {:className "no-data-in-period"
            :x (const [:no-data :x-offset-px])
            :y (+ (:y-offset draw-result)
                  (const [:no-data :y-offset-px]))
            :textAnchor "middle"}
           "No data for this period"))
       (result-add-to-y
         (+ (const [:no-data :y-offset-px])
            (const [:no-data :bottom-padding-px])))))

(defn svg-glow
  [id color & {:keys [radius std-dev] :or {:radius 1 :std-dev 1}}]
  ;; from http://stackoverflow.com/a/36564885/345910
  (str "<filter id='" id "' x='-5000%' y='-5000%' width='10000%' height='10000%'>
         <feFlood result='flood' flood-color='" color "' flood-opacity='1'></feFlood>
         <feComposite in='flood' result='mask' in2='SourceGraphic' operator='in'>
         </feComposite>
         <feMorphology in='mask' result='dilated' operator='dilate' "
         "radius='" radius "'></feMorphology>
         <feGaussianBlur in='dilated' result='blurred' stdDeviation='" std-dev "'></feGaussianBlur>
         <feMerge>
           <feMergeNode in='blurred'></feMergeNode>
           <feMergeNode in='SourceGraphic'></feMergeNode>
         </feMerge>
       </filter>"))
