(ns nilernium.core
  (:require cljsjs.virtual-dom
            [cljs.core.async :as async :refer [go chan <! >! put!]]

            [nilernium.ui :as ui]))

(defn- reductions-chan [f acc c]
  (let [dest (chan)]
    (go
      (>! dest acc)
      (loop [acc acc]
        (let [acc (f acc (<! c))]
          (>! dest acc)
          (recur acc))))
    dest))

;;;;

(defn- start-renderer! [render mount-point states events]
  (go
    (let [vdom (render (<! states) events)
          root (js/virtualDom.create vdom)]
      (.. js/document -body (replaceChild root mount-point))
      (loop [root root, vdom vdom]
        (let [vdom* (render (<! states) events)
              root* (js/virtualDom.patch root (js/virtualDom.diff vdom vdom*))]
          (recur root* vdom*))))))

;;;;

(def ^:private start-state
  {:window {:width (.-innerWidth js/window)}
            :height (.-innerHeight js/window)})

(defn- transition [state [event-type event-data]]
  (case event-type
    :resize (assoc state :window event-data)
    state))

;;;;

(defn- on-resize [events]
  (put! events [:resize {:width (.-innerWidth js/window),
                         :height (.-innerHeight js/window)}]))

(defn- init! []
  (let [events (chan)
        root (.getElementById js/document "app-root")
        states (reductions-chan transition start-state events)]
    (.addEventListener js/window "resize" (partial on-resize events))
    (start-renderer! ui/render root states events)))

(init!)
