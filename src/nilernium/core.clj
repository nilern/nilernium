(ns nilernium.core
  (:require [net.cgrand.enlive-html :refer [deftemplate] :as en]))

(deftemplate main-template "templates/main.html" [title content entries]
  [:header :h1] (en/html-content title)
  [:article] (en/html-content content)
  [:nav :li] (en/clone-for [{:keys [short-filename]} entries]
               [:a] (comp (en/html-content short-filename)
                          (en/set-attr :href (str short-filename ".html")))))

(defn render [{{:keys [title content]} :entry :keys [entries]}]
  (apply str (main-template title content entries)))
