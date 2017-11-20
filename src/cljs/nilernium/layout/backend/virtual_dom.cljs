(ns nilernium.layout.backend.virtual-dom
  (:require cljsjs.virtual-dom
            [nilernium.layout.node :as n]))

(defprotocol IRender
  (render [self]))

(defn render-attrs [node]
  (let [res #js {}]
    (doseq [[k v] (n/attrs node)
            :let [k (name k)
                  v (n/unwrap-attr! v)]]
      (aset res k (if (= k "style") (clj->js v) v)))
    res))

(defn render-children [node]
  (->> node n/children (map render) to-array))

(extend-protocol IRender
  n/DomNode
  (render [self]
    (js/virtualDom.VNode. (name (.-tag- self)) (render-attrs self) (render-children self)))

  n/TextNode
  (render [self] (js/virtualDom.VText. (:text self))))
