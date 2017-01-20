(ns nilernium.core
  (:require [net.cgrand.enlive-html :refer [deftemplate] :as en]))

(deftemplate main-template "templates/main.html" [title content entries]
  [:title] (en/content (str "Nilernium > " title))
  [:header :#page-title] (en/html-content title)
  [:article] (comp (en/append (en/html [:div.clearfix]))
                   (en/append (en/html-snippet content)))
  [:nav :li] (en/clone-for [{:keys [short-filename]} entries]
               [:a] (comp (en/html-content short-filename)
                          (en/set-attr :href (str short-filename ".html")))))

(defn render [{{:keys [title content]} :entry :keys [entries]}]
  (apply str (main-template title content entries)))
