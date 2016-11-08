(ns nilernium.core)

(defn render [{:keys [entries]}]
  (get-in entries [0 :content]))
