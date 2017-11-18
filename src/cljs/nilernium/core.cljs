(ns nilernium.core
  (:require cljsjs.virtual-dom
            [cljs.core.async :as async :refer [go chan <! >! put!]]))

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
  (js/virtualDom.h "div" #js {"id" "app-root"}
    #js [(js/virtualDom.h "button"
           #js {"onclick" (fn [e] (put! events e))}
           #js ["foo"])
         (js/virtualDom.h "p" #js {} #js [(str (:click-count state))])]))

;;;;

(defn init! []
  (let [events (chan)
        root (.getElementById js/document "app-root")
        states (reductions-chan transition start-state events)]
    (start-renderer! render root states events)))

(init!)
