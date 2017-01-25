(ns nilernium.core
  (:require [net.cgrand.enlive-html :refer [deftemplate] :as en]
            [clojure.set :refer [union]]))

(def all-tags
  (memoize
    (fn [entries]
      (transduce (map (comp set :tags)) union #{} entries))))

(deftemplate main-template "templates/main.html" [title created content entries]
  [:title] (en/content (str "Nilernium > " title))
  [:header :#page-title :h1] (en/html-content title)
  [:header :#page-title :p :span] (en/html-content created)
  [:article] (comp (en/append (en/html [:div.clearfix]))
                   (en/append (en/html-snippet content)))
  [:nav :> :ul :> :li]
  (en/clone-for [tag (all-tags entries)]
    [:span] (en/html-content tag)
    [:ul]  (en/clone-for [{:keys [short-filename tags]} entries
                          :when (contains? (set tags) tag)]
             [:li :a]
             (comp (en/html-content short-filename)
                   (en/set-attr :href (str short-filename ".html"))))))

(defn render [{{:keys [title created content]} :entry :keys [entries]}]
  (apply str (main-template title created content entries)))
