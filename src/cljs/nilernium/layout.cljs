(ns nilernium.layout
  (:require [nilernium.layout.node :as n]))

(defn init-parents! [node]
  (doseq [child (n/children node)]
    (init-parents! child)
    (n/init-parent! child node))
  (doseq [[_ attr] (n/attrs node) :when (n/query-attr? attr)]
    (n/init-parent! attr node)))
             
(defn id-table [node]
  (letfn [(make [table node]
            (let [table (reduce make table (n/children node))]
              (if-let [id (:id (n/attrs node))]
                (assoc table id node)
                table)))]
    (make {} node)))

(defn resolve-attrs! [id-table node]
  (doseq [[_ attr] (n/attrs node)]
    (n/resolve-attr! id-table attr))
  (doseq [child (n/children node)]
    (resolve-attrs! id-table child)))

(defn render [backend node]
  (init-parents! node)
  (resolve-attrs! (id-table node) node)
  (backend node))
