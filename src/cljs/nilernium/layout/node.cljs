(ns nilernium.layout.node)

(defprotocol IParentRef
  (parent [self])
  (init-parent! [self parent*]))

(defprotocol INode
  (attrs [self])
  (children [self]))

;;;;

(deftype DomNode [tag- attrs- ^:mutable parent- children-]
  IParentRef
  (parent [_] parent-)
  (init-parent! [_ parent*] (set! parent- parent*))

  INode
  (attrs [self] attrs-)
  (children [_] children-)

  ILookup
  (-lookup [_ k] (get attrs- k))
  (-lookup [_ k default] (get attrs- k default)))

(defn dom-node [tag attrs children]
  (->DomNode tag attrs nil children))
  
;;;;

(deftype TextNode [attrs- ^:mutable parent-]
  IParentRef
  (parent [_] parent-)
  (init-parent! [_ parent*] (set! parent- parent*))

  INode
  (attrs [self] attrs-)
  (children [_] '())
  
  ILookup
  (-lookup [_ k] (get attrs- k))
  (-lookup [_ k default] (get attrs- k default)))

(defn text-node [attrs]
  (->TextNode attrs nil))

;;;;

(deftype QueryAttr [dependencies- ^:mutable parent- ^:mutable value- ^:mutable color-]
  IParentRef
  (parent [_] parent-)
  (init-parent! [_ parent*] (set! parent- parent*)))

(defn query-attr [dependencies f]
  (->QueryAttr dependencies nil f :white))

(def query-attr? #(instance? QueryAttr %))

(defn unwrap-attr! [attr]
  (if-not (query-attr? attr)
    attr
    (.-value- attr)))

(declare resolve-attr!)

(defn resolve-dep! [id-table attr [start-descr & selectors]]
  (let [start (cond
                (= start-descr 'self) (parent attr)
                (keyword? start-descr) (get id-table start-descr))]
    (resolve-attr! id-table (reduce #(%2 %1) start selectors))))

(defn resolve-attr! [id-table attr]
  (if-not (query-attr? attr)
    attr
    (case (.-color- attr)
      :black (.-value- attr)
      :white (do
               (set! (.-color- attr) :grey)
               (set! (.-value- attr) (apply (.-value- attr) (map #(resolve-dep! id-table attr %)
                                                                  (.-dependencies- attr))))
               (set! (.-color- attr) :black)
               (.-value- attr))
      :grey (throw (js/Error. "Attribute cycle detected.")))))
