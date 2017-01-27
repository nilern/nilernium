(ns nilernium.core
  (:require [net.cgrand.enlive-html :refer [deftemplate defsnippet do->] :as en]
            [clojure.set :refer [union]]))

(def all-tags (memoize (partial into #{} (mapcat :tags))))

(defsnippet nav "templates/main.html" [:nav] [entries all-tags content]
  [:nav :> :ul :> :li]
  (en/clone-for [tag all-tags
                 :let [id (str tag "-ncb")]]
  [:label] (do-> (en/html-content tag)
                 (en/set-attr :for id))
  [(en/attr= :type "checkbox")] (en/set-attr :id id)
  [:ul]   (en/clone-for [{:keys [title short-filename tags]} entries
                         :when (some #(= % tag) tags)]
            [:li :> :a] (do-> (en/content title)
                              (en/set-attr
                                :href (str short-filename ".html"))))))

(defsnippet article "templates/main.html" [:article] [created tags content]
  [:article] (en/append (en/html-snippet content))
  [[:tr en/first-of-type] :> [:td (en/nth-of-type 2)]]
  (en/content created)
  [:#page-tags :> :li] (en/clone-for [tag tags]
                         [:li] (en/content tag)))

(deftemplate main-template "templates/main.html"
  [{:keys [title created content tags]} entries all-tags]

  [:title]       (en/append " > " title)
  [:#page-title :h1] (en/content title)
  [:nav]     (en/substitute (nav entries all-tags content))
  [:article] (en/substitute (article created tags content)))

(defn render [{:keys [entry entries]}]
  (apply str (main-template entry entries (all-tags entries))))
