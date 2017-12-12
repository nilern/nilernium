(ns nilernium.layout
  (:require [clojure.walk :refer [prewalk]]
            cljsjs.virtual-dom))

(def ^:dynamic *id-table*)

;;;;

(defprotocol IInit
  (init! [self x] "Initialize. Throw if already initialized."))

(defprotocol IInitParent
  (init-parent! [self x] "Initialize parent. Throw if already initialized."))

(extend-type default IInitParent
  (init-parent! [_ _] nil))

(defprotocol IInitCtx
  (init-ctx! [self]
    "Initialize self if uninitialized. Throw if self is semi-initialized.
     Needs *id-table* defined."))

(defprotocol IDerefExn
  (deref-exn [self] "Like deref, but throws if uninitialized."))

(defprotocol IDerefCtx
  (deref-ctx [self] "Like deref-ctx, but needs *id-table* defined."))

(extend-type default IDerefCtx
  (deref-ctx [self] self))

;;;;

(deftype SARef [^:mutable initialized ^:mutable value]
  IInit
  (init! [self x]
    (if-not initialized
      (do
        (set! value x)
        (set! initialized true))
      (throw (js/Error. "Reinitializing SARef"))))

  IDerefExn
  (deref-exn [self]
    (if initialized
      value
      (throw (js/Error. "Uninitialized SARef"))))

  IDerefCtx
  (deref-ctx [self] (deref-exn self)))

(defn sa-ref []
  (->SARef false nil))

;;;;

(defn resolve-path [node [start-descr & selectors :as path]]
  (let [start (cond
                (= start-descr 'self) node
                (= start-descr 'parent) (deref-exn (:parent node))
                (keyword? start-descr) (get *id-table* start-descr)
                :else (throw (js/Error. (str "Invalid path " path))))]
    (reduce (fn [node selector] (deref-ctx (selector node))) start selectors)))

(deftype QueryAttr [^:mutable parent dependencies ^:mutable value ^:mutable color]
  IInitParent
  (init-parent! [self p]
    (if (nil? parent)
      (set! parent p)
      (throw (js/Error. "Reinitializing QueryAttr.parent"))))

  IInitCtx
  (init-ctx! [self]
    (case color
      :white (do
               (set! color :grey)
               (if-let [parent parent]
                 (set! value (apply value (map (partial resolve-path parent)
                                               dependencies)))
                 (throw (js/Error. "Uninitialized QueryAttr.parent")))
               (set! color :black))
      :grey (throw (js/Error. "Property cycle detected"))
      :black nil))

  IDerefCtx
  (deref-ctx [self]
    (init-ctx! self)
    value))

(defn query-attr [dependencies f]
  (->QueryAttr nil dependencies f :white))

(def query-attr? (partial instance? QueryAttr))

;;;;

(def layout-keys [:left :hcenter :right :width :top :vcenter :bottom :height])
(def hlayout-keys (set (take 4 layout-keys)))
(def vlayout-keys (set (drop 4 layout-keys)))

(declare layout-query-attr?)

(defn resolve-non-lq-dep [layout selector]
  (let [attr (selector layout)]
    (if (layout-query-attr? attr)
      attr
      (deref-ctx attr))))

(def boundary-kind
  {:left :start
   :top :start
   :hcenter :center
   :vcenter :center
   :right :end
   :bottom :end})

(defmulti resolve-layout-attr (fn [target start center end length]
                                [(boundary-kind target)
                                 (not (layout-query-attr? start))
                                 (not (layout-query-attr? center))
                                 (not (layout-query-attr? end))
                                 (not (layout-query-attr? length))]))

(defmethod resolve-layout-attr [:start false true true false] [_ _ center end _]
  (- center (- end center)))
(defmethod resolve-layout-attr [:start false true false true] [_ _ center _ length]
  (- center (/ length 2)))
(defmethod resolve-layout-attr [:start false false true true] [_ _ _ end length]
  (- end length))

(defmethod resolve-layout-attr [:center true false true false] [_ start _ end _]
  (/ (+ start end) 2))
(defmethod resolve-layout-attr [:center true false false true] [_ start _ _ length]
  (+ start (/ length 2)))
(defmethod resolve-layout-attr [:center false false true true] [_ _ _ end length]
  (- end (/ length 2)))

(defmethod resolve-layout-attr [:end true true false false] [_ start center _ _]
  (+ center (- center start)))
(defmethod resolve-layout-attr [:end true false false true] [_ start _ _ length]
  (+ start length))
(defmethod resolve-layout-attr [:end false true false true] [_ _ center _ length]
  (+ center (/ length 2)))

(defmethod resolve-layout-attr [:length true true false false] [_ start center _ _]
  (* 2 (- center start)))
(defmethod resolve-layout-attr [:length true false true false] [_ start _ end _]
  (- end start))
(defmethod resolve-layout-attr [:length false true true false] [_ _ center end _]
  (* 2 (- end center)))

(deftype LayoutQueryAttr [^:mutable parent target dependencies ^:mutable value ^:mutable color]
  IInitParent
  (init-parent! [self p]
    (if (nil? parent)
      (set! parent p)
      (throw (js/Error. "Reinitializing LayoutQueryAttr.parent"))))

  IInitCtx
  (init-ctx! [self]
    (case color
      :white (do
               (set! color :grey)
               (if-let [parent parent]
                 (set! value (apply resolve-layout-attr target
                                    (map (partial resolve-non-lq-dep (:layout parent))
                                         dependencies)))
                 (throw (js/Error. "Uninitialized LayoutQueryAttr.parent")))
               (set! color :black))
      :grey (throw (js/Error. "Property cycle detected"))
      :black nil))

  IDerefCtx
  (deref-ctx [self]
    (init-ctx! self)
    value))

(defn layout-query-attr [target]
  (->LayoutQueryAttr nil target
                         (cond
                           (hlayout-keys target) (take 4 layout-keys)
                           (vlayout-keys target) (drop 4 layout-keys))
                          nil :white))

(def layout-query-attr? (partial instance? LayoutQueryAttr))

;;;;

(deftype ContentHeightAttr [^:mutable value ^:mutable color]
  IDerefCtx
  (deref-ctx [self]
    (case color
      :black value
      (throw (js/Error. "Uninitialized ContentHeightAttr")))))

(defn init-ch-attr! [attr dom-node]
  (set! (.-value attr) (.-offsetHeight dom-node))
  (set! (.-color attr) :black))

(def content-height? (partial instance? ContentHeightAttr))

(defn content-height []
  (->ContentHeightAttr nil :white))

(def init-st-attr! init-ch-attr!)

(def subtree-attr? content-height?)

;;;;

(defn normalize-layout [node]
  (let [layout (:layout node)
        updater (fn [layout]
                  (merge (into {} (map (fn [k] [k (layout-query-attr k)])) layout-keys)
                         layout))]
    (cond
      (false? layout) (dissoc node :layout)
      (or (map? layout) (nil? layout))
        (update node :layout updater))))

(defn normalize-style [node]
  (if (contains? node :layout)
    (let [updater (fn [style]
                    (merge (into {:position "absolute"}
                                 (map (fn [k] [k (query-attr [['self :layout k]] #(str % "px"))]))
                                 [:left :width :top :height])
                           style))]
      (update node :style updater))
    node))

(defn normalize [node]
  (if (map? node)
    (-> node
        (assoc :parent (sa-ref))
        normalize-layout
        normalize-style
        (update :children #(or % []))
        (update :children (partial map normalize)))
    node))

;;;;

(defn init-attr-parents! [parent attr-v]
  (cond
    (map? attr-v) (doseq [v (vals attr-v)]
                    (init-attr-parents! parent v))
    (or (set? attr-v) (vector? attr-v)) (doseq [v attr-v]
                                          (init-attr-parents! parent v))
    :else (init-parent! attr-v parent)))

(defn init-parents! [parent node]
  (doseq [[k v] node]
    (case k
      :parent   (when parent (init! v parent))
      :children (doseq [child v]
                  (init-parents! node child))
      (init-attr-parents! node v))))

(defn id-table [node]
  (letfn [(make [table node]
            (let [table (reduce make table (:children node))]
              (if-let [id (:id node)]
                (assoc table id node)
                table)))]
    (make {} node)))

(defn vdom-render [node]
  (letfn [(subtree-attrs [node]
            (if-let [height (some-> node :layout :height)]
              (if (subtree-attr? height)
                [[height] (-> node
                              (update :layout dissoc :height)
                              (update :style dissoc :height))]
                [nil node])
              [nil node]))
          (render-tag [node] (name (deref-ctx (get node :tag :div))))
          (render-attr! [attrs k v]
            (case k
              (:tag :parent :children :layout) nil
              :style (aset attrs (name k) (clj->js (prewalk deref-ctx v)))
              (aset attrs (name k) (str (prewalk deref-ctx v)))))
          (render-attrs [with-st-attrs node]
            (let [res #js {}]
              (doseq [[k v] node :when (or with-st-attrs (not (subtree-attr? v)))]
                (render-attr! res k v))
              (when-not with-st-attrs
                (render-attr! res :visibility "hidden"))
              res))
          (render-children [children] (to-array (map vdom-render children)))
          (render-node [with-st-attrs node]
            (let [vdom-children (render-children (:children node))]
              (js/virtualDom.VNode. (render-tag node)
                                    (render-attrs with-st-attrs node)
                                    vdom-children)))]
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
      (js/virtualDom.VText. (str node)))))

(defn render [backend node]
  (let [node (normalize node)]
    (init-parents! nil node)
    (binding [*id-table* (id-table node)]
      (backend node))))
