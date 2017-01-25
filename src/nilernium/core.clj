(ns nilernium.core
  (:require [net.cgrand.enlive-html :refer [deftemplate defsnippet do->] :as en]
            [clojure.set :refer [union]]))

(def all-tags (memoize (partial into #{} (mapcat :tags))))

(defsnippet page-header "templates/main.html" [:#page-title]
  [title created tags]

  [:h1] (en/content title)
  [[:tr en/first-of-type] :> [:td (en/nth-of-type 2)]]
  (en/content created)
  [:#page-tags :> :li] (en/clone-for [tag tags]
                         [:li] (en/content tag)))

(defsnippet article "templates/main.html" [:article] [entries all-tags content]
  [:nav] (en/after (en/html-snippet content))
  [:nav :> :ul :> :li]
  (en/clone-for [tag all-tags]
    [:span] (en/html-content tag)
    [:ul]   (en/clone-for [{:keys [title short-filename tags]} entries
                           :when (some #(= % tag) tags)]
              [:li :> :a] (do-> (en/content title)
                                (en/set-attr
                                  :href (str short-filename ".html"))))))

(deftemplate main-template "templates/main.html"
  [{:keys [title created content tags]} entries all-tags]

  [:title]       (en/append " > " title)
  [:#page-title] (en/substitute (page-header title created tags))
  [:article]     (en/substitute (article entries all-tags content)))

(defn render [{:keys [entry entries]}]
  (apply str (main-template entry entries (all-tags entries))))
