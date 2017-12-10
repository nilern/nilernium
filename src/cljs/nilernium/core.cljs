(ns nilernium.core
  (:require cljsjs.virtual-dom
            [cljs.core.async :as async :refer [go chan <! >! put!]]

            [nilernium.layout :as l]))

(defn reductions-chan [f acc c]
  (let [dest (chan)]
    (go
      (>! dest acc)
      (loop [acc acc]
        (let [acc (f acc (<! c))]
          (>! dest acc)
          (recur acc))))
    dest))

;;;;

(defn start-renderer! [render mount-point states events]
  (go
    (let [vdom (render (<! states) events)
          root (js/virtualDom.create vdom)]
      (.. js/document -body (replaceChild root mount-point))
      (loop [root root, vdom vdom]
        (let [vdom* (render (<! states) events)
              root* (js/virtualDom.patch root (js/virtualDom.diff vdom vdom*))]
          (recur root* vdom*))))))

;;;;

(def start-state {:click-count 0})

(defn transition [state event]
  (update state :click-count inc))

(defn render [state events]
  (l/render
    l/vdom-render
    {:tag :div
     :id "app-root"
     :layout {:left 0, :width 800, :top 0, :height 600}
     :children [{:tag :header,
                 :id :site-header
                 :layout {:left 0, :width (l/query-attr [['parent :layout :width]] identity)
                          :top 0, :height 50}
                 :style {:background-color "navy"}}
                {:tag :nav
                 :id :site-nav
                 :layout {:left 0, :width 300
                          :top    (l/query-attr [[:site-header :layout :height]] identity)
                          :height (l/query-attr [['parent :layout :height]
                                                 [:site-header :layout :height]] -)}
                 :style {:background-color "purple"}}
                {:tag :article
                 :layout {:left   (l/query-attr [[:site-nav :layout :width]] identity)
                          :width  (l/query-attr [['parent :layout :width]
                                                 [:site-nav :layout :width]] -)
                          :top    (l/query-attr [[:site-header :layout :height]] identity)
                          :height (l/query-attr [['parent :layout :height]
                                                 [:site-header :layout :height]] -)}
                 :style {:background-color "pink"}}]}))

;;;;

(defn init! []
  (let [events (chan)
        root (.getElementById js/document "app-root")
        states (reductions-chan transition start-state events)]
    (start-renderer! render root states events)))

(init!)
