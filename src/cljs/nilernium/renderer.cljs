(ns nilernium.renderer
  (:require [clojure.walk :refer [prewalk]]

            [nilernium.layout :refer [subtree-attr? init-st-attr! deref-ctx]]))

(defn- subtree-attrs [node]
  (if-let [height (some-> node :layout :height)]
    (if (subtree-attr? height)
      [[height] (-> node
                    (update :layout dissoc :height)
                    (update :style dissoc :height))]
      [nil node])
    [nil node]))

(defn- render-tag [node]
  (name (deref-ctx (get node :tag :div))))

(defn- render-attr! [attrs k v]
  (case k
    (:tag :parent :children :layout) nil
    :style (aset attrs (name k) (clj->js (prewalk deref-ctx v)))
    (aset attrs (name k) (str (prewalk deref-ctx v)))))

(defn- render-attrs [with-st-attrs node]
  (let [res #js {}]
    (doseq [[k v] node :when (or with-st-attrs (not (subtree-attr? v)))]
      (render-attr! res k v))
    (when-not with-st-attrs
      (render-attr! res :visibility "hidden"))
    res))

(declare vdom-render)

(defn- render-node [with-st-attrs node]
  (let [vdom-children (to-array (map vdom-render (:children node)))]
    (js/virtualDom.VNode. (render-tag node)
                          (render-attrs with-st-attrs node)
                          vdom-children)))

(defn vdom-render [node]
  (if (map? node)
    (if-let [[st-attrs node*] (subtree-attrs node)]
      (do
        (doseq [st-attr st-attrs]
          (set! (.-color st-attr) :grey))
        (let [vdom (render-node false node*)
              temp-dom (js/virtualDom.create vdom)]
          (.. js/document -body (appendChild temp-dom))
          (doseq [st-attr st-attrs]
            (init-st-attr! st-attr temp-dom))
          (.. js/document -body (removeChild temp-dom))
          (render-node true node)))
      (render-node true node))
    (js/virtualDom.VText. (str node))))
