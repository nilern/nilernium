(ns nilernium.core
  (:require [hiccup.page :refer [html5]]
            [hiccup.element :refer [link-to]]))

(defn render [{{:keys [content]} :entry :keys [entries]}]
  (html5
    content
    [:ul
      (for [{:keys [short-filename]} entries]
        [:li (link-to (str short-filename ".html") short-filename)])]))
