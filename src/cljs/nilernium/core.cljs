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

(def start-state
  {:window {:width (.-innerWidth js/window)}
            :height (.-innerHeight js/window)})

(defn transition [state [event-type event-data]]
  (case event-type
    :resize (assoc state :window event-data)
    state))

(defn render [state events]
  (l/render
    l/vdom-render
    {:tag :div
     :id "app-root"
     :layout {:hcenter (/ (-> state :window :width) 2), :width (min (-> state :window :width) 960)
              :top 0, :height (-> state :window :height)}
     :children [{:tag :header,
                 :id :site-header
                 :layout {:left 0, :width (l/query-attr [['parent :layout :width]] identity)
                          :top 0, :height 50}
                 :style {:background-color "navy"}}
                {:tag :article
                 :id :content
                 :layout {:left   (l/query-attr [[:site-nav :layout :width]] identity)
                          :width  (l/query-attr [['parent :layout :width]
                                                 [:site-nav :layout :width]] -)
                          :top    (l/query-attr [[:site-header :layout :height]] identity)
                          :height (l/content-height)}
                 :style {:background-color "pink"}
                 :children [{:tag :h1
                             :layout false
                             :children ["Hello World!"]}
                            {:tag :p
                             :layout false
                             :children ["foo bar baz quux."]}]}
                {:tag :nav
                 :id :site-nav
                 :layout {:left 0, :width 300
                          :top    (l/query-attr [[:site-header :layout :height]] identity)
                          :height (l/query-attr [[:content :layout :height]] identity)}
                 :style {:background-color "purple"}}]}))

;;;;

(defn on-resize [events]
  (put! events [:resize {:width (.-innerWidth js/window),
                         :height (.-innerHeight js/window)}]))

(defn init! []
  (let [events (chan)
        root (.getElementById js/document "app-root")
        states (reductions-chan transition start-state events)]
    (.addEventListener js/window "resize" (partial on-resize events))
    (start-renderer! render root states events)))

(init!)
